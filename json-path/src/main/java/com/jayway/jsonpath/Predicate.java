/*
 * Copyright 2011 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jayway.jsonpath;

import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import com.jayway.jsonpath.spi.mapper.MappingException;

/**
 * Interface to define predicate filter methods.
 */
public interface Predicate {

	/** check if a predicate filter applies to an Object in the json */
    boolean apply(PredicateContext ctx);
    /**
     * Get a List of SimpleEntry objects containing the left and right values of the relational expressions in the Json Path.
     * 
     * @param valuesMap The List of SimpleEntry objects containing the left and right values of the relational expressions.
     */
    void getRelationalExprValues(final List<SimpleEntry<String,String>> valuesMap);

    public interface PredicateContext {

        /**
         * Returns the current item being evaluated by this predicate
         * @return current document
         */
        Object item();

        /**
         * Returns the current item being evaluated by this predicate. It will be mapped
         * to the provided class
         * @return current document
         */
        <T> T item(Class<T> clazz) throws MappingException;

        /**
         * Returns the root document (the complete JSON)
         * @return root document
         */
        Object root();

        /**
         * Configuration to use when evaluating
         * @return configuration
         */
        Configuration configuration();
    }
}
