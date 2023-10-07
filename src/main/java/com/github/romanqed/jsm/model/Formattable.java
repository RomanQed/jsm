package com.github.romanqed.jsm.model;

/**
 * An interface describing an entity for which a certain format string can be obtained.
 */
public interface Formattable {

    /**
     * Returns format string.
     *
     * @return format string
     */
    String format();
}
