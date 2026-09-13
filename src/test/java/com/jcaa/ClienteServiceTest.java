package com.jcaa;

import com.jcaa.imc.aplicacion.dto.CalcularImcCommand;
import com.jcaa.imc.aplicacion.dto.ConectarCommand;
import com.jcaa.imc.aplicacion.excepciones.ClienteRedException;
import com.jcaa.imc.aplicacion.mapper.ClienteMapper;
import com.jcaa.imc.aplicacion.servicios.ClienteImcService;
import com.jcaa.imc.dominio.enums.EstadoConexion;
import com.jcaa.imc.dominio.modelos.DatosImc;
import com.jcaa.imc.dominio.modelos.EventoCliente;
import com.jcaa.imc.dominio.modelos.ResultadoImc;
import com.jcaa.imc.dominio.puertos.salida.ClienteUdpPort;
import com.jcaa.imc.dominio.puertos.salida.PuertoNotificacionCliente;
import com.jcaa.imc.dominio.vo.DestinoServidor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteServiceTest {
    @Test
    void orquestaConexionCalculoYDesconexion() {
        final UdpMemoria udp = new UdpMemoria();
        final NotificadorMemoria notificador = new NotificadorMemoria();
        final ClienteImcService servicio = new ClienteImcService(udp, notificador, new ClienteMapper(), Runnable::run);
        servicio.conectar(new ConectarCommand("localhost", 9876)).join();
        assertTrue(servicio.estaConectado());
        assertEquals(List.of(EstadoConexion.CONECTANDO, EstadoConexion.CONECTADO), notificador.estados);
        assertEquals(22.86, servicio.calcular(new CalcularImcCommand(70, 1.75)).join().imc());
        assertEquals(1, notificador.resultados.size());
        servicio.desconectar().join();
        assertFalse(servicio.estaConectado());
        assertEquals(EstadoConexion.DESCONECTADO, notificador.estados.get(2));
    }

    @Test
    void notificaErroresAsincronos() {
        final UdpMemoria udp = new UdpMemoria();
        udp.fallar = true;
        final NotificadorMemoria notificador = new NotificadorMemoria();
        final ClienteImcService servicio = new ClienteImcService(udp, notificador, new ClienteMapper(), Runnable::run);
        assertThrows(CompletionException.class, () -> servicio.conectar(new ConectarCommand("localhost", 9876)).join());
        assertFalse(notificador.errores.isEmpty());
    }

    @Test
    void notificaErroresDeCalculoYDesconexion() {
        final UdpMemoria udp = new UdpMemoria();
        final NotificadorMemoria notificador = new NotificadorMemoria();
        final ClienteImcService servicio = new ClienteImcService(udp, notificador, new ClienteMapper(), Runnable::run);
        udp.fallarCalculo = true;
        assertThrows(CompletionException.class, () -> servicio.calcular(new CalcularImcCommand(70, 1.75)).join());
        udp.fallarDesconexion = true;
        assertThrows(CompletionException.class, () -> servicio.desconectar().join());
        assertEquals(2, notificador.errores.size());
    }

    private static final class UdpMemoria implements ClienteUdpPort {
        private boolean conectado;
        private boolean fallar;
        private boolean fallarCalculo;
        private boolean fallarDesconexion;

        @Override
        public void conectar(final DestinoServidor destino) throws ClienteRedException {
            if (fallar)
                throw new ClienteRedException("fallo");
            conectado = true;
        }

        @Override
        public void desconectar() throws ClienteRedException {
            if (fallarDesconexion)
                throw new ClienteRedException("fallo desconexión");
            conectado = false;
        }

        @Override
        public ResultadoImc solicitarCalculo(final DatosImc datos) throws ClienteRedException {
            if (fallarCalculo)
                throw new ClienteRedException("fallo cálculo");
            return new ResultadoImc(22.86, "22.86", "Normal", "Mantener hábitos");
        }

        @Override
        public boolean estaConectado() {
            return conectado;
        }
    }

    private static final class NotificadorMemoria implements PuertoNotificacionCliente {
        private final List<EstadoConexion> estados = new ArrayList<>();
        private final List<ResultadoImc> resultados = new ArrayList<>();
        private final List<String> errores = new ArrayList<>();

        @Override
        public void notificarEstado(final EstadoConexion estado, final String endpoint) {
            estados.add(estado);
        }

        @Override
        public void notificarEvento(final EventoCliente evento) {
        }

        @Override
        public void notificarResultado(final ResultadoImc resultado) {
            resultados.add(resultado);
        }

        @Override
        public void notificarError(final String mensaje) {
            errores.add(mensaje);
        }
    }
}
