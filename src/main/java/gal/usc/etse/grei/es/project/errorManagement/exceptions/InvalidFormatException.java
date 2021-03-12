package gal.usc.etse.grei.es.project.errorManagement.exceptions;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;

/**
 * Clase que representa las excepciones lanzadas en caso de que el formato de las peticiones no sea el apropiado.
 *
 * @author Manuel Bendaña
 */
public class InvalidFormatException extends GeneralApiException{
    /**
     * Constructor de la clase
     * @param errorType Tipo de error.
     * @param message   Mensaje destinado al error.
     */
    public InvalidFormatException(ErrorType errorType, String message) {
        super(errorType, message);
    }
}
