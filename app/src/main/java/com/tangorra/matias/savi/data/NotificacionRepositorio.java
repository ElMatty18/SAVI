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
}
