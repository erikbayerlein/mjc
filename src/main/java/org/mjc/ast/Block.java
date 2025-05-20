package org.mjc.ast;

import lombok.*;
import org.mjc.visitor.Visitor;

import java.util.ArrayList;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Block extends Statement {
	@Builder.default
	private ArrayList<Statement> stmts;

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
