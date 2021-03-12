package gal.usc.etse.grei.es.project.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.errorManagement.ErrorType;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Clase con utilidades comunes para gestionar servicios que empleen el método PATCH.
 *
 * @author Manuel Bendaña - Obtenida referencia del "Anexo 1" elaborado por los profesores de la materia.
 */

@Service
public class PatchUtils {
    //Nececsitaremos unicamente una instancia de la clase ObjectMapper:
    private final ObjectMapper mapper;

    /**
     * Constructor de la clase:
     * @param mapper Instancia de la clase ObjectMapper que nos permitirá hacer los cambios que correspondan.
     */
    @Autowired
    public PatchUtils(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Método que permite aplicar los cambios deseados sobre el objeto correspondiente.
     * @param data Los datos originales.
     * @param updates Las actualizaciones a realizar
     * @param <T> La clase del objeto a actualizar
     * @return El objeto con los cambios aplicados.
     * @throws JsonPatchException Excepción asociada a problemas al aplicar los cambios.
     */
    @SuppressWarnings("unchecked")
    public <T> T patch(T data, List<Map<String, Object>> updates) throws InvalidFormatException {
        //Traducimos las actualizaciones a una instancia de JsonPatch
        try {
            JsonPatch operations = mapper.convertValue(updates, JsonPatch.class);
            //Traducimos el objeto a un formato JSON:
            JsonNode json = mapper.convertValue(data, JsonNode.class);
            //Aplicamos las operaciones sobre el JSON:
            JsonNode updatedJson = operations.apply(json);
            //Volvemos a transformar el JSON en una instancia de usuario mediante Jackson
            return (T) mapper.convertValue(updatedJson, data.getClass());
        } catch (Exception e) {
            //Si se captura una excepción asociada a las operaciones anteriores, lanzamos la nuestra propia explicando el error:
            throw new InvalidFormatException(ErrorType.INVALID_INFO, "Invalid parameters on PATCH request. Message: "
                    + e.getMessage());
        }
    }
}
