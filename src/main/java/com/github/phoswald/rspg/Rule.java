package com.github.phoswald.rspg;

import java.util.List;
import java.util.Objects;

import com.github.phoswald.record.builder.RecordBuilder;

/**
 * A grammar consists of a list of rules.
 * <p>
 * Each rule has a name and consists of a list of alternatives.
 */
@RecordBuilder
public record Rule( //
        /**
         * Unique identifier for this rule.
         */
        String name, //
        /**
         * The Java class produced by this rule.
         */
        String javaType, //
        /**
         * Whether the rule is publicly visible.
         */
        boolean export, //
        /**
         * Alternatives for this rule. Each alternative consists of a list of elements.
         * An empty alternative makes a rule optional.
         */
        List<Alternative> alternatives //
) {

    public Rule {
        Objects.requireNonNull(name);
        Objects.requireNonNull(javaType);
        Objects.requireNonNull(alternatives);
    }

    static RuleBuilder builder() {
        return new RuleBuilder();
    }
}
