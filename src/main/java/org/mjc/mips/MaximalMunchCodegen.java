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
		munchStm(s);
		InstrList l = ilist;
		ilist = last = null;
		// Garante que nunca retorna null
		return l != null ? l : new InstrList(new AssemOPER("# no-op\n", null, null), null);
	}

	private void emit(Instr instr) {
		if (last != null) {
			last = ilist.tail = new InstrList(instr, null);
		} else {
			last = ilist = new InstrList(instr, null);
		}
	}

	private TempList munchArgs(ExpList args) {
		int i = 0;
		Temp[] argRegisters = {MipsFrame.A0, MipsFrame.A1, MipsFrame.A2, MipsFrame.A3};
		TempList head = null;
		TempList tail = null;

		while (args != null && i < argRegisters.length) {
			Temp argTemp = munchExp(args.head);
			emit(new AssemOPER(
					"move `d0, `s0\n",
					new TempList(argRegisters[i], null),
					new TempList(argTemp, null)
			));

			TempList current = new TempList(argRegisters[i], null);
			if (head == null) {
				head = current;
				tail = current;
			} else {
				tail.tail = current;
				tail = current;
			}

			args = args.tail;
			i++;
		}
		return head;
	}


	public void munchStm(Stm stm) {
		if (stm instanceof SEQ seq) {
			munchStm(seq.left);
			munchStm(seq.right);
		} else if (stm instanceof MOVE move) {
			munchStmMove(move.dst, move.src);
		} else if (stm instanceof LABEL label) {
			emit(new AssemLABEL(label.label + ":\n", label.label));
		} else if (stm instanceof JUMP jump && jump.exp instanceof NAME name) {
			emit(new AssemOPER("j `j0\n", null, null, new LabelList(name.label, null)));
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
			emit(new AssemOPER(op + " `s0, `s1, `j0\n", null,
					new TempList(left, new TempList(right, null)),
					new LabelList(cjump.iftrue, new LabelList(cjump.iffalse, null))));
		} else if (stm instanceof EXP exp) {
			if (exp.exp instanceof CALL call) {
				munchExpCall(call);
			} else {
				munchExp(exp.exp); // ignora o resultado
			}
		} else {
			emit(new AssemOPER("# unknown Stm\n", null, null));
		}
	}

	public void munchStmCJUMP(CJUMP cjump) {
		String relop = switch (cjump.relop) {
			case CJUMP.EQ -> "beq";
			case CJUMP.GE -> "bge";
			case CJUMP.LT -> "blt";
			case CJUMP.NE -> "bne";
			case CJUMP.GT -> "bgt";
			case CJUMP.LE -> "ble";
			default -> throw new IllegalArgumentException("Operador relacional não suportado: " + cjump.relop);
		};
		Temp l = munchExp(cjump.left);
		Temp r = munchExp(cjump.right);
		emit(new AssemOPER(relop + "`s0, `s1, `j0\n", null, new TempList(l, new TempList(r, null)),
			new LabelList(cjump.iftrue, new LabelList(cjump.iffalse, null))));
	}

	private void munchStmJUMP(JUMP jump) {
		NAME jname = ((NAME) jump.exp);
		emit(new AssemOPER("jump `j0\n", null, null, new LabelList(jname.label, null)));
	}

	public void munchStmSeq(SEQ seq) {
		munchStm(seq.left);
		munchStm(seq.right);
	}

	public void munchStmMove(Exp_ dst, Exp_ src) {
		if (dst instanceof MEM mem) {
			munchStmMove(mem, src);
		} else if (dst instanceof TEMP dstTemp && src instanceof CALL call) {
			Temp result = munchExpCall(call);
			emit(new AssemOPER("move `d0, `s0\n",
					new TempList(dstTemp.temp, null),
					new TempList(result, null)));
		} else if (dst instanceof TEMP temp) {
			Temp srcTemp = munchExp(src);
			emit(new AssemOPER("move `d0, `s0\n",
					new TempList(temp.temp, null),
					new TempList(srcTemp, null)));
		}

	}

	void munchStmMove(MEM dst, Exp_ src) {
		Temp value = munchExp(src);
		Temp addr = munchExp(dst.exp);

		emit(new AssemOPER("sw `s0, 0(`s1)\n", null, new TempList(value, new TempList(addr, null))));
	}

	Temp munchExp(Exp_ exp2) {
		if (exp2 instanceof CONST c) {
			return munchExpConst(c);
		}

		if (exp2 instanceof TEMP t) {
			return munchExpTemp(t);
		}

		if (exp2 instanceof BINOP b) {
			return munchExpBinop(b);
		}

		if (exp2 instanceof CALL c) {
			return munchExpCall(c);
		}

		if (exp2 instanceof MEM m) {
			return munchExpMem(m);
		}

		if (exp2 instanceof NAME name) {
			// Carrega o endereço do label (ex: para acessar uma string, ou endereço de função)
			Temp temp = new Temp();
			emit(new AssemOPER("la `d0, " + name.label + "\n", new TempList(temp, null), null));
			return temp;
		}

		if (exp2 instanceof ESEQ eseq) {
			// Executa primeiro a instrução do ESEQ, depois avalia a expressão
			munchStm(eseq.stm);
			return munchExp(eseq.exp);
		}

		throw new RuntimeException("Unsupported expression: " + exp2.getClass());
	}

	Temp munchExpCall(CALL call) {
		// 1. Coloca os argumentos nos registradores $a0–$a3 e coleta os temporários
		Temp[] argRegisters = {MipsFrame.A0, MipsFrame.A1, MipsFrame.A2, MipsFrame.A3};
		TempList srcTemps = null;
		ExpList args = call.args;
		int i = 0;

		while (args != null && i < argRegisters.length) {
			Temp argTemp = munchExp(args.head);

			// move `d0, `s0 (argumento -> registrador de argumento)
			emit(new AssemOPER("move `d0, `s0\n",
					new TempList(argRegisters[i], null),
					new TempList(argTemp, null)));

			// constrói a TempList para a instrução de chamada (jal)
			srcTemps = new TempList(argRegisters[i], srcTemps);

			args = args.tail;
			i++;
		}

		// 2. Chama a função com jal <label>
		Label label = ((NAME) call.func).label;
		emit(new AssemOPER("jal " + label + "\n",
				null,
				srcTemps)); // argumentos como fonte

		// 3. Move o valor de retorno de $v0 para um novo temporário
		Temp dst = new Temp();
		emit(new AssemOPER("move `d0, `s0\n",
				new TempList(dst, null),
				new TempList(frame.RV(), null)));

		return dst;
	}

	Temp munchExpMem(MEM mem) {
		if (mem.exp instanceof BINOP b &&
				b.binop == BINOP.PLUS &&
				b.right instanceof CONST c &&
				b.left instanceof TEMP t) {

			Temp addr = munchExp(b.left);
			Temp dst = new Temp();
			emit(new AssemOPER("lw `d0, " + c.value + "(`s0)\n", new TempList(dst, null), new TempList(addr, null)));
			return dst;
		}
		// Fallback
		Temp addr = munchExp(mem.exp);
		Temp dst = new Temp();
		emit(new AssemOPER("lw `d0, 0(`s0)\n", new TempList(dst, null), new TempList(addr, null)));
		return dst;
	}


	Temp munchExpBinop(BINOP binop) {
		Temp temp_reg = new Temp();
		TempList d = new TempList(temp_reg, null);
		TempList munchTempList = new TempList(munchExp(binop.left), new TempList(munchExp(binop.right), null));

		switch (binop.binop) {
			case BINOP.PLUS: {
				if (binop.right instanceof CONST cons) {
					emit(new AssemOPER("addi `d0, `s0," + cons.value + "\n", d, munchTempList));
				} else if (binop.left instanceof CONST cons) {
					emit(new AssemOPER("addi `d0, `s0," + cons.value + "\n", d, munchTempList));
				} else {
					emit(new AssemOPER("add `d0, `s0, `s1 \n", d, munchTempList));
				}
				break;
			}
			case BINOP.MINUS: {
				if (binop.right instanceof CONST cons) {
					emit(new AssemOPER("subi `d0, `s0," + cons.value + "\n", d, munchTempList));
				} else if (binop.left instanceof CONST cons) {
					emit(new AssemOPER("subi `d0, `s0," + cons.value + "\n", d, munchTempList));
				} else {
					emit(new AssemOPER("sub `d0, `s0, `s1 \n", d, munchTempList));
				}
				break;
			}
			case BINOP.MUL:
				emit(new AssemOPER("mul `d0, `s0, `s1 \n", d, munchTempList));
				break;
			case BINOP.DIV:
				emit(new AssemOPER("div `s0,`s1\nmflo `d0\n", d, munchTempList));
				break;
			default:
				break;
		}
		return temp_reg;
	}

	Temp munchExpTemp(TEMP temp) {
		return temp.temp;
	}

	Temp munchExpConst(CONST cons) {
		Temp temp = new Temp();
		emit(new AssemOPER("li `d0, " + cons.value + "\n", new TempList(temp, null), null));
		return temp;
	}
}
