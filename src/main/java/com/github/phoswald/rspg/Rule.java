package com.github.phoswald.rspg;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

/**
 * A grammar consists of a list of rules.
 * <p>
 * Each rule has a name and consists of a list of alternatives.
 */
@Getter
@Builder
public class Rule {

    /**
     * Unique identifier for this rule.
     */
    private final @NonNull String name;

    /**
     * The Java class produced by this rule.
     */
    private final @NonNull String javaType;

    /**
     * Whether the rule is publicly visible.
     */
    private final boolean export;

    /**
     * Alternatives for this rule. Each alternative consists of a list of elements.
     * An empty alternative makes a rule optional.
     */
    @Singular
    private final @NonNull List<Alternative> alternatives;
}
