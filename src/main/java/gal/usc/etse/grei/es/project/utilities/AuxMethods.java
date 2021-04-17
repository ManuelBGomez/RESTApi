package gal.usc.etse.grei.es.project.utilities;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
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
        //Antes de nada, si se indicase un criterio de ordenación por fecha, se consideraria por los tres campos:
        //Si no hacemos esto, la ordenación sería incorrecta (ordenaría primero por día).
        List<String> sortAux = new ArrayList<>(sort);
        AuxMethods.sortDate(sortAux, "+releaseDate");
        AuxMethods.sortDate(sortAux, "-releaseDate");
        AuxMethods.sortDate(sortAux, "+birthday");
        AuxMethods.sortDate(sortAux, "-birthday");
        AuxMethods.sortDate(sortAux, "+since");
        AuxMethods.sortDate(sortAux, "-since");

        //Elaboramos la lista que se devolverá como resultado:
        return sortAux.stream().map(string -> {
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
    }

    /**
     * Método que permite hacer una ordenación por fecha, cambiando el campo que contiene la fecha
     * por su descomposición en año, mes y día.
     * @param sort Lista de criterios de ordenación.
     * @param nameDate El nombre del criterio que puede suponer una fecha.
     */
    public static void sortDate(List<String> sort, String nameDate){
        //Comprobamos que la lista contenga ese valor:
        if(sort.contains(nameDate)){
            //Si lo contiene, se borra:
            sort.remove(nameDate);
            //Se añaden tres criterios con el mismo nombre pero terminando en year, month y day:
            sort.add(nameDate + ".year");
            sort.add(nameDate + ".month");
            sort.add(nameDate + ".day");
        }
    }
}
