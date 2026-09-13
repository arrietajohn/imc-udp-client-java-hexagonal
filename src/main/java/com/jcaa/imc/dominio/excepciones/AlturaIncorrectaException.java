package com.jcaa.imc.dominio.excepciones;

import java.io.Serial;

public final class AlturaIncorrectaException extends DominioException {
    @Serial
    private static final long serialVersionUID = 1L;

    public AlturaIncorrectaException(final String mensaje) {
        super(mensaje);
    }
}
