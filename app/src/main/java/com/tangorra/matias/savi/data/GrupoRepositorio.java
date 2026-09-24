package com.tangorra.matias.savi.data;

import androidx.lifecycle.LiveData;

import com.tangorra.matias.savi.Entidades.Grupo;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

public final class GrupoRepositorio {

    private GrupoRepositorio() {
    }

    public static LiveData<Grupo> grupo(String idGrupo) {
        return new FirebaseLiveData<>(FirebaseUtils.db().getReference(FirebaseUtils.dbGrupo).child(idGrupo),
                FirebaseLiveData.objeto(Grupo.class));
    }
}
