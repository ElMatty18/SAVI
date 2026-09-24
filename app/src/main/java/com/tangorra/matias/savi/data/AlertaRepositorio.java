package com.tangorra.matias.savi.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.RespuestaAlerta;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AlertaRepositorio {

    private AlertaRepositorio() {
    }

    public static DatabaseReference alertas(String idGrupo) {
        return FirebaseUtils.db().getReference(FirebaseUtils.dbGrupo).child(idGrupo).child(FirebaseUtils.dbAlerta);
    }

    /** Alertas del grupo, las mas nuevas primero. */
    public static LiveData<List<Alerta>> alertasGrupo(String idGrupo) {
        return new FirebaseLiveData<>(alertas(idGrupo), new FirebaseLiveData.Parser<List<Alerta>>() {
            @Override
            public List<Alerta> parse(@NonNull DataSnapshot snapshot) {
                List<Alerta> lista = FirebaseLiveData.lista(Alerta.class).parse(snapshot);
                Collections.sort(lista, POR_FECHA_DESC);
                return lista;
            }
        });
    }

    public static LiveData<Alerta> alerta(String idGrupo, String idAlerta) {
        return new FirebaseLiveData<>(alertas(idGrupo).child(idAlerta), FirebaseLiveData.objeto(Alerta.class));
    }

    /** Crea una alerta nueva firmada por el usuario. Devuelve su id. */
    public static String emitir(String idGrupo, Usuario creador, String tipo, Usuario destinatario) {
        String id = alertas(idGrupo).push().getKey();
        Alerta alerta = new Alerta(id,
                destinatario != null ? destinatario.getGlosaFormateada() : com.tangorra.matias.savi.Utils.StringUtils.ALL_USERS,
                tipo, new Date(), creador.getGlosa());
        alerta.setEstado(com.tangorra.matias.savi.Utils.StringUtils.alertaActiva);
        alerta.setCreadoById(creador.getId());
        if (destinatario != null) {
            alerta.setDirigidaId(destinatario.getId());
        }
        alertas(idGrupo).child(id).setValue(alerta);
        return id;
    }

    private static final Comparator<Alerta> POR_FECHA_DESC = new Comparator<Alerta>() {
        @Override
        public int compare(Alerta a, Alerta b) {
            long ta = a.getCreacion() != null ? a.getCreacion().getTime() : 0;
            long tb = b.getCreacion() != null ? b.getCreacion().getTime() : 0;
            return Long.compare(tb, ta);
        }
    };

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
