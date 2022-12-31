package com.github.phoswald.rspg;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * An alternative consists of a list of elements
 */
public record Alternative(List<Element> elements) {

    public Alternative {
        Objects.requireNonNull(elements);
    }

    public static Alternative alternative(Element... elements) {
        return new Alternative(Arrays.asList(elements));
    }
}
