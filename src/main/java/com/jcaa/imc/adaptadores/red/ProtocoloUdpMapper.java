package com.jcaa.imc.adaptadores.red;

import com.jcaa.imc.dominio.excepciones.RespuestaServidorException;
import com.jcaa.imc.dominio.modelos.DatosImc;
import com.jcaa.imc.dominio.modelos.ResultadoImc;

import java.util.Locale;
import java.util.Objects;

public final class ProtocoloUdpMapper {
    public static final String CONECTAR = "CONECTAR";
    public static final String DESCONECTAR = "DESCONECTAR";

    public String serializarCalculo(final DatosImc datos) {
        Objects.requireNonNull(datos, "Los datos del IMC son obligatorios.");
        return String.format(Locale.US, "CALCULAR;%.2f;%.2f", datos.peso().valor(), datos.altura().valor());
    }

    public void validarConexion(final String respuesta) {
        if (Objects.isNull(respuesta) || !respuesta.startsWith("CONECTADO_OK;")) {
            throw new RespuestaServidorException("Respuesta de conexión inesperada: " + respuesta);
        }
    }

    public ResultadoImc parsearCalculo(final String respuesta) {
        if (Objects.isNull(respuesta) || respuesta.isBlank()) {
            throw new RespuestaServidorException("El servidor envió una respuesta vacía.");
        }
        if (respuesta.startsWith("ERROR;")) {
            throw new RespuestaServidorException("Error del servidor: " + respuesta.substring(6));
        }
        final String[] partes = respuesta.split(";", -1);
        if (partes.length != 4 || !"OK_CALCULO".equals(partes[0])) {
            throw new RespuestaServidorException("Respuesta de cálculo no estructurada: " + respuesta);
        }
        try {
            final double imc = Double.parseDouble(partes[1].replace(',', '.'));
            return new ResultadoImc(imc, partes[1], partes[2], partes[3]);
        } catch (final NumberFormatException excepcion) {
            throw new RespuestaServidorException("El IMC recibido no es numérico.");
        }
    }
}
