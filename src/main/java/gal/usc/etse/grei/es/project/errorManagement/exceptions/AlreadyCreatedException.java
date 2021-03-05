package gal.usc.etse.grei.es.project.errorManagement.exceptions;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;

/**
 * AlreadyCreatedException: excepción que se lanzará cuando ya exista algún dato que se quiere insertar.
 * @author Manuel Bendaña
 */
public class AlreadyCreatedException extends GeneralApiException{
    /**
     * Constructor de la clase
     * @param errorType Tipo de error.
     * @param message   Mensaje destinado al error.
     */
    public AlreadyCreatedException(ErrorType errorType, String message) {
        super(errorType, message);
    }
}
