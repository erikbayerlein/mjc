package org.mjc.visitor.irtree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mjc.irtree.ExpAbstract;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Exp {

	public ExpAbstract exp;

	public ExpAbstract unEx() {
		return exp;
	}
}
