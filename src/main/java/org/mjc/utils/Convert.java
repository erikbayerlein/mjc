package org.mjc.utils;

import java.util.ArrayList;
import java.util.List;
import org.mjc.assem.InstrList;
import org.mjc.assem.Instr;

public class Convert {
    public static InstrList ArrayToInstrList(List<Instr> array) {
        InstrList instrList = null;

        for (int i = array.size()-1; i >= 0; --i) {
            instrList = new InstrList(array.get(i), instrList);
        }

        return instrList;
    }
}
