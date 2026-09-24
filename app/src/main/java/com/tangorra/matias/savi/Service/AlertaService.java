package com.tangorra.matias.savi.Service;

import com.tangorra.matias.savi.data.AlertaRepositorio;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;

/**
 * Escucha las alertas del grupo vecinal mientras la app esta activa.
 * Con la app cerrada las alertas llegan por FCM (ver SaviMessagingService y functions/).
 */
public class AlertaService extends Service {

    private static final String TAG = "AlertaService";

    private DatabaseReference dbAlertas;
    private String idGrupoEscuchado;
    private final ChildEventListener listenerAlertas = getListenerAlertas();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Usuario usuario = SesionManager.getUsuario();
        if (!SesionManager.haySesion() || usuario.getIdGrupo() == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (!usuario.getIdGrupo().equals(idGrupoEscuchado)) {
            dejarDeEscuchar();
            idGrupoEscuchado = usuario.getIdGrupo();
            dbAlertas = AlertaRepositorio.alertas(idGrupoEscuchado);
            dbAlertas.addChildEventListener(listenerAlertas);
            Log.i(TAG, "Escuchando alertas del grupo " + idGrupoEscuchado);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        dejarDeEscuchar();
        super.onDestroy();
    }

    private void dejarDeEscuchar() {
        if (dbAlertas != null) {
            dbAlertas.removeEventListener(listenerAlertas);
            dbAlertas = null;
        }
    }

    @NonNull
    private ChildEventListener getListenerAlertas() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                Usuario usuario = SesionManager.getUsuario();
                if (usuario == null) {
                    return;
                }
                Alerta alerta = dataSnapshot.getValue(Alerta.class);
                ProcesadorAlertas.procesar(AlertaService.this, alerta, usuario, idGrupoEscuchado);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Listener de alertas cancelado: " + databaseError.getMessage());
            }
        };
    }
}
