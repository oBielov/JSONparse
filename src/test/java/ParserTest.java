import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import static  org.junit.Assert.*;

import java.io.FileReader;

/**
 *
 */
public class ParserTest {

    private final Parser parser = new Parser();
    private final ObjectMapper mapper = new ObjectMapper();
    JsonNode before;
    JsonNode after;

    @Before
    @SneakyThrows
    public void setup() {
        // TODO Load in test data from before.json and after.json

        before = mapper.readTree(new FileReader("src/test/resources/before.json"));
        after = mapper.readTree(new FileReader("src/test/resources/after.json"));
    }

    @Test
    public void parseJsonAndFindDifferencesInMeta(){

    }

    // TODO Define tests here

}
