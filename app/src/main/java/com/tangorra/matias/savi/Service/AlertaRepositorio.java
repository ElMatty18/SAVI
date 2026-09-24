package com.tangorra.matias.savi.Service;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.RespuestaAlerta;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.Date;

public final class AlertaRepositorio {

    private AlertaRepositorio() {
    }

    public static DatabaseReference alertas(String idGrupo) {
        return FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo).child(idGrupo).child(FirebaseUtils.dbAlerta);
    }

    public static RespuestaAlerta nuevaRespuesta(Usuario usuario, String idAlerta) {
        RespuestaAlerta respuesta = new RespuestaAlerta();
        respuesta.setIdUsuario(usuario.getId());
        respuesta.setNombreUsuario(usuario.getNombre());
        respuesta.setApellidoUsuario(usuario.getApellido());
        respuesta.setCreacion(new Date());
        respuesta.setIdAlarma(idAlerta);
        return respuesta;
    }

    /**
     * Agrega la respuesta dentro de una transaccion: antes se reescribia la alerta completa con la copia local,
     * y si dos vecinos respondian a la vez se perdia una de las respuestas.
     *
     * @param nuevoEstado estado a asignar a la alerta, o null para no modificarlo.
     */
    public static void responder(String idGrupo, String idAlerta, final RespuestaAlerta respuesta, final String nuevoEstado) {
        if (idGrupo == null || idAlerta == null) {
            return;
        }
        alertas(idGrupo).child(idAlerta).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData actual) {
                Alerta alerta = actual.getValue(Alerta.class);
                if (alerta == null) {
                    return Transaction.success(actual);
                }
                if (alerta.getRespuestas() == null) {
                    alerta.setRespuestas(new ArrayList<RespuestaAlerta>());
                }
                alerta.getRespuestas().add(respuesta);
                if (nuevoEstado != null) {
                    alerta.setEstado(nuevoEstado);
                }
                actual.setValue(alerta);
                return Transaction.success(actual);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
            }
        });
    }
}
