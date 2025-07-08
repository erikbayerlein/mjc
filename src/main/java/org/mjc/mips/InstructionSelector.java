//package org.mjc.mips;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.NoArgsConstructor;
//import org.mjc.assem.*;
//import org.mjc.frame.Frame;
//import org.mjc.irtree.*;
//import org.mjc.temp.Label;
//import org.mjc.temp.LabelList;
//import org.mjc.temp.Temp;
//import org.mjc.temp.TempList;
//
//import java.util.ArrayList;
//import java.util.List;
//
//class InstructionSelector {
//
//    private final Frame frame;
//    private final List<Instr> instrs;
//
//    public InstructionSelector(Frame frame) {
//        this.frame = frame;
//        this.instrs = new ArrayList<>();
//    }
//
//    public List<Instr> select(Stm stm) {
//        munchStm(stm);
//        return instrs;
//    }
//
//    private void emit(Instr instr) {
//        instrs.add(instr);
//    }
//
//    private void munchStm(Stm stm) {
//        if (stm instanceof SEQ seq) {
//            munchStm(seq.left);
//            munchStm(seq.right);
//        } else if (stm instanceof MOVE move) {
//            munchMove(move);
//        } else if (stm instanceof LABEL label) {
//            emit(new AssemLABEL(label.label.toString() + ":", label.label));
//        } else if (stm instanceof JUMP jump && jump.exp instanceof NAME name) {
//            emit(new AssemOPER("j `j0", null, null, new LabelList(name.label, null)));
//        } else if (stm instanceof CJUMP cjump) {
//            String op = switch (cjump.relop) {
//                case CJUMP.EQ -> "beq";
//                case CJUMP.NE -> "bne";
//                case CJUMP.LT -> "blt";
//                case CJUMP.GT -> "bgt";
//                case CJUMP.LE -> "ble";
//                case CJUMP.GE -> "bge";
//                default -> throw new RuntimeException("Unknown relop: " + cjump.relop);
//            };
//            Temp left = munchExp(cjump.left);
//            Temp right = munchExp(cjump.right);
//            emit(new AssemOPER(op + " `s0, `s1, `j0", null,
//                    new TempList(left, new TempList(right, null)),
//                    new LabelList(cjump.iftrue, new LabelList(cjump.iffalse, null))));
//        } else if (stm instanceof EXP exp) {
//            if (exp.exp instanceof CALL call) {
//                munchCall(call);
//            } else {
//                munchExp(exp.exp); // ignore result
//            }
//        } else {
//            emit(new AssemOPER("# unknown Stm", null, null));
//        }
//    }
//
//    private void munchMove(MOVE move) {
//        Exp_ dst = move.dst;
//        Exp_ src = move.src;
//
//        if (dst instanceof TEMP tdst) {
//            Temp d = tdst.temp;
//            if (src instanceof CONST c) {
//                emit(new AssemOPER("li `d0, " + c.value, new TempList(d, null), null));
//            } else if (src instanceof TEMP tsrc) {
//                emit(new AssemMOVE("move `d0, `s0", d, tsrc.temp));
//            } else if (src instanceof NAME name) {
//                emit(new AssemOPER("la `d0, " + name.label, new TempList(d, null), null));
//            } else if (src instanceof MEM mem) {
//                Temp addr = munchExp(mem.exp);
//                emit(new AssemOPER("lw `d0, 0(`s0)", new TempList(d, null), new TempList(addr, null)));
//            } else if (src instanceof CALL call) {
//                Temp result = munchCall(call);
//                emit(new AssemMOVE("move `d0, `s0", d, result));
//            } else {
//                Temp s = munchExp(src);
//                emit(new AssemMOVE("move `d0, `s0", d, s));
//            }
//        } else if (dst instanceof MEM mem) {
//            Temp addr = munchExp(mem.exp);
//            Temp val = munchExp(src);
//            emit(new AssemOPER("sw `s0, 0(`s1)", null, new TempList(val, new TempList(addr, null))));
//        } else {
//            throw new RuntimeException("Invalid MOVE destination: " + dst);
//        }
//    }
//
//    private Temp munchExp(Exp_ exp) {
//        if (exp instanceof CONST c) {
//            Temp r = new Temp();
//            emit(new AssemOPER("li `d0, " + c.value, new TempList(r, null), null));
//            return r;
//        } else if (exp instanceof TEMP t) {
//            return t.temp;
//        } else if (exp instanceof NAME n) {
//            Temp r = new Temp();
//            emit(new AssemOPER("la `d0, " + n.label, new TempList(r, null), null));
//            return r;
//        } else if (exp instanceof BINOP binop) {
//            Temp left = munchExp(binop.left);
//            Temp right = munchExp(binop.right);
//            Temp r = new Temp();
//            String op = switch (binop.binop) {
//                case BINOP.PLUS -> "add";
//                case BINOP.MINUS -> "sub";
//                case BINOP.MUL -> "mul";
//                case BINOP.DIV -> "div";
//                default -> throw new RuntimeException("Unknown BINOP: " + binop.binop);
//            };
//            emit(new AssemOPER(op + " `d0, `s0, `s1", new TempList(r, null), new TempList(left, new TempList(right, null))));
//            return r;
//        } else if (exp instanceof MEM mem) {
//            Temp addr = munchExp(mem.exp);
//            Temp r = new Temp();
//            emit(new AssemOPER("lw `d0, 0(`s0)", new TempList(r, null), new TempList(addr, null)));
//            return r;
//        } else if (exp instanceof CALL call) {
//            return munchCall(call);
//        } else {
//            Temp r = new Temp();
//            emit(new AssemOPER("# unhandled Exp", new TempList(r, null), null));
//            return r;
//        }
//    }
//
//    private Temp munchCall(CALL call) {
//        Temp dst = new Temp();
//        TempList args = munchArgs(call.args);
//
//        if (call.func instanceof NAME name) {
//            emit(new AssemOPER("jal " + name.label, null, args));
//        } else {
//            Temp f = munchExp(call.func);
//            emit(new AssemOPER("jalr `s0", null, new TempList(f, args)));
//        }
//
//        emit(new AssemMOVE("move `d0, $v0", dst, frame.RV()));
//        return dst;
//    }
//
//    private TempList munchArgs(ExpList args) {
//        TempList res = null;
//        while (args != null) {
//            Temp arg = munchExp(args.head);
//            res = new TempList(arg, res); // note: reverse order!
//            args = args.tail;
//        }
//        return res;
//    }
//}