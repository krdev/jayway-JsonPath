package com.jayway.jsonpath;

import static com.jayway.jsonpath.JsonPath.using;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ShortNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.MappingException;

public class JacksonJsonNodeJsonProviderTest extends BaseTest {

    private static final String JSON =
            "[" +
            "{\n" +
            "   \"foo\" : \"foo0\",\n" +
            "   \"bar\" : 0,\n" +
            "   \"baz\" : true,\n" +
            "   \"gen\" : {\"eric\" : \"yepp\"}" +
            "}," +
            "{\n" +
            "   \"foo\" : \"foo1\",\n" +
            "   \"bar\" : 1,\n" +
            "   \"baz\" : true,\n" +
            "   \"gen\" : {\"eric\" : \"yepp\"}" +
            "}," +
            "{\n" +
            "   \"foo\" : \"foo2\",\n" +
            "   \"bar\" : 2,\n" +
            "   \"baz\" : true,\n" +
            "   \"gen\" : {\"eric\" : \"yepp\"}" +
            "}" +
            "]";

    @Test
    public void json_can_be_parsed() {
        ObjectNode node = using(JACKSON_JSON_NODE_CONFIGURATION).parse(JSON_DOCUMENT).read("$");
        assertThat(node.get("string-property").asText()).isEqualTo("string-value");
    }

    @Test
    public void jackson_json_set_property() {
    	JacksonJsonNodeJsonProvider jjnp= new JacksonJsonNodeJsonProvider();
    	final ObjectMapper mapper = new ObjectMapper();
    	final ArrayNode root = mapper.createArrayNode();
    	jjnp.setProperty(root, "0", "10");
    	assertThat(jjnp.getArrayIndex(root, 0).toString()).isEqualTo("\"10\"");
    }
    
    @Test
    public void jackson_json_remove_property() {
    	JacksonJsonNodeJsonProvider jjnp= new JacksonJsonNodeJsonProvider();
    	final ObjectMapper mapper = new ObjectMapper();
    	final ArrayNode root = mapper.createArrayNode();
    	jjnp.setProperty(root, "0", "10");
    	assertThat(jjnp.getArrayIndex(root, 0).toString()).isEqualTo("\"10\"");
    	jjnp.removeProperty(root, "0");
    	Assert.assertFalse(root.has("0"));
    }

    @Test
    public void jackson_json_remove_property_Map() {
    	JacksonJsonNodeJsonProvider jjnp= new JacksonJsonNodeJsonProvider();
    	final ObjectMapper mapper = new ObjectMapper();
    	final ObjectNode root = mapper.createObjectNode();
    	jjnp.setProperty(root, "0", "10");
    	assertThat(jjnp.getProperty(root, 0).toString()).isEqualTo("10");
    	jjnp.removeProperty(root, "0");
    	Assert.assertFalse(root.has("0"));
    }

    @Test(expected=JsonPathException.class)
    public void jackson_tojson_Non_json_node() {
    	JacksonJsonNodeJsonProvider jjnp= new JacksonJsonNodeJsonProvider();
    	Boolean bn = new Boolean(true);
    	jjnp.toJson(bn);
    }

    @Test
    public void jackson_get_object_mapper() {
    	final ObjectMapper mapper = new ObjectMapper();
    	JacksonJsonNodeJsonProvider jjnp= new JacksonJsonNodeJsonProvider(mapper);
    	Assert.assertEquals(jjnp.getObjectMapper(), mapper);
    }

    @Test
    public void jackson_unwrap_null() {
    	JacksonJsonNodeJsonProvider jjnp= new JacksonJsonNodeJsonProvider();
    	Assert.assertNull(jjnp.unwrap(null));
    }

    @Test(expected=JsonPathException.class)
    public void jackson_json_length_boolean() {
    	JacksonJsonNodeJsonProvider jjnp= new JacksonJsonNodeJsonProvider();
    	Boolean bn = new Boolean(true);
    	jjnp.length(bn);
    }

    @Test
    public void jackson_json_length_TextNode() {
    	JacksonJsonNodeJsonProvider jjnp= new JacksonJsonNodeJsonProvider();
    	TextNode tn = new TextNode("");
    	assertThat(jjnp.length(tn)).isEqualTo(0);
    }

