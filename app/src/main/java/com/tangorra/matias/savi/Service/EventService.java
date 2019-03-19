package com.tangorra.matias.savi.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tangorra.matias.savi.Activitys.MainActivity;

public class EventService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        startService(new Intent(this, MainActivity.class));

    }

    @Override
    public void onStart(Intent intent, int startId){
        System.out.println("El servicio a Comenzado");
        this.stopSelf();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopService(new Intent(this, MainActivity.class));
        System.out.println("El servicio a Terminado");
    }
}
