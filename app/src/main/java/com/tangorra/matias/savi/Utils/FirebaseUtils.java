package com.tangorra.matias.savi.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tangorra.matias.savi.Activitys.AccesoActivity;
import com.tangorra.matias.savi.Activitys.MainActivity;
import com.tangorra.matias.savi.Entidades.Grupo;
import com.tangorra.matias.savi.Entidades.Persona;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

public class FirebaseUtils extends AppCompatActivity  {

    public static String dbGrupo = "Grupo";
    public static String dbUsuario = "Usuario";
    public static String dbFamilia = "Familia";

    /*
    private DatabaseReference dbUsuarios = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbUsuario);

    private Usuario usuario;
    private boolean finalizadaTarea = false;

   public Usuario getUsuarioById(String id){
       MiCallable miCallable = new MiCallable();
       miCallable.setIdUsuario(id);
       Usuario resultado = new Usuario();
       try {
           resultado = miCallable.call();
       } catch (Exception e) {
           e.printStackTrace();
       }
       return resultado;
   }


    public class MiCallable implements Callable<Usuario> {
       private String idUsuario;
       private Usuario usuario;

        public String getIdUsuario() {
            return idUsuario;
        }

        public void setIdUsuario(String idUsuario) {
            this.idUsuario = idUsuario;
        }

        public Usuario getUsuario() {
            return usuario;
        }

        public void setUsuario(Usuario usuario) {
            this.usuario = usuario;
        }

        @Override
        public Usuario call() throws Exception {
            finalizadaTarea = false;
            dbUsuarios.orderByChild("id").equalTo(idUsuario).limitToFirst(1).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Usuario usuario = new Usuario();
                    for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                        usuario = imageSnapshot.getValue(Usuario.class);
                    }
                    finalizadaTarea = true;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            while (!finalizadaTarea){
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }
            System.out.println(Thread.currentThread().getName());
            return usuario;
        }

    }

    */


}
