package org.mjc.visitor.irtree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mjc.ast.*;
import org.mjc.irtree.*;
import org.mjc.mips.MipsFrame;
import org.mjc.temp.Label;
import org.mjc.visitor.Visitor;
import org.mjc.visitor.symbols.ClassTable;
import org.mjc.visitor.symbols.MainTable;
import org.mjc.visitor.symbols.MethodTable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Log4j2
public class IRTreeVisitor implements Visitor<Exp> {
	private MipsFrame frame;
	private Frag frag;
	private Frag initialFrag;
	@Builder.Default
	private List<Exp_> listExp = new ArrayList<>();
	@Builder.Default
	private MainTable mainTable = new MainTable();
	@Builder.Default
	private ClassTable currentClassTable = null;
	@Builder.Default
	private MethodTable currentMethodTable = null;

	public IRTreeVisitor(MainTable mainTable, MipsFrame frame) {
		this.mainTable = mainTable;
		this.frame = frame;
		this.frag = new Frag(null);
		this.initialFrag = this.frag;
		this.listExp = new ArrayList<>();
	}

	public Exp visit(And a) {
		var lheExpr = a.getLhe().accept(this);
		var rheExpr = a.getRhe().accept(this);
		if (lheExpr == null || rheExpr == null) return new Exp(new CONST(0));
		var andBinop = BINOP.builder()
				.binop(BINOP.AND)
				.left(lheExpr.unEx()).right(rheExpr.unEx())
				.build();
		addExp(andBinop);
		return new Exp(andBinop);
	}

	public Exp visit(BooleanType b) {
		return new Exp(new CONST(0));
	}

	public Exp visit(Not n) {
		var exp = n.getE().accept(this);
		if (exp == null) return new Exp(new CONST(0));
		var notBinop = BINOP.builder()
				.binop(BINOP.MINUS)
				.left(new CONST(1))
				.right(exp.unEx())
				.build();
		addExp(notBinop);
		return new Exp(notBinop);
	}

	public Exp visit(True t) {
		var constExpr = new CONST(1);
		addExp(constExpr);
		return new Exp(constExpr);
	}

	public Exp visit(False f) {
		var constExpr = new CONST(0);
		addExp(constExpr);
		return new Exp(constExpr);
	}

	public Exp visit(Identifier i) {
		var allocLocal = frame.allocLocal();
		var tempReg = allocLocal.exp(new TEMP(frame.FP()));
		addExp(tempReg);
		return new Exp(tempReg);
	}

	public Exp visit(Call c) {
		ClassTable tmpClassTable = null;
		MethodTable tmpMethodTable;
		String tmpClassSymbol = null;
		ExpList expList = null;

		int size = c.getExpressionList().getList().size();

		for (int i = size - 1; i >= 0; i--) {
			var expr = c.getExpressionList().getList().get(i).accept(this);
			if (expr == null) return new Exp(new CONST(0));
			expList = new ExpList(expr.unEx(), expList);
		}

		var ownerExp = c.getOwner().accept(this);
		if (ownerExp == null) return new Exp(new CONST(0));
		expList = new ExpList(ownerExp.unEx(), expList);

		if (c.getOwner() instanceof Call) {
			tmpClassTable = currentClassTable;
			tmpMethodTable = tmpClassTable.getMethodsContext().get(c.getMethod().getS());
			tmpClassTable = mainTable.getMap().get(tmpMethodTable.getClassParent().getClassName());
		}

		if (c.getOwner() instanceof IdentifierExpression idExp) {
			if (currentMethodTable.getLocalsContext().get(idExp.getId()) instanceof IdentifierType idType) {
				tmpClassSymbol = idType.getS();
			} else if (currentMethodTable.getParamsContext().get(idExp.getId()) instanceof IdentifierType idType)
				tmpClassSymbol = idType.getS();
			else if (currentClassTable.getFieldsContext().get(idExp.getId()) instanceof IdentifierType idType) {
				tmpClassSymbol = idType.getS();
			}
		}

		if (c.getOwner() instanceof NewObject idNewObject) {
			tmpClassTable = mainTable.getMap().get(idNewObject.getIdentifier().getS());
		}
		if (c.getOwner() instanceof This) tmpClassTable = currentClassTable;
		if (tmpClassTable != null) tmpClassSymbol = tmpClassTable.getClassName();

		var label = new Label(tmpClassSymbol + "." + c.getMethod().getS());

		var callExpr = CALL.builder()
				.func(new NAME(label))
				.args(expList)
				.build();
		addExp(callExpr);
		return new Exp(callExpr);
	}

