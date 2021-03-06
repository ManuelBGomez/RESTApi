package gal.usc.etse.grei.es.project.errorManagement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Objects;

/**
 * Clase ErrorObject: se utiliza para encapsular la información de un error que se enviará en caso de no
 * poder satisfacer una petición.
 *
 * @author Manuel Bendaña
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorObject {
    //Tipo de error: clase Enum.
    private ErrorType errorType;
    //Descripción del error.
    private String description;
    //Detalle de errores
    private List<String> errorDetails;

    /**
     * Constructor de la clase.
     * Se usará cuando no queramos dar más detalles de un error que una breve descripción.
     * @param type Tipo de error ocurrido.
     * @param description Detalle del error ocurrido.
     */
    public ErrorObject(ErrorType type, String description){
        this.errorType = type;
        this.description = description;
    }

    /**
     * Constructor de la clase.
     * Se usará cuando pueda haber varios errores y se quieran detallar.
     * @param errorType Tipo de error ocurrido.
     * @param description Descripción del error ocurrido.
     * @param errorDetails Errores detallados.
     */
    public ErrorObject(ErrorType errorType, String description, List<String> errorDetails) {
        this.errorType = errorType;
        this.description = description;
        this.errorDetails = errorDetails;
    }

    /**
     * Getter del tipo de error
     * @return El tipo de error almacenado.
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * Setter del tipo de error
     * @param errorType El tipo de error
     * @return El objeto modificado.
     */
    public ErrorObject setErrorType(ErrorType errorType) {
        this.errorType = errorType;
        return this;
    }

    /**
     * Getter de la desccripción del error.
     * @return La descripción
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter de la descripción del error.
     * @param description la descripción a asociar.
     * @return El objeto modificado.
     */
    public ErrorObject setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Getter de los detalles del error
     * @return Lista de problemas.
     */
    public List<String> getErrorDetails() {
        return errorDetails;
    }

    /**
     * Setter de los detalles del error.
     * @param errorDetails Lista de problemas.
     * @return El objeto de error una vez modificado.
     */
    public ErrorObject setErrorDetails(List<String> errorDetails) {
        this.errorDetails = errorDetails;
        return this;
    }

    /**
     * Método toString
     * @return el objeto de error convertido a string
     */
    @Override
    public String toString() {
        return "ErrorObject{" +
                "errorType=" + errorType +
                ", description='" + description + '\'' +
                '}';
    }

    /**
     * Método equals, comprueba que dos objetos sean iguales.
     * @param o
     * @return true si los objetos son iguales, false en otro caso.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorObject that = (ErrorObject) o;
        return errorType == that.errorType && Objects.equals(description, that.description);
    }

    /**
     * Método hashCode
     * @return el hash del objeto.
     */
    @Override
    public int hashCode() {
        return Objects.hash(errorType, description);
    }

}
