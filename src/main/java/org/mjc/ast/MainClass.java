package org.mjc.ast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mjc.visitor.Visitor;

import java.util.ArrayList;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
public class MainClass extends Node {
	private Identifier className;
	private Identifier argsName;
	@Builder.Default
	private ArrayList<Statement> statements = new ArrayList<>();

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}