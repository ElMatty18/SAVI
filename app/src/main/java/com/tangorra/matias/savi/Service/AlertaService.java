package com.tangorra.matias.savi.Service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tangorra.matias.savi.Activitys.MainActivity;
import com.tangorra.matias.savi.Activitys.RespuestaAlertaActivity;
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.Configuracion;
import com.tangorra.matias.savi.Entidades.RespuestaAlerta;
import com.tangorra.matias.savi.Entidades.RespuestaVisto;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.FirebaseUtils;
import com.tangorra.matias.savi.Utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AlertaService extends IntentService {

    private static final String TAG = "AlertaService";

    private DatabaseReference dbGrupoVecinal;
    private ChildEventListener listenerAlertas = getListenerAlertas();

    public AlertaService() {
        super("AlertaService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent workIntent) {
        String dataString = workIntent.getDataString();
        Log.i(TAG, "The service is on");

        if (SesionManager.getUsuario() != null && SesionManager.getUsuario().getIdGrupo() != null){
            dbGrupoVecinal = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo).child(SesionManager.getUsuario().getIdGrupo());
            escucharAlertas();
        }


    }



    @NonNull
    private ChildEventListener getListenerAlertas() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                eventoAlerta(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                //eventoAlerta(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }

    private boolean condicionRespuestaAlerta(Alerta alerta){
        if ((alerta.getEstado() != null) && (alerta.getEstado().equals(StringUtils.alertaActiva) && !respondioAlerta(alerta, SesionManager.getUsuario().getId()))){
            if (!alerta.getCreadoById().equals(SesionManager.getUsuario().getId())){
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private void lanzarResponderAlerta(Alerta alerta){
        Intent intent = new Intent(this, RespuestaAlertaActivity.class);
        intent.putExtra(StringUtils.parametroAlerta, alerta);
        startActivity(intent);
    }

    private boolean respondioAlerta(Alerta alerta, String idUsuario){
        if (alerta.getRespuestas() == null){
            return false;
        }
        for (RespuestaAlerta item : alerta.getRespuestas()){
            if (item != null && item.getIdUsuario() != null && item.getIdUsuario().equals(idUsuario)){
                return true;
            }
        }
        return false;
    }

    private void eventoAlerta(DataSnapshot dataSnapshot) {
        Alerta alerta = dataSnapshot.getValue(Alerta.class);
        if (condicionRespuestaAlerta(alerta)) {
            if (tieneConfiguacionActiva(SesionManager.getUsuario())){
                lanzarRespuestaConfigurada(SesionManager.getUsuario().getConfiguracion(),alerta);
                notificacionAlerta(alerta);
            } else {
                lanzarResponderAlerta(alerta);
                notificacionAlerta(alerta);
            }
        }
    }


    private boolean tieneConfiguacionActiva(Usuario u){
        if (u.getConfiguracion() != null){
            if (u.getConfiguracion().isConfiguracionActiva()){
                return true;
            }
        }
        return false;
    }


    private void lanzarRespuestaConfigurada(Configuracion configuracion, Alerta alerta) {
        responderAlerta(alerta, configuracion.getConfiguracionSeleccionada(), configuracion.getMensaje());
    }

    private void escucharAlertas(){
        dbGrupoVecinal.child("alertas").addChildEventListener(listenerAlertas);
    }

    private void notificacionAlerta(Alerta alerta) {
        if (alerta == null){
            return;
        }
        String tipoAlerta = alerta.getAlarma();
        String dirigida = alerta.getDirigida();
        if (tipoAlerta.equals(StringUtils.ALARMA_SONANDO)){
            //Se dara un aviso sonoro al usuario al que esta dirijida dicha alarma
            if (dirigida.equals(StringUtils.getTextoFormateado(SesionManager.getUsuario().getGlosa()))){
                Notification(alerta, StringUtils.notificacion_alerta , true, true);
            }
        } else if (tipoAlerta.equals(StringUtils.SOSPECHA_ROBO)){
            //La alarma sera ruidosa para todos los usuarios excepto para el que fue destinada
            if (!dirigida.equals(StringUtils.getTextoFormateado(SesionManager.getUsuario().getGlosa()))){
                Notification(alerta, StringUtils.notificacion_riesgo, true, false);
            }
        } else if (tipoAlerta.equals(StringUtils.ACTITUD_SOSPECHOSA)){
            //Se da aviso sonoro a todos los usuarios
            Notification(alerta, StringUtils.notificacion_cuidado, true, true);
        } else if (tipoAlerta.equals(StringUtils.DANO_VEHICULO)){
            //Si existe una alerta de Daño al vehiculo, informara al usuario respectivo
            if (dirigida.equals(StringUtils.getTextoFormateado(SesionManager.getUsuario().getGlosa()))){
                //vibrar
                Notification(alerta, StringUtils.notificacion_advertencia, false, true);
            }
        } else if (tipoAlerta.equals(StringUtils.PRINCIPIO_FUEGO)){
            //Se creara una alama sonora para el usuario destinatario de dicha alerta, y una notificacion silenciosa a los demas usuarios
            if (dirigida.equals(StringUtils.getTextoFormateado(SesionManager.getUsuario().getGlosa()))){
                //sonar
                Notification(alerta, StringUtils.notificacion_peligro, true, true);
            }else {
                //vibrar
                Notification(alerta,  StringUtils.notificacion_peligro, false, true);
            }
        } else if (tipoAlerta.equals(StringUtils.AGRESION)){
            //Se creara una alerta sonora para todos los usuarios
            Notification(alerta, StringUtils.notificacion_amenaza, true, true);

        } else if (tipoAlerta.equals(StringUtils.MAL_ESTACIONADO)){
            //Se crea una alarta silenciosa para todos los usuarios
            Notification(alerta, StringUtils.notificacion_aviso, false, false);
        }
    }


    private void responderAlerta(Alerta alerta, String respuesta, String mensaje){
        FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo).child(SesionManager.getGrupo().getId()).child("alertas").child(alerta.getId()).child("estado").setValue(respuesta);

        RespuestaAlerta respuestaAlerta = new RespuestaAlerta();
        respuestaAlerta.setIdUsuario(SesionManager.getUsuario().getId());
        respuestaAlerta.setNombreUsuario(SesionManager.getUsuario().getNombre());
        respuestaAlerta.setApellidoUsuario(SesionManager.getUsuario().getApellido());
        respuestaAlerta.setCreacion(new Date());
        respuestaAlerta.setIdAlarma(alerta.getId());

        respuestaAlerta.setRespuestaAutomatica(respuesta);
        if (mensaje != null){
            respuestaAlerta.setMensajeAutomatica(mensaje);
        }

        if (alerta.getRespuestas() == null){
            alerta.setRespuestas(new ArrayList<RespuestaAlerta>());
        }

        alerta.getRespuestas().add(respuestaAlerta);

        FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo).child(SesionManager.getGrupo().getId()).child("alertas").child(alerta.getId()).setValue(alerta);
    }

    public void Notification(Alerta alerta, String nivel, boolean sonora, boolean vibracion){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        MediaPlayer mp = MediaPlayer.create(getBaseContext(), R.raw.zxing_beep);
        mp.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            //notificationChannel.setSound();
            notificationChannel.enableVibration(vibracion);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Intent stopped = new Intent(this,MainActivity.class);
        stopped.setAction("test");
        if (stopped.getAction().equals(stopped)) {
            mp.stop();
        }

        PendingIntent actionPendingIntent = PendingIntent.getActivity(this,1,stopped,PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon_casa)
                .setContentTitle(nivel + "! " + alerta.getAlarma())
                .setContentText("Dirigida -> "+alerta.getDirigida());

        if (sonora){
            notificationBuilder.setSound(Uri.parse("android.resource://"+getPackageName()+"/" + R.raw.zxing_beep));
        }

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }
}
