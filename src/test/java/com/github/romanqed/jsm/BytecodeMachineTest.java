package com.github.romanqed.jsm;

import com.github.romanqed.jsm.bytecode.BytecodeMachineFactory;
import com.github.romanqed.jsm.model.MachineModel;
import com.github.romanqed.jsm.model.MachineModelBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

public final class BytecodeMachineTest extends Assertions {
    private static final StateMachineFactory FACTORY = new BytecodeMachineFactory();
    private static final MachineModel<Integer, Integer> MODEL = createModel();
    private static final StateMachine<Integer, Integer> MACHINE = FACTORY.create(MODEL);

    private static MachineModel<Integer, Integer> createModel() {
        var builder = MachineModelBuilder
                .create(Integer.class, Integer.class)
                .setInitState(0)
                .setExitState(-1);
        for (var i = 1; i <= 9; ++i) {
            builder.addState(i).addTransition(i - 1, i, i);
        }
        return builder.build();
    }

    @Test
    public void testFullSequence() {
        assertEquals(9, MACHINE.run(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9}));
    }

    @Test
    public void testEmptySequence() {
        assertEquals(0, MACHINE.run(new Integer[0]));
    }

    @Test
    public void testUnknownNumber() {
        assertEquals(-1, MACHINE.run(new Integer[]{10}));
    }

    @Test
    public void testUnorderedSequence() {
        assertEquals(-1, MACHINE.run(new Integer[]{3, 1, 2, 4, 7, 8, 6, 5, 9}));
    }

    @Test
    public void testIncompleteSequence() {
        assertEquals(3, MACHINE.run(new Integer[]{1, 2, 3}));
    }

    @Test
    public void testStepByStepProcessing() {
        MACHINE.reset();
        assertAll(
                () -> assertEquals(0, MACHINE.getState()),
                () -> assertEquals(1, MACHINE.step(1)),
                () -> assertEquals(2, MACHINE.step(2)),
                () -> assertEquals(3, MACHINE.step(3)),
                () -> assertEquals(4, MACHINE.step(4)),
                () -> assertEquals(5, MACHINE.step(5)),
                () -> assertEquals(6, MACHINE.step(6)),
                () -> assertEquals(7, MACHINE.step(7)),
                () -> assertEquals(8, MACHINE.step(8)),
                () -> assertEquals(9, MACHINE.step(9)),
                () -> assertEquals(-1, MACHINE.step(10)),
                () -> assertEquals(-1, MACHINE.step(1))
        );
    }

    @Test
    public void testFullIterableSequence() {
        assertEquals(9, MACHINE.run(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9)));
    }

    @Test
    public void testEmptyIterableSequence() {
        assertEquals(0, MACHINE.run(Collections.emptyList()));
    }
}
