package com.jayway.jsonpath;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.spi.json.JsonProvider;

import java.util.List;
import java.util.Map;

import static com.jayway.jsonpath.JsonPath.using;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonOrgJsonProviderTest extends BaseTest {


    @Test
    public void an_object_can_be_read() {

        JSONObject book = using(JSON_ORG_CONFIGURATION).parse(JSON_DOCUMENT).read("$.store.book[0]");

        assertThat(book.get("author").toString()).isEqualTo("Nigel Rees");
    }

    @Test
    public void an_object_can_be_readroot() {

        Object root = using(JSON_ORG_CONFIGURATION_FOR_READROOT).parse(JSON_DOCUMENT).readRoot(new String[] {"$.store.book[0]"});
        JsonProvider jp = JSON_ORG_CONFIGURATION_FOR_READROOT.jsonProvider();
        Object store = jp.getProperty(root, "store");
        Object bookarray = jp.getProperty(store, "book");
        Object book = jp.getProperty(bookarray, "0");
        Object author = jp.getProperty(book, "author");
        assertThat(author).isEqualTo("Nigel Rees");
    }
    
    @Test
    public void a_property_can_be_read() {

        String category = using(JSON_ORG_CONFIGURATION).parse(JSON_DOCUMENT).read("$.store.book[0].category");

        assertThat(category).isEqualTo("reference");
    }

    @Test
    public void a_property_can_be_readroot() {

        Object root = using(JSON_ORG_CONFIGURATION_FOR_READROOT).parse(JSON_DOCUMENT).readRoot(new String[] {"$.store.book[0].category"});
        JsonProvider jp = JSON_ORG_CONFIGURATION_FOR_READROOT.jsonProvider();
        Object store = jp.getProperty(root, "store");
        Object bookarray = jp.getProperty(store, "book");
        Object book = jp.getProperty(bookarray, "0");
        Object category = jp.getProperty(book, "category");
        assertThat(category).isEqualTo("reference");
    }
    
    @Test
    public void a_filter_can_be_applied() {

        JSONArray fictionBooks = using(JSON_ORG_CONFIGURATION).parse(JSON_DOCUMENT).read("$.store.book[?(@.category == 'fiction')]");

        assertThat(fictionBooks.length()).isEqualTo(3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void a_filter_can_be_applied_readroot() {
    	JSON_ORG_CONFIGURATION_FOR_READROOT.setComputeRoot(true);
        using(JSON_ORG_CONFIGURATION_FOR_READROOT).parse(JSON_DOCUMENT).read("$.store.book[?(@.category == 'fiction')]");
    }
    
    @Test
    public void result_can_be_mapped_to_object() {

        List<Map<String, Object>> books = using(JSON_ORG_CONFIGURATION).parse(JSON_DOCUMENT).read("$.store.book", List.class);

        assertThat(books.size()).isEqualTo(4);
    }

    @Test
    public void read_books_with_isbn() {

        JSONArray books = using(JSON_ORG_CONFIGURATION).parse(JSON_DOCUMENT).read("$..book[?(@.isbn)]");

        assertThat(books.length()).isEqualTo(2);
    }
}
