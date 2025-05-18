package org.mjc.ast;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.mjc.visitor.Visitor;

@EqualsAndHashCode(callSuper = false)
@ToString
public class BooleanType extends Type {
	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
