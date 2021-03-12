package gal.usc.etse.grei.es.project.errorManagement.exceptions;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;

/**
 * Excepción que se lanza en caso de ejecutar alguna acción no permitida.
 *
 * @author Manuel Bendaña
 */
public class ForbiddenActionException extends GeneralApiException{

    /**
     * Constructor de la clase
     * @param errorType Tipo de error.
     * @param message   Mensaje destinado al error.
     */
    public ForbiddenActionException(ErrorType errorType, String message) {
        super(errorType, message);
    }
}
