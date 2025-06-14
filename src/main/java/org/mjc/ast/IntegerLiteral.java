package org.mjc.ast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mjc.visitor.Visitor;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@Builder
public class IntegerLiteral extends Expression {
	private int value;

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
