package org.mjc.visitor.irtree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mjc.irtree.Exp_;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Exp {

	public Exp_ exp;

	public Exp_ unEx() {
		return exp;
	}
}
