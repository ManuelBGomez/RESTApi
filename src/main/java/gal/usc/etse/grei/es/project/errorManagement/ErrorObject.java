package gal.usc.etse.grei.es.project.errorManagement;

import java.util.Objects;

public class ErrorObject {
    private ErrorType type;
    private String description;

    public ErrorObject(ErrorType type, String description){
        this.type = type;
        this.description = description;
    }

    public ErrorType getType() {
        return type;
    }

    public ErrorObject setType(ErrorType type) {
        this.type = type;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ErrorObject setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "ErrorObject{" +
                "type=" + type +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorObject that = (ErrorObject) o;
        return type == that.type && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, description);
    }
}
