package org.mjc.assem;

import lombok.*;
import org.mjc.temp.Temp;
import org.mjc.temp.TempList;


@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class AssemMOVE extends Instr {
	public Temp dst;
	public Temp src;

	public AssemMOVE(String a, Temp d, Temp s) {
		assem = a;
		dst = d;
		src = s;
	}

	public TempList use() {
		return new TempList(src, null);
	}
	public TempList def() {
		return new TempList(dst, null);
	}
	public Targets jumps() {
		return null;
	}

}