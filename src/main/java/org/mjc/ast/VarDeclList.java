package org.mjc.ast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VarDeclList {
	@Builder.Default
	private ArrayList<VarDecl> varDecls = new ArrayList<>();

	public void addVarDecl(VarDecl varDecl) {
		varDecls.add(varDecl);
	}
}
