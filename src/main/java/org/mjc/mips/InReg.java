package org.mjc.mips;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mjc.frame.Access;
import org.mjc.irtree.Exp_;
import org.mjc.irtree.TEMP;
import org.mjc.temp.Temp;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Data
@Builder
public class InReg extends Access {
	Temp temp;

	public Exp_ exp(Exp_ fp) {
		return new TEMP(temp);
	}

	public String toString() {
		return temp.toString();
	}
}