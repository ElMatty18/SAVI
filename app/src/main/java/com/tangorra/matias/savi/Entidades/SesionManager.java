package com.tangorra.matias.savi.Entidades;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Mantiene el usuario y el grupo de la sesion. Se respalda en disco para que la sesion
 * sobreviva cuando Android mata el proceso de la app en segundo plano.
 */
public class SesionManager {
    private static final String TAG = "SesionManager";
    private static final String ARCHIVO = "sesion.bin";

    private static final SesionManager ourInstance = new SesionManager();

    public static SesionManager getInstance() {
        return ourInstance;
    }

    private static Grupo grupo;
    private static Usuario usuario;
    private static File archivo;
    private static boolean restaurada;

    private SesionManager() {
    }

    public static void init(Context context) {
        archivo = new File(context.getFilesDir(), ARCHIVO);
    }

    public static synchronized Grupo getGrupo() {
        restaurar();
        return grupo;
    }

    public static synchronized void setGrupo(Grupo grupo) {
        restaurar();
        SesionManager.grupo = grupo;
        guardar();
    }

    public static synchronized Usuario getUsuario() {
        restaurar();
        return usuario;
    }

    public static synchronized void setUsuario(Usuario usuario) {
        restaurar();
        SesionManager.usuario = usuario;
        guardar();
    }

    public static synchronized boolean haySesion() {
        return getUsuario() != null && usuario.getId() != null;
    }

    public static synchronized void clean() {
        grupo = new Grupo("sin nombre");
        usuario = new Usuario();
        restaurada = true;
        if (archivo != null) {
            archivo.delete();
        }
    }

    /** Persiste el estado actual (las pantallas modifican el usuario en memoria sin volver a setearlo). */
    public static synchronized void guardar() {
        restaurar();
        if (archivo == null) {
            return;
        }
        if (usuario == null || usuario.getId() == null) {
            archivo.delete();
            return;
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(archivo))) {
            out.writeObject(usuario);
            out.writeObject(grupo);
        } catch (Exception e) {
            Log.w(TAG, "No se pudo guardar la sesion", e);
        }
    }

    private static void restaurar() {
        if (restaurada || archivo == null) {
            return;
        }
        restaurada = true;
        if (usuario != null || !archivo.exists()) {
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(archivo))) {
            usuario = (Usuario) in.readObject();
            grupo = (Grupo) in.readObject();
        } catch (Exception e) {
            Log.w(TAG, "No se pudo restaurar la sesion", e);
            archivo.delete();
        }
    }
}
