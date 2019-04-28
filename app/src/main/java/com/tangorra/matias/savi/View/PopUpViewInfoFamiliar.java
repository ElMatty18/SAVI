package com.tangorra.matias.savi.View;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.StringUtils;

public class PopUpViewInfoFamiliar extends AppCompatActivity  implements OnMapReadyCallback {

    private Context popAlarmas;
    private Usuario usuario;

    private TextView nombre;
    private TextView apellido;
    private TextView dni;
    private TextView fecha_nacimiento;
    private TextView celular;
    private TextView fijo;

    private GoogleMap mMap;
    private Marker marcador;
    private double lat = 0.0;
    private double lng = 0.0;

    private Circle circle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pop_up_view_info_familia);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int heigth = dm.heightPixels;

        getWindow().setLayout((int)(width*.9),(int)(heigth*.7) );

        getSupportActionBar().hide();

        popAlarmas=this;

        usuario = (Usuario) getIntent().getSerializableExtra("usuarioSelecc");

        nombre = findViewById(R.id.txtNombreUsuario);
        nombre.setText(StringUtils.getTextoFormateado(usuario.getNombre()));
        nombre.setFocusable(false);

        apellido = findViewById(R.id.txtApellidoUsuario);
        apellido.setText(StringUtils.getTextoFormateado(usuario.getApellido()));
        apellido.setFocusable(false);

        dni = findViewById(R.id.txtDniUsuario);
        dni.setText(StringUtils.getTextoFormateado(usuario.getDni()));
        dni.setFocusable(false);

        fecha_nacimiento = findViewById(R.id.dateFechaNacimientoUsuario);
        if (usuario != null &&  usuario.getFechaNacimiento() != null){
            int year = usuario.getFechaNacimiento().getYear();
            int month = usuario.getFechaNacimiento().getMonth();
            int dayOfMonth = usuario.getFechaNacimiento().getDate();

            String date = dayOfMonth + "/" + month + "/" + year;

            fecha_nacimiento.setText(date);

        } else {
            fecha_nacimiento.setText("");
        }

        fecha_nacimiento.setFocusable(false);

        celular = findViewById(R.id.txtCelularUsuario);
        celular.setText(StringUtils.getTextoFormateado(usuario.getCelular()));
        celular.setFocusable(false);

        fijo = findViewById(R.id.txtTelefonoUsuario);
        fijo.setText(StringUtils.getTextoFormateado(usuario.getFijo()));
        fijo.setFocusable(false);

        ubicacionActual();
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

        marcadorDomicilioPrincipal();
    }

    private void marcadorDomicilioPrincipal() {
        if (usuario.getPerfil() != null && usuario.getPerfil().getDomicilio() != null ){
            lat = usuario.getPerfil().getDomicilio().getLat();
            lng = usuario.getPerfil().getDomicilio().getLng();
            if (lat != 0 && lng != 0){
                agregarMarcador(lat, lng, "Casa " + usuario.getGlosa());
            }
        }
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


    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }


}