    @Test
    public void jackson_json_set_property_Map() {
    	JacksonJsonNodeJsonProvider jjnp= new JacksonJsonNodeJsonProvider();
    	final ObjectMapper mapper = new ObjectMapper();
    	final ObjectNode root = mapper.createObjectNode();
    	jjnp.setProperty(root, "a", "b");
    	jjnp.setProperty(root, "x", 10);
    	jjnp.setProperty(root, "xL", 100L);
    	jjnp.setProperty(root, "xL", 100L);
    	jjnp.setProperty(root, "xs", (short)1);
    	jjnp.setProperty(root, "f", (float)1.10);
    	jjnp.setProperty(root, "bool", true);
    	assertThat(jjnp.getMapValue(root, "a")).isEqualTo("b");
    	assertThat(jjnp.getMapValue(root, "x")).isEqualTo(10);
    	assertThat(jjnp.getMapValue(root, "xL")).isEqualTo(100L);
    	ShortNode s = new ShortNode((short) 1); 
    	assertThat(jjnp.getMapValue(root, "xs").toString()).isEqualTo(s.asText());
    	Float f = new Float((float) 1.10); 
    	assertThat(jjnp.getMapValue(root, "f")).isEqualTo(f.floatValue());
    	assertThat(jjnp.getMapValue(root, "bool")).isEqualTo(true);
    }

    @Test
    public void json_can_be_parsed_readLineageRoot() {
    	ObjectNode node = (ObjectNode) using(JACKSON_JSON_NODE_CONFIGURATION_FOR_READROOT).parse(JSON_DOCUMENT).readRoot(new String[] { "$" });
    	assertThat(node.get("string-property").asText()).isEqualTo("string-value");
    }

    @Test
    public void always_return_same_object() { // Test because of Bug #211
    	DocumentContext context = using(JACKSON_JSON_NODE_CONFIGURATION).parse(JSON_DOCUMENT);
        ObjectNode node1 = context.read("$");
        ObjectNode child1 = new ObjectNode(JsonNodeFactory.instance);
        child1.put("name", "test");
        context.put("$", "child", child1);
        ObjectNode node2 = context.read("$");
        ObjectNode child2 = context.read("$.child");

        assertThat(node1).isSameAs(node2);
        assertThat(child1).isSameAs(child2);
    }

    @Test
    public void always_return_same_object_readroot() { // Test because of Bug #211
    	DocumentContext context = using(JACKSON_JSON_NODE_CONFIGURATION_FOR_READROOT).parse(JSON_DOCUMENT);
        ObjectNode node1 = (ObjectNode) context.readRoot(new String[] {"$"});
        ObjectNode child1 = new ObjectNode(JsonNodeFactory.instance);
        child1.put("name", "test");
        context.put("$", "child", child1);
        ObjectNode node2 = (ObjectNode) context.readRoot(new String[] {"$"});
        ObjectNode child2 = (ObjectNode) context.readRoot(new String[] {"$.child"});

        assertThat(node1).isSameAs(node2);
        assertThat(child1).isSameAs(child2.get("child"));
    }
    
    @Test
    public void always_return_same_object_readLineageRoot() { // Test because of Bug #211
        DocumentContext context1 = using(JACKSON_JSON_NODE_CONFIGURATION_FOR_READROOT).parse(JSON_DOCUMENT);
        DocumentContext context2 = using(JACKSON_JSON_NODE_CONFIGURATION).parse(JSON_DOCUMENT);
        ObjectNode node1 = (ObjectNode) context1.readRoot(new String[] { "$" });
        ObjectNode node2 = context2.read("$");

        Assert.assertTrue(node1.equals(node2));
    }

    @Test
    public void strings_are_unwrapped() {
        JsonNode node = using(JACKSON_JSON_NODE_CONFIGURATION).parse(JSON_DOCUMENT).read("$.string-property");
        String unwrapped = using(JACKSON_JSON_NODE_CONFIGURATION).parse(JSON_DOCUMENT).read("$.string-property", String.class);

        assertThat(unwrapped).isEqualTo("string-value");
        assertThat(unwrapped).isEqualTo(node.asText());
    }

    /*
     * invalid
     *
     * @Test public void strings_are_unwrapped_readLineageRoot() { JsonNode node =
     * using(JACKSON_JSON_NODE_CONFIGURATION).parse(JSON_DOCUMENT).readLineageRoot("$.string-property"); String unwrapped =
     * using(JACKSON_JSON_NODE_CONFIGURATION).parse(JSON_DOCUMENT).readLineageRoot("$.string-property", String.class);
     *
     * assertThat(unwrapped).isEqualTo("string-value"); assertThat(unwrapped).isEqualTo(node.asText()); }
     */

    @Test
    public void ints_are_unwrapped() {
        JsonNode node = using(JACKSON_JSON_NODE_CONFIGURATION).parse(JSON_DOCUMENT).read("$.int-max-property");
        int unwrapped = using(JACKSON_JSON_NODE_CONFIGURATION).parse(JSON_DOCUMENT).read("$.int-max-property", int.class);
        assertThat(unwrapped).isEqualTo(Integer.MAX_VALUE);
        assertThat(unwrapped).isEqualTo(node.asInt());
    }

