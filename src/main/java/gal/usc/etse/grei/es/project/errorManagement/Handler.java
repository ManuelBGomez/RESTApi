package gal.usc.etse.grei.es.project.errorManagement;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase Handler - La usamos para manejar los errores recibidos en la validación de campos y poder
 * mostrarlos a nuestra manera.
 *
 * @author Manuel Bendaña
 */
@RestControllerAdvice
public class Handler extends ResponseEntityExceptionHandler {

    /**
     * Método invocado en caso de que una bean validation dé un error.
     *
     * @param ex La excepción lanzada.
     * @param headers Objeto de HttpHeaders
     * @param status Estado HTTP
     * @param request Objeto de WebRequest
     * @return La respuesta que se enviará en caso de error.
     */
    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                               HttpHeaders headers, HttpStatus status,
                                                               WebRequest request) {
        //Creamos un arraylist que contenga todos los errores encontrados:
        List<String> errorList = new ArrayList<>();
        //Recuperamos los errores en los campos de la clase y los vamos añadiendo al array:
        //Se usa un formato especial.
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errorList.add("In: " + fieldError.getField() + " - " + fieldError.getDefaultMessage());
        });

        //Se devuelve una llamada al método handleExceptionInternal incluyendo el objeto de error que queremos que
        //se ofrezca, el cual contiene en este caso la lista de errores.
        return handleExceptionInternal(ex, new ErrorObject(ErrorType.INVALID_PARAMETER,
                        "There was an error on the request data", errorList), headers, status, request);
    }

    /**
     * Manejador de la excepción AlreadyCreatedException
     * @param exception La excepción lanzada
     * @return Un estado de error conflict.
     */
    @ExceptionHandler(AlreadyCreatedException.class)
    public ResponseEntity<ErrorObject> handle(AlreadyCreatedException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getErrorObject());
    }

    /**
     * Manejador de la excepción ForbiddenActionException
     * @param exception La excepción lanzada.
     * @return Un estado de error Forbidden.
     */
    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<ErrorObject> handle(ForbiddenActionException exception){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getErrorObject());
    }

    /**
     * Manejador de la excepción InvalidDataException
     * @param exception La excepción lanzada.
     * @return Un estado de error Bad Request.
     */
    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorObject> handle(InvalidDataException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getErrorObject());
    }

    /**
     * Manejador de la excepción InvalidFormatException
     * @param exception La excepción lanzada
     * @return Un estado de error Unprocessable Entity
     */
    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ErrorObject> handle(InvalidFormatException exception){
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(exception.getErrorObject());
    }

    /**
     * Manejador de la excepción NoDataException
     * @param exception La excepción lanzada
     * @return Un estado de error Not Found
     */
    @ExceptionHandler(NoDataException.class)
    public ResponseEntity<ErrorObject> handle(NoDataException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getErrorObject());
    }
}
