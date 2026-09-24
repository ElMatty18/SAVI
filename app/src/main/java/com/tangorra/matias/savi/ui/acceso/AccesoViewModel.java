package com.tangorra.matias.savi.ui.acceso;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.tangorra.matias.savi.Entidades.Grupo;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Utils.FirebaseUtils;
import com.tangorra.matias.savi.data.UsuarioRepositorio;

import java.util.ArrayList;
import java.util.List;

/**
 * Login, registro y carga de la sesion (usuario, grupo e integrantes).
 */
public class AccesoViewModel extends ViewModel {

    public static final class Estado {
        public enum Tipo { INICIAL, CARGANDO, ERROR, INGRESO }

        public final Tipo tipo;
        public final String mensaje;
        public final Usuario usuario;

        private Estado(Tipo tipo, String mensaje, Usuario usuario) {
            this.tipo = tipo;
            this.mensaje = mensaje;
            this.usuario = usuario;
        }

        static Estado inicial() { return new Estado(Tipo.INICIAL, null, null); }
        static Estado cargando() { return new Estado(Tipo.CARGANDO, null, null); }
        static Estado error(String mensaje) { return new Estado(Tipo.ERROR, mensaje, null); }
        static Estado ingreso(Usuario usuario) { return new Estado(Tipo.INGRESO, null, usuario); }
    }

    private static final String CLAVE_LEGADA = "clave";

    // Sin conexion (o con una sesion revocada) las lecturas de la base pueden no terminar nunca
    private static final long LIMITE_CARGA_MS = 15_000;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final MutableLiveData<Estado> estado = new MutableLiveData<>(Estado.inicial());
    private final android.os.Handler temporizador = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable vencimiento = () -> {
        if (estado.getValue() != null && estado.getValue().tipo == Estado.Tipo.CARGANDO) {
            estado.setValue(Estado.error("Sin conexión. Revisá tu internet e intentá de nuevo"));
        }
    };

    public LiveData<Estado> estado() {
        return estado;
    }

    /** Firebase Auth recuerda la sesion: si hay un usuario autenticado se entra sin pedir la clave. */
    public boolean continuarSesion() {
        if (auth.getCurrentUser() == null || auth.getCurrentUser().getEmail() == null) {
            return false;
        }
        estado.setValue(Estado.cargando());
        cargarSesion(auth.getCurrentUser().getEmail());
        return true;
    }

    public void ingresar(final String mail, String clave) {
        estado.setValue(Estado.cargando());
        auth.signInWithEmailAndPassword(mail, clave)
                .addOnSuccessListener(r -> cargarSesion(mail))
                .addOnFailureListener(this::fallo);
    }

    public void registrar(final String mail, String clave) {
        estado.setValue(Estado.cargando());
        auth.createUserWithEmailAndPassword(mail, clave)
                .onSuccessTask(r -> {
                    // El uid de Firebase Auth es la clave del usuario en la base
                    String uid = auth.getUid();
                    return UsuarioRepositorio.usuarios().child(uid).setValue(new Usuario(uid, mail));
                })
                .addOnSuccessListener(r -> cargarSesion(mail))
                .addOnFailureListener(this::fallo);
    }

    private void cargarSesion(final String mail) {
        temporizador.removeCallbacks(vencimiento);
        temporizador.postDelayed(vencimiento, LIMITE_CARGA_MS);
        final String uid = auth.getUid();
        UsuarioRepositorio.usuarios().child(uid).get()
                .continueWithTask(t -> {
                    if (t.getResult().exists()) {
                        return Tasks.forResult(t.getResult().getValue(Usuario.class));
                    }
                    // Cuentas creadas antes de usar el uid como clave
                    return UsuarioRepositorio.usuarios().orderByChild("mail").equalTo(mail).limitToFirst(1).get()
                            .continueWith(t2 -> primero(t2.getResult(), Usuario.class));
                })
                .onSuccessTask(usuario -> {
                    if (usuario == null) {
                        throw new IllegalStateException("No se encontraron los datos del usuario");
                    }
                    return cargarGrupo(usuario).continueWith(t -> usuario);
                })
                .addOnSuccessListener(usuario -> {
                    temporizador.removeCallbacks(vencimiento);
                    if (estado.getValue() == null || estado.getValue().tipo != Estado.Tipo.CARGANDO) {
                        // Ya se informo que vencio el tiempo: no entrar por detras del usuario
                        return;
                    }
                    SesionManager.setUsuario(usuario);
                    // Limpieza de datos legados: la clave nunca debio persistirse en la base
                    UsuarioRepositorio.usuarios().child(usuario.getId()).child(CLAVE_LEGADA).removeValue();
                    estado.setValue(Estado.ingreso(usuario));
                })
                .addOnFailureListener(e -> {
                    temporizador.removeCallbacks(vencimiento);
                    auth.signOut();
                    fallo(e);
                });
    }

    private Task<Grupo> cargarGrupo(Usuario usuario) {
        final String idGrupo = usuario.getIdGrupo();
        if (idGrupo == null) {
            SesionManager.setGrupo(new Grupo());
            return Tasks.forResult(SesionManager.getGrupo());
        }
        return FirebaseUtils.db().getReference(FirebaseUtils.dbGrupo).child(idGrupo).get()
                .onSuccessTask(snapshot -> {
                    Grupo grupo = snapshot.getValue(Grupo.class);
                    final Grupo cargado = grupo != null ? grupo : new Grupo();
                    return UsuarioRepositorio.usuarios().orderByChild("idGrupo").equalTo(idGrupo).get()
                            .continueWith(t -> {
                                List<Usuario> integrantes = new ArrayList<>();
                                for (DataSnapshot hijo : t.getResult().getChildren()) {
                                    integrantes.add(hijo.getValue(Usuario.class));
                                }
                                cargado.setIntegrantes(integrantes);
                                SesionManager.setGrupo(cargado);
                                return cargado;
                            });
                });
    }

    private static <T> T primero(DataSnapshot snapshot, Class<T> clase) {
        for (DataSnapshot hijo : snapshot.getChildren()) {
            return hijo.getValue(clase);
        }
        return null;
    }

    @Override
    protected void onCleared() {
        temporizador.removeCallbacks(vencimiento);
    }

    private void fallo(@NonNull Exception e) {
        estado.setValue(Estado.error(mensaje(e)));
    }

    static String mensaje(Exception e) {
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Ya existe una cuenta con ese mail";
        } else if (e instanceof FirebaseAuthWeakPasswordException) {
            return "La clave es demasiado debil";
        } else if (e instanceof FirebaseAuthInvalidUserException || e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Mail o clave incorrectos";
        } else if (e instanceof FirebaseNetworkException) {
            return "Sin conexion. Revisa tu internet e intenta de nuevo";
        }
        return e.getMessage() != null ? e.getMessage() : "No se pudo ingresar";
    }
}
