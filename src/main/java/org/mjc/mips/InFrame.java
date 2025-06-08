package org.mjc.mips;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mjc.frame.Access;
import org.mjc.irtree.BINOP;
import org.mjc.irtree.CONST;
import org.mjc.irtree.Exp_;
import org.mjc.irtree.MEM;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Data
@Builder
public class InFrame extends Access {
	int offset;

	public Exp_ exp(Exp_ fp) {
		return new MEM
			(new BINOP(BINOP.PLUS, fp, new CONST(offset)));
	}

	public String toString() {
		return Integer.toString(this.offset);
	}
}
