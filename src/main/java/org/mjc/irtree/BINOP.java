package org.mjc.irtree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
public class BINOP extends Exp_ {
	public int binop;
	public Exp_ left, right;
	public final static int PLUS = 0, MINUS = 1, MUL = 2, DIV = 3,
		AND = 4, OR = 5, LSHIFT = 6, RSHIFT = 7, ARSHIFT = 8, XOR = 9;

	public ExpList kids() {
		return new ExpList(left, new ExpList(right, null));
	}
	public Exp_ build(ExpList kids) {
		return new BINOP(binop, kids.head, kids.tail.head);
	}
}
