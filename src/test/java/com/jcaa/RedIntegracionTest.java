package com.jcaa;

import com.jcaa.imc.adaptadores.red.AdaptadorClienteUdp;
import com.jcaa.imc.adaptadores.red.CanalUdp;
import com.jcaa.imc.adaptadores.red.ProtocoloUdpMapper;
import com.jcaa.imc.aplicacion.excepciones.ClienteRedException;
import com.jcaa.imc.dominio.modelos.DatosImc;
import com.jcaa.imc.dominio.vo.Altura;
import com.jcaa.imc.dominio.vo.DestinoServidor;
import com.jcaa.imc.dominio.vo.Peso;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedIntegracionTest {
    @Test
    void intercambiaProtocoloCompletoConServidorUdpReal()
            throws IOException, ClienteRedException, InterruptedException {
        try (DatagramSocket servidor = new DatagramSocket(0)) {
            final CountDownLatch terminado = new CountDownLatch(1);
            final Thread hilo = new Thread(() -> atender(servidor, terminado));
            hilo.start();
            final AdaptadorClienteUdp cliente = new AdaptadorClienteUdp(new CanalUdp(2000), new ProtocoloUdpMapper());
            cliente.conectar(new DestinoServidor("127.0.0.1", servidor.getLocalPort()));
            assertTrue(cliente.estaConectado());
            final var resultado = cliente.solicitarCalculo(new DatosImc(new Peso(70), new Altura(1.75)));
            assertEquals(22.86, resultado.imc());
            cliente.desconectar();
            assertFalse(cliente.estaConectado());
            assertTrue(terminado.await(2, TimeUnit.SECONDS));
        }
    }

    @Test
    void rechazaCalculoSinConexionYHostDesconocido() {
        final AdaptadorClienteUdp cliente = new AdaptadorClienteUdp(new CanalUdp(100), new ProtocoloUdpMapper());
        assertThrows(ClienteRedException.class,
                () -> cliente.solicitarCalculo(new DatosImc(new Peso(70), new Altura(1.75))));
        assertThrows(ClienteRedException.class, () -> cliente.conectar(new DestinoServidor("host.invalid", 9876)));
    }

    private static void atender(final DatagramSocket socket, final CountDownLatch terminado) {
        try {
            responder(socket, "CONECTADO_OK;listo");
            responder(socket, "OK_CALCULO;22.86;Peso normal;Mantener hábitos");
            final DatagramPacket cierre = new DatagramPacket(new byte[64], 64);
            socket.receive(cierre);
            terminado.countDown();
        } catch (final IOException excepcion) {
            throw new AssertionError(excepcion);
        }
    }

    private static void responder(final DatagramSocket socket, final String respuesta) throws IOException {
        final DatagramPacket entrada = new DatagramPacket(new byte[256], 256);
        socket.receive(entrada);
        final byte[] datos = respuesta.getBytes(StandardCharsets.UTF_8);
        socket.send(new DatagramPacket(datos, datos.length, entrada.getAddress(), entrada.getPort()));
    }
}