	public Exp visit(IdentifierExpression i) {
		var allocLocal = frame.allocLocal();
		var tempReg = allocLocal.exp(new TEMP(frame.FP()));
		addExp(tempReg);
		return new Exp(tempReg);
	}

	public Exp visit(IdentifierType i) {
		return new Exp(new CONST(0));
	}

	public Exp visit(NewObject n) {
		currentClassTable = mainTable.getMap().get(n.getIdentifier().getS());
		int sizeOfFields = mainTable.getMap().get(n.getIdentifier().getS()).getFieldsContext().size();

		var parametersList = new LinkedList<Exp_>();
		parametersList.add(
				BINOP.builder()
						.binop(BINOP.MUL)
						.left(new CONST(sizeOfFields + 1))
						.right(new CONST(frame.wordSize()))
						.build()
		);

		var externalCall = frame.externalCall("malloc", parametersList);
		addExp(externalCall);
		return new Exp(externalCall);
	}

	public Exp visit(This t) {
		var tempAllocation = MEM.builder()
				.exp(new TEMP(frame.FP()))
				.build();
		addExp(tempAllocation);
		return new Exp(tempAllocation);
	}

	public Exp visit(ArrayLookup a) {
		var idx = a.getIdx().accept(this);
		var array = a.getArray().accept(this);
		if (idx == null || array == null) return new Exp(new CONST(0));
		var arrayLoopUp = MEM.builder()
				.exp(
						BINOP.builder()
								.binop(BINOP.PLUS)
								.left(array.unEx())
								.right(
										BINOP.builder()
												.binop(BINOP.MUL)
												.left(idx.unEx())
												.right(new CONST(frame.wordSize()))
												.build()
								)
								.build()
				)
				.build();
		addExp(arrayLoopUp);
		return new Exp(arrayLoopUp);
	}

	public Exp visit(ArrayAssign a) {
		var id = a.getIdentifier().accept(this);
		var idx = a.getIndex().accept(this);
		var val = a.getValue().accept(this);
		if (id == null || idx == null || val == null) return new Exp(new CONST(0));
		var offset = BINOP.builder()
				.binop(BINOP.MUL)
				.left(idx.unEx())
				.right(new CONST(frame.wordSize()))
				.build();
		var pointer = BINOP.builder()
				.binop(BINOP.PLUS)
				.left(BINOP.builder()
						.binop(BINOP.PLUS)
						.left(id.unEx()).right(new CONST(1))
						.build()
				)
				.right(offset)
				.build();

		var move = MOVE.builder()
				.dst(new MEM(pointer))
				.src(val.unEx())
				.build();

		var arrayAssign = ESEQ.builder()
				.stm(move)
				.exp(new CONST(0))
				.build();
		addExp(arrayAssign);
		return new Exp(arrayAssign);
	}

	public Exp visit(ArrayLength a) {
		var pointer = a.getArray().accept(this);
		if (pointer == null) return new Exp(new CONST(0));
		var tempAlloc = MEM.builder()
				.exp(pointer.unEx())
				.build();
		addExp(tempAlloc);
		return new Exp(tempAlloc);
	}

	public Exp visit(Plus p) {
		var lheExpr = p.getLhe().accept(this);
		var rheExpr = p.getRhe().accept(this);
		if (lheExpr == null || rheExpr == null) return new Exp(new CONST(0));
		var plusBinop = BINOP.builder()
				.binop(BINOP.PLUS)
				.left(lheExpr.unEx())
				.right(rheExpr.unEx())
				.build();
		addExp(plusBinop);
		return new Exp(plusBinop);
	}

	public Exp visit(Minus m) {
		var lheExpr = m.getLhe().accept(this);
		var rheExpr = m.getRhe().accept(this);
		if (lheExpr == null || rheExpr == null) return new Exp(new CONST(0));
		var minusBinop = BINOP.builder()
				.binop(BINOP.MINUS)
				.left(lheExpr.unEx())
				.right(rheExpr.unEx())
				.build();
		addExp(minusBinop);
		return new Exp(minusBinop);
	}

	public Exp visit(Times t) {
		var lheExpr = t.getLhe().accept(this);
		var rheExpr = t.getRhe().accept(this);
		if (lheExpr == null || rheExpr == null) return new Exp(new CONST(0));
		var timeBinop = BINOP.builder()
				.binop(BINOP.MUL)
				.left(lheExpr.unEx())
				.right(rheExpr.unEx())
				.build();
		addExp(timeBinop);
		return new Exp(timeBinop);
	}

