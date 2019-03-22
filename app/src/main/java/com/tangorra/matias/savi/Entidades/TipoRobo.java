package com.tangorra.matias.savi.Entidades;

public class TipoRobo implements TipoAlarma {
    @Override
    public void dispararAlarma(Alerta alerta) {
        //la idea es que le suene a todos, menos al dueño de la casa
        //salvo que no este en la casa --> Agente!!!
    }
}
