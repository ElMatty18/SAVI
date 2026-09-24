package com.tangorra.matias.savi.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
import com.tangorra.matias.savi.Entidades.Notificacion;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import java.util.Collections;
import java.util.List;

public final class NotificacionRepositorio {

    private NotificacionRepositorio() {
    }

    /** Todas las notificaciones, las mas nuevas primero (las push-keys son cronologicas). */
    public static LiveData<List<Notificacion>> notificaciones() {
        return new FirebaseLiveData<>(FirebaseUtils.db().getReference(FirebaseUtils.dbNotificacion),
                new FirebaseLiveData.Parser<List<Notificacion>>() {
                    @Override
                    public List<Notificacion> parse(@NonNull DataSnapshot snapshot) {
                        List<Notificacion> lista = FirebaseLiveData.lista(Notificacion.class).parse(snapshot);
                        Collections.reverse(lista);
                        return lista;
                    }
                });
    }

    public static com.google.android.gms.tasks.Task<Void> crear(com.tangorra.matias.savi.Entidades.Usuario autor,
                                                               String titulo, String contenido,
                                                               double lat, double lng, int rangoMetros) {
        com.google.firebase.database.DatabaseReference ref = FirebaseUtils.db().getReference(FirebaseUtils.dbNotificacion).push();
        Notificacion n = new Notificacion();
        n.setId(ref.getKey());
        n.setCreadoBy(autor.getId());
        n.setTitle(titulo);
        n.setContenido(contenido);
        n.setLat(lat);
        n.setLng(lng);
        n.setRango(rangoMetros);
        n.setCreacion(new java.util.Date());
        return ref.setValue(n);
    }
}
