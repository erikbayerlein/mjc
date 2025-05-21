package org.mjc.parser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.mjc.antlr.MiniJavaParser;
import org.mjc.ast.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Stack;

public class ASTGenerator extends org.mjc.antlr.MiniJavaBaseListener implements org.antlr.v4.runtime.tree.ParseTreeListener {

	private Program prog;
	private MainClass mc;
	private ClassDeclList cdl;

	private Stack<MethodDeclList> mdlStack;
	private Stack<ArrayList<MethodDecl>> methodDecls;
	private Stack<FormalList> flclStack;
	private Stack<VarDeclList> vdlStack;
	private Stack<ArrayList<VarDecl>> varDecls;
	private Stack<Type> typeStack;
	private Stack<Statement> stmStack;
	private Stack<Stack<Statement>> stmStackStack;
	private Stack<StatementList> stmListStack;
	private Stack<Expression> expStack;
	private Stack<Stack<Expression>> expStackStack;
	private Stack<ExpressionList> expListStack;
	private Stack<ArrayList<Statement>> statement;
	private Stack<Object> stack;

	public ASTGenerator() {
		super();
	}

	public Program getProgram() {
		return prog;
	}

	private Identifier getId(ParseTree ctx, int i) {
		return new Identifier(ctx.getChild(i).getText());
	}

	public void enterProgram(MiniJavaParser.ProgramContext ctx) {
		mdlStack = new Stack<>();
		methodDecls = new Stack<>();
		flclStack = new Stack<>();
		vdlStack = new Stack<>();
		varDecls = new Stack<>();
		typeStack = new Stack<>();
		expStack = new Stack<>();
		expStackStack = new Stack<>();
		expListStack = new Stack<>();

		stmStack = new Stack<>();
		stmStackStack = new Stack<>();
		stmListStack = new Stack<>();
		statement = new Stack<>();
		stack = new Stack<>();
		cdl = new ClassDeclList();
	}

	public void exitProgram(MiniJavaParser.ProgramContext ctx) {
		ArrayList<ClassDecl> classDecls = new ArrayList<>();
		for (int i = 0; i < ctx.classDeclaration().size(); i++) {
			classDecls.add(0, (ClassDecl) stack.pop());
		}
		MainClass mainClass = (MainClass) stack.pop();
		prog = new Program(mainClass, classDecls);
	}

	public void exitMainClass(MiniJavaParser.MainClassContext ctx) {
		Statement statement = ctx.statement() != null ? (Statement) stack.pop() : null;
		Identifier className = new Identifier(ctx.IDENTIFIER(0).getText());
		Identifier argName = new Identifier(ctx.IDENTIFIER(1).getText());
		ArrayList<Statement> statements = new ArrayList<>();
		if (statement != null) {
			statements.add(statement);
		}
		stack.push(new MainClass(className, argName, statements));
	}

	public void exitClassDecl(MiniJavaParser.ClassDeclContext ctx) {
		Identifier className = new Identifier(ctx.IDENTIFIER(0).getText());
		Identifier superClass = ctx.EXTENDS() != null ? new Identifier(ctx.IDENTIFIER(1).getText()) : null;

		ArrayList<MethodDecl> methods = new ArrayList<>();
		for (int i = 0; i < ctx.methodDeclaration().size(); i++) {
			methods.add(0, (MethodDecl) stack.pop());
		}

		ArrayList<VarDecl> vars = new ArrayList<>();
		for (int i = 0; i < ctx.varDeclaration().size(); i++) {
			vars.add(0, (VarDecl) stack.pop());
		}

		ClassDecl decl = (superClass != null)
				? new ClassDeclExtends(className, superClass, vars, methods)
				: new ClassDeclSimple(className, vars, methods);

		stack.push(decl);
	}

