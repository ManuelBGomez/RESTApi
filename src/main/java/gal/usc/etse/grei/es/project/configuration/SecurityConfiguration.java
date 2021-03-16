package gal.usc.etse.grei.es.project.configuration;

import gal.usc.etse.grei.es.project.filter.AuthenticationFilter;
import gal.usc.etse.grei.es.project.filter.AuthorizationFilter;
import gal.usc.etse.grei.es.project.service.AuthenticationService;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyUtils;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

import java.security.Key;
import java.util.*;

/**
 * Clase para la configuración que permite ajustar el funcionamiento de la seguridad.
 * Clase elaborada por los profesores de la materia. Adaptada por Manuel Bendaña
 */
@Configuration
//Con esta anotación activamos la seguridad para la aplicación.
@EnableWebSecurity
//Activamos el procesamento de etiquetas @Preauthorize y @Postauthorize
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final AuthenticationService auth;
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    /**
     * Constructor de la clase
     * @param auth Referencia al servicio de autenticación:
     */
    @Autowired
    public SecurityConfiguration(AuthenticationService auth) {
        this.auth = auth;
    }

    /**
     * Método que permite establecer el servicio que se empleará para obtener los
     * detalles del usuario: el PasswordEncoder que emplearemos
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(this.auth).passwordEncoder(passwordEncoder());
    }

    /**
     * Método en el cual se establece la jerarquía de roles.
     * @param web
     */
    @Override
    public void configure(WebSecurity web) {
        DefaultWebSecurityExpressionHandler handler = new DefaultWebSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy());
        web.expressionHandler(handler);
    }

    /**
     * Método que permite deshabilitar la protección contra ataques CRSF
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                //Indicamos que por defecto permitimos el acceso de cualquiera (any) a todos los servizos
                .authorizeRequests().anyRequest().permitAll()
                .and()
                //Añadimos nuestros filtros (authentication y authorization) a la cadena de filtros de las llamadas.
                .addFilter(new AuthenticationFilter(authenticationManager(), tokenSignKey()))
                .addFilter(new AuthorizationFilter(authenticationManager(), tokenSignKey()))
                //Especificamos que queremos sesións sin estado (pues REST, por definición, carece de estado)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    /**
     * Método que permite crear una instancia del algoritmo BCrypt para emplear como
     * encoder de contraseñas.
     * @return El encoder creado.
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * Método que permite establecer la jerarquía de roles.
     * @return La jerarquía de roles creada.
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        Map<String, List<String>> roles = new HashMap<>();
        //Definimos la jerarquía de roles en un map.
        //Los valores representan los roles incluidos en el rol especificado como clave
        roles.put("ROLE_ADMIN", Collections.singletonList("ROLE_USER"));

        //Creamos nuestra jerarquía de roles a partir del map definido previamente
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(RoleHierarchyUtils.roleHierarchyFromMap(roles));

        //La devolvemos:
        return hierarchy;
    }

    /**
     * Método que permite generar una clave de firmado aleatoria para el algoritmo SHA512.
     * @return La clave generada.
     */
    @Bean
    public Key tokenSignKey() {
        return SecurityConfiguration.key;
    }
}
