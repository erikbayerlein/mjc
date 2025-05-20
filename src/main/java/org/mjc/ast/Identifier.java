package org.mjc.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.mjc.visitor.Visitor;

@EqualsAndHashCode(callSuper = false)
@ToString
@Data
@AllArgsConstructor
public class Identifier extends Node {
	private String name;

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
