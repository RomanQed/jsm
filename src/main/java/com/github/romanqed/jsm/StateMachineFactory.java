package com.github.romanqed.jsm;

import com.github.romanqed.jsm.model.MachineModel;

/**
 * An interface describing an abstract finite state machine factory.
 */
public interface StateMachineFactory {

    /**
     * Creates a finite state machine based on the specified model.
     *
     * @param model specified machine model, must be non-null
     * @param <S>   state type
     * @param <T>   token type
     * @return created finite state machine
     */
    <S, T> StateMachine<S, T> create(MachineModel<S, T> model);
}
