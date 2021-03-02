package gal.usc.etse.grei.es.project.errorManagement.exceptions;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;

/**
 * InvalidDataException: Excepción que se lanzará cuando se faciliten datos incorrectos.
 * @author Manuel Bendaña
 */
public class InvalidDataException extends GeneralApiException{

    /**
     * Constructor de la clase
     * @param errorType Tipo de error.
     * @param message   Mensaje destinado al error.
     */
    public InvalidDataException(ErrorType errorType, String message) {
        super(errorType, message);
    }
}
