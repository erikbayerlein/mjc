package org.mjc.canon;

import lombok.*;
import org.mjc.irtree.CALL;
import org.mjc.irtree.EXP;
import org.mjc.irtree.ExpList;
import org.mjc.irtree.Stm;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class ExpCall extends Stm {
	CALL call;

	public ExpList kids() {
		return call.kids();
	}

	public Stm build(ExpList kids) {
		return new EXP(call.build(kids));
	}
}