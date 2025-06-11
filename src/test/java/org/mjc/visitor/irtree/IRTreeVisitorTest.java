package org.mjc.visitor.irtree;

import org.mjc.ast.*;
import org.mjc.irtree.*;
import org.mjc.mips.MipsFrame;
import org.mjc.temp.Label;
import org.mjc.visitor.symbols.SymbolTableVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class IRTreeVisitorTest {

	static MainClass mockedMainClass() {
		ArrayList<Statement> statements = new ArrayList<>();
		statements.add(new Sout(new IntegerLiteral(1)));

		return MainClass.builder()
				.className(new Identifier("Main"))
				.argsName(new Identifier("args"))
				.statements(new StatementList(statements))
				.build();
	}

	static SimpleEntry<Exp, Sout> mockedSout() {
		ExpList printArgs = new ExpList(new CONST(1), new ExpList(new CONST(0), null));
		Exp exp = new Exp(new CALL(new NAME(new Label("_print")), printArgs));
		Sout sout = new Sout(new IntegerLiteral(1));
		return new SimpleEntry<>(exp, sout);
	}

	static Program createTestProgram() {
		ArrayList<VarDecl> varDecls = new ArrayList<>();
		varDecls.add(VarDecl.builder()
				.name("x")
				.type(new IntegerType())
				.build());

		ArrayList<Statement> statements = new ArrayList<>();
		statements.add(new Sout(new IdentifierExpression("x")));

		ArrayList<MethodDecl> methods = new ArrayList<>();
		methods.add(MethodDecl.builder()
				.identifier("main")
				.formals(new FormalList(new ArrayList<>()))
				.varDecls(new VarDeclList(varDecls))
				.statements(new StatementList(statements))
				.type(new IntegerType())
				.returnExpression(new IntegerLiteral(1))
				.build());

		ArrayList<ClassDecl> classes = new ArrayList<>();
		classes.add(ClassDeclSimple.builder()
				.className(new Identifier("method"))
				.methods(new MethodDeclList(methods))
				.build());

		return Program.builder()
				.mainClass(mockedMainClass())
				.classes(new ClassDeclList(classes))
				.build();
	}

	static Stream<Arguments> shouldParseSimpleLiterals() {
		return Stream.of(
				Arguments.of(new IntegerLiteral(1), new Exp(new CONST(1))),
				Arguments.of(new True(), new Exp(new CONST(1))),
				Arguments.of(new False(), new Exp(new CONST(0)))
		);
	}

	static Stream<Arguments> shouldParseASOUTStatement() {
		return Stream.of(
				createSoutArgument(1),
				createSoutArgument(2),
				Arguments.of(
						new Sout(new Plus(new IntegerLiteral(1), new IntegerLiteral(2))),
						new Exp(new CALL(
								new NAME(new Label("_print")),
								new ExpList(
										new BINOP(BINOP.PLUS, new CONST(1), new CONST(2)),
										new ExpList(new CONST(0), null)
								)
						))
				)
		);
	}

	private static Arguments createSoutArgument(int value) {
		return Arguments.of(
				new Sout(new IntegerLiteral(value)),
				new Exp(new CALL(
						new NAME(new Label("_print")),
						new ExpList(new CONST(value), new ExpList(new CONST(0), null))
				))
		);
	}

	// Test Methods
	@Test
	@DisplayName("Should check a non empty list of expression")
	void shouldCheckANonEmptyListOfExpression() {
		// ARRANGE
		Program program = createTestProgram();
		SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
		program.accept(symbolTableVisitor);

		IRTreeVisitor irTreeVisitor = new IRTreeVisitor(
				symbolTableVisitor.getMainTable(),
				new MipsFrame()
		);

		// ACT
		program.accept(irTreeVisitor);

		// ASSERT
		assertFalse(irTreeVisitor.getListExp().isEmpty());
	}

	@ParameterizedTest
	@DisplayName("Should parse simple literals")
	@MethodSource
	void shouldParseSimpleLiterals(Node node, Exp expectedNode) {
		// ARRANGE
		IRTreeVisitor visitor = IRTreeVisitor.builder().build();

		// ACT
		Exp actualNode = node.accept(visitor);

		// ASSERT
		assertEquals(expectedNode, actualNode);
	}

	@DisplayName("Should parse a SOUT statement")
	@ParameterizedTest
	@MethodSource
	void shouldParseASOUTStatement(Sout node, Exp expectedNode) {
		// ARRANGE
		MipsFrame frame = new MipsFrame();
		IRTreeVisitor visitor = IRTreeVisitor.builder()
				.frame(frame)
				.build();

		// ACT
		Exp actualNode = node.accept(visitor);

		// ASSERT
		assertEquals(expectedNode, actualNode);
	}

	@Test
	@DisplayName("Should parse a if statement")
	void shouldParseIfStatement() {
		// ARRANGE
		MipsFrame frame = new MipsFrame();
		SimpleEntry<Exp, Sout> mockedSout = mockedSout();

		If ifStatement = If.builder()
				.condition(new True())
				.thenBranch(mockedSout.getValue())
				.elseBranch(mockedSout.getValue())
				.build();

		IRTreeVisitor visitor = IRTreeVisitor.builder()
				.frame(frame)
				.build();

		ESEQ expectedNode = createExpectedIfStatementNode(mockedSout);

		// ACT
		Exp actualNode = ifStatement.accept(visitor);

		// ASSERT
		assertEquals(expectedNode, actualNode.unEx());
	}

	private ESEQ createExpectedIfStatementNode(SimpleEntry<Exp, Sout> mockedSout) {
		Label trueLabel = new Label("L0");
		Label falseLabel = new Label("L1");
		Label endLabel = new Label("L2");

		SEQ trueBranch = SEQ.builder()
				.left(new LABEL(trueLabel))
				.right(new EXP(mockedSout.getKey().unEx()))
				.build();

		SEQ trueBranchWithJump = SEQ.builder()
				.left(trueBranch)
				.right(new JUMP(endLabel))
				.build();

		SEQ falseBranch = SEQ.builder()
				.left(new LABEL(falseLabel))
				.right(new EXP(mockedSout.getKey().unEx()))
				.build();

		SEQ falseBranchWithJump = SEQ.builder()
				.left(falseBranch)
				.right(new JUMP(endLabel))
				.build();

		CJUMP conditionalJump = CJUMP.builder()
				.relop(CJUMP.EQ)
				.left(new CONST(1))
				.right(new CONST(1))
				.iftrue(trueLabel)
				.iffalse(falseLabel)
				.build();

		SEQ combinedBranches = SEQ.builder()
				.left(trueBranchWithJump)
				.right(falseBranchWithJump)
				.build();

		SEQ mainSequence = SEQ.builder()
				.left(conditionalJump)
				.right(combinedBranches)
				.build();

		SEQ completeSequence = SEQ.builder()
				.left(mainSequence)
				.right(new LABEL(endLabel))
				.build();

		return ESEQ.builder()
				.stm(completeSequence)
				.exp(null)
				.build();
	}
}