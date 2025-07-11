package org.mjc.assem;

import lombok.*;
import org.mjc.temp.Label;
import org.mjc.temp.TempList;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class AssemLABEL extends Instr {
	public Label label;

	public AssemLABEL(String a, Label l) {
		assem = a;
		label = l;
	}

	public TempList use() {
		return null;
	}
	public TempList def() {
		return null;
	}
	public Targets jumps() {
		return null;
	}

}
