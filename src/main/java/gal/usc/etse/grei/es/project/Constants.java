package gal.usc.etse.grei.es.project;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class Constants {
    public static final String URL = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
}
