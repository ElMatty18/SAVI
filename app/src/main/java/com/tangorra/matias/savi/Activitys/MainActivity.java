package com.tangorra.matias.savi.Activitys;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import com.tangorra.matias.savi.BroadCastReciber.EstadoDispositivo;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.R;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final long SPLASH_SCREEN_DELAY = 2000;

    private EstadoDispositivo estadoDispositivo;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //ocultar action bar
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        SesionManager.clean();

        //se dispara despues de 2 segundos
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Start the next activity
                Intent navegar = new Intent(MainActivity.this, AccesoActivity.class);
                startActivity(navegar);
                finish();
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }

    private void initSystem(){
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        estadoDispositivo = new EstadoDispositivo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //registerReceiver(estadoDispositivo, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(estadoDispositivo);
    }
}
