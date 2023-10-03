package com.github.romanqed.jsm.bytecode;

import com.github.romanqed.jsm.model.MachineModel;

import java.util.HashMap;
import java.util.Map;

final class Translation {
    private final Map<?, Integer> to;
    private final Map<Integer, ?> from;

    Translation(Map<?, Integer> to, Map<Integer, ?> from) {
        this.to = to;
        this.from = from;
    }

    static Translation make(Map<Integer, ?> from) {
        var to = new HashMap<Object, Integer>();
        from.forEach((k, v) -> to.put(v, k));
        return new Translation(to, from);
    }

    static Translation make(MachineModel<?, ?> model) {
        var ret = new HashMap<Integer, Object>();
        var count = 0;
        ret.put(count++, model.getExit().getValue());
        ret.put(count++, model.getInit().getValue());
        for (var state : model.getStates().values()) {
            ret.put(count++, state.getValue());
        }
        return make(ret);
    }

    Map<?, Integer> getTo() {
        return to;
    }

    Map<Integer, ?> getFrom() {
        return from;
    }
}
