package com.jayway.jsonpath;

import static com.jayway.jsonpath.JsonPath.using;
import static com.jayway.jsonpath.TestUtils.assertEvaluationThrows;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class MultiPropTest {

    @Test
    public void multi_prop_can_be_read_from_root() {

        Map<String, Object> model = new HashMap<String, Object>(){{
            put("a", "a-val");
            put("b", "b-val");
            put("c", "c-val");
        }};

        Configuration conf = Configuration.defaultConfiguration();

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

        Configuration conf = Configuration.defaultConfiguration();

        Object ret1 = using(conf).parse(model).readRoot("$['a', 'b']", Map.class);

        Assert.assertNotNull(ret1);
        conf.jsonProvider().getMapValue(ret1, "a").equals("a-val");
        conf.jsonProvider().getMapValue(ret1, "b").equals("b-val");

        Object ret2 = using(conf).parse(model).readRoot("$['a', 'd']", Map.class);
        Assert.assertNotNull(ret2);
        conf.jsonProvider().getMapValue(ret2, "a").equals("a-val");
        conf.jsonProvider().getMapValue(ret2, "d").equals(null);
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
        Object result = JsonPath.parse(json).readRoot("$.v[1]");
        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.REQUIRE_PROPERTIES);
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
        Object result = JsonPath.parse(json).readRoot("$..['a', 'c'].v");
        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.REQUIRE_PROPERTIES);

        // conf.jsonProvider().getMapValue(result, "a")
        // assertThat(result).asList().containsOnly(5, 1);

        result = JsonPath.parse(json).readRoot("$..v");
        Assert.assertNotNull(result);

        result = JsonPath.parse(json).readRoot("$..['a', 'c'][?(@.flag)].v");
        assertThat(result).asList().containsOnly(1);
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
}
