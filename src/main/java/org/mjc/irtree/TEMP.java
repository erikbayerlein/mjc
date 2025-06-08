package org.mjc.irtree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mjc.temp.Temp;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
public class TEMP extends Exp_ {
	public Temp temp;

	public ExpList kids() {
		return null;
	}
	public Exp_ build(ExpList kids) {
		return this;
	}
}
