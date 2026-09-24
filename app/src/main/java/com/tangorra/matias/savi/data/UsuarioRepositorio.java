package com.tangorra.matias.savi.data;

import androidx.lifecycle.LiveData;

import com.google.firebase.database.DatabaseReference;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import java.util.List;
import java.util.Map;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageMetadata;

public final class UsuarioRepositorio {

    private UsuarioRepositorio() {
    }

    public static DatabaseReference usuarios() {
        return FirebaseUtils.db().getReference(FirebaseUtils.dbUsuario);
    }

    public static LiveData<Usuario> usuario(String uid) {
        return new FirebaseLiveData<>(usuarios().child(uid), FirebaseLiveData.objeto(Usuario.class));
    }

    public static LiveData<List<Usuario>> integrantesGrupo(String idGrupo) {
        return new FirebaseLiveData<>(usuarios().orderByChild("idGrupo").equalTo(idGrupo), FirebaseLiveData.lista(Usuario.class));
    }

    public static LiveData<List<Usuario>> familiares(String idFamilia) {
        return new FirebaseLiveData<>(usuarios().orderByChild("idFamilia").equalTo(idFamilia), FirebaseLiveData.lista(Usuario.class));
    }

    /** Actualiza solo los campos indicados (no pisa cambios hechos por otros, como idFamilia). */
    public static Task<Void> actualizar(String uid, Map<String, Object> campos) {
        return usuarios().child(uid).updateChildren(campos);
    }

    public static Task<Uri> urlFoto(String uid) {
        return FirebaseUtils.storage().getReference().child("Fotos").child(uid).getDownloadUrl();
    }

    /** Sube la foto de perfil (JPEG ya reducido) y devuelve su URL. */
    public static Task<Uri> subirFoto(String uid, byte[] jpeg) {
        StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpeg").build();
        return FirebaseUtils.storage().getReference().child("Fotos").child(uid).putBytes(jpeg, metadata)
                .continueWithTask(t -> {
                    if (!t.isSuccessful()) {
                        throw t.getException();
                    }
                    return urlFoto(uid);
                });
    }
}
