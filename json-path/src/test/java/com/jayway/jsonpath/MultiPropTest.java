package com.jayway.jsonpath;

import static com.jayway.jsonpath.JsonPath.using;
import static com.jayway.jsonpath.TestUtils.assertEvaluationThrows;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.jayway.jsonpath.internal.DefaultsImpl;
import com.jayway.jsonpath.spi.json.JsonProvider;

public class MultiPropTest {

    @Test
    public void multi_prop_can_be_read_from_root() {

        Map<String, Object> model = new HashMap<String, Object>(){{
            put("a", "a-val");
            put("b", "b-val");
            put("c", "c-val");
        }};

        Configuration conf = Configuration.builder().jsonProvider(DefaultsImpl.INSTANCE.jsonProvider()).options(DefaultsImpl.INSTANCE.options())
                .build();

        assertThat(using(conf).parse(model).read("$['a', 'b']", Map.class))
                .containsEntry("a", "a-val")
                .containsEntry("b", "b-val");

        // current semantics: absent props are skipped
        assertThat(using(conf).parse(model).read("$['a', 'd']", Map.class))
                .hasSize(1).containsEntry("a", "a-val");
    }


    @Test
    public void multi_prop_can_be_read_from_root_readRoot() {

        Map<String, Object> model = new HashMap<String, Object>() {
            {
                put("a", "a-val");
                put("b", "b-val");
                put("c", "c-val");
            }
        };

        Configuration conf = Configuration.builder().jsonProvider(DefaultsImpl.INSTANCE.jsonProvider()).options(DefaultsImpl.INSTANCE.options())
                .build();

        Object ret1 = using(conf).parse(model).readRoot(new String[] { "$['a', 'b']" });

        Assert.assertNotNull(ret1);
        Assert.assertEquals(conf.jsonProvider().getProperty(ret1, "a"), "a-val");
        Assert.assertEquals(conf.jsonProvider().getProperty(ret1, "b"), "b-val");

        Object ret2 = using(conf).parse(model).readRoot(new String[] { "$['a', 'd']" });
        Assert.assertNotNull(ret2);
        Assert.assertEquals(conf.jsonProvider().getProperty(ret2, "a"), "a-val");
        Assert.assertNull(conf.jsonProvider().getProperty(ret2, "d"));
    }

    @Test
    public void multi_props_can_be_defaulted_to_null() {

        Map<String, Object> model = new HashMap<String, Object>(){{
            put("a", "a-val");
            put("b", "b-val");
            put("c", "c-val");
        }};

        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

        assertThat(using(conf).parse(model).read("$['a', 'd']", Map.class))
                .containsEntry("a", "a-val")
                .containsEntry("d", null);
    }

    @Test(expected = PathNotFoundException.class)
    public void multi_props_can_be_required() {

        Map<String, Object> model = new HashMap<String, Object>(){{
            put("a", "a-val");
            put("b", "b-val");
            put("c", "c-val");
        }};

        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.REQUIRE_PROPERTIES);

