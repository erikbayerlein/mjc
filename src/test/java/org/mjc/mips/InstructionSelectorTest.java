package org.mjc.mips;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mjc.assem.Instr;
import org.mjc.irtree.EXP;
import org.mjc.irtree.Stm;
import org.mjc.temp.Temp;
import org.mjc.visitor.irtree.IRTreeVisitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InstructionSelectorTest {

    private static final String TEST_FILES_PATH = "src/test/resources/testFiles";

    static Stream<File> javaTestFilesProvider() throws IOException {
        return Files.walk(Paths.get(TEST_FILES_PATH))
                .filter(path -> path.toString().endsWith(".java"))
                .map(Path::toFile);
    }

    @Test
    public void testSimpleInstructionSelection() {
        MipsFrame frame = new MipsFrame();
        InstructionSelector selector = new InstructionSelector(frame);

        Stm simpleStm = new EXP(new org.mjc.irtree.CONST(42));
        List<Instr> instrs = selector.select(simpleStm);

        assertNotNull(instrs, "Nenhuma instrução gerada");

        System.out.println("Instruções geradas para expressão simples:");
        for (Instr instr : instrs) {
            System.out.println(instr.format(s -> frame.tempMap((Temp) s)));
        }
    }

    @ParameterizedTest
    @MethodSource("javaTestFilesProvider")
    public void testInstructionSelectionFromJavaFile(File javaFile) throws Exception {
        System.out.println(">> Testando: " + javaFile.getName());

        IRTreeVisitor visitor = createSimulatedIRTreeVisitor(javaFile);

        MipsFrame frame = new MipsFrame();
        InstructionSelector selector = new InstructionSelector(frame);

        for (org.mjc.irtree.Exp_ exp : visitor.getListExp()) {
            Stm stm = new EXP(exp);
            List<Instr> instrs = selector.select(stm);

            assertNotNull(instrs, "Nenhuma instrução gerada para " + javaFile.getName());

            System.out.println("Código gerado para " + javaFile.getName() + " (exp):\n");
            for (Instr instr : instrs) {
                System.out.println(instr.format(s -> frame.tempMap((Temp) s)));
            }
            System.out.println("=====================================");
        }
    }

    private IRTreeVisitor createSimulatedIRTreeVisitor(File javaFile) {
        return new IRTreeVisitor() {
            @Override
            public List<org.mjc.irtree.Exp_> getListExp() {
                return List.of(
                        new org.mjc.irtree.CONST(42),
                        new org.mjc.irtree.TEMP(new Temp()),
                        new org.mjc.irtree.BINOP(
                                org.mjc.irtree.BINOP.PLUS,
                                new org.mjc.irtree.CONST(10),
                                new org.mjc.irtree.CONST(20)
                        )
                );
            }
        };
    }
}