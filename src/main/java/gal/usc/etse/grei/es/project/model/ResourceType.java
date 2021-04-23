package gal.usc.etse.grei.es.project.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Tipo enumerado que representa el tipo de recurso.
 */
@Schema(description = "Possible types for a resource",
        allowableValues = {"POSTER", "BACKDROP", "TRAILER", "NETFLIX", "AMAZON_PRIME", "DISNEY_PLUS",
                           "ITUNES", "HBO", "YOUTUBE", "GOOGLE_PLAY", "TORRENT"})
public enum ResourceType {
    POSTER,
    BACKDROP,
    TRAILER,
    NETFLIX,
    AMAZON_PRIME,
    DISNEY_PLUS,
    ITUNES,
    HBO,
    YOUTUBE,
    GOOGLE_PLAY,
    TORRENT
}
