package com.jayway.jsonpath;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JacksonTest extends BaseTest {

    @Test
    public void an_object_can_be_mapped_to_pojo() {

        String json = "{\n" +
                "   \"foo\" : \"foo\",\n" +
                "   \"bar\" : 10,\n" +
                "   \"baz\" : true\n" +
                "}";


        FooBarBaz fooBarBaz = JsonPath.using(JACKSON_CONFIGURATION).parse(json).read("$", FooBarBaz.class);

        assertThat(fooBarBaz.foo).isEqualTo("foo");
        assertThat(fooBarBaz.bar).isEqualTo(10L);
        assertThat(fooBarBaz.baz).isEqualTo(true);

    }

    public static class FooBarBaz {
        public String foo;
        public Long bar;
        public boolean baz;
    }

    @Test
    public void jackson_converts_dates() {

        Date now = new Date();

        Object json = singletonMap("date_as_long", now.getTime());

        Date date = JsonPath.using(JACKSON_CONFIGURATION).parse(json).read("$['date_as_long']", Date.class);

        assertThat(date).isEqualTo(now);
    }

    @Test
    // https://github.com/jayway/JsonPath/issues/275
    public void single_quotes_work_with_in_filter() {
        final String jsonArray = "[{\"foo\": \"bar\"}, {\"foo\": \"baz\"}]";
        final Object readFromSingleQuote = JsonPath.using(JACKSON_CONFIGURATION).parse(jsonArray).read("$.[?(@.foo in ['bar'])].foo");
        final Object readFromDoubleQuote = JsonPath.using(JACKSON_CONFIGURATION).parse(jsonArray).read("$.[?(@.foo in [\"bar\"])].foo");
        assertThat(readFromSingleQuote).isEqualTo(readFromDoubleQuote);

    }

    @Test
    public void testShallowCopy() throws JsonProcessingException {
        ObjectMapper m = new ObjectMapper();
        ObjectNode n1 = m.createObjectNode();
        n1.put("myInt", 23);
        ObjectNode n2 = m.createObjectNode();
        n2.put("name", "adam");
        n2.put("age", "33");
        n1.put("person", n2);

        System.out.println(" -- " + m.writeValueAsString(n1));
        ObjectNode n3 = n1.deepCopy();
        System.out.println(" -- " + m.writeValueAsString(n3));
        ((ObjectNode) n3.get("person")).retain();
        System.out.println(" -- " + m.writeValueAsString(n3));

    }

}