	public Exp visit(IntegerLiteral i) {
		var integerValue = new CONST(i.getValue());
		addExp(integerValue);
		return new Exp(integerValue);
	}

	public Exp visit(IntegerType i) {
		return new Exp(new CONST(0));
	}

	public Exp visit(IntArrayType i) {
		return new Exp(new CONST(0));
	}

	public Exp visit(LessThan l) {
		var lheExpr = l.getLhe().accept(this);
		var rheExpr = l.getRhe().accept(this);
		if (lheExpr == null || rheExpr == null) return new Exp(new CONST(0));
		var lessThanBinop = BINOP.builder()
				.binop(BINOP.MINUS)
				.left(lheExpr.unEx())
				.right(rheExpr.unEx())
				.build();
		addExp(lessThanBinop);
		return new Exp(lessThanBinop);
	}

	public Exp visit(NewArray n) {
		var newArraySize = n.getSize().accept(this);
		if (newArraySize == null) return new Exp(new CONST(0));
		ExpList parametersList = null;

		var exp = BINOP.builder()
				.binop(BINOP.MUL)
				.left(
						BINOP.builder()
								.binop(BINOP.PLUS)
								.left(newArraySize.unEx())
								.right(new CONST(1))
								.build()
				)
				.right(new CONST(frame.wordSize()))
				.build();

		parametersList = new ExpList(exp.unEx(), argsList);

		var arrayAlloc = frame.externalCall("initArray", parametersList);
		addExp(arrayAlloc);
		return new Exp(arrayAlloc);
	}

	public Exp visit(While w) {
		var condExp = w.getCondition().accept(this);
		var bodyExp = w.getBody().accept(this);
		if (condExp == null || bodyExp == null) return new Exp(new CONST(0));
		var cond = condExp.unEx();
		var body = new EXP(bodyExp.unEx());

		var condLabel = new Label();
		var bodyLabel = new Label();
		var endLabel = new Label();

		var bodyStm = new SEQ(new LABEL(bodyLabel), body);
		var condJump = new JUMP(condLabel);
		var bodyJump = new SEQ(bodyStm, condJump);

		var cjump = CJUMP.builder()
				.relop(CJUMP.GT)
				.left(cond)
				.right(new CONST(1))
				.iftrue(bodyLabel)
				.iffalse(endLabel)
				.build();

		var whileStatement = SEQ.builder()
				.left(
						SEQ.builder()
								.left(
										SEQ.builder()
												.left(new LABEL(condLabel))
												.right(cjump)
												.build()
								)
								.right(bodyJump)
								.build()
				)
				.right(new LABEL(endLabel))
				.build();

		var whileEseq = new ESEQ(whileStatement, null);
		addExp(whileEseq);
		return new Exp(whileEseq);
	}

	public Exp visit(If i) {
		var condExpr = i.getCondition().accept(this);
		var thenExpr = i.getThenBranch().accept(this);
		var elseExpr = i.getElseBranch().accept(this);
		if (condExpr == null || thenExpr == null || elseExpr == null) return new Exp(new CONST(0));
		var trueLabel = new Label();
		var falseLabel = new Label();
		var endLabel = new Label();

		var trueStmt = new EXP(thenExpr.unEx());
		var falseStmt = new EXP(elseExpr.unEx());

		var thenStatement = new SEQ(
				new SEQ(new LABEL(trueLabel), trueStmt),
				new JUMP(endLabel)
		);
		var elseStatement = new SEQ(
				new SEQ(new LABEL(falseLabel), falseStmt),
				new JUMP(endLabel)
		);
		var thenElseStmt = new SEQ(thenStatement, elseStatement);

		var condStmt = CJUMP.builder()
				.relop(CJUMP.EQ)
				.left(new CONST(1))
				.right(condExpr.unEx())
				.iftrue(trueLabel)
				.iffalse(falseLabel)
				.build();

		var ifStmt = new SEQ(condStmt, thenElseStmt);

		var ifESEQ = new ESEQ(new SEQ(ifStmt, new LABEL(endLabel)), null);
		addExp(ifESEQ);
		return new Exp(ifESEQ);
	}

	public Exp visit(Assign a) {
		var id = a.getIdentifier().accept(this);
		var value = a.getValue().accept(this);
		if (id == null || value == null) return new Exp(new CONST(0));
		var moveValue = MOVE.builder()
				.dst(id.unEx())
				.src(value.unEx())
				.build();

		var moveValueESEQ = ESEQ.builder()
				.stm(moveValue)
				.exp(new CONST(0))
				.build();
		addExp(moveValueESEQ);
		return new Exp(moveValueESEQ);
	}

