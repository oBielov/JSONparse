
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class Parser {

    private final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    public JsonNode parse(JsonNode before, JsonNode after) {
        ObjectNode node = mapper.createObjectNode();
        node.put("meta", compareMeta(before, after));
        node.put("candidates", compareCandidates(before, after));
        return node;
    }

    @SneakyThrows
    private ArrayNode compareMeta(JsonNode before, JsonNode after){
        Map<String, Object> beforeMeta = mapper.convertValue(before.get("meta")
                , new TypeReference<Map<String, Object>>(){});
        Map<String, Object> afterMeta = mapper.convertValue(after.get("meta")
                , new TypeReference<Map<String, Object>>(){});
        ArrayNode arrayNode = mapper.createArrayNode();

        for(String key : beforeMeta.keySet()){
            Object a = beforeMeta.get(key);
            Object b = afterMeta.get(key);
            if(!a.equals(b)){
                ObjectNode object = mapper.createObjectNode();
                object.put("field", key)
                        .put("before", a.toString())
                        .put("after", b.toString());
                arrayNode.add(object);
            }
        }
        return arrayNode;
    }

    @SneakyThrows
    private JsonNode compareCandidates(JsonNode before, JsonNode after){
        ObjectNode node = mapper.createObjectNode();
        node.put("edited", compareCommon(before, after));
        node.put("added", findAdded(before, after));
        node.put("removed", findRemoved(before, after));
        return node;
    }

    @SneakyThrows
    private JsonNode findAdded(JsonNode before, JsonNode after){
        ArrayNode array = mapper.createArrayNode();
        JsonNode beforeC = mapper.readTree(before.get("candidates").toPrettyString());
        JsonNode afterC = mapper.readTree(after.get("candidates").toPrettyString());
        List<JsonNode> beforeIds = beforeC.findValues("id");
        List<JsonNode> afterIds = afterC.findValues("id");
        List<JsonNode> added = afterIds.stream()
                .filter(id -> !beforeIds.contains(id))
                .collect(Collectors.toList());
        for(JsonNode node : added){
            ObjectNode obj = mapper.createObjectNode();
            obj.put("id", node.toPrettyString());
            array.add(obj);
        }
        return array;
    }

    @SneakyThrows
    private JsonNode findRemoved(JsonNode before, JsonNode after){
        ArrayNode array = mapper.createArrayNode();
        JsonNode beforeC = mapper.readTree(before.get("candidates").toPrettyString());
        JsonNode afterC = mapper.readTree(after.get("candidates").toPrettyString());
        List<JsonNode> beforeIds = beforeC.findValues("id");
        List<JsonNode> afterIds = afterC.findValues("id");
        List<JsonNode> removed = beforeIds.stream()
                .filter(id -> !afterIds.contains(id))
                .collect(Collectors.toList());
        for(JsonNode node : removed){
            ObjectNode obj = mapper.createObjectNode();
            obj.put("id", node.toPrettyString());
            array.add(obj);
        }
        return array;
    }

    @SneakyThrows
    private JsonNode compareCommon(JsonNode before, JsonNode after) {
        ArrayNode array = mapper.createArrayNode();
        JsonNode beforeC = mapper.readTree(before.get("candidates").toPrettyString());
        JsonNode afterC = mapper.readTree(after.get("candidates").toPrettyString());
        List<JsonNode> beforeIds = beforeC.findValues("id");
        List<JsonNode> afterIds = afterC.findValues("id");
        List<String> common = beforeIds.stream()
                .filter(afterIds::contains)
                .map(JsonNode::toString)
                .collect(Collectors.toList());
        List<JsonNode> beforeList = new ArrayList<>();
        List<JsonNode> afterList = new ArrayList<>();
        for (String s : common){
            for (JsonNode n : beforeC){
                if(n.findValue("id").toString().equals(s)){
                    beforeList.add(n);
                }
            }
            for (JsonNode n : afterC){
                if(n.findValue("id").toString().equals(s)){
                    afterList.add(n);
                }
            }
        }
        for(int i = 0; i < beforeList.size(); i++){
            JsonNode b = beforeList.get(i);
            JsonNode a = afterList.get(i);
            if(!b.equals(a)){
                ObjectNode obj = mapper.createObjectNode();
                obj.put("id", b.findValue("id").toPrettyString());
                array.add(obj);
            }
        }
        return array;
    }


}
