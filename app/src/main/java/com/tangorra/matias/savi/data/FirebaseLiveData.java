package com.tangorra.matias.savi.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Expone una consulta de Firebase como LiveData. Escucha solo mientras hay observadores activos
 * (la pantalla esta visible) y se desuscribe sola, asi no quedan listeners colgados.
 */
public class FirebaseLiveData<T> extends LiveData<T> {

    private static final String TAG = "FirebaseLiveData";

    public interface Parser<T> {
        T parse(@NonNull DataSnapshot snapshot);
    }

    private final Query query;
    private final Parser<T> parser;
    private final ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            setValue(parser.parse(snapshot));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.w(TAG, "Consulta cancelada " + query.getRef() + ": " + error.getMessage());
        }
    };

    public FirebaseLiveData(Query query, Parser<T> parser) {
        this.query = query;
        this.parser = parser;
    }

    @Override
    protected void onActive() {
        query.addValueEventListener(listener);
    }

    @Override
    protected void onInactive() {
        query.removeEventListener(listener);
    }

    public static <T> Parser<T> objeto(final Class<T> clase) {
        return new Parser<T>() {
            @Override
            public T parse(@NonNull DataSnapshot snapshot) {
                return snapshot.getValue(clase);
            }
        };
    }

    public static <T> Parser<List<T>> lista(final Class<T> clase) {
        return new Parser<List<T>>() {
            @Override
            public List<T> parse(@NonNull DataSnapshot snapshot) {
                List<T> lista = new ArrayList<>();
                for (DataSnapshot hijo : snapshot.getChildren()) {
                    T valor = hijo.getValue(clase);
                    if (valor != null) {
                        lista.add(valor);
                    }
                }
                return lista;
            }
        };
    }
}
