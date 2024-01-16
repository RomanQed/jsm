package com.github.romanqed.jsm;

import com.github.romanqed.jsm.model.InvalidStateException;
import com.github.romanqed.jsm.model.MachineModelBuilder;
import com.github.romanqed.jsm.model.SingleToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class ModelBuilderTest extends Assertions {
    private static MachineModelBuilder<Object, Object> makeBuilder() {
        return new MachineModelBuilder<>(Object.class, Object.class);
    }

    @Test
    public void testEmptyBuilder() {
        var builder = makeBuilder();
        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    public void testMatchingRootStates() {
        var builder = makeBuilder();
        assertAll(
                () -> assertThrows(InvalidStateException.class, () -> builder.setInitState("1").setExitState("1")),
                () -> assertThrows(InvalidStateException.class, () -> builder.setExitState("2").setInitState("2"))
        );
    }

    @Test
    public void testTransitionFromExit() {
        var builder = makeBuilder();
        assertThrows(InvalidStateException.class, () -> builder
                .setInitState("1")
                .setExitState("2")
                .addTransition("2", "1"));
    }

    @Test
    public void testTransitionFromUnknownState() {
        var builder = makeBuilder();
        assertThrows(InvalidStateException.class, () -> builder
                .setInitState("1")
                .addTransition("3", "1"));
    }

    @Test
    public void testTransitionToUnknownState() {
        var builder = makeBuilder();
        assertThrows(InvalidStateException.class, () -> builder
                .setInitState("1")
                .setExitState("2")
                .addTransition("1", "3"));
    }

    @Test
    public void testDuplicateTransition() {
        var builder = makeBuilder();
        assertThrows(IllegalArgumentException.class, () -> builder
                .setInitState("1")
                .setExitState("2")
                .addTransition("1", "2")
                .addTransition("1", "2"));
    }

    @Test
    public void testInvalidTypeRangeTransition() {
        var builder = makeBuilder();
        assertThrows(IllegalStateException.class, () -> builder
                .setInitState("1")
                .setExitState("2")
                .addRangeTransition("1", "2", 1, 0));
    }

    @Test
    public void testInvalidRangeTransition() {
        var builder = MachineModelBuilder.create(Integer.class, Integer.class);
        assertThrows(IllegalArgumentException.class, () -> builder
                .setInitState(1)
                .setExitState(2)
                .addRangeTransition(1, 2, 2, 1));
    }

    @Test
    public void testTransitionRemoving() {
        var builder = makeBuilder();
        var model = builder
                .setInitState("1")
                .setExitState("2")
                .addTransition("1", "2")
                .removeTransition("1", "2")
                .build();
        assertEquals(0, model.getInit().getTransitions().size());
    }

    @Test
    public void testInitStateChanging() {
        var builder = makeBuilder();
        var model = builder
                .setInitState("1")
                .setExitState("3")
                .addTransition("1", "3")
                .setInitState("2")
                .build();
        assertAll(
                () -> assertEquals("2", model.getInit().getValue()),
                () -> assertEquals(0, model.getInit().getTransitions().size())
        );
    }

    @Test
    public void testExitStateChanging() {
        var builder = makeBuilder();
        var model = builder
                .setInitState("1")
                .setExitState("2")
                .addTransition("1", "2")
                .setExitState("3")
                .build();
        assertAll(
                () -> assertEquals("3", model.getExit().getValue()),
                () -> assertEquals(0, model.getInit().getTransitions().size())
        );
    }

    @Test
    public void testStateRemoving() {
        var builder = makeBuilder();
        var model = builder
                .setInitState("1")
                .setExitState("2")
                .addState("3")
                .addTransition("1", "3")
                .addTransition("3", "2")
                .removeState("3")
                .build();
        assertAll(
                () -> assertEquals(0, model.getStates().size()),
                () -> assertEquals(0, model.getInit().getTransitions().size())
        );
    }

    @Test
    public void testModelBuilding() {
        var builder = makeBuilder();
        var model = builder
                .setInitState("Init")
                .setExitState("Exit")
                .addState("1")
                .addState("2")
                .addTransition("Init", "1", "t1")
                .addTransition("Init", "2", "t2")
                .addTransition("1", "2", "t3")
                .build();
        var states = model.getStates();
        var initToOne = model.getInit().getTransitions().get("1");
        var initToTwo = model.getInit().getTransitions().get("2");
        var oneToTwo = states.get("1").getTransitions().get("2");
        assertAll(
                // Assert basics
                () -> assertEquals("ExitInit1t12t212t32", model.format()),
                () -> assertEquals("Init", model.getInit().getValue()),
                () -> assertEquals("Exit", model.getExit().getValue()),
                () -> assertEquals("1", states.get("1").getValue()),
                () -> assertEquals("2", states.get("2").getValue()),
                // Assert Init->1 by t1
                () -> assertNotNull(initToOne),
                () -> assertEquals("1", initToOne.getTarget()),
                () -> assertEquals(SingleToken.class, initToOne.getToken().getClass()),
                () -> assertEquals("t1", ((SingleToken<?>) initToOne.getToken()).getValue()),
                // Assert Init->2 by t2
                () -> assertNotNull(initToTwo),
                () -> assertEquals("2", initToTwo.getTarget()),
                () -> assertEquals(SingleToken.class, initToTwo.getToken().getClass()),
                () -> assertEquals("t2", ((SingleToken<?>) initToTwo.getToken()).getValue()),
                // Assert 1->2 by t3
                () -> assertNotNull(oneToTwo),
                () -> assertEquals("2", oneToTwo.getTarget()),
                () -> assertEquals(SingleToken.class, oneToTwo.getToken().getClass()),
                () -> assertEquals("t3", ((SingleToken<?>) oneToTwo.getToken()).getValue())
        );
    }
}
