package org.mjc.mjc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.mjc.assem.InstrList;
import org.mjc.ast.Program;
import org.mjc.canon.BasicBlocks;
import org.mjc.canon.Canon;
import org.mjc.canon.TraceSchedule;
import org.mjc.exceptions.LexicalOrSemanticAnalysisException;
import org.mjc.exceptions.SemanticAnalysisException;
import org.mjc.mips.MaximalMunchCodegen;
import org.mjc.mips.MipsFrame;
import org.mjc.parser.AntlrParser;
import org.mjc.temp.DefaultMap;
import org.mjc.visitor.irtree.IRTreeVisitor;
import org.mjc.visitor.irtree.ProcFrag;
import org.mjc.visitor.symbols.MainTable;
import org.mjc.visitor.types.TypeCheckingVisitor;

import java.io.InputStream;
import java.io.OutputStream;

@AllArgsConstructor
@Builder
@Log4j2
public class MjcCompiler {
    @Builder.Default
    private ParserStrategy parser = new AntlrParser();
    @Builder.Default
    @Getter
    private SemanticAnalysisStrategy semanticAnalysis = new TypeCheckingVisitor();

    public boolean isLexicalAndSyntacticallyOk(InputStream stream) {
        log.debug("Checking lexical and syntactic analysis");
        if (parser.validateSyntax(stream)) {
            log.info("Lexical and syntactic analysis is correct");
            return true;
        } else {
            log.error("Lexical and syntactic analysis is incorrect");
            return false;
        }
    }

    public Program getAbstractSyntaxTreeFromStream(InputStream stream) throws LexicalOrSemanticAnalysisException {
        log.debug("Getting abstract syntax tree from stream");

        try {
            Program program = parser.getProgramOrThrow(stream);
            log.info("Program is lexically and syntactically correct");
            return program;
        } catch (LexicalOrSemanticAnalysisException e) {
            log.error("Program is not lexically or semantically ok");
            throw e;
        }
    }

    public boolean isSemanticallyOk(Program program) {
        log.debug("Checking semantics");
        var ok = semanticAnalysis.isSemanticsOk(program);

        if (ok) {
            log.info("Semantics is correct");
        } else {
            log.error("Semantics is incorrect");
        }

        return ok;
    }

    public void isSemanticallyOkOrThrow(Program program) throws SemanticAnalysisException {
        log.debug("Checking semantics");
        try {
            semanticAnalysis.isSemanticsOkOrThrow(program);
            log.info("Semantics is correct");
        } catch (SemanticAnalysisException e) {
            log.error("Semantics is incorrect");
            throw e;
        }
    }

    public IRTreeVisitor getIRTreeVisitor(MainTable table, Program program, MipsFrame frame) {
        log.info("Generating IRTree code");
        var irTree = new IRTreeVisitor(table, frame);
        program.accept(irTree);

        log.info("IRTree code generated");
        return irTree;
    }

    public void compile(InputStream inputStream, OutputStream outputStream) throws LexicalOrSemanticAnalysisException, SemanticAnalysisException {
        // Check lexical and syntactic analysis
        Program program = getAbstractSyntaxTreeFromStream(inputStream);
        isSemanticallyOkOrThrow(program);

        // Intermediate code generation
        log.info("Processing intermediate code");
        var frame = new MipsFrame();
        IRTreeVisitor irTree = getIRTreeVisitor(semanticAnalysis.getMainTable(), program, frame);

        log.info("Processing canon code");
        var nextFrag = (ProcFrag) irTree.getInitialFrag().getNext();
        var traceSchedule = new TraceSchedule(new BasicBlocks(Canon.linearize(nextFrag.getBody())));

        log.info("Generating instruction List");
        var codegen = new MaximalMunchCodegen(frame);
        frame.setMipsCodegen(codegen);
        InstrList instrList = frame.codegen(traceSchedule.stms);

        for (var instr = instrList; instr != null; instr = instr.tail)
            System.out.println(instrList.head.format(new DefaultMap()));

    }

}
