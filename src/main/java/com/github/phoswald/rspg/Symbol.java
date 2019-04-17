package com.github.phoswald.rspg;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A symbol is a reference to a rule.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Symbol implements Element {

    /**
     * The name of the rule.
     */
    private final String name;

    /**
     * Indicates whether the output is passed as an argument to the rule.
     */
    private final boolean linked;

    /**
     * Indicates whether the output is passed as an argument to the callback.
     */
    private final boolean callbackLinked;

    /**
     * The callback to be called if the symbol matches.
     */
    private final String callback;

    public static Symbol symbol(String name) {
        return new Symbol(name, false, false, null);
    }

    public Symbol linked() {
        return new Symbol(name, true, callbackLinked, callback);
    }

    public Symbol callback(String callback) {
        return new Symbol(name, linked, false, callback);
    }

    public Symbol callbackLinked(String callback) {
        return new Symbol(name, linked, true, callback);
    }
}
