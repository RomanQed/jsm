package com.github.romanqed.jsm.model;

/**
 * Enumeration of possible transition types.
 */
public enum TransitionType {
    /**
     * Conditional transition, has the highest priority when choosing a transition.
     */
    CONDITIONAL,
    /**
     * Unconditional transition, performed with the lowest priority.
     * There can be only 1 unconditional transition make the state.
     */
    UNCONDITIONAL
}
