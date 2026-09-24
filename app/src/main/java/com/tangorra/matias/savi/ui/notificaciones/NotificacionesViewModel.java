package com.tangorra.matias.savi.ui.notificaciones;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.tangorra.matias.savi.Entidades.Domicilio;
import com.tangorra.matias.savi.Entidades.Notificacion;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Service.PoliticaNotificaciones;
import com.tangorra.matias.savi.data.NotificacionRepositorio;

import java.util.ArrayList;
import java.util.List;

/** Notificaciones del barrio que alcanzan alguno de los domicilios del usuario, mas las propias. */
public class NotificacionesViewModel extends ViewModel {

    private final Usuario yo = SesionManager.getUsuario();
    private final LiveData<List<Notificacion>> notificaciones = Transformations.map(
            NotificacionRepositorio.notificaciones(), this::relevantes);

    public LiveData<List<Notificacion>> notificaciones() {
        return notificaciones;
    }

    public Usuario yo() {
        return yo;
    }

    private List<Notificacion> relevantes(List<Notificacion> todas) {
        List<Notificacion> lista = new ArrayList<>();
        for (Notificacion n : todas) {
            boolean propia = yo != null && yo.getId().equals(n.getCreadoBy());
            if (propia || alcanza(n)) {
                lista.add(n);
            }
        }
        return lista;
    }

    private boolean alcanza(Notificacion n) {
        if (yo == null || yo.getPerfil() == null) {
            return false;
        }
        return PoliticaNotificaciones.enRango(n, yo.getPerfil().getDomicilio())
                || PoliticaNotificaciones.enRango(n, yo.getPerfil().getDomicilioAlterno());
    }

    /** Distancia en metros al domicilio mas cercano, o -1 si no tiene domicilios. */
    public double distancia(Notificacion n) {
        if (yo == null || yo.getPerfil() == null) {
            return -1;
        }
        double minima = -1;
        for (Domicilio d : new Domicilio[]{yo.getPerfil().getDomicilio(), yo.getPerfil().getDomicilioAlterno()}) {
            if (d != null && (d.getLat() != 0 || d.getLng() != 0)) {
                double metros = PoliticaNotificaciones.distanciaMetros(n, d);
                minima = minima < 0 ? metros : Math.min(minima, metros);
            }
        }
        return minima;
    }
}
