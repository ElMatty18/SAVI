package com.tangorra.matias.savi.data;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Grupo familiar: /Familia/{idFamilia}/{uid}: uid y Usuario/{uid}/idFamilia.
 * Todas las operaciones son escrituras multi-ruta atomicas.
 */
public final class FamiliaRepositorio {

    private FamiliaRepositorio() {
    }

    /**
     * Vincula al usuario con quien le mostro su QR. Si ninguno tiene familia se crea una nueva;
     * si alguno ya tiene, el otro se suma a esa.
     *
     * @return el familiar vinculado.
     */
    public static Task<Usuario> vincular(final Usuario yo, String idOtro) {
        if (idOtro == null || idOtro.equals(yo.getId())) {
            return Tasks.forException(new IllegalArgumentException("Ese código es el tuyo"));
        }
        return UsuarioRepositorio.usuarios().child(idOtro).get().onSuccessTask(snapshot -> {
            final Usuario otro = snapshot.getValue(Usuario.class);
            if (otro == null || otro.getId() == null) {
                throw new IllegalArgumentException("El código no corresponde a un usuario de SAVI");
            }
            if (yo.getIdFamilia() != null && yo.getIdFamilia().equals(otro.getIdFamilia())) {
                throw new IllegalArgumentException("Ya son familia");
            }
            Map<String, Object> cambios = new HashMap<>();
            String idFamilia;
            if (yo.getIdFamilia() == null && otro.getIdFamilia() == null) {
                idFamilia = FirebaseUtils.db().getReference(FirebaseUtils.dbFamilia).push().getKey();
                agregar(cambios, idFamilia, yo.getId());
                agregar(cambios, idFamilia, otro.getId());
            } else if (yo.getIdFamilia() != null) {
                idFamilia = yo.getIdFamilia();
                agregar(cambios, idFamilia, otro.getId());
            } else {
                idFamilia = otro.getIdFamilia();
                agregar(cambios, idFamilia, yo.getId());
            }
            return FirebaseUtils.db().getReference().updateChildren(cambios).continueWith(t -> {
                if (!t.isSuccessful()) {
                    throw t.getException();
                }
                return otro;
            });
        });
    }

    /** Saca a un integrante de la familia (a uno mismo o a otro familiar). */
    public static Task<Void> quitar(final String idFamilia, final String idUsuario) {
        return FirebaseUtils.db().getReference(FirebaseUtils.dbFamilia).child(idFamilia).get().onSuccessTask(snapshot -> {
            Map<String, Object> cambios = new HashMap<>();
            // Las entradas viejas usan una push-key como clave: se busca por valor
            for (DataSnapshot entrada : snapshot.getChildren()) {
                if (idUsuario.equals(entrada.getValue(String.class))) {
                    cambios.put(FirebaseUtils.dbFamilia + "/" + idFamilia + "/" + entrada.getKey(), null);
                }
            }
            cambios.put(FirebaseUtils.dbUsuario + "/" + idUsuario + "/idFamilia", null);
            return FirebaseUtils.db().getReference().updateChildren(cambios);
        });
    }

    private static void agregar(Map<String, Object> cambios, String idFamilia, String idUsuario) {
        cambios.put(FirebaseUtils.dbFamilia + "/" + idFamilia + "/" + idUsuario, idUsuario);
        cambios.put(FirebaseUtils.dbUsuario + "/" + idUsuario + "/idFamilia", idFamilia);
    }
}
