package org.mjc.irtree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mjc.temp.Label;
import org.mjc.temp.LabelList;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
public class JUMP extends Stm {
	public Exp_ exp;
	public LabelList targets;

	public JUMP(Label target) {
		this(new NAME(target), new LabelList(target, null));
	}

	public ExpList kids() {
		return new ExpList(exp, null);
	}

	public Stm build(ExpList kids) {
		return new JUMP(kids.head, targets);
	}
}
