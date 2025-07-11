package org.mjc.mips;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mjc.assem.Instr;
import org.mjc.visitor.irtree.IRTreeVisitor;

import java.util.List;
import java.util.stream.Stream;


    /**
     * Testa a seleção de instruções MIPS para um IR Tree simples
     */
    @Test
    public void testSimpleInstructionSelection() {
        // Criar um frame MIPS
        MipsFrame frame = new MipsFrame();


        // Criar uma árvore IR simples (exemplo: uma constante)
        Stm simpleStm = new EXP(new org.mjc.irtree.CONST(42));


    }

    /**
     * Testa a seleção de instruções MIPS para cada arquivo Java da pasta
     * usando uma implementação simplificada
     */
    @ParameterizedTest


        MipsFrame frame = new MipsFrame();


        }
    }
            }
    }
}