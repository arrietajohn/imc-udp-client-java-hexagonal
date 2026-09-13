package com.jcaa.imc.aplicacion.servicios;

import com.jcaa.imc.aplicacion.dto.CalcularImcCommand;
import com.jcaa.imc.aplicacion.dto.ConectarCommand;
import com.jcaa.imc.aplicacion.excepciones.ClienteRedException;
import com.jcaa.imc.aplicacion.mapper.ClienteMapper;
import com.jcaa.imc.aplicacion.puertos.entrada.CalcularImcInputPort;
import com.jcaa.imc.aplicacion.puertos.entrada.GestionarConexionInputPort;
import com.jcaa.imc.dominio.enums.EstadoConexion;
import com.jcaa.imc.dominio.modelos.EventoCliente;
import com.jcaa.imc.dominio.modelos.ResultadoImc;
import com.jcaa.imc.dominio.puertos.salida.ClienteUdpPort;
import com.jcaa.imc.dominio.puertos.salida.PuertoNotificacionCliente;
import com.jcaa.imc.dominio.vo.DestinoServidor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public final class ClienteImcService implements GestionarConexionInputPort, CalcularImcInputPort {
    private static final Logger LOG = LoggerFactory.getLogger(ClienteImcService.class);
    private static final String LOG_ERROR_OPERACION = "Falló la operación UDP {}";
    private static final String OPERACION_CONECTAR = "conectar";
    private static final String OPERACION_DESCONECTAR = "desconectar";
    private static final String OPERACION_CALCULAR = "calcular";
    @NonNull private final ClienteUdpPort clienteUdp;
    @NonNull private final PuertoNotificacionCliente notificador;
    @NonNull private final ClienteMapper mapper;
    @NonNull private final Executor executor;

    @Override
    public CompletableFuture<Void> conectar(final ConectarCommand comando) {
        final DestinoServidor destino = mapper.toDestino(comando);
        notificador.notificarEstado(EstadoConexion.CONECTANDO, destino.endpoint());
        return CompletableFuture.runAsync(() -> {
            try {
                clienteUdp.conectar(destino);
                notificador.notificarEstado(EstadoConexion.CONECTADO, destino.endpoint());
                notificador.notificarEvento(new EventoCliente("CONEXIÓN", "Conectado a " + destino.endpoint()));
            } catch (final ClienteRedException excepcion) {
                LOG.error(LOG_ERROR_OPERACION, OPERACION_CONECTAR, excepcion);
                notificador.notificarEstado(EstadoConexion.DESCONECTADO, destino.endpoint());
                notificador.notificarError(excepcion.getMessage());
                throw new CompletionException(excepcion);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> desconectar() {
        return CompletableFuture.runAsync(() -> {
            try {
                clienteUdp.desconectar();
                notificador.notificarEstado(EstadoConexion.DESCONECTADO, "");
                notificador.notificarEvento(new EventoCliente("CONEXIÓN", "Cliente desconectado."));
            } catch (final ClienteRedException excepcion) {
                LOG.error(LOG_ERROR_OPERACION, OPERACION_DESCONECTAR, excepcion);
                notificador.notificarError(excepcion.getMessage());
                throw new CompletionException(excepcion);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<ResultadoImc> calcular(final CalcularImcCommand comando) {
        final var datos = mapper.toDatos(comando);
        return CompletableFuture.supplyAsync(() -> {
            try {
                final ResultadoImc resultado = clienteUdp.solicitarCalculo(datos);
                notificador.notificarResultado(resultado);
                return resultado;
            } catch (final ClienteRedException excepcion) {
                LOG.error(LOG_ERROR_OPERACION, OPERACION_CALCULAR, excepcion);
                notificador.notificarError(excepcion.getMessage());
                throw new CompletionException(excepcion);
            }
        }, executor);
    }

    @Override
    public boolean estaConectado() {
        return clienteUdp.estaConectado();
    }
}
