package com.tangorra.matias.savi.Utils;

import android.support.v7.app.AppCompatActivity;

public class FirebaseUtils extends AppCompatActivity  {

    public static String dbGrupo = "Grupo";
    public static String dbUsuario = "Usuario";
    public static String dbFamilia = "Familia";
    public static String dbNotificacion = "Notificacion";

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
