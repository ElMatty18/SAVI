package com.tangorra.matias.savi.data;

import androidx.lifecycle.LiveData;

import com.google.firebase.database.DatabaseReference;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import java.util.List;

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
}
