package com.tangorra.matias.savi.Utils;

import com.google.android.gms.flags.impl.DataUtils;

public  class StringUtils {

    public static String fieldRequired = "Campo requerido!";
    public static String fieldInvalid = "Campo no valido!";
    public static String userLogin = "Logueando usuario...";

    public static String userWithEqualMail = "Ya existe un usuario registrado con el Mail ingresado";
    public static String errorRegister = "Error registrando usuario";
    public static String userRegister = "Registrando usuario";
    public static String checkData = "Verifique los datos ingresados";
    public static String welcome = "Bienvenido ";

    public static String alert = "ALERTA!";

    public static String ALARMA_SONANDO = "Alarma sonando";
    public static String SOSPECHA_ROBO = "Sospecha de robo";
    public static String ACTITUD_SOSPECHOSA = "Actitud sospechosa";
    public static String DANO_VEHICULO = "Daño al vehiculo";
    public static String PRINCIPIO_FUEGO = "Principio de fuego";
    public static String AGRESION = "Agresion";
    public static String MAL_ESTACIONADO = "Auto mal estacionado";
    public static String ALL_USERS = "TODOS";

    public static String SAVI = "Sistema de Asistencia Vecinal Integral";
    public static String RESUME_SAVI = "SAVI";

    public static String SAVI_CANALES = "Da canales seguros de accion, ante eventos cotidianos";
    public static String SAVI_AGENTE = "Usa tu telefono como agente independiente, ninguno de tus contactos tendra acceso a tu datos precisos";
    public static String SAVI_GRUPOS = "Permite crea un grupo vecinal y un grupo familiar, para dara accion a las alertas";

    public static String DESCRIP_ALARMA_SONANDO = "Se dara un aviso sonoro al usuario al que esta dirijida dicha alarma";
    public static String DESCRIP_SOSPECHA_ROBO = "La alarma sera ruidosa para todos los usuarios excepto para el que fue destinada. Se recomendara contactar a las autoridades en caso de que la sospecha sea concreta";
    public static String DESCRIP_ACTITUD_SOSPECHOSA = "Se da aviso sonoro a todos los usuarios";
    public static String DESCRIP_DANO_VEHICULO = "Si existe una alerta de Daño al vehiculo, informara al usuario respectivo. En caso de que el usuario no ese en su domicilio se informara dicha situacion y se les informa a sus familiares";
    public static String DESCRIP_PRINCIPIO_FUEGO = "Se creara una alama sonora para el usuario destinatario de dicha alerta, y una notificacion silenciosa a los demas usuarios";
    public static String DESCRIP_AGRESION = "Se creara una alerta sonora para todos los usuarios";
    public static String DESCRIP_MAL_ESTACIONADO = "Se crea una alarta silenciosa para todos los usuarios";


    public static String openScanGroup = "SAVI ScanQR - Grupo";
    public static String cancelScan = "Se cancelo Scan";

    public static String denegatePermission = "Permiso denegado";


    public static String alertaActiva = "activa";

    public static String parametroAlerta = "parametroAlerta";

    public static String config_vacaciones = "Vacaciones";
    public static String config_casaSola = "Casa Sola";
    public static String config_visitasCasa = "Visitas Casa";
    public static String config_noMolestar = "No Molestar";
    public static String config_ignorarTodo = "Ignorar Todo";




    public static String getTextoFormateado(String texto){
        String formateado="";
        if (texto != null && !texto .isEmpty()){
            String[] separado = texto.split(" ");
            for (int i=0; i<separado.length;i++){
                formateado += uperFirst(separado[i]) + " ";
            }
        }
        return formateado;
    }

    private static String uperFirst(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        } else {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
    }
}
