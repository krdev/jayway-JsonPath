package com.jayway.jsonpath;

import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;

import junit.framework.Assert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jayway.jsonpath.JsonPath.using;
import static com.jayway.jsonpath.Option.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class OptionsTest extends BaseTest {

    @Test(expected = PathNotFoundException.class)
    public void a_leafs_is_not_defaulted_to_null() {

        Configuration conf = Configuration.defaultConfiguration();

        assertThat(using(conf).parse("{\"foo\" : \"bar\"}").read("$.baz")).isNull();
    }
    
    /**
     * Test to check if there is a missing element in json document, we get an empty map back even with SUPRESS_EXCEPTIONS and no exception should be thrown.
     *  
     */
    @Test
    public void missing_elements_in_jsonpath_readroot() {

        Configuration conf = Configuration.defaultConfiguration();
        conf.setOptions(Option.SUPPRESS_EXCEPTIONS);
        Object root = using(conf).parse("{\"foo\" : \"bar\"}").readRoot(new String[] {"$.baz"});
        Assert.assertEquals(((Map)root).size(), 0);
    }

    @Test
    public void a_leafs_can_be_defaulted_to_null() {

        Configuration conf = Configuration.builder().options(DEFAULT_PATH_LEAF_TO_NULL).build();

        assertThat(using(conf).parse("{\"foo\" : \"bar\"}").read("$.baz", Object.class)).isNull();
    }

    /**
     * Test to check when there is a missing element in json document, we get an map even with DEFAULT_PATH_LEAF_TO_NULL set.
     * The value of the missing element should be null.
     *  
     */
    @Test
    public void a_leafs_can_be_defaulted_to_null_readroot() {

        Configuration conf = Configuration.builder().options(DEFAULT_PATH_LEAF_TO_NULL).build();
        Object root = using(conf).parse("{\"foo\" : \"bar\"}").readRoot(new String[] {"$.baz"});
        assertThat(root).isNotNull();
        Assert.assertNull(conf.jsonProvider().getMapValue(root, "baz"));
    }
    
    /**
     * Test to check when there is a missing element in json document, we get an empty map with DEFAULT_PATH_LEAF_TO_NULL not set.
     *  
     */
    @Test
    public void a_leafs_not_defaulted_to_null_readroot() {

        Configuration conf = Configuration.builder().build();
        Object root = using(conf).parse("{\"foo\" : \"bar\"}").readRoot(new String[] {"$.baz"});
        assertThat(root).isNotNull();
        Assert.assertEquals(((Map)root).size(), 0);
    }
    
    @Test
    public void a_definite_path_is_not_returned_as_list_by_default() {

        Configuration conf = Configuration.defaultConfiguration();

        assertThat(using(conf).parse("{\"foo\" : \"bar\"}").read("$.foo")).isInstanceOf(String.class);
    }

    /**
     * Test to check if a map instance is returned, without ALWAYS_RETURN_LIST set.
     * 
     */
    @Test
    public void a_definite_path_is_not_returned_as_list_by_default_map_readroot() {

        Configuration conf = Configuration.defaultConfiguration();
        Object root = using(conf).parse("{\"foo\" : \"bar\"}").readRoot(new String[] {"$.foo"});
        assertThat(root).isInstanceOf(Map.class);
    }

    /**
     * Test to check if a map instance is returned, even with ALWAYS_RETURN_LIST set.
     * 
     */
    @Test
    public void a_definite_path_is_returned_as_list_by_default_map_readroot() {

        Configuration conf = Configuration.defaultConfiguration();
        conf.setOptions(Option.ALWAYS_RETURN_LIST);
        Object root = using(conf).parse("{\"foo\" : \"bar\"}").readRoot(new String[] {"$.foo"});
        assertThat(root).isInstanceOf(Map.class);
    }
    
    @Test
    public void a_definite_path_can_be_returned_as_list() {

        Configuration conf = Configuration.builder().options(ALWAYS_RETURN_LIST).build();

        assertThat(using(conf).parse("{\"foo\" : \"bar\"}").read("$.foo")).isInstanceOf(List.class);

        assertThat(using(conf).parse("{\"foo\": null}").read("$.foo")).isInstanceOf(List.class);

        assertThat(using(conf).parse("{\"foo\": [1, 4, 8]}").read("$.foo")).asList()
                .containsExactly(Arrays.asList(1, 4, 8));
    }

    @Test
    public void an_indefinite_path_can_be_returned_as_list() {
        Configuration conf = Configuration.builder().options(ALWAYS_RETURN_LIST).build();

        List<Object> result = using(conf).parse("{\"bar\": {\"foo\": null}}").read("$..foo");
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNull();

        assertThat(using(conf).parse("{\"bar\": {\"foo\": [1, 4, 8]}}").read("$..foo")).asList()
                .containsExactly(Arrays.asList(1, 4, 8));
    }

    @Test
    public void a_path_evaluation_is_returned_as_VALUE_by_default() {
        Configuration conf = Configuration.defaultConfiguration();

        assertThat(using(conf).parse("{\"foo\" : \"bar\"}").read("$.foo")).isEqualTo("bar");
    }

    @Test
    public void a_path_evaluation_can_be_returned_as_PATH_LIST() {
        Configuration conf = Configuration.builder().options(AS_PATH_LIST).build();

        List<String> pathList = using(conf).parse("{\"foo\" : \"bar\"}").read("$.foo");

        assertThat(pathList).containsOnly("$['foo']");
    }

    @Test
    public void multi_properties_are_merged_by_default() {

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("a", "a");
        model.put("b", "b");
        model.put("c", "c");

        Configuration conf = Configuration.defaultConfiguration();

        Map<String, Object> result = using(conf).parse(model).read("$.['a', 'b']");

        //assertThat(result).isInstanceOf(List.class);
        //assertThat((List)result).containsOnly("a", "b");

        assertThat(result)
                .containsEntry("a", "a")
                .containsEntry("b", "b");
    }

    @Test
    public void when_property_is_required_exception_is_thrown() {
        List<Map<String, String>> model = asList(singletonMap("a", "a-val"),singletonMap("b", "b-val"));

        Configuration conf = Configuration.defaultConfiguration();

        assertThat(using(conf).parse(model).read("$[*].a", List.class)).containsExactly("a-val");


        conf = conf.addOptions(Option.REQUIRE_PROPERTIES);

        try{
            using(conf).parse(model).read("$[*].a", List.class);
            fail("Should throw PathNotFoundException");
        } catch (PathNotFoundException pnf){}
    }

    @Test
    public void when_property_is_required_exception_is_thrown_readroot() {
        List<Map<String, String>> model = asList(singletonMap("a", "a-val"),singletonMap("b", "b-val"));

        Configuration conf = Configuration.defaultConfiguration();
        Object root = using(conf).parse(model).readRoot(new String[] {"$[*].a"});
        Assert.assertEquals(((Map)((List)root).get(0)).get("a"), "a-val");

        conf = conf.addOptions(Option.REQUIRE_PROPERTIES);

        try{
            using(conf).parse(model).readRoot(new String[] {"$[*].a"});
            fail("Should throw PathNotFoundException");
        } catch (PathNotFoundException pnf){}
    }
    
    @Test
    public void when_property_is_required_exception_is_thrown_2() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("a", singletonMap("a-key", "a-val"));
        model.put("b", singletonMap("b-key", "b-val"));

        Configuration conf = Configuration.defaultConfiguration();

        assertThat(using(conf).parse(model).read("$.*.a-key", List.class)).containsExactly("a-val");


        conf = conf.addOptions(Option.REQUIRE_PROPERTIES);

        try{
            using(conf).parse(model).read("$.*.a-key", List.class);
            fail("Should throw PathNotFoundException");
        } catch (PathNotFoundException pnf){}
    }


    @Test
    public void issue_suppress_exceptions_does_not_break_indefinite_evaluation() {
        Configuration conf = Configuration.builder().options(SUPPRESS_EXCEPTIONS).build();

        assertThat(using(conf).parse("{\"foo2\": [5]}").read("$..foo2[0]")).asList().containsOnly(5);
        assertThat(using(conf).parse("{\"foo\" : {\"foo2\": [5]}}").read("$..foo2[0]")).asList().containsOnly(5);
        assertThat(using(conf).parse("[null, [{\"foo\" : {\"foo2\": [5]}}]]").read("$..foo2[0]")).asList().containsOnly(5);

        assertThat(using(conf).parse("[null, [{\"foo\" : {\"foo2\": [5]}}]]").read("$..foo.foo2[0]")).asList().containsOnly(5);

        assertThat(using(conf).parse("{\"aoo\" : {}, \"foo\" : {\"foo2\": [5]}, \"zoo\" : {}}").read("$[*].foo2[0]")).asList().containsOnly(5);
    }

    @Test
    public void isbn_is_defaulted_when_option_is_provided() {
        List<String> result1 = JsonPath.using(JSON_SMART_CONFIGURATION).parse(JSON_DOCUMENT).read("$.store.book.*.isbn");

        assertThat(result1).containsExactly("0-553-21311-3","0-395-19395-8");

        List<String> result2 = JsonPath.using(JSON_SMART_CONFIGURATION.addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)).parse(JSON_DOCUMENT).read("$.store.book.*.isbn");

        assertThat(result2).containsExactly(null, null, "0-553-21311-3", "0-395-19395-8");
    }
}
