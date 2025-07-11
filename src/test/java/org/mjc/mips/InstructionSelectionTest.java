//package org.mjc.mips;
//
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.MethodSource;
//import org.junit.jupiter.api.DisplayName;
//import org.mjc.ast.Program;
//import org.mjc.canon.BasicBlocks;
//import org.mjc.canon.Canon;
//import org.mjc.canon.TraceSchedule;
//import org.mjc.assem.Instr;
//import org.mjc.irtree.StmList;
//import org.mjc.mips.MipsFrame;
//import org.mjc.mjc.MjcCompiler;
//import org.mjc.parser.AntlrParser;
//import org.mjc.visitor.irtree.IRTreeVisitor;
//import org.mjc.visitor.irtree.ProcFrag;
//import org.mjc.visitor.symbols.MainTable;
//import org.mjc.visitor.types.TypeCheckingVisitor;
//
//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.List;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class InstructionSelectionTest {
//
//	private static final String TEST_FILES_DIR = "src/test/resources/testFiles/";
//
//	static Stream<Path> testFilesProvider() throws Exception {
//		return Files.list(Path.of(TEST_FILES_DIR))
//				.filter(Files::isRegularFile);
//	}
//
//	@ParameterizedTest
//	@MethodSource("testFilesProvider")
//	@DisplayName("Deve gerar instruções MIPS para todos os arquivos de testFiles")
//	void deveGerarInstrucoesMIPSDeTodosOsArquivos(Path filePath) throws Exception {
//		try {
//			String content = Files.readString(filePath);
//			InputStream inputStream = new ByteArrayInputStream(content.getBytes());
//
//			MjcCompiler compiler = MjcCompiler.builder().build();
//			Program program = compiler.getAbstractSyntaxTreeFromStream(inputStream);
//			assertNotNull(program, "Program should not be null for file: " + filePath.getFileName());
//
//			compiler.isSemanticallyOkOrThrow(program);
//
//			MipsFrame frame = new MipsFrame();
//			MainTable table = ((TypeCheckingVisitor) compiler.getSemanticAnalysis()).getMainTable();
//			IRTreeVisitor irTreeVisitor = compiler.getIRTreeVisitor(table, program, frame);
//			ProcFrag proc = (ProcFrag) irTreeVisitor.getInitialFrag().getNext();
//			StmList linear = Canon.linearize(proc.getBody());
//			TraceSchedule schedule = new TraceSchedule(new BasicBlocks(linear));
//
//			InstructionSelector selector = new InstructionSelector(frame);
//			List<Instr> instrs = new java.util.ArrayList<>();
//			for (StmList list = schedule.stms; list != null; list = list.tail) {
//				instrs.addAll(selector.select(list.head));
//			}
//
//			assertNotNull(instrs);
//			assertFalse(instrs.isEmpty(), "Assembly should be generated for " + filePath.getFileName());
//
//		} catch (Exception e) {
//			System.err.println("Erro ao processar arquivo: " + filePath.getFileName());
//			e.printStackTrace();
//			fail("Erro ao processar arquivo: " + filePath.getFileName() + " - " + e.getMessage());
//		}
//}