package gal.usc.etse.grei.es.project.errorManagement.exceptions;

import gal.usc.etse.grei.es.project.errorManagement.ErrorObject;
import gal.usc.etse.grei.es.project.errorManagement.ErrorType;

/**
 * Clase GeneralApiException: representa una excepción de la aplicación general. Las demás derivarán de ella.
 *
 * @author Manuel Bendaña
 */
public abstract class GeneralApiException extends Exception{

    //Objeto de error, que será el que se devuelve al usuario.
    private final ErrorObject errorObject;

    /**
     * Constructor de la clase
     * @param errorType Tipo de error.
     * @param message Mensaje destinado al error.
     */
    public GeneralApiException(ErrorType errorType, String message){
        //Se llama al constructor de la clase superior (Exception).
        super(message);
        //Se asigna el ErrorObject:
        this.errorObject = new ErrorObject(errorType, message);
    }

    /**
     * Getter del errorObject, para devolverlo después en la respuesta.
     * @return El objeto de error guardado en la clase.
     */
    public ErrorObject getErrorObject() {
        return errorObject;
    }
}
