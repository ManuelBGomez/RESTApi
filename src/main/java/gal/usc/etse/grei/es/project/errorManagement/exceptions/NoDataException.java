package gal.usc.etse.grei.es.project.errorManagement.exceptions;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;

/**
 * Clase NoDataException: excepción lanzada cuando no existe algún dato que se pasa.
 *
 * @author Manuel Bendaña
 */
public class NoDataException extends GeneralApiException{
    /**
     * Constructor de la clase
     * @param errorType Tipo de error.
     * @param message   Mensaje destinado al error.
     */
    public NoDataException(ErrorType errorType, String message) {
        super(errorType, message);
    }
}
