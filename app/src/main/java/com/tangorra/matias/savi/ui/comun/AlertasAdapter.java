package com.tangorra.matias.savi.ui.comun;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.R;

import java.util.Objects;

public class AlertasAdapter extends ListAdapter<Alerta, AlertasAdapter.Vista> {

    public interface AlTocar {
        void alerta(Alerta alerta);
    }

    private final AlTocar alTocar;

    public AlertasAdapter(AlTocar alTocar) {
        super(DIFERENCIAS);
        this.alTocar = alTocar;
    }

    @NonNull
    @Override
    public Vista onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alerta_resumen, parent, false);
        return new Vista(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull Vista vista, int posicion) {
        final Alerta alerta = getItem(posicion);
        vista.imagen.setImageResource(PresentacionAlerta.imagen(alerta.getAlarma()));
        vista.titulo.setText(alerta.getAlarma());
        vista.detalle.setText(PresentacionAlerta.detalle(vista.itemView.getContext(), alerta));

        PresentacionAlerta.Estado estado = PresentacionAlerta.estado(alerta);
        vista.estado.setText(estado.etiqueta);
        vista.estado.setTextColor(ContextCompat.getColor(vista.itemView.getContext(), estado.color));
        vista.estado.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(vista.itemView.getContext(), estado.contenedor)));

        vista.itemView.setOnClickListener(v -> alTocar.alerta(alerta));
    }

    static final class Vista extends RecyclerView.ViewHolder {
        final ImageView imagen;
        final TextView titulo;
        final TextView detalle;
        final TextView estado;

        Vista(View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imagen);
            titulo = itemView.findViewById(R.id.titulo);
            detalle = itemView.findViewById(R.id.detalle);
            estado = itemView.findViewById(R.id.estado);
        }
    }

    private static final DiffUtil.ItemCallback<Alerta> DIFERENCIAS = new DiffUtil.ItemCallback<Alerta>() {
        @Override
        public boolean areItemsTheSame(@NonNull Alerta a, @NonNull Alerta b) {
            return Objects.equals(a.getId(), b.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Alerta a, @NonNull Alerta b) {
            return Objects.equals(a.getEstado(), b.getEstado())
                    && a.listaRespuestas().size() == b.listaRespuestas().size()
                    && Objects.equals(a.getAlarma(), b.getAlarma());
        }
    };
}
