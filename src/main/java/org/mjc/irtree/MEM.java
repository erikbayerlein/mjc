package org.mjc.irtree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
public class MEM extends Exp_ {
	public Exp_ exp;

	public ExpList kids() {
		return new ExpList(exp, null);
	}
	public Exp_ build(ExpList kids) {
		return new MEM(kids.head);
	}
}