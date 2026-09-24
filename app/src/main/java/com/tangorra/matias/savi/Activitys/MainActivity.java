package com.tangorra.matias.savi.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.R;

public class MainActivity extends AppCompatActivity {

    private static final long SPLASH_SCREEN_DELAY = 2000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable navegar = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(MainActivity.this, AccesoActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //ocultar action bar
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        SesionManager.clean();

        //se dispara despues de 2 segundos
        handler.postDelayed(navegar, SPLASH_SCREEN_DELAY);
    }

    @Override
    protected void onDestroy() {
        // Evita abrir el login si el usuario salio durante el splash
        handler.removeCallbacks(navegar);
        super.onDestroy();
    }
}
