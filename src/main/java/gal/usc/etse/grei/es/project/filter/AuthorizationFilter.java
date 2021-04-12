package gal.usc.etse.grei.es.project.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import gal.usc.etse.grei.es.project.errorManagement.ErrorObject;
import gal.usc.etse.grei.es.project.errorManagement.ErrorType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.util.List;


/**
 * Filtro para el control de acceso.
 * Clase elaborada por los profesores de la materia. Adaptada por Manuel Bendaña
 */
public class AuthorizationFilter extends BasicAuthenticationFilter {
    private final Key key;

    /**
     * Constructor de la clase
     * @param manager Instancia del authentication manager
     * @param key La clave usada
     */
    public AuthorizationFilter(AuthenticationManager manager, Key key){
        super(manager);
        this.key = key;
    }

    /**
     * Método ejecutado en la comprobación del control del acceso
     * @param request La solicitud http
     * @param response La respuesta http
     * @param chain cadena de filtros aplicados
     * @throws IOException Excepción que se puede lanzar desde este método
     * @throws ServletException Excepción que se puede lanzar desde este método
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try{
            //Leemos el token de la cabecera con etiqueta authorization:
            String header = request.getHeader("Authorization");

            //Si el token no es correcto o no empieza con el string "Bearer" (no es token JWT), seguimos
            //con la cadena de filtros: no hacemos más en este.
            if(header == null || !header.startsWith("Bearer")){
                chain.doFilter(request, response);
                return;
            }

            //Si el tocken es JWT, comprobamos validez del mismo:
            UsernamePasswordAuthenticationToken authentication = getAuthentication(header);

            //Si era válido, lo establecemos en el contexto de seguridad de Spring para poder emplearlo
            //en nuestros servicios
            SecurityContextHolder.getContext().setAuthentication(authentication);

            //Se sigue con la cadena de filtros.
            chain.doFilter(request, response);
        } catch(ExpiredJwtException e){
            //Si el token expira, se devuelve el error adecuado.
            response.setStatus(419);
            response.getOutputStream().println(new ObjectMapper().writeValueAsString(new ErrorObject(ErrorType.EXPIRED_TOKEN,
                    "Authentication timed out.")));
        } catch(MalformedJwtException | SignatureException e){
            //Si el token no es correcto, se devuelve también error (en este caso unhauthorized):
            response.setStatus(401);
            response.getOutputStream().println(new ObjectMapper().writeValueAsString(new ErrorObject(ErrorType.INVALID_TOKEN,
                    "The provided token is not valid.")));
        }
    }

    /**
     * Método que permite recuperar los datos de autenticación a partir del token.
     * @param token El token pasado por el usuario.
     * @return Los datos de autenticación.
     * @throws ExpiredJwtException Excepción lanzada si el token expira.
     */
    private UsernamePasswordAuthenticationToken getAuthentication(String token) throws ExpiredJwtException {
        //Creamos un parser para el token con la clave de firmado de la aplicación
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                //Parseamos el body del token
                .parseClaimsJws(token.replace("Bearer", "").trim())
                .getBody();

        //Recuperamos el nombre del propietario del token:
        String user = claims.getSubject();

        //Obtenemos el listado de roles del usuario:
        List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(claims.get("roles").toString());

        //Devolvemos el token interno de Spring, que será añadido en el contexto:
        return user == null ? null : new UsernamePasswordAuthenticationToken(user, token, authorities);
    }

}
