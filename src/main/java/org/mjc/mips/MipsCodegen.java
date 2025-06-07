package org.mjc.mips;

import org.mjc.assem.*;
import org.mjc.irtree.*;

public interface MipsCodegen {
	InstrList codegen(Stm stm);
}

