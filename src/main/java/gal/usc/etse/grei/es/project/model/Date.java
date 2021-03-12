package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import gal.usc.etse.grei.es.project.model.validation.createValidation;
import gal.usc.etse.grei.es.project.model.validation.modifyValidation;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Clase que representa una fecha.
 *
 * Elaborada estructura por los profesores de la materia.
 * Etiquetas sobre los atributos hechas por Manuel Bendaña.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Date {
    @NotNull(groups = {createValidation.class}, message = "no birthday day specified")
    @Min(groups = {createValidation.class, modifyValidation.class}, value = 1, message = "must be between 1 and 31")
    @Max(groups = {createValidation.class, modifyValidation.class}, value = 31, message = "must be between 1 and 31")
    private Integer day;
    @NotNull(groups = {createValidation.class}, message = "no birthday month specified")
    @Min(groups = {createValidation.class, modifyValidation.class}, value = 1, message = "must be between 1 and 12")
    @Max(groups = {createValidation.class, modifyValidation.class}, value = 12, message = "must be between 1 and 12")
    private Integer month;
    @NotNull(groups = {createValidation.class}, message = "no birthday year specified")
    private Integer year;

    public Date() {
    }

    public Date(Integer day, Integer month, Integer year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public Integer getDay() {
        return day;
    }

    public Date setDay(Integer day) {
        this.day = day;
        return this;
    }

    public Integer getMonth() {
        return month;
    }

    public Date setMonth(Integer month) {
        this.month = month;
        return this;
    }

    public Integer getYear() {
        return year;
    }

    public Date setYear(Integer year) {
        this.year = year;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Date date = (Date) o;
        return Objects.equals(day, date.day) && Objects.equals(month, date.month) && Objects.equals(year, date.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, month, year);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Date.class.getSimpleName() + "[", "]")
                .add("day=" + day)
                .add("month=" + month)
                .add("year=" + year)
                .toString();
    }
}
