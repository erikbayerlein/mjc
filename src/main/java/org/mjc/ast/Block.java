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
	@Builder.Default
	private ArrayList<Statement> stmts = new ArrayList<>();

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
