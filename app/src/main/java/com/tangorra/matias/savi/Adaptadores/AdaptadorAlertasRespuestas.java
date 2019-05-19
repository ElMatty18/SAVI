package com.tangorra.matias.savi.Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.RespuestaAlerta;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.StringUtils;

import java.util.ArrayList;

public class AdaptadorAlertasRespuestas extends BaseAdapter {

    private Context context;
    private ArrayList<RespuestaAlerta> respuestaAlertas;

    @Override
    public int getCount() {
        return respuestaAlertas.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < respuestaAlertas.size()){
            return respuestaAlertas.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RespuestaAlerta item = (RespuestaAlerta) getItem(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.item_respuesta_alerta,null);

        TextView usuarioRespuesta = convertView.findViewById(R.id.viewRespestaAlerta);
        usuarioRespuesta.setText(StringUtils.getTextoFormateado(item.getGlosa()));

        ImageView imagenAlertaRespuestaA = convertView.findViewById(R.id.imagenRespuestaAlerta);

        if (item.getRespuestaAutomatica() != null){
            if (item.getRespuestaAutomatica().equals(StringUtils.config_vacaciones)){
                imagenAlertaRespuestaA.setImageResource(R.drawable.respuesta_viaje);
            }
            if (item.getRespuestaAutomatica().equals(StringUtils.config_casaSola)){
                imagenAlertaRespuestaA.setImageResource(R.drawable.respuesta_lejos);
            }
            if (item.getRespuestaAutomatica().equals(StringUtils.config_visitasCasa)){
                imagenAlertaRespuestaA.setImageResource(R.drawable.respuesta_visitas);
            }
            if (item.getRespuestaAutomatica().equals(StringUtils.config_noMolestar)){
                imagenAlertaRespuestaA.setImageResource(R.drawable.respuesta_no_molestar);
            }
            if (item.getRespuestaAutomatica().equals(StringUtils.config_ignorarTodo)){
                imagenAlertaRespuestaA.setImageResource(R.drawable.respuesta_ignorar);
            }
        } else if (item.getRespuesta() != null){
            if (item.getRespuesta().equals(StringUtils.respuesta_cancela)){
                imagenAlertaRespuestaA.setImageResource(R.drawable.respuesta_ok);
            }
            if (item.getRespuesta().equals(StringUtils.respuesta_confirma)){
                imagenAlertaRespuestaA.setImageResource(R.drawable.respuesta_no_ok);
            }
        }
        return convertView;
    }

    public AdaptadorAlertasRespuestas(Context context, ArrayList<RespuestaAlerta> respuestaAlertas) {
        this.context = context;
        this.respuestaAlertas = respuestaAlertas;
    }


}