	public void exitMethodDecl(MiniJavaParser.MethodDeclContext ctx) {
		ArrayList<Statement> statements = new ArrayList<>();
		for (int i = 0; i < ctx.statement().size(); i++) {
			statements.add(0, (Statement) stack.pop());
		}

		ArrayList<VarDecl> vars = new ArrayList<>();
		for (int i = 0; i < ctx.varDeclaration().size(); i++) {
			vars.add(0, (VarDecl) stack.pop());
		}

		Expression returnExp = (Expression) stack.pop();

		ArrayList<Formal> formals = new ArrayList<>();
		if (ctx.formalList() != null && ctx.formalList().getChildCount() > 0) {
			formals = (ArrayList<Formal>) stack.pop();
		}

		Identifier methodName = new Identifier(ctx.IDENTIFIER().getText());
		Type returnType = (Type) stack.pop();

		stack.push(new MethodDecl(returnType, methodName, returnExp, formals, vars, statements));
	}

	public void exitFormalList(MiniJavaParser.FormalListContext ctx) {
		ArrayList<Formal> formals = new ArrayList<>();
		for (int i = 0; i < ctx.formalRest().size(); i++) {
			formals.add(0, (Formal) stack.pop()); // formalRest
		}
		if (ctx.type() != null) {
			Identifier name = new Identifier(ctx.IDENTIFIER().getText());
			Type type = (Type) stack.pop();
			formals.add(0, new Formal(type, name));
		}
		stack.push(formals);
	}

	public void exitFormalRest(MiniJavaParser.FormalRestContext ctx) {
		Identifier name = new Identifier(ctx.IDENTIFIER().getText());
		Type type = (Type) stack.pop();
		stack.push(new Formal(type, name));
	}

	public void exitVarDecl(MiniJavaParser.VarDeclContext ctx) {
		Identifier name = new Identifier(ctx.IDENTIFIER().getText());
		Type type = (Type) stack.pop();
		stack.push(new VarDecl(type, name));
	}

	public void exitTypeInteger(MiniJavaParser.TypeIntegerContext ctx) {
		stack.push(new IntegerType());
	}

	public void exitTypeBoolean(MiniJavaParser.TypeBooleanContext ctx) {
		stack.push(new BooleanType());
	}

	public void exitTypeIntArray(MiniJavaParser.TypeIntArrayContext ctx) {
		stack.push(new IntArrayType());
	}

	public void exitTypeIdentifier(MiniJavaParser.TypeIdentifierContext ctx) {
		stack.push(new IdentifierType(ctx.IDENTIFIER().getText()));
	}

	public void exitStmBlock(@NotNull MiniJavaParser.StmBlockContext ctx) {
		ArrayList<Statement> stmts = new ArrayList<>();
		for (int i = 0; i < ctx.statement().size(); i++) {
			stmts.add(0, (Statement) stack.pop());
		}
		stack.push(new Block(stmts));
	}

	public void exitStmWhile(@NotNull MiniJavaParser.StmWhileContext ctx) {
		Statement body = (Statement) stack.pop();
		Expression cond = (Expression) stack.pop();
		stack.push(new While(cond, body));
	}

	public void exitStmArrayAssign(@NotNull MiniJavaParser.StmArrayAssignContext ctx) {
		Expression value = (Expression) stack.pop();
		Expression index = (Expression) stack.pop();
		Identifier id = new Identifier(ctx.IDENTIFIER().getText());
		stack.push(new ArrayAssign(id, index, value));
	}

	public void exitStmIf(@NotNull MiniJavaParser.StmIfContext ctx) {
		Statement elseStm = (Statement) stack.pop();
		Statement thenStm = (Statement) stack.pop();
		Expression cond = (Expression) stack.pop();
		stack.push(new If(cond, thenStm, elseStm));
	}

	public void exitStmPrint(@NotNull MiniJavaParser.StmPrintContext ctx) {
		Expression exp = (Expression) stack.pop();
		stack.push(new Sout(exp));
	}

	public void exitStmAssign(@NotNull MiniJavaParser.StmAssignContext ctx) {
		Expression exp = (Expression) stack.pop();
		Identifier id = new Identifier(ctx.IDENTIFIER().getText());
		stack.push(new Assign(id, exp));
	}

	public void exitExpArrayLookup(@NotNull MiniJavaParser.ExpArrayLookupContext ctx) {
		Expression index = (Expression) stack.pop();
		Expression array = (Expression) stack.pop();
		stack.push(new ArrayLookup(array, index));
	}

