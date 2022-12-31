package com.github.phoswald.rspg;

import java.util.Objects;

/**
 * A token is a sequence of characters that occurs in the input.
 */
public record Token( //
        /**
         * The sequence of characters.
         */
        String text, //
        /**
         * Defines how the characters are interpreted.
         */
        Type type, //
        /**
         * Indicates whether the token is to be passed to the callback.
         */
        boolean pass, //
        /**
         * Indicates whether the output is passed as an argument to the callback.
         */
        boolean callbackLinked, //
        /**
         * The callback to be called if the token matches.
         */
        String callback //
) implements Element {

    public Token {
        Objects.requireNonNull(text);
        Objects.requireNonNull(type);
    }

    public static Token token(String text) {
        return new Token(text, Type.Token, false, false, null);
    }

    public static Token set(String text) {
        return new Token(text, Type.Set, false, false, null);
    }

    public Token withPass() {
        return new Token(text, type, true, callbackLinked, callback);
    }

    public Token withCallback(String callback) {
        return new Token(text, type, pass, false, callback);
    }

    public Token withCallbackLinked(String callback) {
        return new Token(text, type, pass, true, callback);
    }

    public enum Type {
        /**
         * If true 'ab' means 'a' followed by 'b.
         */
        Token,
        /**
         * If true 'ab' means either 'a' or 'b'.
         */
        Set
    }
}
