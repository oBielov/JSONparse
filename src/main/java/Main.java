
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        Object obj1 = parser.parse(new FileReader("src/test/resources/before.json"));
        Object obj2 = parser.parse(new FileReader("src/test/resources/after.json"));
        JSONObject jsonObject1 = (JSONObject) obj1;
        JSONObject jsonObject2 = (JSONObject) obj2;
        Parser parse = new Parser();
        String result = parse.parse(jsonObject1, jsonObject2).toPrettyString();
        System.out.println(result);

    }


}
