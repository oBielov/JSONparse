
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON parser to compare two JSON objects and produce new with differences.
 * Returns empty JSON if both inputs are equal.
 */
public class Parser {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Main method. Executes {@link #compareMeta(JsonNode, JsonNode)} and {@link #compareCandidates(JsonNode, JsonNode)}
     * @param before  before JsonNode
     * @param after  after JsonNode
     * @return  comparing result. Empty Json if inputs are equal
     */
    @SneakyThrows
    public JsonNode parse(JsonNode before, JsonNode after) {
        ObjectNode node = mapper.createObjectNode();
        if(before.equals(after)){
            return node;
        }
        node.put("meta", compareMeta(before, after));
        node.put("candidates", compareCandidates(before, after));
        return node;
    }

    /**
     * Method to compare "meta" block of inputs
     * @param before before JsonNode
     * @param after after JsonNode
     * @return Array of Jsons with differences in pattern "field: name of field , before: old data, after: new data"
     */
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
            if("startTime".equals(key) || "endTime".equals(key)){
                a = convertDate(a.toString());
                b = convertDate(b.toString());
            }
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

    /**
     * Method to compare "candidates" block. Uses {@link #compareCommon(JsonNode, JsonNode)},
     * {@link #findAdded(JsonNode, JsonNode)}, {@link #findRemoved(JsonNode, JsonNode)}
     * @param before before JsonNode
     * @param after afterJsonNode
     * @return Array of Jsons with differences in pattern: "edited": [array of objects that were edited],
     * "added"" [id's of added objects],
     * "removed": [id's of removed objects]
     *
     */
    @SneakyThrows
    private JsonNode compareCandidates(JsonNode before, JsonNode after){
        ObjectNode node = mapper.createObjectNode();
        node.put("edited", compareCommon(before, after));
        node.put("added", findAdded(before, after));
        node.put("removed", findRemoved(before, after));
        return node;
    }

    /**
     * Method to find id's of added objects
     * @param before before JsonNode
     * @param after after JsonNode
     * @return Array of added id's
     */
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

    /**
     * Method to find id's of removed objects
     * @param before before JsonNode
     * @param after after JsonNode
     * @return Array of removed id's
     */
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

    /**
     * Method to compare fields common for both objects
     * @param before before JsonNode
     * @param after after JsonNode
     * @return Array of id's for objects with modified fields
     */
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

    /**
     * Method to convert date from UTC to CEST/CET format. Now works inappropriate: does not offset hours in output string
     * @param date String value of UTC date/time
     * @return String of input UTC date/time converted to CEST/CET format.
     */
    private String convertDate(String date){
        DateTimeFormatter input = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DateTimeFormatter output = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                .appendOffset("+HH", "+00")
                .toFormatter();
        ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.parse(date, input), ZoneId.of("Europe/Oslo"));
        return zonedDateTime.format(output);
    }


}
