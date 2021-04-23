package gal.usc.etse.grei.es.project.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Tipo enum Status, representa el estado de una película.
 */
@Schema(description = "Possible status for a film",
        allowableValues = {"RUMORED", "PLANNED", "PRODUCTION", "POSTPRODUCTION", "RELEASED", "CANCELLED"})
public enum Status {
    RUMORED, PLANNED, PRODUCTION, POSTPRODUCTION, RELEASED, CANCELLED;

    public static Status of(String value) {
        switch (value) {
            case "RUMORED": return RUMORED;
            case "PLANNED": return PLANNED;
            case "IN PRODUCTION": return PRODUCTION;
            case "POST PRODUCTION": return POSTPRODUCTION;
            case "RELEASED": return RELEASED;
            case "CANCELED": return CANCELLED;
            default: throw new IllegalStateException("Unexpected value: " + value);
        }
    }
}
