package com.jayway.jsonpath;

import static com.jayway.jsonpath.JsonPath.using;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class PredicateTest extends BaseTest {

    private static ReadContext reader = using(GSON_CONFIGURATION).parse(JSON_DOCUMENT);

    @Test
    public void predicates_filters_can_be_applied() {
        Predicate booksWithISBN = new Predicate() {
            @Override
            public boolean apply(PredicateContext ctx) {
                return ctx.item(Map.class).containsKey("isbn");
            }
        };

        assertThat(reader.read("$.store.book[?].isbn", List.class, booksWithISBN)).containsOnly("0-395-19395-8", "0-553-21311-3");
    }

    @Test
    public void predicates_filters_can_be_applied_readRoot() {
        Predicate booksWithISBN = new Predicate() {
            @Override
            public boolean apply(PredicateContext ctx) {
                return ctx.item(Map.class).containsKey("isbn");
            }
        };

        Object result = using(JACKSON_JSON_NODE_CONFIGURATION_FOR_READROOT).parse(JSON_DOCUMENT).readRoot(new String[] { "$.store.book[?].isbn" },
                booksWithISBN);
        Assert.assertNotNull(result);
        Assert.assertTrue(JACKSON_JSON_NODE_CONFIGURATION_FOR_READROOT.jsonProvider().isMap(result));
        Object storeObj = JACKSON_JSON_NODE_CONFIGURATION_FOR_READROOT.jsonProvider().getMapValue(result, "store");
        Assert.assertTrue(JACKSON_JSON_NODE_CONFIGURATION_FOR_READROOT.jsonProvider().isMap(storeObj));
        Object booksObj = JACKSON_JSON_NODE_CONFIGURATION_FOR_READROOT.jsonProvider().getMapValue(storeObj, "book");
        Object elem1 = JACKSON_JSON_NODE_CONFIGURATION_FOR_READROOT.jsonProvider().getArrayIndex(booksObj, 2);
        Object elem2 = JACKSON_JSON_NODE_CONFIGURATION_FOR_READROOT.jsonProvider().getArrayIndex(booksObj, 3);

        Assert.assertEquals(JACKSON_JSON_NODE_CONFIGURATION_FOR_READROOT.jsonProvider().getMapValue(elem1, "isbn"), "0-553-21311-3");
        Assert.assertEquals(JACKSON_JSON_NODE_CONFIGURATION_FOR_READROOT.jsonProvider().getMapValue(elem2, "isbn"), "0-395-19395-8");
    }
}
