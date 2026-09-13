package com.jcaa.imc.dominio.modelos;

import com.jcaa.imc.dominio.vo.Altura;
import com.jcaa.imc.dominio.vo.Peso;

import java.util.Objects;

public record DatosImc(Peso peso, Altura altura) {
    public DatosImc {
        Objects.requireNonNull(peso, "El peso es obligatorio.");
        Objects.requireNonNull(altura, "La altura es obligatoria.");
    }
}
