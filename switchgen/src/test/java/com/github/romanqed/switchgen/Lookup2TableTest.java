package com.github.romanqed.switchgen;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class Lookup2TableTest {

    @Test
    public void testSmallDelta() {
        var keys = Set.of("1", "2");
        assertEquals(TableSwitchMap.class, SwitchMaps.create(keys, 10).getClass());
    }

    @Test
    public void testBigDelta() {
        var keys = Set.of("1", "11");
        assertEquals(LookupSwitchMap.class, SwitchMaps.create(keys, 10).getClass());
    }

    @Test
    public void testOverflowDelta() {
        var keys = Set.of("static", "dynamic");
        assertEquals(LookupSwitchMap.class, SwitchMaps.create(keys, 10).getClass());
    }
}
