package com.github.phoswald.rspg;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * A token is a sequence of characters that occurs in the input.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Token implements Element {

    /**
     * The sequence of characters.
     */
    private final @NonNull String text;

    /**
     * Defines how the characters are interpreted.
     */
    private final @NonNull Type type;

    /**
     * Indicates whether the token is to be passed to the callback.
     */
    private final boolean pass;

    /**
     * Indicates whether the output is passed as an argument to the callback.
     */
    private final boolean callbackLinked;

    /**
     * The callback to be called if the token matches.
     */
    private final String callback;

    public static Token token(String text) {
        return new Token(text, Type.Token, false, false, null);
    }

    public static Token set(String text) {
        return new Token(text, Type.Set, false, false, null);
    }

    public Token pass() {
        return new Token(text, type, true, callbackLinked, callback);
    }

    public Token callback(String callback) {
        return new Token(text, type, pass, false, callback);
    }

    public Token callbackLinked(String callback) {
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
