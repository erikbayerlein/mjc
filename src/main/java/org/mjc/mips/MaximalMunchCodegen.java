package org.mjc.mips;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.mjc.assem.*;
import org.mjc.irtree.*;
import org.mjc.temp.Label;
import org.mjc.temp.LabelList;
import org.mjc.temp.Temp;
import org.mjc.temp.TempList;

@EqualsAndHashCode()
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaximalMunchCodegen implements MipsCodegen {
	private MipsFrame frame;
	private InstrList ilist;
	private InstrList last;

	public MaximalMunchCodegen(MipsFrame f) {
		frame = f;
		ilist = null;
		last = null;
	}

	public MaximalMunchCodegen(MipsFrame f, InstrList list) {
		frame = f;
		ilist = list;
		last = list;

		while (last.tail != null) {
			last = last.tail;
		}
	}

	public InstrList codegen(Stm s) {
		InstrList l;
		munchStm(s);
		l = ilist;
		ilist = last = null;
		return l;
	}

	private void emit(Instr instr) {
		if (last != null) {
			last = last.tail = new InstrList(instr, null);
		} else {
			last = ilist = new InstrList(instr, null);
		}
	}

	private TempList munchArgs(int i, ExpList args) {
		TempList res = null;
		while (args != null) {
			Temp arg = munchExp(args.head);
			res = new TempList(arg, res); // reverse order
			args = args.tail;
		}
		return res;
	}

	public void munchStm(Stm stm) {
		if (stm == null) {
			throw new IllegalArgumentException("Stm passado para munchStm é nulo");
		}
		if (stm instanceof SEQ seq) {
			munchStm(seq.left);
			munchStm(seq.right);
		} else if (stm instanceof MOVE move) {
			munchStmMove(move.dst, move.src);
		} else if (stm instanceof LABEL label) {
			emit(new AssemLABEL(label.label + ":", label.label));
		} else if (stm instanceof JUMP jump) {
			NAME jname = ((NAME) jump.exp);
			emit(new AssemOPER("j `j0", null, null, new LabelList(jname.label, null)));
		} else if (stm instanceof CJUMP cjump) {
			String relop = switch (cjump.relop) {
				case CJUMP.EQ -> "beq";
				case CJUMP.NE -> "bne";
				case CJUMP.LT -> "blt";
				case CJUMP.GE -> "bge";
				default -> throw new RuntimeException("Unknown relop");
			};
			Temp l = munchExp(cjump.left);
			Temp r = munchExp(cjump.right);
			emit(new AssemOPER(relop + " `s0, `s1, `j0", null,
					new TempList(l, new TempList(r, null)),
					new LabelList(cjump.iftrue, new LabelList(cjump.iffalse, null))));
		} else if (stm instanceof EXP exp) {
			if (exp.exp instanceof CALL call) {
				if (call.args != null && call.args.head != null) {
					Temp arg = munchExp(call.args.head);
					emit(new AssemMOVE("move $a0, `s0", frame.RV(), arg));
				}
				if (call.func instanceof NAME name) {
					emit(new AssemOPER("jal " + name.label, null, null));
				} else {
					Temp f = munchExp(call.func);
					emit(new AssemOPER("jalr `s0", null, new TempList(f, null)));
				}
			} else {
				munchExp(exp.exp);
			}
		}
	}

	public void munchStmMove(Exp_ dst, Exp_ src) {
		if (dst instanceof TEMP tdst) {
			Temp d = tdst.temp;

			if (src instanceof CONST c) {
				emit(new AssemOPER("li `d0, " + c.value, new TempList(d, null), null));
			} else if (src instanceof TEMP tsrc) {
				emit(new AssemMOVE("move `d0, `s0", d, tsrc.temp));
			} else if (src instanceof CALL call) {
				TempList args = munchArgs(0, call.args);
				if (call.func instanceof NAME name) {
					emit(new AssemOPER("jal " + name.label, null, args));
				} else {
					Temp f = munchExp(call.func);
					emit(new AssemOPER("jalr `s0", null, new TempList(f, args)));
				}
				emit(new AssemMOVE("move `d0, $v0", d, frame.RV()));
			} else {
				Temp s = munchExp(src);
				emit(new AssemMOVE("move `d0, `s0", d, s));
			}
		} else if (dst instanceof MEM mem) {
			Temp addr = munchExp(mem.exp);
			Temp val = munchExp(src);
			emit(new AssemOPER("sw `s0, 0(`s1)", null, new TempList(val, new TempList(addr, null))));
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
			Temp addr = munchExp(mem.exp);
			Temp r = new Temp();
			emit(new AssemOPER("lw `d0, 0(`s0)", new TempList(r, null), new TempList(addr, null)));
			return r;
		} else if (exp instanceof CALL call) {
			TempList args = munchArgs(0, call.args);
			if (call.func instanceof NAME name) {
				emit(new AssemOPER("jal " + name.label, null, args));
			} else {
				Temp f = munchExp(call.func);
				emit(new AssemOPER("jalr `s0", null, new TempList(f, args)));
			}
			Temp r = new Temp();
			emit(new AssemMOVE("move `d0, $v0", r, frame.RV()));
			return r;
		} else {
			Temp r = new Temp();
			emit(new AssemOPER("# unhandled expression", new TempList(r, null), null));
			return r;
		}
	}

	Temp munchExpCall(CALL call) {
		Temp dst = new Temp();
		TempList args = munchArgs(0, call.args);
		if (call.func instanceof NAME name) {
			emit(new AssemOPER("jal " + name.label, null, args));
		} else {
			Temp f = munchExp(call.func);
			emit(new AssemOPER("jalr `s0", null, new TempList(f, args)));
		}
		emit(new AssemMOVE("move `d0, $v0", dst, frame.RV()));
		return dst;
	}

	Temp munchExpBinop(BINOP binop) {
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
			default -> throw new RuntimeException("Unknown BINOP: " + binop.binop);
		};

		emit(new AssemOPER(op + " `d0, `s0, `s1", dst, src));
		return r;
	}
}
