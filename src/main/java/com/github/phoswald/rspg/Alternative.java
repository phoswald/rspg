package com.github.phoswald.rspg;

import java.util.Arrays;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * An alternative consists of a list of elements
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Alternative {

    private final @NonNull List<Element> elements;

    public static Alternative alternative(Element... elements) {
        return new Alternative(Arrays.asList(elements));
    }
}
