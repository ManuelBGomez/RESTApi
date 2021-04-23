package gal.usc.etse.grei.es.project.errorManagement;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Tipo enumerado ErrorType que permite identificar el tipo de error encontrado.
 * @author Manuel Bendaña
 */
@Schema(description = "Types of errors that can appear on an ErrorObject",
        allowableValues = {"INVALID_INFO", "UNKNOWN_INFO", "EXISTING_DATA", "ALREADY_MODIFIED", "FORBIDDEN",
                           "INVALID_PARAMETER", "EXPIRED_TOKEN", "INVALID_TOKEN"})
public enum ErrorType {
    INVALID_INFO,
    UNKNOWN_INFO,
    EXISTING_DATA,
    ALREADY_MODIFIED,
    FORBIDDEN,
    INVALID_PARAMETER,
    EXPIRED_TOKEN,
    INVALID_TOKEN

}
