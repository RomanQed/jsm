package com.github.romanqed.switchgen;

import java.util.*;

/**
 *
 */
public final class SwitchMaps {
    private static final Set<Class<?>> TYPES = Set.of(
            // Boolean
            Boolean.class,
            // Char
            Character.class,
            // String
            String.class,
            // Int-types
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            // Float-types
            Float.class,
            Double.class
    );

    private SwitchMaps() {
    }

    /**
     * @param set
     * @param <T>
     * @return
     */
    public static <T> Map<Integer, List<T>> calculateHashes(Set<T> set) {
        var ret = new HashMap<Integer, List<T>>();
        for (var value : set) {
            var hash = value.hashCode();
            var list = ret.computeIfAbsent(hash, k -> new LinkedList<>());
            list.add(value);
        }
        return ret;
    }

    private static Class<?> getType(Set<?> keys) {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("Cannot create empty switch map");
        }
        var type = keys.iterator().next().getClass();
        if (!TYPES.contains(type)) {
            throw new IllegalArgumentException("Cannot create switch map of type " + type);
        }
        return type;
    }

    private static Comparator getComparator(Class<?> type) {
        if (type == Long.class) {
            return new LongComparator();
        }
        if (type == Double.class) {
            return new DoubleComparator();
        }
        if (type == String.class) {
            return new StringComparator();
        }
        return null;
    }

    /**
     * @param keys
     * @param <T>
     * @return
     */
    public static <T> SwitchMap<T> createLookup(Set<T> keys) {
        var type = getType(keys);
        var comparator = getComparator(type);
        var hashes = calculateHashes(keys);
        var intKeys = hashes.keySet()
                .stream()
                .mapToInt(Integer::intValue)
                .sorted()
                .toArray();
        return new LookupSwitchMap<>(hashes, comparator, intKeys);
    }

    /**
     * @param keys
     * @param <T>
     * @return
     */
    public static <T> SwitchMap<T> createTable(Set<T> keys) {
        var type = getType(keys);
        var comparator = getComparator(type);
        var hashes = calculateHashes(keys);
        var set = hashes.keySet();
        var min = Collections.min(set);
        var max = Collections.max(set);
        return new TableSwitchMap<>(hashes, comparator, min, max);
    }

    /**
     * @param keys
     * @param maxDelta
     * @param <T>
     * @return
     */
    public static <T> SwitchMap<T> create(Set<T> keys, int maxDelta) {
        if (maxDelta <= 0) {
            throw new IllegalArgumentException("Max delta must be greater than zero");
        }
        var type = getType(keys);
        var comparator = getComparator(type);
        var hashes = calculateHashes(keys);
        var intKeys = hashes.keySet()
                .stream()
                .mapToInt(Integer::intValue)
                .sorted()
                .toArray();
        var actualDelta = 0;
        var last = intKeys.length - 1;
        for (var i = 0; i < last; ++i) {
            var delta = intKeys[i + 1] - intKeys[i];
            if (delta > actualDelta) {
                actualDelta = delta;
            }
        }
        if (actualDelta <= maxDelta) {
            return new TableSwitchMap<>(hashes, comparator, intKeys[0], intKeys[last]);
        }
        return new LookupSwitchMap<>(hashes, comparator, intKeys);
    }
}
