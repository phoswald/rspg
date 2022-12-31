package com.github.phoswald.rspg;

import java.util.List;
import java.util.Objects;

import com.github.phoswald.record.builder.RecordBuilder;

/**
 * A grammar consists of a list of rules.
 */
@RecordBuilder
public record Grammar( //
        /**
         * The name of the grammar.
         */
        String name, //
        /**
         * The java package and class for the grammar's parser.
         */
        String javaType, //
        /**
         * This list of rules.
         */
        List<Rule> rules //
) {

    public Grammar {
        Objects.requireNonNull(name);
        Objects.requireNonNull(javaType);
        Objects.requireNonNull(rules);
    }

    static public GrammarBuilder builder() {
        return new GrammarBuilder();
    }
}
