package gal.usc.etse.grei.es.project.model;

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
