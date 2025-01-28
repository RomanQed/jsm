package com.github.romanqed.jsm.bytecode;

import com.github.romanqed.jsm.model.MachineModel;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

final class Translation {
    final Map<?, Integer> to;
    final Object[] from;
    final int size;

    Translation(Map<?, Integer> to, Object[] from, int size) {
        this.to = to;
        this.from = from;
        this.size = size;
    }

    static Map<?, Integer> makeTo(Object[] from) {
        var ret = new HashMap<Object, Integer>();
        for (var i = 0; i < from.length; ++i) {
            ret.put(from[i], i);
        }
        return ret;
    }

    static Translation of(MachineModel<?, ?> model) {
        var values = model.getStates().values();
        var length = values.size() + 2;
        var ret = (Object[]) Array.newInstance(model.getStateType(), length);
        ret[0] = model.getExit().getValue();
        ret[1] = model.getInit().getValue();
        var count = 2;
        for (var state : values) {
            ret[count++] = state.getValue();
        }
        return new Translation(makeTo(ret), ret, length);
    }
}
