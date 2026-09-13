package com.jcaa.imc.aplicacion.puertos.entrada;

import com.jcaa.imc.aplicacion.dto.CalcularImcCommand;
import com.jcaa.imc.dominio.modelos.ResultadoImc;

import java.util.concurrent.CompletableFuture;

public interface CalcularImcInputPort {
    CompletableFuture<ResultadoImc> calcular(CalcularImcCommand comando);
}
