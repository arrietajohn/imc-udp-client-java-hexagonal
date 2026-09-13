package com.jcaa;

import com.jcaa.imc.adaptadores.red.ProtocoloUdpMapper;
import com.jcaa.imc.aplicacion.dto.CalcularImcCommand;
import com.jcaa.imc.aplicacion.dto.ConectarCommand;
import com.jcaa.imc.aplicacion.mapper.ClienteMapper;
import com.jcaa.imc.dominio.excepciones.AlturaIncorrectaException;
import com.jcaa.imc.dominio.excepciones.DestinoIncorrectoException;
import com.jcaa.imc.dominio.excepciones.PesoIncorrectoException;
import com.jcaa.imc.dominio.excepciones.RespuestaServidorException;
import com.jcaa.imc.dominio.modelos.DatosImc;
import com.jcaa.imc.dominio.modelos.ResultadoImc;
import com.jcaa.imc.dominio.vo.Altura;
import com.jcaa.imc.dominio.vo.Peso;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DominioYProtocoloTest {
    @Test
    void validaValores() {
        assertThrows(PesoIncorrectoException.class, () -> new Peso(0));
        assertThrows(PesoIncorrectoException.class, () -> new Peso(Double.NaN));
        assertThrows(AlturaIncorrectaException.class, () -> new Altura(-1));
        assertThrows(AlturaIncorrectaException.class, () -> new Altura(Double.POSITIVE_INFINITY));
        assertThrows(DestinoIncorrectoException.class,
                () -> new ClienteMapper().toDestino(new ConectarCommand(" ", 9876)));
        assertThrows(DestinoIncorrectoException.class,
                () -> new ClienteMapper().toDestino(new ConectarCommand("host", 80)));
    }

    @Test
    void validaIntegridadDelResultado() {
        assertThrows(RespuestaServidorException.class, () -> new ResultadoImc(Double.NaN, "x", "Normal", "ok"));
        assertThrows(RespuestaServidorException.class, () -> new ResultadoImc(20, " ", "Normal", "ok"));
        assertThrows(RespuestaServidorException.class, () -> new ResultadoImc(20, "20", " ", "ok"));
        assertThrows(RespuestaServidorException.class, () -> new ResultadoImc(20, "20", "Normal", null));
    }

    @Test
    void transformaComandos() {
        final ClienteMapper mapper = new ClienteMapper();
        assertEquals("localhost:9876", mapper.toDestino(new ConectarCommand(" localhost ", 9876)).endpoint());
        final DatosImc datos = mapper.toDatos(new CalcularImcCommand(70, 1.75));
        assertEquals("CALCULAR;70.00;1.75", new ProtocoloUdpMapper().serializarCalculo(datos));
    }

    @Test
    void parseaResultadoYErrores() {
        final ProtocoloUdpMapper protocolo = new ProtocoloUdpMapper();
        protocolo.validarConexion("CONECTADO_OK;listo");
        final var resultado = protocolo.parsearCalculo("OK_CALCULO;22.86;Normal;Mantener hábitos");
        assertEquals(22.86, resultado.imc());
        assertEquals("Normal", resultado.clasificacion());
        assertThrows(RespuestaServidorException.class, () -> protocolo.validarConexion("ERROR"));
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo(null));
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo("  "));
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo("ERROR;fallo"));
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo("OTRA"));
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo("OK_CALCULO;x;Normal;ok"));
    }
}
