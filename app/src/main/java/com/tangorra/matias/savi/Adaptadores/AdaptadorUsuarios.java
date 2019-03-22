package com.tangorra.matias.savi.Adaptadores;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tangorra.matias.savi.Activitys.FamiliaActivity;
import com.tangorra.matias.savi.Entidades.Domicilio;
import com.tangorra.matias.savi.Entidades.Grupo;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.FirebaseUtils;
import com.tangorra.matias.savi.View.PopUpFamiliarAlertas;
import com.tangorra.matias.savi.View.PopUpViewInfoFamiliar;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorUsuarios extends BaseAdapter {

    private Context context;
    private ArrayList<Usuario> personas;

    private StorageReference storageUsuarios;

    private DatabaseReference dbGrupo = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo);
    private ValueEventListener grupoListener = getGrupoListener();

    private DatabaseReference dbUsuarios = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbUsuario);
    private ValueEventListener domicilioListener = getDomicilioListener();
    private ValueEventListener domicilioAlternoListener = getDomicilioAlternoListener();

    private DatabaseReference dbFamilias = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbFamilia);
    private ValueEventListener familiarBorrarListener = getBorrarFamiliarListener();


    private Usuario usuarioSelecc = new Usuario();
    private Grupo grupoSelecc = new Grupo();

    @Override
    public int getCount() {
        return personas.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < personas.size()){
            return personas.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        usuarioSelecc = (Usuario) getItem(position);
        if (context != null){
        convertView = LayoutInflater.from(context).inflate(R.layout.item_persona,null);
        final ImageView foto = convertView.findViewById(R.id.imagenPersona);
        TextView titulo = convertView.findViewById(R.id.nombrePersona);
        TextView usuario = convertView.findViewById(R.id.descripcion);

            storageUsuarios = FirebaseStorage.getInstance().getReference();

            storageUsuarios.child("Fotos").child(usuarioSelecc.getId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context)
                            .load(uri)
                            .fitCenter()
                            .centerCrop()
                            .into(foto);
                }
            });

            titulo.setText(usuarioSelecc.getGlosa());
            usuario.setText(usuarioSelecc.getMail());

            LinearLayout eliminarPersona = convertView.findViewById(R.id.familiarBorrar);
            LinearLayout infoPersona = convertView.findViewById(R.id.familiarInfo);
            LinearLayout alertasPersona = convertView.findViewById(R.id.familiarAlarmas);

            eliminarPersona.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popUpEliminarFamiliar(usuarioSelecc);
                }
            });

            infoPersona.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    usuarioSelecc= personas.get(position);
                    obtenerDatosDomicilioA();
                }
            });

            alertasPersona.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    usuarioSelecc= personas.get(position);
                    ((FamiliaActivity)context).startActivity(new Intent(context, PopUpFamiliarAlertas.class).putExtra("usuarioSelecc", usuarioSelecc));
                }
            });
        }
        return convertView;
    }

    private void obtenerDatosGrupo() {
        dbGrupo.orderByChild("id").equalTo(usuarioSelecc.getIdGrupo()).limitToFirst(1).addValueEventListener(grupoListener);
    }

    private void obtenerDatosDomicilioA() {
        dbUsuarios.orderByChild("id").equalTo(usuarioSelecc.getId()).limitToFirst(1).addValueEventListener(domicilioListener);
    }

    private void obtenerDatosDomicilioB() {
        dbUsuarios.orderByChild("id").equalTo(usuarioSelecc.getId()).limitToFirst(1).addValueEventListener(domicilioAlternoListener);
    }

    public AdaptadorUsuarios(Context context, ArrayList<Usuario> persona) {
        this.context = context;
        this.personas = persona;
    }

    @NonNull
    private ValueEventListener getGrupoListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {
                    grupoSelecc = imageSnapshot.getValue(Grupo.class);
                }
                usuarioSelecc.setGrupo(grupoSelecc);
                accionGrupoCompleto();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }


    @NonNull
    private ValueEventListener getDomicilioListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Domicilio domicilio = new Domicilio();
                for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {
                        /*domicilio.setLng(imageSnapshot.child("lat").getValue(Double.class));
                        domicilio.setLat(imageSnapshot.child("lng").getValue(Double.class));*/
                        domicilio = imageSnapshot.child("perfil").child("domicilio").getValue(Domicilio.class);

                }
                usuarioSelecc.getPerfil().setDomicilio(domicilio);
                accionDomicilioCompleto();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }


    @NonNull
    private ValueEventListener getDomicilioAlternoListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Domicilio domicilio = new Domicilio();
                for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {
                        /*domicilio.setLng(imageSnapshot.child("lat").getValue(Double.class));
                        domicilio.setLat(imageSnapshot.child("lng").getValue(Double.class));*/
                    domicilio = imageSnapshot.child("perfil").child("domicilioAlterno").getValue(Domicilio.class);

                }
                usuarioSelecc.getPerfil().setDomicilioAlterno(domicilio);
                accionDomicilioAlternoCompleto();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void accionGrupoCompleto(){
        ((FamiliaActivity)context).startActivity(new Intent(context, PopUpViewInfoFamiliar.class).putExtra("usuarioSelecc", usuarioSelecc));
    }

    private void accionDomicilioCompleto(){
        obtenerDatosDomicilioB();
    }

    private void accionDomicilioAlternoCompleto(){
        obtenerDatosGrupo();
    }

    public ArrayList<Usuario> getPersonas() {
        return personas;
    }

    public void setPersonas(ArrayList<Usuario> personas) {
        this.personas = personas;
    }



    private void popUpEliminarFamiliar(Usuario usuarioSelecc) {
        final AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this.context);
        dialogo1.setTitle("Eliminar");
        dialogo1.setMessage("¿ Desea eliminar a "+usuarioSelecc.getGlosa()+" del grupo familiar?");
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                aceptarBorrado();
            }
        });
        dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                dialogo1.cancel();
            }
        });

        dialogo1.show();
    }

    private void aceptarBorrado() {
        dbFamilias.child(SesionManager.getUsuario().getIdFamilia()).addValueEventListener(familiarBorrarListener);
    }

    @NonNull
    private ValueEventListener getBorrarFamiliarListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> idsFamilia = new ArrayList<String>();
                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                    String value = imageSnapshot.getValue(String.class);
                    String key = imageSnapshot.getKey();
                    if (value.equals(usuarioSelecc.getId())){
                        dbFamilias.child(SesionManager.getUsuario().getIdFamilia()).child(key).removeValue();
                        SesionManager.getUsuario().setIdFamilia(null);
                        dbFamilias.removeEventListener(familiarBorrarListener);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

}
