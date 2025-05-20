package org.mjc.ast;

import lombok.*;

import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class FormalList {
	@Builder.Default
	private ArrayList<Formal> formals;

	public void addFormal(Formal formal) {
		formals.add(formal);
	}
}
