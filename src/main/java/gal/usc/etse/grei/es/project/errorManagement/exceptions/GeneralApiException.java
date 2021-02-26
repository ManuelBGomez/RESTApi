package gal.usc.etse.grei.es.project.errorManagement.exceptions;

import gal.usc.etse.grei.es.project.errorManagement.ErrorObject;
import gal.usc.etse.grei.es.project.errorManagement.ErrorType;

public class GeneralApiException extends Exception{

    private final ErrorObject errorObject;

    public GeneralApiException(ErrorType errorType, String message){
        super(message);
        this.errorObject = new ErrorObject(errorType, message);
    }

    public ErrorObject getErrorObject() {
        return errorObject;
    }
}
