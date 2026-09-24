package com.tangorra.matias.savi.Activitys;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.Grupo;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Service.ServiciosSesion;
import com.tangorra.matias.savi.Utils.FirebaseUtils;
import com.tangorra.matias.savi.Utils.StringUtils;
import com.tangorra.matias.savi.View.PopUpDomiciliosMenu;
import com.tangorra.matias.savi.View.PopUpInformacion;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.activity.result.ActivityResultLauncher;
import com.tangorra.matias.savi.data.GrupoRepositorio;
import com.tangorra.matias.savi.data.Sesion;
import com.tangorra.matias.savi.ui.grupo.CrearGrupoActivity;
import com.tangorra.matias.savi.ui.grupo.GrupoActivity;
import com.tangorra.matias.savi.ui.acceso.AccesoActivity;
import com.tangorra.matias.savi.ui.alertas.AlertasActivity;
import com.tangorra.matias.savi.ui.alertas.DetalleAlertaActivity;
import com.tangorra.matias.savi.ui.alertas.EmitirAlertaActivity;
import com.tangorra.matias.savi.ui.comun.AlertasAdapter;
import com.tangorra.matias.savi.ui.comun.PresentacionAlerta;
import com.tangorra.matias.savi.ui.familia.FamiliaActivity;
import com.tangorra.matias.savi.ui.inicio.InicioViewModel;
import com.tangorra.matias.savi.ui.notificaciones.NotificacionesActivity;
import com.tangorra.matias.savi.ui.notificaciones.NuevaNotificacionActivity;
import com.tangorra.matias.savi.ui.perfil.PerfilActivity;

import java.util.ArrayList;
import java.util.List;

