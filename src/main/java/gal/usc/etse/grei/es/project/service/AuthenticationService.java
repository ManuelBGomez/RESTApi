package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Clase que representa el servicio de autenticación -> Obtener detalles del usuario a partir de su id.
 * Clase elaborada por los profesores de la materia. Adaptada por Manuel Bendaña
 */
@Service
public class AuthenticationService implements UserDetailsService {
    private final UserRepository users;

    /**
     * Constructor de la clase.
     * @param users Referencia al repositorio de usuarios.
     */
    @Autowired
    public AuthenticationService(UserRepository users) {
        this.users = users;
    }

    /**
     * Método que devuelve los detalles de un usuario a partir de su identificador.
     * @param username El identificador del usuario.
     * @return Los detalles del usuario correspondiente.
     * @throws UsernameNotFoundException Excepción que salta si no se encuentra al usuario en la DB.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //Buscamos el usuario correspondiente al id proporcionado en la base de datos,
        //y lanzamos si no existe una excepción adecuada:
        User user = users.findById(username).orElseThrow(() -> new UsernameNotFoundException(username));

        //Creamos usuario de spring con el builder
        return org.springframework.security.core.userdetails.User.builder()
                //Establecemos nombre de usuario
                .username(user.getEmail())
                //Establecemos contraseña del usuario
                .password(user.getPassword())
                //Establecemos la lista de roles del usuario.
                //Por convenio, los roles siempre tienen como prefijo "ROLE_"
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList(
                        String.join(",", user.getRoles())
                ))
                //Generamos el objeto del usuario a partir de los datos introducidos en el builder
                .build();
    }
}
