package com.tangorra.matias.savi.Activitys;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hitomi.cmlibrary.CircleMenu;
import com.hitomi.cmlibrary.OnMenuSelectedListener;
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.RespuestaAlerta;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.DateUtils;
import com.tangorra.matias.savi.Utils.FirebaseUtils;
import com.tangorra.matias.savi.Utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;

public class RespuestaAlertaActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static Alerta alerta = new Alerta();

    private TextView respuestaAlertaTipo;

    private ImageView respuestaAlertaTipoImagen;
    private ImageView respuestaAlertaDirigidaImagen;

    private TextView respuestaAlertaID;
    private TextView respuestaAlertaUsuarioCreador;
    private TextView respuestaAlertaFechaCreacion;
    private TextView respuestaAlertaFor;
    private TextView respuestaAlertaForTitle;
    private TextView respuestaEstadoAlerta;

    private SeekBar seekBar_nivel_alerta_respuesta;

    private LinearLayout estadoAlertarAutoridades;

    private GoogleMap mMap;
    private Marker marcador;
    private double lat = 0.0;
    private double lng = 0.0;

    private Button respuestaAlertabtnConfirmar;
    private Button respuestaAlertaCancelar;

    private LocationListener locationListener = getLocationListener();

    private StorageReference storageUsuarios = FirebaseStorage.getInstance().getReference();

    private DatabaseReference dbGrupoVecinal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respuesta_alerta);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int heigth = dm.heightPixels;
        getWindow().setLayout((int )(width*.9),(int )(heigth*.8) );
        getSupportActionBar().hide();

        Intent i = getIntent();
        alerta = (Alerta)i.getSerializableExtra(StringUtils.parametroAlerta);

        asociarElementos();
        populatarElemento(alerta);

    }

    private void populatarElemento(Alerta source) {
        respuestaAlertaTipo.setText(source.getAlarma());
        respuestaAlertaID.setText(source.getId());
        respuestaAlertaUsuarioCreador.setText(StringUtils.getTextoFormateado(source.getCreadoBy()));
        respuestaAlertaFechaCreacion.setText(DateUtils.sdf2.format(source.getCreacion()));
        respuestaAlertaFor.setText(source.getDirigida());
        respuestaAlertaForTitle.setText(source.getDirigida());
        respuestaEstadoAlerta.setText(source.getEstado());

    }

    private void asociarElementos() {
        respuestaAlertaTipo = findViewById(R.id.respuestaAlertaTipo);

        respuestaAlertaTipoImagen = findViewById(R.id.respuestaAlertaTipoImagen);
        cargarImagen();

        respuestaAlertaDirigidaImagen = findViewById(R.id.respuestaAlertaDirigidaImagen);
        respuestaAlertaID = findViewById(R.id.respuestaAlertaID);
        respuestaAlertaUsuarioCreador = findViewById(R.id.respuestaAlertaUsuarioCreador);
        respuestaAlertaFechaCreacion = findViewById(R.id.respuestaAlertaFechaCreacion);
        respuestaAlertaFor = findViewById(R.id.respuestaAlertaFor);
        seekBar_nivel_alerta_respuesta = findViewById(R.id.seekBar_nivel_alerta_respuesta);

        respuestaAlertaForTitle = findViewById(R.id.respuestaAlertaForTitle);

        respuestaEstadoAlerta = findViewById(R.id.respuestaEstadoAlerta);

        estadoAlertarAutoridades = findViewById(R.id.estadoAlertarAutoridades);
        estadoAlertarAutoridades.setVisibility(LinearLayout.GONE);

        ubicacionAlerta();

        respuestaAlertabtnConfirmar = findViewById(R.id.respuestaAlertabtnConfirmar);
        respuestaAlertabtnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                responderAlerta(StringUtils.respuesta_confirma);
            }
        });

        respuestaAlertaCancelar = findViewById(R.id.respuestaAlertaCancelar);
        respuestaAlertaCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                responderAlerta(StringUtils.respuesta_cancela);
            }
        });

        int nivelAlerta = alerta.obtenerNivelAlerta();
        seekBar_nivel_alerta_respuesta.setProgress(nivelAlerta);
        if (nivelAlerta >= 50 || alerta.getEstado().equals(StringUtils.alertaConfirmadaDirigida)){
            crearAlertarAutoridades();
            estadoAlertarAutoridades.setVisibility(LinearLayout.VISIBLE);

        }

        seekBar_nivel_alerta_respuesta.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });


    }

    private void cargarImagen(){
        storageUsuarios.child("Fotos").child(SesionManager.getUsuario().getId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(RespuestaAlertaActivity.this)
                        .load(uri)
                        .fitCenter()
                        .centerCrop()
                        .into(respuestaAlertaDirigidaImagen);
            }
        });
    }


    private void ubicacionAlerta() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (status == ConnectionResult.SUCCESS) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.respuestaAlertaMapDomicilio);
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

        if (getBaseContext() != null){
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {

                }
            });
        }
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

    private void miUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER );
        actualizarUbicacion(location);

        if (SesionManager.getUsuario().getPerfil() != null && SesionManager.getUsuario().getPerfil().getDomicilio() != null ){
            lat = SesionManager.getUsuario().getPerfil().getDomicilio().getLat();
            lng = SesionManager.getUsuario().getPerfil().getDomicilio().getLng();
            if (lat != 0 && lng != 0){
                agregarMarcador(lat, lng);
            }
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000,0,locationListener);
    }

    private void actualizarUbicacion(Location location) {

    }


    @NonNull
    private LocationListener getLocationListener() {
        return new LocationListener() {
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
    }

    @Override
    public  void onBackPressed(){
        Intent menu = new Intent(RespuestaAlertaActivity.this, MenuPrincipalActivity.class);
        startActivity(menu);
        finish();
    }


    private void responderAlerta(String respuesta){
        FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo).child(SesionManager.getGrupo().getId()).child("alertas").child(alerta.getId()).child("estado").setValue(respuesta);

        RespuestaAlerta respuestaAlerta = new RespuestaAlerta();
        respuestaAlerta.setIdUsuario(SesionManager.getUsuario().getId());
        respuestaAlerta.setNombreUsuario(SesionManager.getUsuario().getNombre());
        respuestaAlerta.setApellidoUsuario(SesionManager.getUsuario().getApellido());
        respuestaAlerta.setCreacion(new Date());
        respuestaAlerta.setIdAlarma(alerta.getId());
        respuestaAlerta.setRespuesta(respuesta);

        if (alerta.getRespuestas() == null){
            alerta.setRespuestas(new ArrayList<RespuestaAlerta>());
        }
        alerta.getRespuestas().add(respuestaAlerta);

        if (alerta.getDirigidaId().equals(SesionManager.getUsuario().getId())) {
            if (respuesta.equals(StringUtils.respuesta_cancela)){
                alerta.setEstado(StringUtils.alertaDesactivada);
            } else if (respuesta.equals(StringUtils.respuesta_confirma)){
                alerta.setEstado(StringUtils.alertaConfirmadaDirigida);
            }
        }

        FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo).child(SesionManager.getGrupo().getId()).child("alertas").child(alerta.getId()).setValue(alerta);
    }


    private void crearAlertarAutoridades(){
        final String arrayName[] = {"Policia", "Emergencia Medica", "Bomberos", "Violencia de genero"};
        CircleMenu circleMenu = findViewById(R.id.seleccRespuesta);
        circleMenu.setMainMenu(Color.parseColor("#FFFF99"), R.drawable.icon_menu, R.drawable.respuesta_elegir)
                .addSubMenu(Color.parseColor("#99FF99"), R.drawable.autoridades_policia)
                .addSubMenu(Color.parseColor("#FFB19A"), R.drawable.autoridades_medica)
                .addSubMenu(Color.parseColor("#9999FF"), R.drawable.autoridades_bomberos)
                .addSubMenu(Color.parseColor("#9ACDFF"), R.drawable.autoridades_violencia_genero)
                .setOnMenuSelectedListener(new OnMenuSelectedListener() {
                    @Override
                    public void onMenuSelected(int i) {
                        Toast.makeText(RespuestaAlertaActivity.this, "seleccionaste" + arrayName[i],Toast.LENGTH_SHORT).show();
                        abrirTelefono("911");
                    }
                });
    }

    public void abrirTelefono(String tel) {
        Intent llamar = new Intent(Intent.ACTION_DIAL);
        llamar.setData(Uri.parse("tel:"+tel));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            checkPhonePermission();
            return;
        }
        startActivity(llamar);
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public void checkPhonePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CALL_PHONE)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        } else {

        }
    }


}

