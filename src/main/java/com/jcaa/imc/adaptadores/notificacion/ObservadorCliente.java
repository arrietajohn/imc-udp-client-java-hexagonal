package com.jcaa.imc.adaptadores.notificacion;

import com.jcaa.imc.dominio.enums.EstadoConexion;
import com.jcaa.imc.dominio.modelos.EventoCliente;
import com.jcaa.imc.dominio.modelos.ResultadoImc;

public interface ObservadorCliente {
    void onEstado(EstadoConexion estado, String endpoint);

    void onEvento(EventoCliente evento);

    void onResultado(ResultadoImc resultado);

    void onError(String mensaje);
}
