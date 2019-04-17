package com.github.phoswald.rspg;

/**
 * An element of a parser rule.
 * <p>
 * There are two types of elements: tokens and symbols.
 */
public interface Element {

    public String getCallback();

    public boolean isCallbackLinked();
}
