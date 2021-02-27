package gal.usc.etse.grei.es.project.errorManagement.exceptions;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;

/**
 * Clase NoResultException: representa el tipo de excepciones a lanzar cuando no se tienen resultados de una búsqueda.
 *
 * @author Manuel Bendaña
 */
public class NoResultException extends GeneralApiException{

    /**
     * Constructor de la clase, cuando la excepción se lanza
     * @param errorType Tipo de error.
     * @param message Mensaje destinado al error.
     */
    public NoResultException(ErrorType errorType, String message) {
        //Los llevamos a la clase padre:
        super(errorType, message);
    }
}
