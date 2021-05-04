package gal.usc.etse.grei.es.project.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Clase que permite habilitar el CORS en backend.
 *
 * @author Manuel Bendaña.
 */
@Configuration
@EnableWebMvc
public class EnableCorsConfiguration extends WebMvcConfigurerAdapter {

    /**
     * Método que permite habilitar CORS
     * @param registry CorsRegistry.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedHeaders("*").exposedHeaders("*");
    }
}