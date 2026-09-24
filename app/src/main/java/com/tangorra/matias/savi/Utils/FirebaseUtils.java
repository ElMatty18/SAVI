package com.tangorra.matias.savi.Utils;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public final class FirebaseUtils {

    private FirebaseUtils() {
    }

    public static String dbGrupo = "Grupo";
    public static String dbUsuario = "Usuario";
    public static String dbFamilia = "Familia";
    public static String dbNotificacion = "Notificacion";

    public static String dbAlerta = "alertas";

    private static FirebaseDatabase database;
    private static FirebaseStorage storage;

    /**
     * Unica instancia de la base para toda la app. FirebaseDatabase.getInstance() puede devolver
     * una instancia nueva (sin la configuracion del emulador) despues de llamar a useEmulator().
     */
    public static synchronized FirebaseDatabase db() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
        }
        return database;
    }

    public static synchronized FirebaseStorage storage() {
        if (storage == null) {
            storage = FirebaseStorage.getInstance();
        }
        return storage;
    }
}
