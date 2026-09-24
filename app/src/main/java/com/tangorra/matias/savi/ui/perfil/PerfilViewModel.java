package com.tangorra.matias.savi.ui.perfil;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.data.UsuarioRepositorio;
import com.tangorra.matias.savi.ui.comun.Evento;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PerfilViewModel extends ViewModel {

    public enum Resultado { GUARDADO, ERROR }

    private final Usuario usuario = SesionManager.getUsuario();
    private final MutableLiveData<Uri> foto = new MutableLiveData<>();
    private final MutableLiveData<Boolean> trabajando = new MutableLiveData<>(false);
    private final MutableLiveData<Evento<Resultado>> resultado = new MutableLiveData<>();
    private final MutableLiveData<Evento<String>> errorFoto = new MutableLiveData<>();

    public PerfilViewModel() {
        if (usuario != null && usuario.getId() == null) {
            // Primer ingreso: el id del usuario es su uid de Firebase Auth
            usuario.setId(FirebaseAuth.getInstance().getUid());
        }
        if (usuario != null && usuario.getId() != null) {
            UsuarioRepositorio.urlFoto(usuario.getId()).addOnSuccessListener(foto::setValue);
        }
    }

    public Usuario usuario() {
        return usuario;
    }

    public LiveData<Uri> foto() {
        return foto;
    }

    public LiveData<Boolean> trabajando() {
        return trabajando;
    }

    public LiveData<Evento<Resultado>> resultado() {
        return resultado;
    }

    public LiveData<Evento<String>> errorFoto() {
        return errorFoto;
    }

    public void guardar(String nombre, String apellido, String dni, String celular, String fijo, Date nacimiento) {
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setDni(dni);
        usuario.setCelular(celular);
        usuario.setFijo(fijo.isEmpty() ? null : fijo);
        usuario.setFechaNacimiento(nacimiento);

        // Solo los campos de esta pantalla: reescribir el usuario completo pisaba cambios hechos por otros
        Map<String, Object> campos = new HashMap<>();
        campos.put("id", usuario.getId());
        campos.put("mail", usuario.getMail());
        campos.put("nombre", nombre);
        campos.put("apellido", apellido);
        campos.put("dni", dni);
        campos.put("celular", celular);
        campos.put("fijo", usuario.getFijo());
        campos.put("fechaNacimiento", nacimiento);

        trabajando.setValue(true);
        UsuarioRepositorio.actualizar(usuario.getId(), campos).addOnCompleteListener(t -> {
            trabajando.setValue(false);
            if (t.isSuccessful()) {
                SesionManager.setUsuario(usuario);
            }
            resultado.setValue(new Evento<>(t.isSuccessful() ? Resultado.GUARDADO : Resultado.ERROR));
        });
    }

    public void subirFoto(byte[] jpeg) {
        trabajando.setValue(true);
        UsuarioRepositorio.subirFoto(usuario.getId(), jpeg)
                .addOnSuccessListener(foto::setValue)
                .addOnFailureListener(e -> errorFoto.setValue(new Evento<>("No se pudo subir la foto")))
                .addOnCompleteListener(t -> trabajando.setValue(false));
    }
}
