package com.github.romanqed.jsm.asm;

import com.github.romanqed.jsm.model.RangeToken;
import com.github.romanqed.jsm.model.SetToken;
import com.github.romanqed.jsm.model.SingleToken;
import com.github.romanqed.jsm.model.TokenVisitor;

import java.util.Map;

final class MapVisitor implements TokenVisitor {
    private final Map<Object, Integer> transitions;
    private final int target;

    MapVisitor(Map<Object, Integer> transitions, int target) {
        this.transitions = transitions;
        this.target = target;
    }

    @Override
    public <T> void visit(SingleToken<T> token) {
        transitions.put(token.getValue(), target);
    }

    @Override
    public <T> void visit(RangeToken<T> token) {
        throw new IllegalArgumentException("Bytecode machine factory does not support range tokens");
    }

    @Override
    public <T> void visit(SetToken<T> token) {
        for (var value : token.getValues()) {
            transitions.put(value, target);
        }
    }
}
