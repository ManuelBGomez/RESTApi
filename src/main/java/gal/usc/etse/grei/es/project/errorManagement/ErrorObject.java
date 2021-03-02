package gal.usc.etse.grei.es.project.errorManagement;

import java.util.Objects;

/**
 * Clase ErrorObject: se utiliza para encapsular la información de un error que se enviará en caso de no
 * poder satisfacer una petición.
 *
 * @author Manuel Bendaña
 */
public class ErrorObject {
    //Tipo de error: clase Enum.
    private ErrorType errorType;
    //Descripción del error.
    private String description;

    /**
     * Constructor de la clase.
     * @param type Tipo de error ocurrido.
     * @param description Detalle del error ocurrido.
     */
    public ErrorObject(ErrorType type, String description){
        this.errorType = type;
        this.description = description;
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
