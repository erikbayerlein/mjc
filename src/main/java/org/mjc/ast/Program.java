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
public class Program extends Node {
	private MainClass mainClass;
	@Builder.Default
	private ArrayList<ClassDecl> classes;

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}