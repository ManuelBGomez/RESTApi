package gal.usc.etse.grei.es;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal, desde la que arranca la aplicación.
 */
@SpringBootApplication
public class Application {
    /**
     * Método main
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
