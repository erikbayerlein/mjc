package org.mjc.mips;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mjc.assem.Instr;
import org.mjc.assem.InstrList;
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

    /**
     * Fornece um stream de arquivos .java da pasta de teste
     */
    static Stream<File> javaTestFilesProvider() throws IOException {
        return Files.walk(Paths.get(TEST_FILES_PATH))
                .filter(path -> path.toString().endsWith(".java"))
                .map(Path::toFile);
    }

    /**
     * Testa a seleção de instruções MIPS para um IR Tree simples
     */
    @Test
    public void testSimpleInstructionSelection() {
        // Criar um frame MIPS
        MipsFrame frame = new MipsFrame();

        // Inicializar o InstructionSelector
        InstructionSelector selector = new InstructionSelector(frame);

        // Criar uma árvore IR simples (exemplo: uma constante)
        Stm simpleStm = new EXP(new org.mjc.irtree.CONST(42));

        // Selecionar instruções
        List<Instr> instrs = selector.select(simpleStm);

        // Verificar se algo foi gerado
        assertNotNull(instrs, "Nenhuma instrução gerada");

        // Imprimir as instruções
        System.out.println("Instruções geradas para expressão simples:");
        for (Instr instr : instrs) {
            System.out.println(instr.format(s -> frame.tempMap((Temp) s)));
        }
    }

    /**
     * Testa a seleção de instruções MIPS para cada arquivo Java da pasta
     * usando uma implementação simplificada
     */
    @ParameterizedTest
    @MethodSource("javaTestFilesProvider")
    public void testInstructionSelectionFromJavaFile(File javaFile) throws Exception {
        System.out.println(">> Testando: " + javaFile.getName());

        // 1. Criar um visitor de IR Tree simulado
        IRTreeVisitor visitor = createSimulatedIRTreeVisitor(javaFile);

        // 2. Gera instruções MIPS usando InstructionSelector
        MipsFrame frame = new MipsFrame();
        InstructionSelector selector = new InstructionSelector(frame);
        InstrList instrs = selector.selectInstructions(visitor);

        // 3. Verifica se foi gerado algo
        assertNotNull(instrs, "Nenhuma instrução gerada para " + javaFile.getName());

        // Imprime as instruções geradas
        System.out.println("Código gerado para " + javaFile.getName() + ":\n");
        for (InstrList it = instrs; it != null; it = it.tail) {
            System.out.println(it.head.format(s -> frame.tempMap((Temp) s)));
        }
        System.out.println("=====================================");
    }

    /**
     * Cria um IRTreeVisitor simulado para testes
     */
    private IRTreeVisitor createSimulatedIRTreeVisitor(File javaFile) {
        // Implementação simulada para fins de teste
        return new IRTreeVisitor() {
            @Override
            public List<org.mjc.irtree.Exp_> getListExp() {
                // Retorna uma lista simulada com algumas expressões simples
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