package com.github.romanqed.jsm.bytecode;

import com.github.romanqed.jsm.model.MachineModel;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

final class Translation {
    final Map<?, Integer> to;
    final Object[] from;

    Translation(Map<?, Integer> to, Object[] from) {
        this.to = to;
        this.from = from;
    }

    static Map<?, Integer> makeTo(Object[] from) {
        var ret = new HashMap<Object, Integer>();
        for (var i = 0; i < from.length; ++i) {
            ret.put(from[i], i);
        }
        return ret;
    }

    static Translation makeTo(MachineModel<?, ?> model) {
        var values = model.getStates().values();
        var length = values.size() + 2;
        var ret = (Object[]) Array.newInstance(model.getStateType(), length);
        ret[0] = model.getExit().getValue();
        ret[1] = model.getInit().getValue();
        var count = 2;
        for (var state : values) {
            ret[count++] = state.getValue();
        }
        return new Translation(makeTo(ret), ret);
    }
}
