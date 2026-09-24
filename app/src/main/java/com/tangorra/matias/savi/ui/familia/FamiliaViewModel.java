package com.tangorra.matias.savi.ui.familia;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.data.FamiliaRepositorio;
import com.tangorra.matias.savi.data.Sesion;
import com.tangorra.matias.savi.data.UsuarioRepositorio;
import com.tangorra.matias.savi.ui.comun.Evento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FamiliaViewModel extends ViewModel {

    private final LiveData<Usuario> yo = Sesion.usuario();
    private final LiveData<List<Usuario>> familiares;
    private final MutableLiveData<Evento<String>> mensajes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> trabajando = new MutableLiveData<>(false);

    public FamiliaViewModel() {
        LiveData<String> idFamilia = Transformations.distinctUntilChanged(
                Transformations.map(yo, u -> u != null ? u.getIdFamilia() : null));
        familiares = Transformations.switchMap(idFamilia, id -> id == null
                ? new MutableLiveData<>(Collections.<Usuario>emptyList())
                : Transformations.map(UsuarioRepositorio.familiares(id), lista -> {
                    List<Usuario> otros = new ArrayList<>();
                    Usuario actual = yo.getValue();
                    for (Usuario u : lista) {
                        if (actual == null || !u.getId().equals(actual.getId())) {
                            otros.add(u);
                        }
                    }
                    return otros;
                }));
    }

    public LiveData<Usuario> yo() {
        return yo;
    }

    public LiveData<List<Usuario>> familiares() {
        return familiares;
    }

    public LiveData<Evento<String>> mensajes() {
        return mensajes;
    }

    public LiveData<Boolean> trabajando() {
        return trabajando;
    }

    public void vincular(String codigo) {
        Usuario actual = yo.getValue();
        if (actual == null) {
            return;
        }
        trabajando.setValue(true);
        FamiliaRepositorio.vincular(actual, codigo)
                .addOnSuccessListener(otro -> avisar("Ahora " + otro.getGlosaFormateada().trim() + " es parte de tu familia"))
                .addOnFailureListener(e -> avisar(e.getMessage() != null ? e.getMessage() : "No se pudo vincular"));
    }

    public void quitar(Usuario familiar) {
        Usuario actual = yo.getValue();
        if (actual == null || actual.getIdFamilia() == null) {
            return;
        }
        trabajando.setValue(true);
        FamiliaRepositorio.quitar(actual.getIdFamilia(), familiar.getId())
                .addOnSuccessListener(v -> avisar(familiar.getGlosaFormateada().trim() + " ya no está en tu familia"))
                .addOnFailureListener(e -> avisar("No se pudo quitar al familiar"));
    }

    public void salir() {
        Usuario actual = yo.getValue();
        if (actual == null || actual.getIdFamilia() == null) {
            return;
        }
        trabajando.setValue(true);
        FamiliaRepositorio.quitar(actual.getIdFamilia(), actual.getId())
                .addOnSuccessListener(v -> avisar("Saliste del grupo familiar"))
                .addOnFailureListener(e -> avisar("No se pudo salir del grupo familiar"));
    }

    private void avisar(String mensaje) {
        trabajando.setValue(false);
        mensajes.setValue(new Evento<>(mensaje));
    }
}
