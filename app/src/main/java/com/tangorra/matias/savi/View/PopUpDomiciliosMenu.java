package com.tangorra.matias.savi.View;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

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
import com.google.firebase.database.DatabaseReference;
import com.tangorra.matias.savi.Entidades.Domicilio;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.R;

public class PopUpDomiciliosMenu extends AppCompatActivity implements OnMapReadyCallback {

    private Context popDomicilios;

    private GoogleMap mMap;
    private Marker marcador;
    private double lat = 0.0;
    private double lng = 0.0;

    private Button domicilioPrincipal;
    private Button domicilioSecundario;

    private Circle circle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pop_up_domicilios_menu);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int heigth = dm.heightPixels;

        getWindow().setLayout((int )(width*.9),(int )(heigth*.7) );

        getSupportActionBar().hide();

        popDomicilios =this;

        addViewDomicilios();

        ubicacionActual();


    }

    private void addViewDomicilios() {
        domicilioPrincipal = findViewById(R.id.viewDomicilioPrincipal);
        domicilioSecundario = findViewById(R.id.viewDomicilioSecundario);

        domicilioPrincipal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SesionManager.getUsuario().getPerfil() != null && SesionManager.getUsuario().getPerfil().getDomicilio() != null ){
                    lat = SesionManager.getUsuario().getPerfil().getDomicilio().getLat();
                    lng = SesionManager.getUsuario().getPerfil().getDomicilio().getLng();
                    if (lat != 0 && lng != 0){
                        agregarMarcador(lat, lng, "Casa");
                    }
                }
            }
        });

        domicilioSecundario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SesionManager.getUsuario().getPerfil() != null && SesionManager.getUsuario().getPerfil().getDomicilioAlterno() != null ){
                    lat = SesionManager.getUsuario().getPerfil().getDomicilioAlterno().getLat();
                    lng = SesionManager.getUsuario().getPerfil().getDomicilioAlterno().getLng();
                    if (lat != 0 && lng != 0){
                        agregarMarcador(lat, lng,"Trabajo");
                    }
                }
            }
        });

    }

    private void radioGrupo(){
        if ((SesionManager.getGrupo() != null) && (SesionManager.getGrupo().getLat() != null) && (SesionManager.getGrupo().getLng() != null) ){
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


    private void ubicacionActual() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (status == ConnectionResult.SUCCESS) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapDomicilios);
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

        domicilioPrincipal.performClick();
        radioGrupo();

    }


    private void agregarMarcador(double lat, double lng, String title) {
        LatLng coordenadas = new LatLng(lat, lng);
        CameraUpdate miUbicacion = CameraUpdateFactory.newLatLngZoom(coordenadas, 16);
        if (marcador != null) {
            marcador.remove();
        }
        marcador = mMap.addMarker(new MarkerOptions().position(coordenadas)
                        .title(title)
        );
        mMap.animateCamera(miUbicacion);
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
    protected void onStop() {
        super.onStop();
        finish();
    }

}