	public Exp visit(Sout s) {
		ExpList argsList = null;
		var exp = s.getExpression().accept(this);
		if (exp == null) return new Exp(new CONST(0));
		argsList = new ExpList(exp.unEx(), argsList);

		var call = frame.externalCall("print", argsList);
		addExp(call);
		return new Exp(call);
	}

	public Exp visit(Block b) {
		var size = b.getStatements().getStatements().size();
		Exp_ expBlock = new CONST(0);

		for (int i = 0; i < size; i++) {
			var stmtExp = b.getStatements().getStatements().get(i).accept(this);
			if (stmtExp == null) continue;
			var expr = new EXP(stmtExp.unEx());

			expBlock = ESEQ.builder()
					.stm(
							SEQ.builder()
									.left(new EXP(expBlock))
									.right(expr)
									.build()
					)
					.exp(new CONST(0))
					.build();
		}

		addExp(expBlock);
		return new Exp(expBlock);
	}

	public Exp visit(MainClass m) {
		currentClassTable = mainTable.getMap().get(m.getClassName().getS());
		currentMethodTable = currentClassTable.getMethodsContext().get("main");

		Stm stmBody = new EXP(new CONST(0));
		var stmList = new ArrayList<Stm>();
		var escapeList = new ArrayList<Boolean>();
		escapeList.add(false);
		frame = frame.newFrame("main", escapeList);

		var size = m.getStatements().getStatements().size();
		for (int i = 0; i < size; i++) {
			var stmtExp = m.getStatements().getStatements().get(i).accept(this);
			if (stmtExp == null) continue;
			stmBody = new SEQ(
					stmBody,
					new EXP(
							stmtExp.unEx()
					)
			);
		}
		stmList.add(stmBody);

		frame.procEntryExit1(stmList);
		frag.setNext(new ProcFrag(stmBody, frame));
		frag = frag.getNext();

		currentClassTable = null;
		currentMethodTable = null;
		return null;
	}

	public Exp visit(ClassDeclSimple c) {
		currentClassTable = mainTable.getMap().get(c.getClassName().getS());

		c.getClassName().accept(this);

		c.getFields().getVarDecls().forEach(field -> field.accept(this));
		c.getMethods().getMethodDecls().forEach(method -> method.accept(this));

		currentClassTable = null;
		return null;
	}

	public Exp visit(ClassDeclExtends c) {
		currentClassTable = mainTable.getMap().get(c.getClassName().getS());

		c.getClassName().accept(this);
		c.getParent().accept(this);

		c.getMethods().getMethodDecls().forEach(method -> method.accept(this));
		c.getFields().getVarDecls().forEach(field -> field.accept(this));

		currentClassTable = null;
		return null;
	}

	public Exp visit(Program p) {
		p.getMainClass().accept(this);
		p.getClasses().getClassDecls().forEach(classDecl -> classDecl.accept(this));
		return null;
	}

	public Exp visit(MethodDecl m) {
		currentMethodTable = currentClassTable.getMethodsContext().get(m.getIdentifier());

		Stm stmBody = new EXP(new CONST(0));
		var escapeList = new ArrayList<Boolean>();
		int sizeFormals = m.getFormals().getFormals().size();
		int sizeStatement = m.getStatements().getStatements().size();

		for (int i = 0; i <= sizeFormals; i++) {
			escapeList.add(false);
		}

		frame = frame.newFrame(
				currentClassTable.getClassName() + "$" + currentMethodTable.getMethodName(),
				escapeList
		);

		m.getFormals().getFormals().forEach(formal -> formal.accept(this));
		m.getVarDecls().getVarDecls().forEach(varDecl -> varDecl.accept(this));

		for (int i = 0; i < sizeStatement; i++) {
			var stmtExp = m.getStatements().getStatements().get(i).accept(this);
			if (stmtExp == null) continue;
			stmBody = new SEQ(
					stmBody,
					new EXP(
							stmtExp.unEx()
					)
			);
		}

		var stmList = new ArrayList<Stm>();
		stmList.add(stmBody);

		frame.procEntryExit1(stmList);
		frag.setNext(new ProcFrag(stmBody, frame));
		frag = frag.getNext();

		currentMethodTable = null;
		return null;
	}

	public Exp visit(VarDecl v) {
		return new Exp(new CONST(0));
	}

	public Exp visit(Formal f) {
		frame.allocLocal();
		return new Exp(new CONST(0));
	}

	public void addExp(Exp_ exp) {
		listExp.add(exp);
	}
}