package com.tangorra.matias.savi.ui.alertas;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.data.AlertaRepositorio;
import com.tangorra.matias.savi.data.UsuarioRepositorio;
import com.tangorra.matias.savi.ui.comun.PresentacionAlerta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Historial de alertas de un grupo, de la familia (alertas que involucran a algun familiar,
 * en todos sus grupos) o de un familiar puntual.
 */
public class AlertasViewModel extends ViewModel {

    private final MediatorLiveData<List<Alerta>> todas = new MediatorLiveData<>();
    private final MutableLiveData<PresentacionAlerta.Estado> filtro = new MutableLiveData<>(null);
    private final MediatorLiveData<List<Alerta>> visibles = new MediatorLiveData<>();

    // Una fuente por grupo, para combinar alertas de varios grupos
    private final Map<String, LiveData<List<Alerta>>> fuentes = new HashMap<>();
    private final Map<String, List<Alerta>> porGrupo = new HashMap<>();
    // null = sin filtrar por involucrados (todas las del grupo)
    private Set<String> involucrados;
    private boolean cargado;

    public AlertasViewModel() {
        visibles.addSource(todas, a -> filtrar());
        visibles.addSource(filtro, f -> filtrar());
    }

    public LiveData<List<Alerta>> alertas() {
        return visibles;
    }

    public LiveData<List<Alerta>> todas() {
        return todas;
    }

    public void filtrar(PresentacionAlerta.Estado estado) {
        filtro.setValue(estado);
    }

    public void cargarGrupo(String idGrupo) {
        if (marcarCargado()) {
            escucharGrupos(Collections.singleton(idGrupo));
        }
    }

    public void cargarMiembro(Usuario miembro) {
        if (!marcarCargado()) {
            return;
        }
        if (miembro.getIdGrupo() == null) {
            todas.setValue(new ArrayList<>());
            return;
        }
        involucrados = Collections.singleton(miembro.getId());
        escucharGrupos(Collections.singleton(miembro.getIdGrupo()));
    }

    public void cargarFamilia(String idFamilia) {
        if (!marcarCargado()) {
            return;
        }
        todas.addSource(UsuarioRepositorio.familiares(idFamilia), familiares -> {
            Set<String> ids = new HashSet<>();
            Set<String> grupos = new HashSet<>();
            for (Usuario familiar : familiares) {
                ids.add(familiar.getId());
                if (familiar.getIdGrupo() != null) {
                    grupos.add(familiar.getIdGrupo());
                }
            }
            involucrados = ids;
            escucharGrupos(grupos);
            combinar();
        });
    }

    private boolean marcarCargado() {
        if (cargado) {
            return false;
        }
        cargado = true;
        return true;
    }

    private void escucharGrupos(Set<String> grupos) {
        for (String idGrupo : new ArrayList<>(fuentes.keySet())) {
            if (!grupos.contains(idGrupo)) {
                todas.removeSource(fuentes.remove(idGrupo));
                porGrupo.remove(idGrupo);
            }
        }
        for (final String idGrupo : grupos) {
            if (fuentes.containsKey(idGrupo)) {
                continue;
            }
            LiveData<List<Alerta>> fuente = AlertaRepositorio.alertasGrupo(idGrupo);
            fuentes.put(idGrupo, fuente);
            todas.addSource(fuente, lista -> {
                porGrupo.put(idGrupo, lista);
                combinar();
            });
        }
    }

    private void combinar() {
        List<Alerta> resultado = new ArrayList<>();
        for (List<Alerta> lista : porGrupo.values()) {
            for (Alerta alerta : lista) {
                if (involucrados == null || alerta.involucraA(involucrados)) {
                    resultado.add(alerta);
                }
            }
        }
        AlertaRepositorio.ordenarPorFecha(resultado);
        todas.setValue(resultado);
    }

    private void filtrar() {
        List<Alerta> base = todas.getValue();
        if (base == null) {
            return;
        }
        PresentacionAlerta.Estado estado = filtro.getValue();
        if (estado == null) {
            visibles.setValue(base);
            return;
        }
        List<Alerta> filtradas = new ArrayList<>();
        for (Alerta alerta : base) {
            if (PresentacionAlerta.estado(alerta) == estado) {
                filtradas.add(alerta);
            }
        }
        visibles.setValue(filtradas);
    }
}