	public void exitExpTimes(@NotNull MiniJavaParser.ExpTimesContext ctx) {
		Expression right = (Expression) stack.pop();
		Expression left = (Expression) stack.pop();
		stack.push(new Times(left, right));
	}

	public void exitExpAnd(@NotNull MiniJavaParser.ExpAndContext ctx) {
		Expression right = (Expression) stack.pop();
		Expression left = (Expression) stack.pop();
		stack.push(new And(left, right));
	}

	public void exitExpPlus(@NotNull MiniJavaParser.ExpPlusContext ctx) {
		Expression right = (Expression) stack.pop();
		Expression left = (Expression) stack.pop();
		stack.push(new Plus(left, right));
	}

	public void exitExpMinus(@NotNull MiniJavaParser.ExpMinusContext ctx) {
		Expression right = (Expression) stack.pop();
		Expression left = (Expression) stack.pop();
		stack.push(new Minus(left, right));
	}

	public void exitExpLessThan(@NotNull MiniJavaParser.ExpLessThanContext ctx) {
		Expression right = (Expression) stack.pop();
		Expression left = (Expression) stack.pop();
		stack.push(new LessThan(left, right));
	}

	public void exitExpIntegerLiteral(@NotNull MiniJavaParser.ExpIntegerLiteralContext ctx) {
		int value = Integer.parseInt(ctx.INTEGER_LITERAL().getText());
		stack.push(new IntegerLiteral(value));
	}

	public void exitExpIdentifierExp(@NotNull MiniJavaParser.ExpIdentifierExpContext ctx) {
		stack.push(new IdentifierExpression(ctx.IDENTIFIER().getText()));
	}

	public void exitExpNot(@NotNull MiniJavaParser.ExpNotContext ctx) {
		Expression exp = (Expression) stack.pop();
		stack.push(new Not(exp));
	}

	public void exitExpNewObject(@NotNull MiniJavaParser.ExpNewObjectContext ctx) {
		stack.push(new NewObject(new Identifier(ctx.IDENTIFIER().getText())));
	}

	public void exitExpTrue(@NotNull MiniJavaParser.ExpTrueContext ctx) {
		stack.push(new True());
	}

	public void exitExpFalse(@NotNull MiniJavaParser.ExpFalseContext ctx) {
		stack.push(new False());
	}

	public void exitExpBracket(@NotNull MiniJavaParser.ExpBracketContext ctx) {
		// Apenas repassa a subexpressão
		Expression exp = (Expression) stack.pop();
		stack.push(exp);
	}

	public void exitExpNewArray(@NotNull MiniJavaParser.ExpNewArrayContext ctx) {
		Expression size = (Expression) stack.pop();
		stack.push(new NewArray(size));
	}

	public void exitExpThis(@NotNull MiniJavaParser.ExpThisContext ctx) {
		stack.push(new This());
	}

	public void exitExpArrayLength(@NotNull MiniJavaParser.ExpArrayLengthContext ctx) {
		Expression array = (Expression) stack.pop();
		stack.push(new ArrayLength(array));
	}

	public void exitExpCall(@NotNull MiniJavaParser.ExpCallContext ctx) {
		ExpressionList args = new ExpressionList();
		if (ctx.expList() != null && ctx.expList().getChildCount() > 0) {
			if (!stack.isEmpty()) {
				args = (ExpressionList) stack.pop();
			}
		}
		Identifier method = new Identifier(ctx.IDENTIFIER().getText());
		Expression obj = null;
		if (!stack.isEmpty()) {
			obj = (Expression) stack.pop();
		} else {
			throw new java.util.EmptyStackException();
		}
		stack.push(new Call(obj, method, args));
	}

	public void exitExpRest(MiniJavaParser.ExpRestContext ctx) {
		Expression exp = (Expression) stack.pop();
		stack.push(exp);
	}

	public void exitExpList(MiniJavaParser.ExpListContext ctx) {
		ArrayList<Expression> args = new ArrayList<>();
		for (int i = 0; i < ctx.expRest().size(); i++) {
			args.add(0, (Expression) stack.pop());
		}
		if (ctx.expression() != null) {
			args.add(0, (Expression) stack.pop());
		}
		stack.push(args);
	}
}