package com.tangorra.matias.savi.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;

/**
 * Usuario logueado, sincronizado en vivo con /Usuario/{uid}. Mantiene al dia a SesionManager para
 * las pantallas que todavia lo usan (por ejemplo, si un familiar lo suma a su familia).
 */
public final class Sesion {

    private static final String TAG = "Sesion";

    private static final MutableLiveData<Usuario> usuario = new MutableLiveData<>();
    private static DatabaseReference referencia;

    private static final ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            Usuario actual = snapshot.getValue(Usuario.class);
            if (actual == null) {
                return;
            }
            // Datos que solo existen en memoria y no se guardan en la base
            Usuario anterior = SesionManager.getUsuario();
            if (anterior != null && actual.getId().equals(anterior.getId())) {
                actual.setGrupo(anterior.getGrupo());
            }
            SesionManager.setUsuario(actual);
            usuario.setValue(actual);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.w(TAG, "Sesion cancelada: " + error.getMessage());
        }
    };

    private Sesion() {
    }

    public static synchronized void iniciar(String uid) {
        if (referencia != null && referencia.getKey() != null && referencia.getKey().equals(uid)) {
            return;
        }
        cerrar();
        // Mientras llega la base, se muestra lo que ya esta en memoria
        Usuario enMemoria = SesionManager.getUsuario();
        if (enMemoria != null && uid.equals(enMemoria.getId())) {
            usuario.setValue(enMemoria);
        }
        referencia = UsuarioRepositorio.usuarios().child(uid);
        referencia.addValueEventListener(listener);
    }

    public static synchronized void cerrar() {
        if (referencia != null) {
            referencia.removeEventListener(listener);
            referencia = null;
        }
        usuario.setValue(null);
    }

    public static LiveData<Usuario> usuario() {
        return usuario;
    }
}
