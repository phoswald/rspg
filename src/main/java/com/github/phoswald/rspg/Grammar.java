package com.github.phoswald.rspg;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * A grammar consists of a list of rules.
 */
@Getter
@Builder
public class Grammar {

    /**
     * The name of the grammar.
     */
    private final @NonNull String name;

    /**
     * The java package and class for the grammar's parser.
     */
    private final @NonNull String javaType;

    /**
     * This list of rules.
     */
    private final @NonNull List<Rule> rules;
}
