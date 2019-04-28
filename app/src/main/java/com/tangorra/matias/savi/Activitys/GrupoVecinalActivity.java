package com.tangorra.matias.savi.Activitys;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.FirebaseDatabase;
import com.tangorra.matias.savi.Entidades.Grupo;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

public class GrupoVecinalActivity extends AppCompatActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    private Marker marcador;
    private double lat = 0.0;
    private double lng = 0.0;
    private Circle circle;

    private EditText nombreGrupo;

    private SeekBar seekBarUsuariosMax;
    private int usuariosMax;
    private TextView selec_usuarios;

    private SeekBar seekBarRangoMax;
    private int rangosMax;
    private TextView selec_rango;

    private Button crearGrupo;
    private Button cancelar;

    private DatabaseReference dbGrupoVecinal = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo);
    private DatabaseReference dbUsuarios = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbUsuario);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo_vecinal);

        crearGrupo = findViewById(R.id.crear_grupo_vecinal);
        cancelar = findViewById(R.id.cancel_grupo_vecinal);

        nombreGrupo = findViewById(R.id.txtNombreGrupo);

        selec_usuarios = findViewById(R.id.usuario_max_selec);
        seekBarUsuariosMax = findViewById(R.id.seekBar_usuarios_max);
        usuariosMax = seekBarUsuariosMax.getProgress();

        selec_rango = findViewById(R.id.usuario_max_rango);
        seekBarRangoMax = findViewById(R.id.seekBar_rango_max);
        rangosMax = seekBarRangoMax.getProgress();

        seekBarUsuarios();
        seekBarRango();

        botones();

        ubicacionGrupo();

    }

    private void botones() {
        crearGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (formValido()){
                    //Captura datos de grupo
                    String datos = "Nombre Grupo " + String.valueOf(nombreGrupo.getText()) + "\n"
                            + "UsuariosMax:" + usuariosMax + "\n" +" latitud: " + lat + " longitud " + lng + "\n" + " radio " + rangosMax;

                    Toast toast1 = Toast.makeText(getApplicationContext(),
                            datos, Toast.LENGTH_LONG);

                    toast1.show();


                    String id = dbGrupoVecinal.push().getKey();
                    SesionManager.setGrupo(new Grupo(id, String.valueOf(nombreGrupo.getText()),usuariosMax, lat,lng,rangosMax));

                    SesionManager.getUsuario().setIdGrupo(id);

                    dbGrupoVecinal.child(id).setValue(SesionManager.getGrupo());
                    dbUsuarios.child(SesionManager.getUsuario().getId()).child("idGrupo").setValue(id);


                    Intent navegar = new Intent(GrupoVecinalActivity.this, MenuPrincipalActivity.class);
                    startActivity(navegar);
                    finish();
                }

            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent navegar = new Intent(GrupoVecinalActivity.this, MenuPrincipalActivity.class);
                startActivity(navegar);
                finish();
            }
        });
    }

    private boolean formValido() {
        boolean valido=true;
        if( nombreGrupo.getText().toString().length() == 0 ){
            nombreGrupo.setError( "Campo requerido!" );
            valido=false;
        }
        if( usuariosMax == 0 ){
            valido=false;
        }
        if( lat == 0 ){
            valido=false;
        }
        if( lng == 0 ){
            valido=false;
        }
        if( rangosMax == 0 ){
            valido=false;
        }
        return valido;
    }

    private void ubicacionGrupo() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (status == ConnectionResult.SUCCESS) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, (Activity) getApplicationContext(), 10);
            dialog.show();
        }
    }

    private void seekBarUsuarios() {
        seekBarUsuariosMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                usuariosMax = progress;
                selec_usuarios.setText(usuariosMax+"");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
    }

    private void seekBarRango() {
        seekBarRangoMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                rangosMax = progress;
                selec_rango.setText(rangosMax+"");
                agregarRadio(rangosMax);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
    }

    @Override
    public  void onBackPressed(){
        Intent menu = new Intent(GrupoVecinalActivity.this, MenuPrincipalActivity.class);
        startActivity(menu);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        miUbicacion();
    }

    private void agregarMarcador(double lat, double lng) {
        LatLng coordenadas = new LatLng(lat, lng);
        CameraUpdate miUbicacion = CameraUpdateFactory.newLatLngZoom(coordenadas, 16);
        if (marcador != null) {
            marcador.remove();
        }
        marcador = mMap.addMarker(new MarkerOptions().position(coordenadas)
                .title("Mi ubicacion")
                //.icon(bitmapDescriptorFromVector(this, R.mipmap.ic_launcher))
        );
        mMap.animateCamera(miUbicacion);
    }

    private void actualizarUbicacion(Location location) {
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();

            agregarMarcador(lat, lng);
        }
    }


    private void agregarRadio(int radio) {

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


    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            actualizarUbicacion(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void miUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER );
        actualizarUbicacion(location);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000,0,locationListener);
    }



}