/** Pantalla de inicio: estado del barrio, acciones principales y ultimas alertas. */
public class MenuPrincipalActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static String usuario = "user";

    private static final int ALERTAS_EN_INICIO = 5;
    public static final int MY_PERMISSIONS_REQUEST = 99;

    private InicioViewModel viewModel;

    private DrawerLayout drawer;
    private MaterialToolbar toolbar;
    private TextView nombreGrupo;
    private TextView estadoBarrio;
    private TextView detalleBarrio;
    private View accionesSinGrupo;
    private View sinAlertas;
    private View btnVerTodas;
    private AlertasAdapter adaptador;

    private List<Alerta> alertas = new ArrayList<>();
    private Grupo grupo;
    private int cantidadVecinos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_principal);

        if (!SesionManager.haySesion()) {
            // Sin sesion (por ejemplo, tras cerrar sesion desde otra pantalla): volver al login
            startActivity(new Intent(this, AccesoActivity.class));
            finish();
            return;
        }
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            Sesion.iniciar(uid);
        }

        drawer = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        nombreGrupo = findViewById(R.id.nombre_grupo);
        estadoBarrio = findViewById(R.id.estado_barrio);
        detalleBarrio = findViewById(R.id.detalle_barrio);
        accionesSinGrupo = findViewById(R.id.acciones_sin_grupo);
        sinAlertas = findViewById(R.id.sin_alertas);
        btnVerTodas = findViewById(R.id.btn_ver_todas);

        View scroll = findViewById(R.id.scroll_inicio);
        ViewCompat.setOnApplyWindowInsetsListener(scroll, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), barras.bottom);
            return insets;
        });

        configurarToolbarYMenu();
        configurarAcciones();

        RecyclerView lista = findViewById(R.id.lista_alertas);
        adaptador = new AlertasAdapter(this::abrirAlerta);
        lista.setAdapter(adaptador);

        viewModel = new ViewModelProvider(this).get(InicioViewModel.class);
        viewModel.usuario().observe(this, this::mostrarUsuario);
        viewModel.grupo().observe(this, g -> {
            grupo = g;
            mostrarBarrio();
        });
        viewModel.vecinos().observe(this, v -> {
            cantidadVecinos = v.size();
            mostrarBarrio();
        });
        viewModel.alertas().observe(this, a -> {
            alertas = a;
            mostrarBarrio();
            mostrarAlertas();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });

        pedirPermisos();
    }

    private void configurarToolbarYMenu() {
        toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
        toolbar.inflateMenu(R.menu.menu_principal);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_exit) {
                cerrarSesion();
                return true;
            }
            return false;
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        cargarImagenPerfil(navigationView.getHeaderView(0).findViewById(R.id.imageView));
    }

    private void configurarAcciones() {
        findViewById(R.id.btn_emitir_alerta).setOnClickListener(v -> {
            if (grupo == null || grupo.getId() == null) {
                Snackbar.make(drawer, StringUtils.notSetGroup, Snackbar.LENGTH_LONG).show();
                return;
            }
            startActivity(new Intent(this, EmitirAlertaActivity.class));
        });
        findViewById(R.id.btn_llamar).setOnClickListener(v -> abrirTelefono());
        findViewById(R.id.mainAlarmasGrupos).setOnClickListener(v -> startActivity(AlertasActivity.grupo(this)));
        findViewById(R.id.mainAlarmasFamilia).setOnClickListener(v -> startActivity(AlertasActivity.familia(this)));
        findViewById(R.id.mainNotificaciones).setOnClickListener(v -> startActivity(new Intent(this, NotificacionesActivity.class)));
        btnVerTodas.setOnClickListener(v -> startActivity(AlertasActivity.grupo(this)));
        findViewById(R.id.btn_unirse_grupo).setOnClickListener(v -> abrirScan());
        findViewById(R.id.btn_crear_grupo).setOnClickListener(v -> startActivity(new Intent(this, CrearGrupoActivity.class)));
    }

    private void mostrarUsuario(Usuario u) {
        if (u == null) {
            return;
        }
        String nombre = u.getNombre() != null ? StringUtils.getTextoFormateado(u.getNombre()).trim() : "";
        toolbar.setTitle(nombre.isEmpty() ? getString(R.string.app_nombre_corto) : getString(R.string.inicio_saludo, nombre));

        View header = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
        ((TextView) header.findViewById(R.id.nombreUsuarioLogeado)).setText(u.getGlosaFormateada().trim());
        ((TextView) header.findViewById(R.id.mailUsuarioLogeado)).setText(u.getMail());
    }

    private void mostrarBarrio() {
        boolean tieneGrupo = grupo != null && grupo.getId() != null;
        accionesSinGrupo.setVisibility(tieneGrupo ? View.GONE : View.VISIBLE);
        if (!tieneGrupo) {
            nombreGrupo.setText(R.string.inicio_sin_grupo);
            estadoBarrio.setVisibility(View.GONE);
            detalleBarrio.setText(R.string.inicio_sin_grupo_detalle);
            return;
        }
        nombreGrupo.setText(grupo.getNombre());
        estadoBarrio.setVisibility(View.VISIBLE);
        int activas = 0;
        for (Alerta a : alertas) {
            if (PresentacionAlerta.estado(a) == PresentacionAlerta.Estado.ACTIVA) {
                activas++;
            }
        }
        estadoBarrio.setText(activas == 0
                ? getString(R.string.inicio_todo_tranquilo)
                : getResources().getQuantityString(R.plurals.inicio_alertas_activas, activas, activas));
        detalleBarrio.setText(getResources().getQuantityString(R.plurals.inicio_vecinos, cantidadVecinos, cantidadVecinos));
    }

    private void mostrarAlertas() {
        List<Alerta> recientes = alertas.size() > ALERTAS_EN_INICIO ? alertas.subList(0, ALERTAS_EN_INICIO) : alertas;
        adaptador.submitList(new ArrayList<>(recientes));
        sinAlertas.setVisibility(alertas.isEmpty() ? View.VISIBLE : View.GONE);
        btnVerTodas.setVisibility(alertas.size() > ALERTAS_EN_INICIO ? View.VISIBLE : View.GONE);
    }

    private void abrirAlerta(Alerta alerta) {
        startActivity(DetalleAlertaActivity.intent(this, alerta.getIdGrupo(), alerta.getId()));
    }

    private void cargarImagenPerfil(final ImageView destino) {
        Usuario u = SesionManager.getUsuario();
        if (u == null || u.getId() == null) {
            return;
        }
        FirebaseUtils.storage().getReference().child("Fotos").child(u.getId()).getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    if (!isDestroyed()) {
                        Glide.with(this).load(uri).centerCrop().into(destino);
                    }
                });
    }

    // Ubicacion (domicilios, mapas) y notificaciones (Android 13+) se piden al entrar a la app.
    private void pedirPermisos() {
        List<String> faltantes = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            faltantes.add(Manifest.permission.ACCESS_FINE_LOCATION);
            faltantes.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            faltantes.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (!faltantes.isEmpty()) {
            ActivityCompat.requestPermissions(this, faltantes.toArray(new String[0]), MY_PERMISSIONS_REQUEST);
        }
    }

    public void abrirTelefono() {
        // ACTION_DIAL abre el marcador sin llamar, no necesita el permiso CALL_PHONE
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:911")));
    }

    private void cerrarSesion() {
        ServiciosSesion.detener(this);
        Sesion.cerrar();
        FirebaseAuth.getInstance().signOut();
        SesionManager.setUsuario(null);
        SesionManager.setGrupo(null);
        startActivity(new Intent(this, AccesoActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.unirseGrupo) {
            abrirScan();
        } else if (id == R.id.agregarDomicilio) {
            startActivity(new Intent(this, PopUpDomiciliosMenu.class));
        } else if (id == R.id.crear_grupo) {
            startActivity(new Intent(this, CrearGrupoActivity.class));
        } else if (id == R.id.mostarGrupo) {
            if (grupo != null && grupo.getId() != null) {
                startActivity(new Intent(this, GrupoActivity.class));
            } else {
                Snackbar.make(drawer, StringUtils.notSetGroup, Snackbar.LENGTH_LONG).show();
            }
        } else if (id == R.id.datosPersonales) {
            startActivity(PerfilActivity.intent(this, false));
        } else if (id == R.id.familiares) {
            startActivity(new Intent(this, FamiliaActivity.class));
        } else if (id == R.id.configurarCuenta) {
            startActivity(new Intent(this, ConfiguracionActivity.class));
        } else if (id == R.id.addNotificacion) {
            startActivity(new Intent(this, NuevaNotificacionActivity.class));
        } else if (id == R.id.informacion) {
            startActivity(new Intent(this, PopUpInformacion.class));
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void abrirScan() {
        escaner.launch(new ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt(StringUtils.openScanGroup)
                .setBeepEnabled(false)
                .setOrientationLocked(false));
    }

    private final ActivityResultLauncher<ScanOptions> escaner = registerForActivityResult(new ScanContract(), resultado -> {
        if (resultado.getContents() == null) {
            return;
        }
        Usuario u = SesionManager.getUsuario();
        GrupoRepositorio.unirse(u.getId(), resultado.getContents())
                .addOnSuccessListener(nuevo -> {
                    // La sesion en vivo actualiza la pantalla; el servicio pasa a escuchar el grupo nuevo
                    ServiciosSesion.iniciar(this);
                    Snackbar.make(drawer, getString(R.string.inicio_unido, nuevo.getNombre()), Snackbar.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> Snackbar.make(drawer,
                        e.getMessage() != null ? e.getMessage() : getString(R.string.inicio_error_unirse), Snackbar.LENGTH_LONG).show());
    });

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            for (int resultado : grantResults) {
                if (resultado != PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(drawer, StringUtils.denegatePermission, Snackbar.LENGTH_LONG).show();
                    break;
                }
            }
        }
    }
}
