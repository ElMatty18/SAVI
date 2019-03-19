package com.tangorra.matias.savi.Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tangorra.matias.savi.Entidades.Casa;
import com.tangorra.matias.savi.R;

import java.util.ArrayList;

public class AdaptadorCasas extends BaseAdapter {

    private Context context;
    private ArrayList<Casa> casas;

    @Override
    public int getCount() {
        return casas.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < casas.size()){
            return casas.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Casa item = (Casa) getItem(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.item,null);
        ImageView foto = convertView.findViewById(R.id.imagenCasa);
        TextView titulo = convertView.findViewById(R.id.tituloCasa);
        TextView usuarioCasa = convertView.findViewById(R.id.usuarioCasa);
        Button botonDetalle = convertView.findViewById(R.id.botonDetalleCasa);

        foto.setImageResource(item.getImgFoto());
        titulo.setText(item.getTitulo());
        usuarioCasa.setText(item.getContenido());

        return convertView;
    }

    public AdaptadorCasas(Context context, ArrayList<Casa> casas) {
        this.context = context;
        this.casas = casas;
    }
}
