package org.mjc.mips;

import org.mjc.assem.*;
import org.mjc.frame.Frame;
import org.mjc.irtree.*;
import org.mjc.temp.LabelList;
import org.mjc.temp.Temp;
import org.mjc.temp.TempList;
import org.mjc.visitor.irtree.IRTreeVisitor;

import java.util.ArrayList;
import java.util.List;

public class InstructionSelector {

    private final Frame frame;
    private final List<Instr> instrs;

    public InstructionSelector(Frame frame) {
        this.frame = frame;
        this.instrs = new ArrayList<>();
    }

    public List<Instr> select(Stm stm) {
        if (stm != null) {
            munchStm(stm);
        }
        return instrs;
    }

    private void emit(Instr instr) {
        instrs.add(instr);
    }

    private void munchStm(Stm stm) {
        if (stm instanceof SEQ seq) {
            munchStm(seq.left);
            munchStm(seq.right);
        } else if (stm instanceof MOVE move) {
            munchMove(move);
        } else if (stm instanceof LABEL label) {
            emit(new AssemLABEL(label.label.toString() + ":", label.label));
        } else if (stm instanceof JUMP jump && jump.exp instanceof NAME name) {
            emit(new AssemOPER("j `j0", null, null, new LabelList(name.label, null)));
        } else if (stm instanceof CJUMP cjump) {
            String op = switch (cjump.relop) {
                case CJUMP.EQ -> "beq";
                case CJUMP.NE -> "bne";
                case CJUMP.LT -> "blt";
                case CJUMP.GT -> "bgt";
                case CJUMP.LE -> "ble";
                case CJUMP.GE -> "bge";
                default -> throw new RuntimeException("Unknown relop: " + cjump.relop);
            };
            Temp left = munchExp(cjump.left);
            Temp right = munchExp(cjump.right);
            emit(new AssemOPER(op + " `s0, `s1, `j0", null,
                    new TempList(left, new TempList(right, null)),
                    new LabelList(cjump.iftrue, new LabelList(cjump.iffalse, null))));
        } else if (stm instanceof EXP exp) {
            if (exp.exp instanceof CALL call) {
                munchCall(call);
            } else {
                munchExp(exp.exp); // ignore result
            }
        } else {
            emit(new AssemOPER("# unknown Stm", null, null));
        }
    }

    private void munchMove(MOVE move) {
        Exp_ dst = move.dst;
        Exp_ src = move.src;

        if (dst instanceof TEMP tdst) {
            Temp d = tdst.temp;
            if (src instanceof CONST c) {
                emit(new AssemOPER("li `d0, " + c.value, new TempList(d, null), null));
            } else if (src instanceof TEMP tsrc) {
                emit(new AssemMOVE("move `d0, `s0", d, tsrc.temp));
            } else if (src instanceof NAME name) {
                emit(new AssemOPER("la `d0, " + name.label, new TempList(d, null), null));
            } else if (src instanceof MEM mem) {
                if (mem.exp instanceof BINOP binop && binop.binop == BINOP.PLUS) {
                    // Otimização para lw com offset
                    if (binop.left instanceof TEMP temp && binop.right instanceof CONST c) {
                        emit(new AssemOPER("lw `d0, " + c.value + "(`s0)",
                                new TempList(d, null), new TempList(temp.temp, null)));
                        return;
                    } else if (binop.left instanceof CONST c && binop.right instanceof TEMP temp) {
                        emit(new AssemOPER("lw `d0, " + c.value + "(`s0)",
                                new TempList(d, null), new TempList(temp.temp, null)));
                        return;
                    }
                }
                Temp addr = munchExp(mem.exp);
                emit(new AssemOPER("lw `d0, 0(`s0)", new TempList(d, null), new TempList(addr, null)));
            } else if (src instanceof CALL call) {
                Temp result = munchCall(call);
                emit(new AssemMOVE("move `d0, `s0", d, result));
            } else {
                Temp s = munchExp(src);
                emit(new AssemMOVE("move `d0, `s0", d, s));
            }
        } else if (dst instanceof MEM mem) {
            if (mem.exp instanceof BINOP binop && binop.binop == BINOP.PLUS) {
                // Otimização para sw com offset
                if (binop.left instanceof TEMP temp && binop.right instanceof CONST c) {
                    Temp val = munchExp(src);
                    emit(new AssemOPER("sw `s0, " + c.value + "(`s1)", null,
                            new TempList(val, new TempList(temp.temp, null))));
                    return;
                } else if (binop.left instanceof CONST c && binop.right instanceof TEMP temp) {
                    Temp val = munchExp(src);
                    emit(new AssemOPER("sw `s0, " + c.value + "(`s1)", null,
                            new TempList(val, new TempList(temp.temp, null))));
                    return;
                }
            }
            Temp addr = munchExp(mem.exp);
            Temp val = munchExp(src);
            emit(new AssemOPER("sw `s0, 0(`s1)", null, new TempList(val, new TempList(addr, null))));
        } else {
            throw new RuntimeException("Invalid MOVE destination: " + dst);
        }
    }

