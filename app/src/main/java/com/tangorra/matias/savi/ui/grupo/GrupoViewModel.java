package com.tangorra.matias.savi.ui.grupo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.tangorra.matias.savi.Entidades.Grupo;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.data.GrupoRepositorio;
import com.tangorra.matias.savi.data.Sesion;
import com.tangorra.matias.savi.data.UsuarioRepositorio;
import com.tangorra.matias.savi.ui.comun.Evento;

import java.util.Collections;
import java.util.List;

public class GrupoViewModel extends ViewModel {

    private final LiveData<Usuario> yo = Sesion.usuario();
    private final LiveData<Grupo> grupo;
    private final LiveData<List<Usuario>> vecinos;
    private final MutableLiveData<Evento<Boolean>> salio = new MutableLiveData<>();

    public GrupoViewModel() {
        LiveData<String> idGrupo = Transformations.distinctUntilChanged(
                Transformations.map(yo, u -> u != null ? u.getIdGrupo() : null));
        grupo = Transformations.switchMap(idGrupo, id -> id == null
                ? new MutableLiveData<>(null) : GrupoRepositorio.grupo(id));
        vecinos = Transformations.switchMap(idGrupo, id -> id == null
                ? new MutableLiveData<>(Collections.<Usuario>emptyList()) : UsuarioRepositorio.integrantesGrupo(id));
    }

    public LiveData<Usuario> yo() {
        return yo;
    }

    public LiveData<Grupo> grupo() {
        return grupo;
    }

    public LiveData<List<Usuario>> vecinos() {
        return vecinos;
    }

    public LiveData<Evento<Boolean>> salio() {
        return salio;
    }

    public void salir() {
        Usuario actual = yo.getValue();
        if (actual == null) {
            return;
        }
        GrupoRepositorio.salir(actual.getId()).addOnCompleteListener(t -> salio.setValue(new Evento<>(t.isSuccessful())));
    }
}
