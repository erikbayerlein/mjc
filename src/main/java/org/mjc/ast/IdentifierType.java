package org.mjc.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.mjc.visitor.Visitor;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@ToString
public class IdentifierType extends Type {
	private String s;

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
