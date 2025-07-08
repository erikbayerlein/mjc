package org.mjc.temp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Data
@Builder
public class Label {
	private static int count = 0;
	private String name;
	public static void reset() {
		count = 0;
	}
	public Label() {
		this("L" + count++);
	}

	public String toString() {
		return name;
	}
}
