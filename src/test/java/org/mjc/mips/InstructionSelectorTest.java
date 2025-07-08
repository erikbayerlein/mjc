package org.mjc.mips;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.DisplayName;
import org.mjc.ast.Program;
import org.mjc.canon.BasicBlocks;
import org.mjc.canon.Canon;
import org.mjc.canon.TraceSchedule;
import org.mjc.assem.Instr;
import org.mjc.irtree.StmList;
import org.mjc.mips.MipsFrame;
import org.mjc.mjc.MjcCompiler;
import org.mjc.parser.AntlrParser;
import org.mjc.temp.DefaultMap;
import org.mjc.visitor.irtree.Frag;
import org.mjc.visitor.irtree.IRTreeVisitor;
import org.mjc.visitor.irtree.ProcFrag;
import org.mjc.visitor.symbols.MainTable;
import org.mjc.visitor.types.TypeCheckingVisitor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.mjc.assem.*;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Instruction Selector Tests")
class InstructionSelectorTest {


    private static final String TEST_FILES_DIR = "src/test/resources/testFiles/";

	static Stream<Path> testFilesProvider() throws Exception {
		return Files.list(Path.of(TEST_FILES_DIR))
				.filter(Files::isRegularFile);
    }

    @ParameterizedTest
    @MethodSource("testFilesProvider")
    @DisplayName("Deve gerar instruções MIPS para todos os arquivos de testFiles")
    void validateInstructionSelectionFromFile(Path filePath) throws Exception {
        String content = Files.readString(filePath);
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        MjcCompiler compiler = MjcCompiler.builder().build();

        // Fase 1: Parser
        Program program = compiler.getAbstractSyntaxTreeFromStream(inputStream);
        assertNotNull(program, "Program should not be null for file: " + filePath.getFileName());

        // Fase 2: Análise semântica
        compiler.isSemanticallyOkOrThrow(program);

        // Fase 3: IRTree
        MipsFrame frame = new MipsFrame();
        var typeChecker = (TypeCheckingVisitor) compiler.getSemanticAnalysis();
        MainTable table = typeChecker.getMainTable();
        IRTreeVisitor visitor = compiler.getIRTreeVisitor(table, program, frame);
        visitor.visit(program);

        List<Instr> instrs = new java.util.ArrayList<>();
        Frag frag = visitor.getInitialFrag();

        while (frag != null) {
            if (frag instanceof ProcFrag proc) {
                // Canonicaliza e agenda
                StmList linear = Canon.linearize(proc.getBody());
                TraceSchedule schedule = new TraceSchedule(new BasicBlocks(linear));

                // Seleção de instruções
                MaximalMunchCodegen selector = new MaximalMunchCodegen(proc.getFrame());
                for (StmList list = schedule.stms; list != null; list = list.tail) {
                    if (list.head == null) {
                        throw new IllegalStateException("StmList.head é nulo em " + filePath.getFileName());
                    }
                    InstrList instrList = selector.codegen(list.head);
                    for (InstrList il = instrList; il != null; il = il.tail) {
                        instrs.add(il.head);
                    }
                }
            }
            frag = frag.getNext();
        }

        // Asserts
        assertNotNull(instrs,"Instructions list must not be null");
        assertFalse(instrs.isEmpty(), "Assembly should be generated for " + filePath.getFileName());

        // Output
        System.out.println("\nGenerated instructions for " + filePath.getFileName() + ":");
        instrs.forEach(instr -> System.out.println(instr.format(new DefaultMap())));
    }

//    @Test
//    public void testInstructionSelection_ExampleProgram() throws Exception {
//        validateInstructionSelectionFromFile("ExampleProgram.java");
//    }
}
