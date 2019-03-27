package com.tangorra.matias.savi.Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.Notificacion;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.DateUtils;
import com.tangorra.matias.savi.Utils.StringUtils;

import java.util.ArrayList;

public class AdaptadorNotificacion extends BaseAdapter {

    private Context context;
    private ArrayList<Notificacion> notificacions;

    @Override
    public int getCount() {
        return notificacions.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < notificacions.size()){
            return notificacions.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Notificacion item = (Notificacion) getItem(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.item_notificacion,null);
        TextView titleNotificacion = convertView.findViewById(R.id.viewNotificacionTitle);
        TextView contenidoNotificacion = convertView.findViewById(R.id.viewNotificacionContenido);
        ImageView imagenNotificacion = convertView.findViewById(R.id.imagenNotificacion);

        titleNotificacion.setText(item.getTitle());
        contenidoNotificacion.setText(item.getContenido());

        imagenNotificacion.setImageResource(R.drawable.menu_notificacion);


        return convertView;
    }

    public AdaptadorNotificacion(Context context, ArrayList<Notificacion> notificacions) {
        this.context = context;
        this.notificacions = notificacions;
    }


}
