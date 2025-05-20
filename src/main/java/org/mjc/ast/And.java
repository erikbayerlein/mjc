package org.mjc.ast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mjc.visitor.Visitor;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
public class And extends Expression {
	private Expression left;
	private Expression right;

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
