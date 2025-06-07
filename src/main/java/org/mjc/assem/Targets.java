package org.mjc.assem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mjc.temp.LabelList;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class Targets {
	public LabelList labels;
}