    Temp munchExp(Exp_ exp) {
        if (exp == null) {
            throw new IllegalArgumentException("Exp_ passado para munchExp é nulo");
        }
        if (exp instanceof CONST c) {
            Temp r = new Temp();
            emit(new AssemOPER("li `d0, " + c.value, new TempList(r, null), null));
            return r;
        } else if (exp instanceof TEMP t) {
            return t.temp;
        } else if (exp instanceof BINOP binop) {
            return munchExpBinop(binop);
        } else if (exp instanceof MEM mem) {
            if (mem.exp instanceof BINOP binop && binop.binop == BINOP.PLUS) {
                // Otimização para lw com offset
                if (binop.left instanceof TEMP temp && binop.right instanceof CONST c) {
                    Temp r = new Temp();
                    emit(new AssemOPER("lw `d0, " + c.value + "(`s0)",
                            new TempList(r, null), new TempList(temp.temp, null)));
                    return r;
                } else if (binop.left instanceof CONST c && binop.right instanceof TEMP temp) {
                    Temp r = new Temp();
                    emit(new AssemOPER("lw `d0, " + c.value + "(`s0)",
                            new TempList(r, null), new TempList(temp.temp, null)));
                    return r;
                }
            }
            Temp addr = munchExp(mem.exp);
            Temp r = new Temp();
            emit(new AssemOPER("lw `d0, 0(`s0)", new TempList(r, null), new TempList(addr, null)));
            return r;
        } else if (exp instanceof CALL call) {
            // NÃO duplicar código - apenas chamar munchCall
            return munchCall(call);
        } else if (exp instanceof NAME name) {
            Temp r = new Temp();
            emit(new AssemOPER("la `d0, " + name.label, new TempList(r, null), null));
            return r;
        } else if (exp instanceof ESEQ eseq) {
            // Processa o statement primeiro
            munchStm(eseq.stm);
            // Depois processa a expressão e retorna seu resultado
            return munchExp(eseq.exp);
        } else {
            Temp r = new Temp();
            emit(new AssemOPER("# unhandled expression: " + exp.getClass().getSimpleName(),
                    new TempList(r, null), null));
            return r;
        }
    }

    Temp munchExpBinop(BINOP binop) {
        // Otimizações para operações com constantes
        if (binop.right instanceof CONST c) {
            Temp left = munchExp(binop.left);
            Temp r = new Temp();

            String op = switch (binop.binop) {
                case BINOP.PLUS -> {
                    emit(new AssemOPER("addi `d0, `s0, " + c.value,
                            new TempList(r, null), new TempList(left, null)));
                    yield null;
                }
                case BINOP.MINUS -> {
                    emit(new AssemOPER("addi `d0, `s0, " + (-c.value),
                            new TempList(r, null), new TempList(left, null)));
                    yield null;
                }
                case BINOP.AND -> {
                    emit(new AssemOPER("andi `d0, `s0, " + c.value,
                            new TempList(r, null), new TempList(left, null)));
                    yield null;
                }
                case BINOP.OR -> {
                    emit(new AssemOPER("ori `d0, `s0, " + c.value,
                            new TempList(r, null), new TempList(left, null)));
                    yield null;
                }
                default -> switch (binop.binop) {
                    case BINOP.MUL -> "mul";
                    case BINOP.DIV -> "div";
                    default -> throw new RuntimeException("Unknown BINOP: " + binop.binop);
                };
            };

            if (op != null) {
                Temp right = munchExp(binop.right);
                emit(new AssemOPER(op + " `d0, `s0, `s1",
                        new TempList(r, null),
                        new TempList(left, new TempList(right, null))));
            }
            return r;
        }

        // Caso geral
        Temp left = munchExp(binop.left);
        Temp right = munchExp(binop.right);
        Temp r = new Temp();
        TempList dst = new TempList(r, null);
        TempList src = new TempList(left, new TempList(right, null));

        String op = switch (binop.binop) {
            case BINOP.PLUS -> "add";
            case BINOP.MINUS -> "sub";
            case BINOP.MUL -> "mul";
            case BINOP.DIV -> "div";
            case BINOP.AND -> "and";
            case BINOP.OR -> "or";
            default -> throw new RuntimeException("Unknown BINOP: " + binop.binop);
        };

        emit(new AssemOPER(op + " `d0, `s0, `s1", dst, src));
        return r;
    }

    private Temp munchCall(CALL call) {
        // Preparar argumentos na ordem correta
        TempList args = munchArgs(call.args);

        // Gerar chamada
        if (call.func instanceof NAME name) {
            emit(new AssemOPER("jal " + name.label, null, args));
        } else {
            Temp f = munchExp(call.func);
            emit(new AssemOPER("jalr `s0", null, new TempList(f, args)));
        }

        // Mover resultado do registrador de retorno
        Temp dst = new Temp();
        emit(new AssemMOVE("move `d0, `s0", dst, frame.RV()));
        return dst;
    }

    private TempList munchArgs(ExpList args) {
        if (args == null) {
            return null;
        }

        // Construir lista na ordem correta
        List<Temp> argTemps = new ArrayList<>();
        ExpList current = args;
        while (current != null) {
            Temp arg = munchExp(current.head);
            argTemps.add(arg);
            current = current.tail;
        }

        // Converter para TempList na ordem correta
        TempList result = null;
        for (int i = argTemps.size() - 1; i >= 0; i--) {
            result = new TempList(argTemps.get(i), result);
        }
        return result;
    }

    public InstrList selectInstructions(IRTreeVisitor visitor) {
        if (visitor == null) {
            return null;
        }

        InstrList result = null;
        List<Exp_> expList = visitor.getListExp();

        if (expList == null || expList.isEmpty()) {
            return null;
        }

        for (int i = expList.size() - 1; i >= 0; i--) {
            Exp_ exp = expList.get(i);
            if (exp != null) {
                try {
                    List<Instr> instrs = select(new EXP(exp));
                    for (int j = instrs.size() - 1; j >= 0; j--) {
                        result = new InstrList(instrs.get(j), result);
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao processar expressão " + i + ": " + e.getMessage());
                    // Adicionar comentário de erro
                    result = new InstrList(
                            new AssemOPER("# Error processing expression: " + e.getMessage(), null, null),
                            result
                    );
                }
            }
        }

        return result;
    }
}