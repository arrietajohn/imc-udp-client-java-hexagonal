package com.jcaa.imc.dominio.puertos.salida;

import com.jcaa.imc.aplicacion.excepciones.ClienteRedException;
import com.jcaa.imc.dominio.modelos.DatosImc;
import com.jcaa.imc.dominio.modelos.ResultadoImc;
import com.jcaa.imc.dominio.vo.DestinoServidor;

public interface ClienteUdpPort {
    void conectar(DestinoServidor destino) throws ClienteRedException;

    void desconectar() throws ClienteRedException;

    ResultadoImc solicitarCalculo(DatosImc datos) throws ClienteRedException;

    boolean estaConectado();
}
