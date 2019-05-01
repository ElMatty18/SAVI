package com.tangorra.matias.savi.Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.DateUtils;
import com.tangorra.matias.savi.Utils.StringUtils;

import java.util.ArrayList;

public class AdaptadorAlertas extends BaseAdapter {

    private Context context;
    private ArrayList<Alerta> alertas;

    @Override
    public int getCount() {
        return alertas.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < alertas.size()){
            return alertas.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Alerta item = (Alerta) getItem(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.item_alarma,null);
        TextView tipoAlerta = convertView.findViewById(R.id.viewTipoAlarma);
        TextView usuario = convertView.findViewById(R.id.viewUsuarioDirigida);
        TextView fecha = convertView.findViewById(R.id.viewFechaAlarma);
        ImageView imagenAlerta = convertView.findViewById(R.id.imagenAlarma);

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

    public AdaptadorAlertas(Context context, ArrayList<Alerta> alertas) {
        this.context = context;
        this.alertas = alertas;
    }


}
