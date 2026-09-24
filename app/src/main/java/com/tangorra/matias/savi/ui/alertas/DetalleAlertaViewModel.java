package com.tangorra.matias.savi.ui.alertas;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.RespuestaAlerta;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Service.PoliticaAlertas;
import com.tangorra.matias.savi.data.AlertaRepositorio;

public class DetalleAlertaViewModel extends ViewModel {

    private String idGrupo;
    private String idAlerta;
    private LiveData<Alerta> alerta;
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> enviando = new MutableLiveData<>(false);

    public void cargar(String idGrupo, String idAlerta) {
        if (alerta != null) {
            return;
        }
        this.idGrupo = idGrupo;
        this.idAlerta = idAlerta;
        alerta = AlertaRepositorio.alerta(idGrupo, idAlerta);
    }

    public LiveData<Alerta> alerta() {
        return alerta;
    }

    public LiveData<String> error() {
        return error;
    }

    public LiveData<Boolean> enviando() {
        return enviando;
    }

    public Usuario usuario() {
        return SesionManager.getUsuario();
    }

    public void responder(String respuesta) {
        Alerta actual = alerta.getValue();
        Usuario usuario = usuario();
        if (actual == null || usuario == null) {
            return;
        }
        RespuestaAlerta nueva = AlertaRepositorio.nuevaRespuesta(usuario, idAlerta);
        nueva.setRespuesta(respuesta);
        enviando.setValue(true);
        AlertaRepositorio.responder(idGrupo, idAlerta, nueva, PoliticaAlertas.estadoAlResponder(actual, usuario, respuesta))
                .addOnCompleteListener(t -> {
                    enviando.setValue(false);
                    if (!t.isSuccessful()) {
                        error.setValue("No se pudo enviar la respuesta");
                    }
                });
    }

    public void cerrar() {
        enviando.setValue(true);
        AlertaRepositorio.cerrar(idGrupo, idAlerta).addOnCompleteListener(t -> {
            enviando.setValue(false);
            if (!t.isSuccessful()) {
                error.setValue("No se pudo cerrar la alerta");
            }
        });
    }
}
