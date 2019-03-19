package com.tangorra.matias.savi.Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tangorra.matias.savi.Entidades.Persona;
import com.tangorra.matias.savi.R;

import java.util.ArrayList;

public class AdaptadorPersonas extends BaseAdapter {

    private Context context;
    private ArrayList<Persona> personas;

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
        Persona item = (Persona) getItem(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.item_persona,null);
        ImageView foto = convertView.findViewById(R.id.imagenPersona);
        TextView titulo = convertView.findViewById(R.id.nombrePersona);
        TextView usuario = convertView.findViewById(R.id.descripcion);

        foto.setImageResource(item.getImgFoto());
        titulo.setText(item.getTitulo());
        usuario.setText(item.getContenido());

        LinearLayout eliminarPersona = convertView.findViewById(R.id.familiarBorrar);
        LinearLayout infoPersona = convertView.findViewById(R.id.familiarInfo);
        LinearLayout alertasPersona = convertView.findViewById(R.id.familiarAlarmas);

        eliminarPersona.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Eliminar persona, pos: "+ position,Toast.LENGTH_LONG).show();
            }
        });

        infoPersona.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Info persona, pos: "+ position,Toast.LENGTH_LONG).show();
            }
        });

        alertasPersona.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Alertas persona, pos: "+ position,Toast.LENGTH_LONG).show();
            }
        });

        return convertView;
    }

    public AdaptadorPersonas(Context context, ArrayList<Persona> persona) {
        this.context = context;
        this.personas = persona;
    }
}
