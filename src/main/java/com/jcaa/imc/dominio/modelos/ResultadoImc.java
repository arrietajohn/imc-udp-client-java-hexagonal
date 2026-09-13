package com.jcaa.imc.dominio.modelos;

import com.jcaa.imc.dominio.excepciones.RespuestaServidorException;

import java.util.Objects;

public record ResultadoImc(double imc, String imcFormateado, String clasificacion, String recomendaciones) {
    public ResultadoImc {
        if (!Double.isFinite(imc) || Objects.isNull(imcFormateado) || imcFormateado.isBlank()
                || Objects.isNull(clasificacion) || clasificacion.isBlank()
                || Objects.isNull(recomendaciones)) {
            throw new RespuestaServidorException("El resultado recibido está incompleto o es inválido.");
        }
    }
}
