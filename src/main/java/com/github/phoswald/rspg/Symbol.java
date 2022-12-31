package com.github.phoswald.rspg;

import java.util.Objects;

/**
 * A symbol is a reference to a rule.
 */
public record Symbol( //
        /**
         * The name of the rule.
         */
        String name, //
        /**
         * Indicates whether the output is passed as an argument to the rule.
         */
        boolean linked, //
        /**
         * Indicates whether the output is passed as an argument to the callback.
         */
        boolean callbackLinked, //
        /**
         * The callback to be called if the symbol matches.
         */
        String callback //
) implements Element {

    public Symbol {
        Objects.requireNonNull(name);
    }

    public static Symbol symbol(String name) {
        return new Symbol(name, false, false, null);
    }

    public Symbol withLinked() {
        return new Symbol(name, true, callbackLinked, callback);
    }

    public Symbol withCallback(String callback) {
        return new Symbol(name, linked, false, callback);
    }

    public Symbol withCallbackLinked(String callback) {
        return new Symbol(name, linked, true, callback);
    }
}