        using(conf).parse(model).read("$['a', 'x']", Map.class);
    }

    @Test(expected = PathNotFoundException.class)
    public void multi_props_can_be_required_readRoot() {

        Map<String, Object> model = new HashMap<String, Object>() {
            {
                put("a", "a-val");
                put("b", "b-val");
                put("c", "c-val");
            }
        };

        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.REQUIRE_PROPERTIES);

        Object ret = using(conf).parse(model).read("$['a', 'x']");
        // using(conf).parse(model).read("$['a', 'x']", Map.class);
    }

    @Test
    public void multi_props_can_be_non_leafs() {
        Object result = JsonPath.parse("{\"a\": {\"v\": 5}, \"b\": {\"v\": 4}, \"c\": {\"v\": 1}}").read(
                "$['a', 'c'].v");
        assertThat(result).asList().containsOnly(5, 1);
    }

    @Test
    public void nonexistent_non_leaf_multi_props_ignored() {
        Object result = JsonPath.parse("{\"a\": {\"v\": 5}, \"b\": {\"v\": 4}, \"c\": {\"v\": 1}}").read(
                "$['d', 'a', 'c', 'm'].v");
        assertThat(result).asList().containsOnly(5, 1);
    }

    @Test
    public void multi_props_with_post_filter() {
        Object result = JsonPath.parse("{\"a\": {\"v\": 5}, \"b\": {\"v\": 4}, \"c\": {\"v\": 1, \"flag\": true}}").read(
                "$['a', 'c'][?(@.flag)].v");
        assertThat(result).asList().containsOnly(1);
    }

    @Test
    public void deep_scan_does_not_affect_non_leaf_multi_props() {
        // deep scan + multiprop is quite redundant scenario, but it's not forbidden, so we'd better check
        final String json = "{\"v\": [[{}, 1, {\"a\": {\"v\": 5}, \"b\": {\"v\": 4}, \"c\": {\"v\": 1, \"flag\": true}}]]}";
        Object result = JsonPath.parse(json).read("$..['a', 'c'].v");
        assertThat(result).asList().containsOnly(5, 1);

        result = JsonPath.parse(json).read("$..['a', 'c'][?(@.flag)].v");
        assertThat(result).asList().containsOnly(1);
    }

    /**
     * Test that the array index property is maintained.
     */
    @Test
    public void test_array_readRoot() {
        final String json = "{\"v\":[{\"a\":\"a-val\"},{\"b\":\"b-val\"}]}";
        Object result = JsonPath.parse(json).readRoot(new String[] { "$.v[1]" });

        Configuration conf = Configuration.builder().jsonProvider(DefaultsImpl.INSTANCE.jsonProvider()).options(DefaultsImpl.INSTANCE.options())
                .build();

        Object arr = conf.jsonProvider().getMapValue(result, "v");
        Assert.assertTrue(conf.jsonProvider().isArray(arr));
        Object elem0 = conf.jsonProvider().getArrayIndex(arr, 0);
        Object elem1 = conf.jsonProvider().getArrayIndex(arr, 1);

        Object emptyObj = conf.jsonProvider().parse("{}");
        Assert.assertEquals(elem0, emptyObj);

        Object objectB = conf.jsonProvider().parse("{\"b\":\"b-val\"}");
        Assert.assertEquals(elem1, objectB);
    }

    @Test
    public void deep_scan_does_not_affect_non_leaf_multi_props_readRoot() {
        // deep scan + multiprop is quite redundant scenario, but it's not forbidden, so we'd better check
        final String json = "{\"v\": [[{}, 1, {\"a\": {\"v\": 5}, \"b\": {\"v\": 4}, \"c\": {\"v\": 1, \"flag\": true}}]]}";
        Object result = JsonPath.parse(json).readRoot(new String[] { "$..['a', 'c'].v" });

        // conf.jsonProvider().getMapValue(result, "a")
        // assertThat(result).asList().containsOnly(5, 1);

        result = JsonPath.parse(json).readRoot(new String[] { "$..v" });
        Assert.assertNotNull(result);

        result = JsonPath.parse(json).readRoot(new String[] { "$..['a', 'c'][?(@.flag)].v" });
        // KR - TODO fix this
        // assertThat(result).asList().containsOnly(1);
    }

    @Test
    public void multi_props_can_be_in_the_middle() {
        final String json = "{\"x\": [null, {\"a\": {\"v\": 5}, \"b\": {\"v\": 4}, \"c\": {\"v\": 1}}]}";
        Object result = JsonPath.parse(json).read("$.x[1]['a', 'c'].v");
        assertThat(result).asList().containsOnly(5, 1);
        result = JsonPath.parse(json).read("$.x[*]['a', 'c'].v");
        assertThat(result).asList().containsOnly(5, 1);
        result = JsonPath.parse(json).read("$[*][*]['a', 'c'].v");
        assertThat(result).asList().containsOnly(5, 1);

        result = JsonPath.parse(json).read("$.x[1]['d', 'a', 'c', 'm'].v");
        assertThat(result).asList().containsOnly(5, 1);
        result = JsonPath.parse(json).read("$.x[*]['d', 'a', 'c', 'm'].v");
        assertThat(result).asList().containsOnly(5, 1);
    }

    @Test
    public void non_leaf_multi_props_can_be_required() {
        final Configuration conf = Configuration.defaultConfiguration().addOptions(Option.REQUIRE_PROPERTIES);
        final String json = "{\"a\": {\"v\": 5}, \"b\": {\"v\": 4}, \"c\": {\"v\": 1}}";

        assertThat(using(conf).parse(json).read("$['a', 'c'].v")).asList().containsOnly(5, 1);
        assertEvaluationThrows(json, "$['d', 'a', 'c', 'm'].v", PathNotFoundException.class, conf);
    }

    @Test
    public void testIosShortPayloadBasicFlightReservation() throws IOException {
        final InputStream jsonStream = this.getClass().getClassLoader().getResourceAsStream("basicFlightReservation.json");
        final String schemaJson = IOUtils.toString(jsonStream, StandardCharsets.UTF_8);
        final String jsonPathArr[] = new String[] { "$.subReservation[0].reservationFor.@type", "$.subReservation[0].reservationFor.flightNumber",
                "$.subReservation[0].reservationFor.departureAirport", "$.subReservation[0].reservationFor.arrivalAirport" };

        Configuration conf = Configuration.defaultConfiguration();
        Object root = using(conf).parse(schemaJson).readRoot(jsonPathArr);
        Assert.assertNotNull(root);

        JsonProvider jp = conf.jsonProvider();
        Assert.assertTrue(jp.isMap(root));

        Object subReservation = jp.getMapValue(root, "subReservation");
        Assert.assertTrue(jp.isArray(subReservation));
        Object subReservation_0 = jp.getArrayIndex(subReservation, 0);
        Assert.assertTrue(jp.isMap(subReservation_0));
        Object reservationFor = jp.getMapValue(subReservation_0, "reservationFor");
        Assert.assertTrue(jp.isMap(reservationFor));

        Object type = jp.getMapValue(reservationFor, "@type");
        Assert.assertEquals(type, "Flight");
        Object flightNumber = jp.getMapValue(reservationFor, "flightNumber");
        Assert.assertEquals(flightNumber, "1993");
        Object departureAirport = jp.getMapValue(reservationFor, "departureAirport");
        Assert.assertTrue(jp.isMap(departureAirport));
        Assert.assertEquals(jp.getMapValue(departureAirport, "@id"), "_:be029eb9-a63b-4160-8420-40dc5d269242");
        Assert.assertEquals(jp.getMapValue(departureAirport, "@type"), "Airport");
        Assert.assertEquals(jp.getMapValue(departureAirport, "iataCode"), "SFO");
        Assert.assertEquals(jp.getMapValue(departureAirport, "name"), "SANFRANCISCO,CA");
        Object departureAirportIdentifierType = jp.getMapValue(jp.getArrayIndex(jp.getMapValue(departureAirport, "identifier"), 0), "@type");
        Assert.assertEquals(departureAirportIdentifierType, "PropertyValue");
        Object departureAirportIdentifierPropertyID = jp.getMapValue(jp.getArrayIndex(jp.getMapValue(departureAirport, "identifier"), 0),
        "propertyID");
        Assert.assertEquals(departureAirportIdentifierPropertyID, "searchTerms");
        Object departureAirportIdentifierValue = jp.getMapValue(jp.getArrayIndex(jp.getMapValue(departureAirport, "identifier"), 0), "value");
        Assert.assertEquals(departureAirportIdentifierValue, "sfo san francisco bay area");

        Object arrivalAirport = jp.getMapValue(reservationFor, "arrivalAirport");
        Assert.assertTrue(jp.isMap(arrivalAirport));
        Assert.assertEquals(jp.getMapValue(arrivalAirport, "@id"), "_:0bce9885-2389-48dc-9e6d-81ddafaaf2f9");
        Assert.assertEquals(jp.getMapValue(arrivalAirport, "@type"), "Airport");
        Assert.assertEquals(jp.getMapValue(arrivalAirport, "iataCode"), "DAL");
        Assert.assertEquals(jp.getMapValue(arrivalAirport, "name"), "DALLAS,TX");
        Object arrivalAirportIdentifierType = jp.getMapValue(jp.getArrayIndex(jp.getMapValue(arrivalAirport, "identifier"), 0), "@type");
        Assert.assertEquals(arrivalAirportIdentifierType, "PropertyValue");
        Object arrivalAirportIdentifierPropertyID = jp.getMapValue(jp.getArrayIndex(jp.getMapValue(arrivalAirport, "identifier"), 0), "propertyID");
        Assert.assertEquals(arrivalAirportIdentifierPropertyID, "searchTerms");
        Object arrivalAirportIdentifierValue = jp.getMapValue(jp.getArrayIndex(jp.getMapValue(arrivalAirport, "identifier"), 0), "value");
        Assert.assertEquals(arrivalAirportIdentifierValue, "dallas ft worth texas");
    }
}
