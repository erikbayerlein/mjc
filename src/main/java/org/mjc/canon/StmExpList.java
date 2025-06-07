package org.mjc.canon;

import lombok.*;
import org.mjc.irtree.ExpList;
import org.mjc.irtree.Stm;


@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class StmExpList {
	Stm stm;
	ExpList exps;
}