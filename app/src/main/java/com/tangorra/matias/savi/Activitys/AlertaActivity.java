package com.tangorra.matias.savi.Activitys;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
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
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.FirebaseUtils;
import com.tangorra.matias.savi.Utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class AlertaActivity extends AppCompatActivity {

    private Context contex;

    public static int INVALID_POSITION = -1;

    private Button emitirAlerta;
    private Button cancelar;

    private Switch onOffSwitch;
    private Boolean allUsers = Boolean.FALSE;

    private String casaSeleccion;
    private String alarmaSeleccion;

    private DatabaseReference dbGrupoVecinal = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo).child(SesionManager.getGrupo().getId());
    private DatabaseReference dbUsuarios = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbUsuario);
    private ValueEventListener integrantesListener = getIntegrantesListener();

    private ArrayList<Usuario> listIntegrantes = new ArrayList<Usuario>();

    private StorageReference storageUsuarios = FirebaseStorage.getInstance().getReference();
    private ImageView[] usuariosImages;
    private String[] usuariosNames;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alerta);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int heigth = dm.heightPixels;

        getWindow().setLayout((int )(width*.9),(int )(heigth*.8) );

        getSupportActionBar().hide();


        this.contex = this;

        recuperarIntegrantesGrupo();

        addBotones();

    }


    private void recuperarIntegrantesGrupo(){
        if (SesionManager.getUsuario().getIdGrupo() != null){
            dbUsuarios.orderByChild("idGrupo").equalTo(SesionManager.getUsuario().getIdGrupo()).addValueEventListener(integrantesListener);
        }
    }

    @NonNull
    private ValueEventListener getIntegrantesListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                    Usuario usuario = imageSnapshot.getValue(Usuario.class);
                    listIntegrantes.add(usuario);
                }
                SesionManager.getGrupo().setIntegrantes(listIntegrantes);

                usuariosImages = new ImageView[listIntegrantes.size()];
                usuariosNames = new String[listIntegrantes.size()];

                getVistaIntegrantes();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void getVistaIntegrantes(){

        final AlarmaAdaptar alarmaAdaptar= new AlarmaAdaptar(this);
        RecyclerView alarmas = (RecyclerView) findViewById(R.id.list_alarma);
        initRecyclerViewAlarma(alarmas, new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false), alarmaAdaptar);

        cargarTitlesCasas();
        cargarImagenCasas(this);

        final CasasAdaptar adapter = new CasasAdaptar(this);
        RecyclerView casas = (RecyclerView) findViewById(R.id.list_horizontal);
        initRecyclerView(casas, new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false), adapter);
    }


    private void cargarImagenCasas(Context context) {
        for (int i = 0; i < SesionManager.getGrupo().getIntegrantes().size(); i++){
            final ImageView imgUsuario = new ImageView(context);
            final int pos = i;
            storageUsuarios.child("Fotos").child(SesionManager.getGrupo().getIntegrantes().get(i).getId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(AlertaActivity.this)
                            .load(uri)
                            .fitCenter()
                            .centerCrop()
                            .into(imgUsuario);

                    if (imgUsuario != null){
                        usuariosImages[pos] = imgUsuario;
                    }
                }
            });
        }
    }

    private void cargarTitlesCasas() {
        for (int i = 0; i < SesionManager.getGrupo().getIntegrantes().size(); i++){
            usuariosNames[i] = StringUtils.getTextoFormateado(SesionManager.getGrupo().getIntegrantes().get(i).getGlosa());
        }
    }

    private void addBotones() {
        emitirAlerta = findViewById(R.id.btnEmitirAlerta);

        emitirAlerta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!allUsers) {
                    persistir(casaSeleccion, alarmaSeleccion);
                }else {
                    persistir(StringUtils.ALL_USERS, alarmaSeleccion);
                }
                Vibrator vibrar = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrar.vibrate(1000);
                Intent alerta = new Intent(AlertaActivity.this, MenuPrincipalActivity.class);
                startActivity(alerta);
                Toast alertar =
                        Toast.makeText(getApplicationContext(),
                                StringUtils.alert, Toast.LENGTH_SHORT);

                alertar.show();
            }
        });


        cancelar = findViewById(R.id.btnCancelarAlerta);
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent volverMenu = new Intent(AlertaActivity.this, MenuPrincipalActivity.class);
                startActivity(volverMenu);
            }
        });

        onOffSwitch = (Switch) findViewById(R.id.switch_todos);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                allUsers = isChecked;
            }

        });
    }


    private void persistir(String usuarioDirigida, String descripcion){
       String id = dbGrupoVecinal.push().getKey();
       Alerta alerta = new Alerta(id, usuarioDirigida, descripcion, new Date(), SesionManager.getUsuario().getGlosa());

       alerta.setEstado("activa");

       dbGrupoVecinal.child("alertas").child(id).setValue(alerta);
    }

    @Override
    public  void onBackPressed(){
        Intent menu = new Intent(AlertaActivity.this, MenuPrincipalActivity.class);
        startActivity(menu);
        finish();
    }

    private void initRecyclerView(final RecyclerView recyclerView, final CarouselLayoutManager layoutManager, final CasasAdaptar adapter) {
        // enable zoom effect. this line can be customized
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());

        recyclerView.setLayoutManager(layoutManager);
        // we expect only fixed sized item for now
        recyclerView.setHasFixedSize(true);
        // sample adapter with random data
        recyclerView.setAdapter(adapter);
        // enable center post scrolling
        recyclerView.addOnScrollListener(new CenterScrollListener());

        layoutManager.addOnItemSelectionListener(new CarouselLayoutManager.OnCenterItemSelectionListener() {

            @Override
            public void onCenterItemChanged(final int adapterPosition) {
                if (INVALID_POSITION != adapterPosition) {
                    final int value = adapter.mPosition[adapterPosition];
                    adapter.mPosition[adapterPosition] = (value % 10) + (value / 10 + 1) * 10;
                    //cambiar seleccion de las casa
                    casaSeleccion=adapter.getValorCasa(adapterPosition);
                    adapter.notifyItemChanged(adapterPosition);
                }
            }
        });
    }

    private void initRecyclerViewAlarma(final RecyclerView recyclerView, final CarouselLayoutManager layoutManager, final AlarmaAdaptar adapter) {
        // enable zoom effect. this line can be customized
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());

        recyclerView.setLayoutManager(layoutManager);
        // we expect only fixed sized item for now
        recyclerView.setHasFixedSize(true);
        // sample adapter with random data
        recyclerView.setAdapter(adapter);
        // enable center post scrolling
        recyclerView.addOnScrollListener(new CenterScrollListener());

        layoutManager.addOnItemSelectionListener(new CarouselLayoutManager.OnCenterItemSelectionListener() {

            @Override
            public void onCenterItemChanged(final int adapterPosition) {
                if (INVALID_POSITION != adapterPosition) {
                    final int value = adapter.mPosition[adapterPosition];
                    adapter.mPosition[adapterPosition] = (value % 10) + (value / 10 + 1) * 10;
                    alarmaSeleccion=adapter.getValorAlarma(adapterPosition);
                    adapter.notifyItemChanged(adapterPosition);
                }
            }
        });
    }


    public final class CasasAdaptar extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @SuppressWarnings("UnsecureRandomNumberGeneration")
        private final Random mRandom = new Random();
        private final int[] mColors;
        private final int[] mPosition;

        private int mItemsCount = usuariosNames.length;
        LayoutInflater inflater;

        CasasAdaptar(Context context) {
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mColors = new int[usuariosNames.length];
            mPosition = new int[usuariosNames.length];

            for (int i = 0; usuariosNames.length > i; ++i) {
                mColors[i] = Color.argb(255, mRandom.nextInt(256), mRandom.nextInt(256), mRandom.nextInt(256));
                mPosition[i] = i;
            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = inflater.inflate( R.layout.item_view, null) ;
                RecyclerView.ViewHolder holder = new RowNewsViewHolder(view);
                return holder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final ImageView imagen = null;
            if (usuariosNames.length > position){
                ((RowNewsViewHolder) holder).cItem1.setText(usuariosNames[position]);

                storageUsuarios.child("Fotos").child(SesionManager.getGrupo().getIntegrantes().get(position).getId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(AlertaActivity.this)
                                .load(uri)
                                .fitCenter()
                                .centerCrop()
                                .into(((RowNewsViewHolder) holder).pp2);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mItemsCount;
        }

        public String getValorCasa(int posicion){
            return usuariosNames[posicion];
        }
    }

    public static class RowNewsViewHolder extends RecyclerView.ViewHolder {
        TextView cItem1;
        CircleImageView pp;
        ImageView pp2;

        public RowNewsViewHolder(View itemView) {
            super(itemView);
            cItem1 = itemView.findViewById(R.id.c_item_1);
            pp = itemView.findViewById(R.id.profilePicture);
            pp2 = itemView.findViewById(R.id.profilePictureImage);
        }
    }

    private static final class AlarmaAdaptar extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @SuppressWarnings("UnsecureRandomNumberGeneration")
        private final Random mRandom = new Random();
        private final int[] mColors;
        private final int[] mPosition;
        private Context context;
        private final int[] image={
                R.drawable.alarma1,
                R.drawable.alarma2,
                R.drawable.alarma3,
                R.drawable.alarma4,
                R.drawable.alarma5,
                R.drawable.alarma6,
                R.drawable.alarma7
        };
        private final String[] title={
                StringUtils.ALARMA_SONANDO,
                StringUtils.SOSPECHA_ROBO,
                StringUtils.ACTITUD_SOSPECHOSA,
                StringUtils.DANO_VEHICULO,
                StringUtils.PRINCIPIO_FUEGO,
                StringUtils.AGRESION,
                StringUtils.MAL_ESTACIONADO,
        };


        private int mItemsCount = 7;
        LayoutInflater inflater;

        AlarmaAdaptar(Context context) {
            this.context=context;

            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mColors = new int[mItemsCount];
            mPosition = new int[mItemsCount];

            for (int i = 0; mItemsCount > i; ++i) {
                mColors[i] = Color.argb(255, mRandom.nextInt(256), mRandom.nextInt(256), mRandom.nextInt(256));
                mPosition[i] = i;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate( R.layout.item_view, null) ;
            RecyclerView.ViewHolder holder = new RowNewsViewHolder(view);
            return holder;

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (position < mItemsCount){
                ((RowNewsViewHolder) holder).cItem1.setText(title[position]);
                ((RowNewsViewHolder) holder).pp.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), image[position], null));
            }
        }

        @Override
        public int getItemCount() {
            return mItemsCount;
        }

        public String getValorAlarma(int posicion){
            if (title.length > posicion){
                return title[posicion];
            }
            return "";
        }
    }


}