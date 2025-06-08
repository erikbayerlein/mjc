package org.mjc.irtree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mjc.exceptions.IRTreeException;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
public class ESEQ extends Exp_ {
	public Stm stm;
	public Exp_ exp;

	public ExpList kids() {
		throw new IRTreeException("kids() not applicable to ESEQ");
	}
	public Exp_ build(ExpList kids) {
		throw new IRTreeException("build() not applicable to ESEQ");
	}
}
