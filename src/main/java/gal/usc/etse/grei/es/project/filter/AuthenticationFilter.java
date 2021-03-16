package gal.usc.etse.grei.es.project.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filtro para la autenticación.
 * Clase elaborada por los profesores de la materia. Adaptada por Manuel Bendaña
 */
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager manager;
    private final Key key;

    // Establecemos duración para los tokens
    private static long TOKEN_DURATION = Duration.ofMinutes(60).toMillis();

    /**
     * Constructor de la clase
     * @param manager
     * @param key
     */
    public AuthenticationFilter(AuthenticationManager manager, Key key){
        this.manager = manager;
        this.key = key;
    }

    /**
     * Método que intenta autenticar al usuario a partir de la llamada HTTP
     * @param request Solicitud HTTP
     * @param response Respuesta HTTP
     * @return Datos de la autenticación
     * @throws AuthenticationException Excepción lanzada en caso de problemas
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            //Recuperamos el objeto JSON de la request HTTP.
            JsonNode credentials = new ObjectMapper().readValue(request.getInputStream(), JsonNode.class);

            //Intentamos autenticarnos con las credenciales.
            return manager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentials.get("email").textValue(),
                            credentials.get("password").textValue()
                    )
            );
        }catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    /**
     * Método llamado cuando la autenticación del método anterior es satisfactoria
     * @param request Solicitud HTTP
     * @param response Respuesta HTTP
     * @param chain Cadena de filtros
     * @param authResult Reultados de la autenticación.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        //Almacenamos tiempo actual:
        long now = System.currentTimeMillis();

        //Recuperamos la lista de roles asignados al usuario y los concatenamos en un string separado por comas
        String authorities = authResult.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        //Se crea el token JWT creando el builder:
        JwtBuilder tokenBuilder = Jwts.builder()
                //Establecemos como "propietario" del token al usuario que realizó login:
                .setSubject(((User)authResult.getPrincipal()).getUsername())
                //Establecemos la fecha de emisión del token:
                .setIssuedAt(new Date(now))
                //Establecemos la fecha máxima de validez del token:
                .setExpiration(new Date(now + TOKEN_DURATION))
                //Añadimos un atributo más al token con los roles del usuario:
                .claim("roles", authorities)
                //Firmamos el token con nuestra clave secreta:
                .signWith(key);

        //Añadimos el token a la respuesta en la cabecera "Authentication"
        response.addHeader("Authentication", String.format("Bearer %s", tokenBuilder.compact()));
    }

}
