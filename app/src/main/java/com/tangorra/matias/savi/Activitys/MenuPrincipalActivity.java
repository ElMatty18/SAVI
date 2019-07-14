package com.tangorra.matias.savi.Activitys;

import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tangorra.matias.savi.Entidades.Grupo;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.FirebaseUtils;
import com.tangorra.matias.savi.Utils.StringUtils;
import com.tangorra.matias.savi.View.PopUpAlertasFamilia;
import com.tangorra.matias.savi.View.PopUpAlertasGrupo;
import com.tangorra.matias.savi.View.PopUpDomiciliosMenu;
import com.tangorra.matias.savi.View.PopUpInformacion;
import com.tangorra.matias.savi.View.PopUpNotificaciones;

public class MenuPrincipalActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    public static String usuario="user";

    private TextView nombreUsuarioLogueado;
    private TextView mailUsuarioLogueado;

    private TextView nombreUsuarioMenuPrincipal;

    public static int INVALID_POSITION = -1;

    private LinearLayout lyAlarmasGrupo;
    private LinearLayout lyAlarmasFamilia;
    private LinearLayout lyNotificaciones;

    private ImageView imgUsuario;

    private StorageReference storageUsuarios;

    private DatabaseReference dbUsuarios = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbUsuario);
    private DatabaseReference dbGrupo = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo);
    private ValueEventListener grupoListener = getGrupoListener();

    private Grupo grupo = new Grupo();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addEventosNotificaciones();

        imgUsuario = findViewById(R.id.imgUsuarioMenuPrincipal);

        cargarImagenPerfil();

        botonesFlotanes();

        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        caracteristicasSAVI();
        addCabecera(navigationView);
    }


    private void cargarImagenPerfil() {

        if (SesionManager.getUsuario() != null && SesionManager.getUsuario().getId() != null){
            storageUsuarios = FirebaseStorage.getInstance().getReference();

            storageUsuarios.child("Fotos").child(SesionManager.getUsuario().getId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(MenuPrincipalActivity.this)
                            .load(uri)
                            .fitCenter()
                            .centerCrop()
                            .into(imgUsuario);
                }
            });
        }

    }

    private void caracteristicasSAVI() {
        final VerticalAdaptar verticalAdaptar = new VerticalAdaptar(this);
        RecyclerView rv = findViewById(R.id.list_muestra);
        initVerRecyclerView(rv, new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false), verticalAdaptar);
    }

    private void botonesFlotanes() {
        FloatingActionButton fab = findViewById(R.id.crearAlertaVecinal);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent alerta = new Intent(MenuPrincipalActivity.this, AlertaActivity.class);
                startActivity(alerta);
            }
        });

        FloatingActionButton autoridades = findViewById(R.id.alertaAutoridades);
        autoridades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirTelefono();
            }
        });
    }

    private void addEventosNotificaciones() {
        lyAlarmasGrupo = findViewById(R.id.mainAlarmasGrupos);
        lyAlarmasFamilia = findViewById(R.id.mainAlarmasFamilia);
        lyNotificaciones = findViewById(R.id.mainNotificaciones);

        lyAlarmasGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuPrincipalActivity.this, PopUpAlertasGrupo.class));
            }
        });

        lyAlarmasFamilia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuPrincipalActivity.this, PopUpAlertasFamilia.class));
            }
        });

        lyNotificaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuPrincipalActivity.this, PopUpNotificaciones.class));
            }
        });
    }

    private void addCabecera(NavigationView navigationView) {
        View header = navigationView.getHeaderView(0);
        nombreUsuarioLogueado = header.findViewById(R.id.nombreUsuarioLogeado);
        mailUsuarioLogueado = header.findViewById(R.id.mailUsuarioLogeado);
        if (SesionManager.getUsuario()!= null){
            nombreUsuarioLogueado.setText(StringUtils.getTextoFormateado(SesionManager.getUsuario().getGlosa()));
            mailUsuarioLogueado.setText(SesionManager.getUsuario().getMail());

            nombreUsuarioMenuPrincipal = findViewById(R.id.textUsuarioMenuPrincipal);
            nombreUsuarioMenuPrincipal.setText(StringUtils.getTextoFormateado(SesionManager.getUsuario().getGlosa()));
        }
    }

    public void abrirTelefono() {
        Intent llamar = new Intent(Intent.ACTION_DIAL);
        llamar.setData(Uri.parse("tel:911"));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            checkPhonePermission();
            return;
        }
        startActivity(llamar);
    }

    private void initVerRecyclerView(final RecyclerView recyclerView, final CarouselLayoutManager layoutManager, final MenuPrincipalActivity.VerticalAdaptar adapter) {
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new CenterScrollListener());

        layoutManager.addOnItemSelectionListener(new CarouselLayoutManager.OnCenterItemSelectionListener() {

            @Override
            public void onCenterItemChanged(final int adapterPosition) {
                if (INVALID_POSITION != adapterPosition) {
                    final int value = adapter.mPosition[adapterPosition];
                    adapter.mPosition[adapterPosition] = (value % 10) + (value / 10 + 1) * 10;
                    adapter.notifyItemChanged(adapterPosition);
                }
            }
        });
    }

    private static final class VerticalAdaptar extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @SuppressWarnings("UnsecureRandomNumberGeneration")
        private final int[] mPosition;
        private int mItemsCount = 10;
        LayoutInflater inflater;

        private final String[] card_title = {
                StringUtils.SAVI,
                StringUtils.RESUME_SAVI,
                StringUtils.RESUME_SAVI,
                StringUtils.ALARMA_SONANDO,
                StringUtils.SOSPECHA_ROBO,
                StringUtils.ACTITUD_SOSPECHOSA,
                StringUtils.DANO_VEHICULO,
                StringUtils.PRINCIPIO_FUEGO,
                StringUtils.AGRESION,
                StringUtils.MAL_ESTACIONADO
        };
        private final String[] card_desc = {
                StringUtils.SAVI_CANALES,
                StringUtils.SAVI_AGENTE,
                StringUtils.SAVI_GRUPOS,
                StringUtils.DESCRIP_ALARMA_SONANDO,
                StringUtils.DESCRIP_SOSPECHA_ROBO,
                StringUtils.DESCRIP_ACTITUD_SOSPECHOSA,
                StringUtils.DESCRIP_DANO_VEHICULO,
                StringUtils.DESCRIP_PRINCIPIO_FUEGO,
                StringUtils.DESCRIP_AGRESION,
                StringUtils.DESCRIP_MAL_ESTACIONADO

        };

        VerticalAdaptar(Context context) {

            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mPosition = new int[10];
            for (int i = 0; 10 > i; ++i) {
                mPosition[i] = i;
            }

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_card, null);
            return new MenuPrincipalActivity.RowVerNewsViewHolder(view);

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ((MenuPrincipalActivity.RowVerNewsViewHolder) holder).cItem1.setText(card_title[position]);
            ((MenuPrincipalActivity.RowVerNewsViewHolder) holder).cItem2.setText(card_desc[position]);
        }

        @Override
        public int getItemCount() {
            return mItemsCount;
        }
    }

    public static class RowVerNewsViewHolder extends RecyclerView.ViewHolder {
        TextView cItem1;
        TextView cItem2;

        public RowVerNewsViewHolder(View itemView) {
            super(itemView);
            cItem1 = itemView.findViewById(R.id.skillTitle);
            cItem2 = itemView.findViewById(R.id.skillDetails);
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            limpiarSesion();
            finish();

        }
        return super.onOptionsItemSelected(item);
    }

    private void limpiarSesion() {
        SesionManager.setUsuario(null);
        SesionManager.setGrupo(null);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.unirseGrupo) {
            abrirScan();

        } else if (id == R.id.agregarDomicilio) {
            Intent menu = new Intent(MenuPrincipalActivity.this, PopUpDomiciliosMenu.class);
            startActivity(menu);

        } else if (id == R.id.crear_grupo) {
            Intent menu = new Intent(MenuPrincipalActivity.this, GrupoVecinalActivity.class);
            startActivity(menu);
            finish();

        } else if (id == R.id.mostarGrupo) {
            if (SesionManager.getGrupo() != null && SesionManager.getGrupo().getId() != null) {
                Intent menu = new Intent(MenuPrincipalActivity.this, GrupoVecinalViewActivity.class);
                startActivity(menu);
                finish();
            } else {
                Toast.makeText(this, StringUtils.notSetGroup, Toast.LENGTH_LONG).show();
            }

        }
        else if (id == R.id.datosPersonales) {
            Intent menu = new Intent(MenuPrincipalActivity.this, PerfilActivity.class);
            startActivity(menu);
            finish();

        } else if (id == R.id.familiares) {
            Intent menu = new Intent(MenuPrincipalActivity.this, FamiliaActivity.class);
            startActivity(menu);
            finish();

        } else if (id == R.id.configurarCuenta) {
            Intent menu = new Intent(MenuPrincipalActivity.this, ConfiguracionActivity.class);
            startActivity(menu);

        } else if (id == R.id.addNotificacion) {
            Intent menu = new Intent(MenuPrincipalActivity.this, NotificacionActivity.class);
            startActivity(menu);
            finish();
        } else if (id == R.id.informacion) {
            Intent menu = new Intent(MenuPrincipalActivity.this, PopUpInformacion.class);
            startActivity(menu);
        }



        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void abrirScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt(StringUtils.openScanGroup);
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (result != null){
            if (result.getContents() == null){
                Toast.makeText(this, StringUtils.cancelScan, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                agregarGrupo(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void agregarGrupo(String contents) {
        SesionManager.getUsuario().setIdGrupo(contents);
        persistirUsuario(SesionManager.getUsuario());
        recuperarDatosGrupoUsuario(SesionManager.getUsuario().getIdGrupo());
    }

    private void persistirUsuario(Usuario u) {
        dbUsuarios.child(u.getId()).setValue(u);
    }

    private void recuperarDatosGrupoUsuario(String idGrupo) {
        dbGrupo.orderByChild("id").equalTo(idGrupo).limitToFirst(1).addValueEventListener(grupoListener);
    }


    @NonNull
    private ValueEventListener getGrupoListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {
                    grupo = imageSnapshot.getValue(Grupo.class);
                }
                SesionManager.setGrupo(grupo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    @NonNull
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        break;
                    }
                    abrirTelefono();
                } else {
                    Toast.makeText(this, StringUtils.denegatePermission, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


}
