package com.tangorra.matias.savi.data;

import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.Task;
import com.tangorra.matias.savi.Entidades.Grupo;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import java.util.HashMap;
import java.util.Map;

public final class GrupoRepositorio {

    private GrupoRepositorio() {
    }

    public static LiveData<Grupo> grupo(String idGrupo) {
        return new FirebaseLiveData<>(FirebaseUtils.db().getReference(FirebaseUtils.dbGrupo).child(idGrupo),
                FirebaseLiveData.objeto(Grupo.class));
    }

    public static Task<Grupo> leer(String idGrupo) {
        return FirebaseUtils.db().getReference(FirebaseUtils.dbGrupo).child(idGrupo).get()
                .continueWith(t -> t.getResult().getValue(Grupo.class));
    }

    /** Crea el grupo y suma a quien lo creo, en una sola escritura. El resultado es el id del grupo. */
    public static Task<String> crear(Usuario creador, String nombre, double lat, double lng, int radioMetros) {
        final String id = FirebaseUtils.db().getReference(FirebaseUtils.dbGrupo).push().getKey();
        Grupo grupo = new Grupo(id, nombre, null, lat, lng, radioMetros);
        Map<String, Object> cambios = new HashMap<>();
        cambios.put(FirebaseUtils.dbGrupo + "/" + id, grupo);
        cambios.put(FirebaseUtils.dbUsuario + "/" + creador.getId() + "/idGrupo", id);
        return FirebaseUtils.db().getReference().updateChildren(cambios).continueWith(t -> {
            if (!t.isSuccessful()) {
                throw t.getException();
            }
            return id;
        });
    }

    /** Se une al grupo del codigo QR, verificando antes que exista. */
    public static Task<Grupo> unirse(final String uid, final String idGrupo) {
        if (idGrupo == null || idGrupo.contains("/") || idGrupo.contains(".") || idGrupo.isEmpty()) {
            return com.google.android.gms.tasks.Tasks.forException(new IllegalArgumentException("El código no es de un grupo de SAVI"));
        }
        return leer(idGrupo).onSuccessTask(grupo -> {
            if (grupo == null || grupo.getId() == null) {
                throw new IllegalArgumentException("El código no es de un grupo de SAVI");
            }
            return UsuarioRepositorio.usuarios().child(uid).child("idGrupo").setValue(idGrupo)
                    .continueWith(t -> {
                        if (!t.isSuccessful()) {
                            throw t.getException();
                        }
                        return grupo;
                    });
        });
    }

    public static Task<Void> salir(String uid) {
        return UsuarioRepositorio.usuarios().child(uid).child("idGrupo").removeValue();
    }
}
