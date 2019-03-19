package com.tangorra.matias.savi.Activitys;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.StringUtils;
import com.tangorra.matias.savi.View.PopUpViewQRGrupo;

public class GrupoVecinalViewActivity extends AppCompatActivity implements OnMapReadyCallback  {

    private GoogleMap mMap;
    private double lat = 0.0;
    private double lng = 0.0;

    private Circle circle;
    private Marker marcador;

    private Button verQRGrupo;
    private Button back_menu_grupo_vecinal;

    private TextView nombreGrupo;
    private TextView maxUsuarios;
    private TextView rangoGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo_vecinal_view);

        verQRGrupo = findViewById(R.id.compartir_grupo_vecinal);
        nombreGrupo = findViewById(R.id.view_txtNombreGrupo);
        maxUsuarios = findViewById(R.id.view_usuario_max_selec);
        rangoGrupo = findViewById(R.id.view_usuario_max_rango);
        back_menu_grupo_vecinal = findViewById(R.id.back_menu_grupo_vecinal);


        verQRGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GrupoVecinalViewActivity.this, PopUpViewQRGrupo.class));
            }
        });

        back_menu_grupo_vecinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        cargarDatosGrupo();
        ubicacionGrupo();
    }

    private void cargarDatosGrupo() {
        nombreGrupo.setText(StringUtils.getTextoFormateado(SesionManager.getGrupo().getNombre().toString()));
        maxUsuarios.setText(SesionManager.getGrupo().getMaxUsuarios().toString());
        rangoGrupo.setText(SesionManager.getGrupo().getMaxRango().toString());
    }


    private void ubicacionGrupo() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (status == ConnectionResult.SUCCESS) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.view_map_grupo);
            mapFragment.getMapAsync(this);
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, (Activity) getApplicationContext(), 10);
            dialog.show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        radioGrupo();

    }


    private void radioGrupo(){
        if (SesionManager.getGrupo() != null){
            lat = SesionManager.getGrupo().getLat();
            lng = SesionManager.getGrupo().getLng();
            if (lat != 0 && lng != 0){
                agregarMarcadorGupo(lat, lng,"Grupo Vecinal");
                agregarRadioGrupo();
            }
        }
    }

    private void agregarRadioGrupo() {
        double lat = SesionManager.getGrupo().getLat();
        double lng = SesionManager.getGrupo().getLng();
        int radio = SesionManager.getGrupo().getMaxRango();

        LatLng coordenadas = new LatLng(lat, lng);
        CircleOptions circleOptions = new CircleOptions()
                .center(coordenadas)
                .radius(radio)
                .strokeColor(Color.RED);

        if ( circle != null) {
            circle.remove();
        }
        circle =mMap.addCircle(circleOptions);
    }

    private void agregarMarcadorGupo(double lat, double lng, String title) {
        LatLng coordenadas = new LatLng(lat, lng);
        CameraUpdate miUbicacion = CameraUpdateFactory.newLatLngZoom(coordenadas, 16);

        marcador = mMap.addMarker(new MarkerOptions().position(coordenadas)
                .title(title)
        );
        mMap.animateCamera(miUbicacion);
    }


    @Override
    public  void onBackPressed(){
        Intent menu = new Intent(GrupoVecinalViewActivity.this, MenuPrincipalActivity.class);
        startActivity(menu);
        finish();
    }


}