    @Test
    public void longs_are_unwrapped() {
        JsonNode node = using(JACKSON_JSON_NODE_CONFIGURATION).parse(JSON_DOCUMENT).read("$.long-max-property");
        long unwrapped = using(JACKSON_JSON_NODE_CONFIGURATION).parse(JSON_DOCUMENT).read("$.long-max-property", long.class);

        assertThat(unwrapped).isEqualTo(Long.MAX_VALUE);
        assertThat(unwrapped).isEqualTo(node.asLong());
    }


    @Test
    public void list_of_numbers() {
        ArrayNode objs = using(JACKSON_JSON_NODE_CONFIGURATION).parse(JSON_DOCUMENT).read("$.store.book[*].display-price");

        assertThat(objs.get(0).asDouble()).isEqualTo(8.95D);
        assertThat(objs.get(1).asDouble()).isEqualTo(12.99D);
        assertThat(objs.get(2).asDouble()).isEqualTo(8.99D);
        assertThat(objs.get(3).asDouble()).isEqualTo(22.99D);
    }

    @Test
    public void list_of_numbers_lineageRoot() {
        ObjectNode root = (ObjectNode) using(JACKSON_JSON_NODE_CONFIGURATION_FOR_READROOT).parse(JSON_DOCUMENT)
                .readRoot(new String[] { "$.store.book[*].display-price" });

        JsonNode store = root.get("store");
        Assert.assertNotNull(store);
        JsonNode book = store.get("book");
        Assert.assertTrue(book.isArray());
        Assert.assertTrue(book.size() == 4);
        assertThat(book.get(0).get("display-price").asDouble()).isEqualTo(8.95D);
        assertThat(book.get(1).get("display-price").asDouble()).isEqualTo(12.99D);
        assertThat(book.get(2).get("display-price").asDouble()).isEqualTo(8.99D);
        assertThat(book.get(3).get("display-price").asDouble()).isEqualTo(22.99D);
        
    }

    @Test
    public void test_type_ref() throws IOException {
        TypeRef<List<FooBarBaz<Gen>>> typeRef = new TypeRef<List<FooBarBaz<Gen>>>() {};

        List<FooBarBaz<Gen>> list = using(JACKSON_JSON_NODE_CONFIGURATION).parse(JSON).read("$", typeRef);

        assertThat(list.get(0).gen.eric).isEqualTo("yepp");
    }

    @Test(expected = MappingException.class)
    public void test_type_ref_fail() throws IOException {
        TypeRef<List<FooBarBaz<Integer>>> typeRef = new TypeRef<List<FooBarBaz<Integer>>>() {};

        using(JACKSON_JSON_NODE_CONFIGURATION).parse(JSON).read("$", typeRef);
    }

    @Test
    // https://github.com/json-path/JsonPath/issues/364
    public void setPropertyWithPOJO() {
      DocumentContext context = JsonPath.using(JACKSON_JSON_NODE_CONFIGURATION).parse("{}");
      UUID uuid = UUID.randomUUID();
      context.put("$", "data", new Data(uuid));
      String id = context.read("$.data.id", String.class);
      assertThat(id).isEqualTo(uuid.toString());
    }
    // https://github.com/json-path/JsonPath/issues/366
    public void empty_array_check_works() throws IOException {
      String json = "[" +
          "  {" +
          "    \"name\": \"a\"," +
          "    \"groups\": [{" +
          "      \"type\": \"phase\"," +
          "      \"name\": \"alpha\"" +
          "    }, {" +
          "      \"type\": \"not_phase\"," +
          "      \"name\": \"beta\"" +
          "    }]" +
          "  }, {" +
          "    \"name\": \"b\"," +
          "    \"groups\": [{" +
          "      \"type\": \"phase\"," +
          "      \"name\": \"beta\"" +
          "    }, {" +
          "      \"type\": \"not_phase\"," +
          "      \"name\": \"alpha\"" +
          "    }]" +
          "  }" +
          "]";
      ArrayNode node = using(JACKSON_JSON_NODE_CONFIGURATION).parse(json).read("$[?(@.groups[?(@.type == 'phase' && @.name == 'alpha')] empty false)]");
      assertThat(node.size()).isEqualTo(1);
      assertThat(node.get(0).get("name").asText()).isEqualTo("a");
    }

    public static class FooBarBaz<T> {
        public T gen;
        public String foo;
        public Long bar;
        public boolean baz;
    }


    public static class Gen {
        public String eric;
    }

    public static final class Data {
      @JsonProperty("id")
      UUID id;

      @JsonCreator
      Data(@JsonProperty("id") final UUID id) {
        this.id = id;
      }
    }

}
