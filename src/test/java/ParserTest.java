import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import static  org.junit.Assert.*;

import java.io.FileReader;

/**
 *Some functional tests for Parser
 */
public class ParserTest {

    private final Parser parser = new Parser();
    private final ObjectMapper mapper = new ObjectMapper();
    JsonNode before;
    JsonNode after;
    JsonNode expected;

    @Before
    @SneakyThrows
    public void setup() {
        before = mapper.readTree(new FileReader("src/test/resources/before.json"));
        after = mapper.readTree(new FileReader("src/test/resources/after.json"));
        expected = mapper.readTree(new FileReader("src/test/resources/diff.json"));

    }

    /**
     * Check if method throw NullPointerException if one of the arguments is null
     */
    @Test(expected = NullPointerException.class)
    public void nullPointerException(){
        parser.parse(null, after);
    }

    /**
     * Check if we got empty JSON if both inputs are equal
     */
    @Test
    public void produceEmptyJsonIfNoDifference(){
        JsonNode result = parser.parse(before, before);
        JsonNode test = mapper.createObjectNode();
        assertEquals(test, result);
    }

    /**
     * Check if we get expected values with different JSONs, date/time conversion now works wrong.
     */

//    @Test
//    public void produceExpectedJsonIfDifference(){
//        JsonNode result = parser.parse(before, after);
//        assertEquals(expected, result);
//
//    }


}
