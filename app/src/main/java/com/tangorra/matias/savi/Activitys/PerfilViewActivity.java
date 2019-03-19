package com.tangorra.matias.savi.Activitys;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.tangorra.matias.savi.R;

public class PerfilViewActivity extends AppCompatActivity {

    private Button volver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_perfil);

        volver = findViewById(R.id.btnVolverMenu);

        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent navegar = new Intent(PerfilViewActivity.this, MenuPrincipalActivity.class);
                startActivity(navegar);
                finish();
            }
        });
    }

    @Override
    public  void onBackPressed(){
        Intent menu = new Intent(PerfilViewActivity.this, MenuPrincipalActivity.class);
        startActivity(menu);
        finish();
    }
}
