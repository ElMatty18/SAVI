package com.tangorra.matias.savi.Service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tangorra.matias.savi.Entidades.RespuestaAlerta;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class AlertaRepositorio {

    private AlertaRepositorio() {
    }

    public static DatabaseReference alertas(String idGrupo) {
        return FirebaseUtils.db().getReference(FirebaseUtils.dbGrupo).child(idGrupo).child(FirebaseUtils.dbAlerta);
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
     * Guarda la respuesta del usuario en alertas/{id}/respuestas/{uid} y, si corresponde, el nuevo estado,
     * en una sola escritura atomica. Cada vecino escribe solo su propia respuesta, asi que no hay conflictos
     * aunque respondan varios a la vez.
     *
     * @param nuevoEstado estado a asignar a la alerta, o null para no modificarlo.
     */
    public static void responder(String idGrupo, String idAlerta, RespuestaAlerta respuesta, String nuevoEstado) {
        if (idGrupo == null || idAlerta == null || respuesta.getIdUsuario() == null) {
            return;
        }
        Map<String, Object> cambios = new HashMap<>();
        cambios.put("respuestas/" + respuesta.getIdUsuario(), respuesta);
        if (nuevoEstado != null) {
            cambios.put("estado", nuevoEstado);
        }
        alertas(idGrupo).child(idAlerta).updateChildren(cambios);
    }
}
