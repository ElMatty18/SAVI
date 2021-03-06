package com.tangorra.matias.savi.Adaptadores;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tangorra.matias.savi.Activitys.FamiliaActivity;
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.Domicilio;
import com.tangorra.matias.savi.Entidades.Grupo;
import com.tangorra.matias.savi.Entidades.RespuestaAlerta;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.DateUtils;
import com.tangorra.matias.savi.Utils.FirebaseUtils;
import com.tangorra.matias.savi.Utils.StringUtils;
import com.tangorra.matias.savi.View.PopUpViewInfoFamiliar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AdaptadorCombinado extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<Usuario> personas;
    private ArrayList<Alerta> listAlertas;
    private Map<Alerta, ArrayList<Alerta>> mapChild;

    private DatabaseReference dbGrupo = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo);
    private ValueEventListener grupoListener = getGrupoListener();

    private DatabaseReference dbUsuarios = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbUsuario);
    private ValueEventListener integrantesListener = getIntegrantesListener();


    private ValueEventListener domicilioListener = getDomicilioListener();
    private ValueEventListener domicilioAlternoListener = getDomicilioAlternoListener();

    private DatabaseReference dbFamilias = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbFamilia);
    private ValueEventListener familiarBorrarListener = getBorrarFamiliarListener();

    private Usuario usuarioSelecc = new Usuario();
    private Grupo grupoSelecc = new Grupo();

    private ArrayList<Usuario> listIntegrantes = new ArrayList<>();
    private ArrayList<RespuestaAlerta> respuestasAlertas = new ArrayList<>();


    public AdaptadorCombinado(Context contex, ArrayList<Alerta> alertas, Map<Alerta, ArrayList<Alerta>> mapChild) {
        this.context = contex;
        this.listAlertas = alertas;
        this.mapChild = mapChild;
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

    public AdaptadorCombinado(Context context, ArrayList<Usuario> persona) {
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

    @Override
    public int getGroupCount() {
        return listAlertas.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mapChild.get(listAlertas.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listAlertas.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return  mapChild.get(listAlertas.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (context != null){
            Alerta item = (Alerta) getGroup(groupPosition);

            convertView = LayoutInflater.from(context).inflate(R.layout.item_alarma_expandible, null);
            TextView titulo = convertView.findViewById(R.id.viewTipoAlarmaExp);

            TextView tipoAlerta = convertView.findViewById(R.id.viewTipoAlarmaExp);
            TextView usuario = convertView.findViewById(R.id.viewUsuarioDirigidaExp);
            TextView fecha = convertView.findViewById(R.id.viewFechaAlarmaExp);
            ImageView imagenAlerta = convertView.findViewById(R.id.imagenAlarmaExp);

            tipoAlerta.setText(item.getAlarma());
            usuario.setText(StringUtils.getTextoFormateado(item.getDirigida()));
            fecha.setText(DateUtils.sdf2.format(item.getCreacion()));

            if (tipoAlerta.getText().equals(StringUtils.ALARMA_SONANDO)){
                imagenAlerta.setImageResource(R.drawable.alarma1);
            } else if (tipoAlerta.getText().equals(StringUtils.SOSPECHA_ROBO)){
                imagenAlerta.setImageResource(R.drawable.alarma2);
            } else if (tipoAlerta.getText().equals(StringUtils.ACTITUD_SOSPECHOSA)){
                imagenAlerta.setImageResource(R.drawable.alarma3);
            } else if (tipoAlerta.getText().equals(StringUtils.DANO_VEHICULO)){
                imagenAlerta.setImageResource(R.drawable.alarma4);
            } else if (tipoAlerta.getText().equals(StringUtils.PRINCIPIO_FUEGO)){
                imagenAlerta.setImageResource(R.drawable.alarma5);
            } else if (tipoAlerta.getText().equals(StringUtils.AGRESION)){
                imagenAlerta.setImageResource(R.drawable.alarma6);
            } else if (tipoAlerta.getText().equals(StringUtils.MAL_ESTACIONADO)){
                imagenAlerta.setImageResource(R.drawable.alarma7);
            }

            return convertView;

        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Alerta itemAlerta = (Alerta) getChild(groupPosition,childPosition);
        convertView = LayoutInflater.from(context).inflate(R.layout.item_alarma_expandible_child, null);

        TextView alertaID = convertView.findViewById(R.id.alertaIdDetailalertaIdDetail);
        alertaID.setText(itemAlerta.getId());

        TextView alertaCreador = convertView.findViewById(R.id.alertaDetailUsuarioCreador);
        alertaCreador.setText(StringUtils.getTextoFormateado(itemAlerta.getCreadoBy()));

        TextView alertaCreacion = convertView.findViewById(R.id.alertaDetailFechaCreacion);
        alertaCreacion.setText(DateUtils.sdf2.format(itemAlerta.getCreacion()));

        TextView alertaDirigida = convertView.findViewById(R.id.alertaDetailFor);
        alertaDirigida.setText(StringUtils.getTextoFormateado(itemAlerta.getDirigida()));

        TextView alertaEstado = convertView.findViewById(R.id.alertaDetailRespuestaEstado);
        alertaEstado.setText(itemAlerta.getEstado());

        SeekBar nivelAlerta = convertView.findViewById(R.id.seekBar_nivel_alerta);
        nivelAlerta.setProgress(itemAlerta.obtenerNivelAlerta());

        nivelAlerta.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        ListView listViewRespuestas =  convertView.findViewById(R.id.listViewRespuestas);
        respuestasAlertas.clear();
        if (itemAlerta.getRespuestas() != null){
            for (RespuestaAlerta itemRespuesta : itemAlerta.getRespuestas() ) {
                respuestasAlertas.add(itemRespuesta);
            }
        }
        AdaptadorAlertasRespuestas adaptadorAlertasRespuestas = new AdaptadorAlertasRespuestas(context,respuestasAlertas);
        listViewRespuestas.setAdapter(adaptadorAlertasRespuestas);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
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
                    respuestasAlertas.add(new RespuestaAlerta(usuario.getId(),usuario.getNombre(),usuario.getApellido(),new Date(),"xxx","xxx","xxx"));
                }
                getVistaRespuestasUsuarios();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void getVistaRespuestasUsuarios() {

    }


}
