package com.jcaa.imc.adaptadores.notificacion;

import com.jcaa.imc.dominio.enums.EstadoConexion;
import com.jcaa.imc.dominio.modelos.EventoCliente;
import com.jcaa.imc.dominio.modelos.ResultadoImc;
import com.jcaa.imc.dominio.puertos.salida.PuertoNotificacionCliente;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AdaptadorNotificacionCliente implements PuertoNotificacionCliente {
    private final CopyOnWriteArrayList<ObservadorCliente> observadores = new CopyOnWriteArrayList<>();

    public void registrar(final ObservadorCliente observador) {
        observadores.addIfAbsent(Objects.requireNonNull(observador, "El observador es obligatorio."));
    }

    public void remover(final ObservadorCliente observador) {
        observadores.remove(observador);
    }

    @Override
    public void notificarEstado(final EstadoConexion estado, final String endpoint) {
        Objects.requireNonNull(estado, "El estado es obligatorio.");
        Objects.requireNonNull(endpoint, "El endpoint es obligatorio.");
        observadores.forEach(o -> o.onEstado(estado, endpoint));
    }

    @Override
    public void notificarEvento(final EventoCliente evento) {
        Objects.requireNonNull(evento, "El evento es obligatorio.");
        observadores.forEach(o -> o.onEvento(evento));
    }

    @Override
    public void notificarResultado(final ResultadoImc resultado) {
        Objects.requireNonNull(resultado, "El resultado es obligatorio.");
        observadores.forEach(o -> o.onResultado(resultado));
    }

    @Override
    public void notificarError(final String mensaje) {
        Objects.requireNonNull(mensaje, "El mensaje es obligatorio.");
        observadores.forEach(o -> o.onError(mensaje));
    }
}
