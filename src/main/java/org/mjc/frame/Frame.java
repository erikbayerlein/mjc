package org.mjc.frame;

import java.util.List;

import org.mjc.assem.InstrList;
import org.mjc.irtree.ExpAbstract;
import org.mjc.irtree.Stm;
import org.mjc.irtree.StmList;
import org.mjc.temp.Label;
import org.mjc.temp.Temp;
import org.mjc.temp.TempMap;

public abstract class Frame implements TempMap {
    public Label name;
    public List<Access> formals;

    public abstract Frame newFrame(String name, List<Boolean> formals);
    public abstract Access allocLocal();
    public abstract ExpAbstract externalCall(String func, List<ExpAbstract> args);

    public abstract Temp FP();
    public abstract Temp RV();
    public abstract Temp[] registers();

    public abstract String string(Label label, String value);
    public abstract String tempMap(Temp temp);
    public abstract String programTail();

    public abstract Label badPtr();
    public abstract Label badSub();

    public abstract int wordSize();
    public abstract void procEntryExit1(List<Stm> body);

    public abstract InstrList codegen(StmList stms);
}