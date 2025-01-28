package com.github.romanqed.switchgen;

import java.util.*;

/**
 * A utility class containing methods that create various {@link SwitchMap} implementations.
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
     * Calculates hash mapping with collisions for given set.
     *
     * @param set the specified set
     * @param <T> the type of elements stored in given set
     * @return the {@link Map} containing hash mapping
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
     * Creates lookup switch map for given set. Its values will be the conditions in the cases.
     *
     * @param keys the specified set
     * @param <T>  the type of the switch-case argument
     * @return the {@link SwitchMap} instance
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
     * Creates table switch map for given set. Its values will be the conditions in the cases.
     *
     * @param keys the specified set
     * @param <T>  the type of the switch-case argument
     * @return the {@link SwitchMap} instance
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
     * Creates table switch map for given min and max int values.
     *
     * @param min the specified min int value
     * @param max the specified max int value
     * @return the {@link SwitchMap} instance
     */
    public static SwitchMap<Integer> createTable(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("Max value must be greater than min");
        }
        var hashes = new HashMap<Integer, List<Integer>>();
        for (var i = min; i <= max; ++i) {
            hashes.put(i, List.of(i));
        }
        return new TableSwitchMap<>(hashes, null, min, max);
    }

    /**
     * Creates switch map for given set. Its values will be the conditions in the cases.
     * Selects table variant if calculated max delta between cases is less or equal than given max delta,
     * lookup otherwise.
     * For example, for ("1", "2", "3") hashes will be (49, 50, 51), and max delta will be 1,
     * so table switch map will be created.
     *
     * @param keys     the specified set
     * @param maxDelta the cap value for the delta between neighbour cases
     * @param <T>      the type of the switch-case argument
     * @return the {@link SwitchMap} instance
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
