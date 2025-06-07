package org.mjc.canon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mjc.irtree.StmList;


@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class StmListList {
	public StmList head;
	public StmListList tail;
}

