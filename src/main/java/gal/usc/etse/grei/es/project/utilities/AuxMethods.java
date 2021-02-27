package gal.usc.etse.grei.es.project.utilities;

import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Clase AuxMétodos: contiene métodos estáticos que resumen utilidades
 * empleadas en más de una ocasión, y que pueden ser agrupadas.
 *
 * @author Manuel Bendaña
 */
public class AuxMethods {

    /**
     * Método que permite obtener los criterios de ordenación adecuados
     * a partir de una lista pasada vía url.
     *
     * @param sort Lista de criterios de ordenación.
     * @return La lista que se podrá facilitar a los métodos de búsqueda para
     *      poder ordenar correctamente los resultados en base a los criterios pasados.
     */
    public static List<Sort.Order> getSortCriteria(List<String> sort){
        //Elaboramos la lista que se devolverá como resultado:
        List<Sort.Order> criteria = sort.stream().map(string -> {
            //Para determinar si la ordenación es ascendente o descendente, se considera el símbolo inicial del string.
            if(string.startsWith("+")){
                //Si es "+", entonces la ordenación es ascendente.
                return Sort.Order.asc(string.substring(1));
            } else if (string.startsWith("-")) {
                //Si es "-", entonces la ordenación es descendente.
                return Sort.Order.desc(string.substring(1));
                //En otro caso, no se devuelve nada.
            } else return null;
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        //Se devuelven finalmente esos criterios:
        return criteria;
    }
}
