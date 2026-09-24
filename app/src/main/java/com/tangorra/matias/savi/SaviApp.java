package com.tangorra.matias.savi;

import com.tangorra.matias.savi.Utils.FirebaseUtils;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Utils.Notificador;

public class SaviApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.USAR_EMULADOR) {
            usarEmuladorFirebase();
        }
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

    // Los puertos se redirigen a la PC con "adb reverse" (lo hace scripts/run.sh --emulador).
    // No se usa 10.0.2.2 porque el emulador de la base le indica al SDK que se reconecte a 127.0.0.1.
    private static final String HOST_EMULADOR = "127.0.0.1";

    private void usarEmuladorFirebase() {
        FirebaseUtils.db().useEmulator(HOST_EMULADOR, 9000);
        FirebaseAuth.getInstance().useEmulator(HOST_EMULADOR, 9099);
        FirebaseUtils.storage().useEmulator(HOST_EMULADOR, 9199);
    }
}
