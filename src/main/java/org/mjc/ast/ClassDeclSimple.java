package org.mjc.ast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mjc.visitor.Visitor;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
public class ClassDeclSimple extends ClassDecl {
	private Identifier className;
	private ArrayList<VarDecl> varList;
	private ArrayList<MethodDecl> methodList;


	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
