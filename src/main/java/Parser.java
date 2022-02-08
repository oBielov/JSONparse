import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import models.JsonModel;
import org.json.simple.JSONObject;

import java.util.Map;
import java.util.Set;


/**
 *
 */
public class Parser {

    @SneakyThrows
    public JsonNode parse(JSONObject before, JSONObject after) {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode leftTree = mapper.readTree(before.toJSONString());
        JsonNode rightTree = mapper.readTree(after.toJSONString());
        return leftTree;
    }

}
