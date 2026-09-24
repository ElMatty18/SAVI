package com.tangorra.matias.savi.ui.alertas;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.data.AlertaRepositorio;
import com.tangorra.matias.savi.data.UsuarioRepositorio;
import com.tangorra.matias.savi.ui.comun.PresentacionAlerta;

import java.util.ArrayList;
import java.util.List;

public class EmitirAlertaViewModel extends ViewModel {

    public enum Resultado { NINGUNO, ENVIANDO, ENVIADA, ERROR }

    private final Usuario yo = SesionManager.getUsuario();
    private final LiveData<List<Usuario>> vecinos;
    private final MutableLiveData<String> tipo = new MutableLiveData<>();
    private final MutableLiveData<Resultado> resultado = new MutableLiveData<>(Resultado.NINGUNO);

    /** null = todo el grupo */
    private Usuario destinatario;
    private boolean destinoElegido;
    private String idAlertaEmitida;

    public EmitirAlertaViewModel() {
        String idGrupo = yo != null ? yo.getIdGrupo() : null;
        vecinos = idGrupo == null
                ? new MutableLiveData<>(new ArrayList<>())
                : Transformations.map(UsuarioRepositorio.integrantesGrupo(idGrupo), lista -> {
                    // No tiene sentido avisarse a uno mismo
                    List<Usuario> otros = new ArrayList<>();
                    for (Usuario u : lista) {
                        if (yo == null || !u.getId().equals(yo.getId())) {
                            otros.add(u);
                        }
                    }
                    return otros;
                });
    }

    public LiveData<List<Usuario>> vecinos() {
        return vecinos;
    }

    public LiveData<String> tipo() {
        return tipo;
    }

    public LiveData<Resultado> resultado() {
        return resultado;
    }

    public String idGrupo() {
        return yo != null ? yo.getIdGrupo() : null;
    }

    public String idAlertaEmitida() {
        return idAlertaEmitida;
    }

    public void elegirTipo(String nuevo) {
        tipo.setValue(nuevo);
        if (destinoElegido && destinatario == null && PresentacionAlerta.requiereDestinatario(nuevo)) {
            destinoElegido = false;
        }
    }

    public void elegirDestino(Usuario vecino) {
        destinatario = vecino;
        destinoElegido = true;
    }

    public void limpiarDestino() {
        destinatario = null;
        destinoElegido = false;
    }

    public Usuario destinatario() {
        return destinatario;
    }

    public boolean listoParaEmitir() {
        String t = tipo.getValue();
        return t != null && destinoElegido && (destinatario != null || !PresentacionAlerta.requiereDestinatario(t));
    }

    public void emitir() {
        if (!listoParaEmitir() || idGrupo() == null) {
            return;
        }
        resultado.setValue(Resultado.ENVIANDO);
        AlertaRepositorio.emitir(idGrupo(), yo, tipo.getValue(), destinatario)
                .addOnSuccessListener(id -> {
                    idAlertaEmitida = id;
                    resultado.setValue(Resultado.ENVIADA);
                })
                .addOnFailureListener(e -> resultado.setValue(Resultado.ERROR));
    }
}
