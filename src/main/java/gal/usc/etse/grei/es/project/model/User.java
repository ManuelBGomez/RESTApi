package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import gal.usc.etse.grei.es.project.model.validation.createValidation;
import gal.usc.etse.grei.es.project.model.validation.friendValidation;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Clase que representa a los usuarios.
 *
 * Elaborada estructura por los profesores de la materia.
 * Etiquetas sobre los atributos hechas por Manuel Bendaña.
 */
@Document(collection = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    @Id
    @NotEmpty(groups = {createValidation.class, friendValidation.class}, message = "no email specified")
    @Email(groups = {createValidation.class, friendValidation.class}, message = "incorrect format")
    private String email;
    @NotEmpty(groups = {createValidation.class, friendValidation.class}, message = "no name specified")
    private String name;
    private String country;
    private String picture;
    @Valid
    @NotNull(groups = {createValidation.class}, message = "no birthday specified")
    private Date birthday;
    @NotEmpty(groups = {createValidation.class}, message = "no password specified")
    private String password;
    @NotEmpty(groups = {createValidation.class}, message = "no roles specified")
    private List<String> roles;

    public User() {}
    public User(String email, String name, String country, String picture, Date birthday, String password, List<String> roles) {
        this.email = email;
        this.name = name;
        this.country = country;
        this.picture = picture;
        this.birthday = birthday;
        this.password = password;
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }
    public String getName() {
        return name;
    }
    public String getCountry() {
        return country;
    }
    public String getPicture() {
        return picture;
    }
    public Date getBirthday() {
        return birthday;
    }
    public String getPassword() {
        return password;
    }
    public List<String> getRoles() {
        return roles;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }
    public User setName(String name) {
        this.name = name;
        return this;
    }
    public User setCountry(String country) {
        this.country = country;
        return this;
    }
    public User setPicture(String picture) {
        this.picture = picture;
        return this;
    }
    public User setBirthday(Date birthday) {
        this.birthday = birthday;
        return this;
    }
    public User setPassword(String password) {
        this.password = password;
        return this;
    }
    public User setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email) && Objects.equals(name, user.name) && Objects.equals(country, user.country) && Objects.equals(picture, user.picture) && Objects.equals(birthday, user.birthday) && Objects.equals(password, user.password) && Objects.equals(roles, user.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, name, country, picture, birthday, password, roles);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", User.class.getSimpleName() + "[", "]")
                .add("email='" + email + "'")
                .add("name='" + name + "'")
                .add("country='" + country + "'")
                .add("picture='" + picture + "'")
                .add("birthday=" + birthday)
                .add("password=" + password)
                .add("roles=" + roles)
                .toString();
    }
}
