package com.tangorra.matias.savi;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Utils.Notificador;

public class SaviApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SesionManager.init(this);
        Notificador.crearCanales(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityStopped(Activity activity) {
                // Las pantallas editan el usuario de la sesion en memoria: se persiste al salir de cada una.
                SesionManager.guardar();
            }

            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
            @Override public void onActivityStarted(Activity activity) {}
            @Override public void onActivityResumed(Activity activity) {}
            @Override public void onActivityPaused(Activity activity) {}
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override public void onActivityDestroyed(Activity activity) {}
        });
    }
}
