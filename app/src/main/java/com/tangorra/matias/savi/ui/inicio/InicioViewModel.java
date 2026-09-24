package com.tangorra.matias.savi.ui.inicio;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.Grupo;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.data.AlertaRepositorio;
import com.tangorra.matias.savi.data.GrupoRepositorio;
import com.tangorra.matias.savi.data.Sesion;
import com.tangorra.matias.savi.data.UsuarioRepositorio;

import java.util.Collections;
import java.util.List;

public class InicioViewModel extends ViewModel {

    private final MediatorLiveData<Usuario> usuario = new MediatorLiveData<>();
    private final LiveData<String> idGrupo;
    private final LiveData<Grupo> grupo;
    private final LiveData<List<Alerta>> alertas;
    private final LiveData<List<Usuario>> vecinos;

    public InicioViewModel() {
        // Arranca con lo que ya esta en memoria y se actualiza en vivo con la base
        usuario.setValue(SesionManager.getUsuario());
        usuario.addSource(Sesion.usuario(), u -> {
            if (u != null) {
                usuario.setValue(u);
            }
        });

        idGrupo = Transformations.distinctUntilChanged(
                Transformations.map(usuario, u -> u != null ? u.getIdGrupo() : null));
        grupo = Transformations.switchMap(idGrupo, id -> id == null
                ? new MutableLiveData<>(null)
                : GrupoRepositorio.grupo(id));
        alertas = Transformations.switchMap(idGrupo, id -> id == null
                ? new MutableLiveData<>(Collections.<Alerta>emptyList())
                : AlertaRepositorio.alertasGrupo(id));
        vecinos = Transformations.switchMap(idGrupo, id -> id == null
                ? new MutableLiveData<>(Collections.<Usuario>emptyList())
                : UsuarioRepositorio.integrantesGrupo(id));
    }

    public LiveData<Usuario> usuario() {
        return usuario;
    }

    public LiveData<Grupo> grupo() {
        return grupo;
    }

    public LiveData<List<Alerta>> alertas() {
        return alertas;
    }

    public LiveData<List<Usuario>> vecinos() {
        return vecinos;
    }
}
