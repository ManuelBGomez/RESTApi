package gal.usc.etse.grei.es.project.utilities;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Clase en la que se introducen constantes variadas.
 *
 * @author Manuel Bendaña
 */
public class Constants {
    //La url de referencia a partir de la cual se puede definir el acceso a diferentes recursos:
    public static final String URL = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
}
