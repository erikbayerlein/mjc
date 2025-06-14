package org.mjc.visitor.types;

import org.mjc.ast.*;
import org.mjc.visitor.symbols.SymbolTableVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeCheckingVisitorTest {
    static MainClass mockedMainClass() {
        return MainClass.builder()
                .className(new Identifier("Main"))
                .argsName(new Identifier("args"))
                .statements(new StatementList(new ArrayList<>() {{
                    add(new Sout(new IntegerLiteral(1)));
                }}))
                .build();
    }

    static Stream<Arguments> shouldCheckForAValidStatement() {
        return Stream.of(
                Arguments.of(new Sout(new IntegerLiteral(1))),
                Arguments.of(If.builder().condition(new True()).build()),
                Arguments.of(While.builder().condition(new True()).build())
        );
    }

    @ParameterizedTest
    @DisplayName("Should check for a valid Statement")
    @MethodSource
    void shouldCheckForAValidStatement(Statement stms) {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(MainClass.builder()
                        .className(new Identifier("Main"))
                        .argsName(new Identifier("args"))
                        .statements(new StatementList(new ArrayList<>() {{
                                    add(stms);
                                }})
                        )
                        .build())
                .build();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertTrue(typeVisitor.getErrors().isEmpty());
    }

    static Stream<Arguments> shouldCheckForAInvalidStatement() {
        return Stream.of(
                Arguments.of(new Sout(new True())),
                Arguments.of(new Sout(new False())),
                Arguments.of(new Sout(new NewArray(new IntegerLiteral(10)))),
                Arguments.of(If.builder().condition(new IntegerLiteral(1)).build()),
                Arguments.of(If.builder().condition(new NewArray(new IntegerLiteral(1))).build()),
                Arguments.of(If.builder().condition(new This()).build()),
                Arguments.of(While.builder().condition(new IntegerLiteral(1)).build()),
                Arguments.of(While.builder().condition(new NewArray(new IntegerLiteral(1))).build()),
                Arguments.of((While.builder().condition(new This()).build()))
        );
    }

    @ParameterizedTest
    @DisplayName("Should check for a invalid Statement")
    @MethodSource
    void shouldCheckForAInvalidStatement(Statement stms) {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(MainClass.builder()
                        .className(new Identifier("Main"))
                        .argsName(new Identifier("args"))
                        .statements(new StatementList(new ArrayList<>() {{
                                    add(stms);
                                }})
                        )
                        .build())
                .build();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertFalse(typeVisitor.getErrors().isEmpty());
    }


    static Stream<Arguments> shouldFindTheIdentifierInTheSymbolTable() {
        return Stream.of(
                Arguments.of(ClassDeclSimple.builder()
                        .className(new Identifier("TubiasWithMethodLocal"))
                        .methods(new MethodDeclList(new ArrayList<>() {{
                            add(MethodDecl.builder()
                                    .identifier("main")
                                    .formals(new FormalList(new ArrayList<>()))
                                    .varDecls(new VarDeclList(new ArrayList<>() {{
                                        add(VarDecl.builder().name("x").type(new IntegerType()).build());
                                    }}))
                                    .statements(new StatementList(new ArrayList<>() {{
                                        add(new Sout(new IdentifierExpression("x")));
                                    }}))
                                    .type(new IntegerType())
                                    .returnExpression(new IntegerLiteral(1))
                                    .build());
                        }}))
                        .build()
                ),
                Arguments.of(ClassDeclSimple.builder()
                        .className(new Identifier("TubiasWithMethodParams"))
                        .methods(new MethodDeclList(new ArrayList<>() {{
                            add(MethodDecl.builder()
                                    .identifier("main")
                                    .formals(new FormalList(new ArrayList<>() {{
                                        add(Formal.builder().name("x").type(new IntegerType()).build());
                                    }}))
                                    .varDecls(new VarDeclList(new ArrayList<>()))
                                    .statements(new StatementList(new ArrayList<>() {{
                                        add(new Sout(new IdentifierExpression("x")));
                                    }}))
                                    .type(new IntegerType())
                                    .returnExpression(new IntegerLiteral(1))
                                    .build());
                        }}))
                        .build()
                ),
                Arguments.of(ClassDeclSimple.builder()
                        .className(new Identifier("TubiasWithClassFields"))
                        .fields(new VarDeclList(new ArrayList<>() {{
                            add(VarDecl.builder().name("x").type(new IntegerType()).build());
                        }}))
                        .methods(new MethodDeclList(new ArrayList<>() {{
                            add(MethodDecl.builder()
                                    .identifier("main")
                                    .formals(new FormalList(new ArrayList<>()))
                                    .varDecls(new VarDeclList(new ArrayList<>()))
                                    .statements(new StatementList(new ArrayList<>() {{
                                        add(new Sout(new IdentifierExpression("x")));
                                    }}))
                                    .type(new IntegerType())
                                    .returnExpression(new IntegerLiteral(1))
                                    .build());
                        }}))
                        .build()
                ),
                Arguments.of(ClassDeclSimple.builder()
                        .className(new Identifier("TubiasWithClassFields"))
                        .methods(new MethodDeclList(new ArrayList<>() {{
                            add(MethodDecl.builder()
                                    .identifier("main")
                                    .formals(new FormalList(new ArrayList<>()))
                                    .varDecls(new VarDeclList(new ArrayList<>() {{
                                        add(VarDecl.builder().name("x").type(new IdentifierType(mockedMainClass().getClassName().getS())).build());
                                    }}))
                                    .statements(new StatementList(new ArrayList<>() {{
                                        add(Assign.builder()
                                                .identifier(new Identifier("x"))
                                                .value(new NewObject(new Identifier(mockedMainClass().getClassName().getS())))
                                                .build());
                                    }}))
                                    .type(new IntegerType())
                                    .returnExpression(new IntegerLiteral(1))
                                    .build());
                        }}))
                        .build()
                )
        );
    }

    @ParameterizedTest
    @DisplayName("Should find the identifier in the symbol table")
    @MethodSource
    void shouldFindTheIdentifierInTheSymbolTable(ClassDeclSimple classDecl) {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(mockedMainClass())
                .classes(new ClassDeclList(new ArrayList<>() {{
                    add(classDecl);
                }}))
                .build();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertTrue(typeVisitor.getErrors().isEmpty());
    }


    static Stream<Arguments> shouldCheckForAValidAssign() {
        return Stream.of(
                Arguments.of(
                        new VarDeclList(new ArrayList<>() {{
                            add(VarDecl.builder().name("x").type(new IdentifierType(mockedMainClass().getClassName().getS())).build());
                        }}),
                        Assign.builder()
                                .identifier(new Identifier("x"))
                                .value(new NewObject(new Identifier(mockedMainClass().getClassName().getS())))
                                .build()
                ),
                Arguments.of(
                        new VarDeclList(new ArrayList<>() {{
                            add(VarDecl.builder().name("x").type(new BooleanType()).build());
                        }}),
                        Assign.builder()
                                .identifier(new Identifier("x"))
                                .value(new False())
                                .build()
                ),
                Arguments.of(
                        new VarDeclList(new ArrayList<>() {{
                            add(VarDecl.builder().name("x").type(new IntegerType()).build());
                        }}),
                        Assign.builder()
                                .identifier(new Identifier("x"))
                                .value(new IntegerLiteral(1))
                                .build()
                ),
                Arguments.of(
                        new VarDeclList(new ArrayList<>() {{
                            add(VarDecl.builder().name("x").type(new IntArrayType()).build());
                        }}),
                        Assign.builder()
                                .identifier(new Identifier("x"))
                                .value(new NewArray(new IntegerLiteral(2)))
                                .build()
                )
        );
    }

    @ParameterizedTest
    @DisplayName("Should check for a valid Assign Statement")
    @MethodSource
    void shouldCheckForAValidAssign(VarDeclList varList, Assign assign) {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(mockedMainClass())
                .classes(new ClassDeclList(new ArrayList<>() {{
                    add(ClassDeclSimple.builder()
                            .className(new Identifier("Gabrigas"))
                            .methods(new MethodDeclList(new ArrayList<>() {{
                                add(MethodDecl.builder()
                                        .identifier("main")
                                        .formals(new FormalList(new ArrayList<>()))
                                        .varDecls(varList)
                                        .statements(new StatementList(new ArrayList<>() {{
                                            add(assign);
                                        }}))
                                        .type(new IntegerType())
                                        .returnExpression(new IntegerLiteral(1))
                                        .build());
                            }}))
                            .build());
                }}))
                .build();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertTrue(typeVisitor.getErrors().isEmpty());
    }

    static Stream<Arguments> shouldCheckForAInvalidAssign() {
        return Stream.of(
                Arguments.of(
                        new VarDeclList(new ArrayList<>() {{
                            add(VarDecl.builder().name("x").type(new IdentifierType(mockedMainClass().getClassName().getS())).build());
                        }}),
                        Assign.builder()
                                .identifier(new Identifier("x"))
                                .value(new IntegerLiteral(2))
                                .build()
                ),
                Arguments.of(
                        new VarDeclList(new ArrayList<>() {{
                            add(VarDecl.builder().name("x").type(new BooleanType()).build());
                        }}),
                        Assign.builder()
                                .identifier(new Identifier("x"))
                                .value(new IntegerLiteral(2))
                                .build()
                ),
                Arguments.of(
                        new VarDeclList(new ArrayList<>()),
                        Assign.builder()
                                .identifier(new Identifier("x"))
                                .value(new False())
                                .build()
                ),
                Arguments.of(
                        new VarDeclList(new ArrayList<>() {{
                            add(VarDecl.builder().name("x").type(new IntArrayType()).build());
                        }}),
                        Assign.builder()
                                .identifier(new Identifier("x"))
                                .value(new False())
                                .build()
                )
        );
    }

    @ParameterizedTest
    @DisplayName("Should check for a invalid Assign Statement")
    @MethodSource
    void shouldCheckForAInvalidAssign(VarDeclList varList, Assign assign) {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(mockedMainClass())
                .classes(new ClassDeclList(new ArrayList<>() {{
                    add(ClassDeclSimple.builder()
                            .className(new Identifier("Gabrigas"))
                            .methods(new MethodDeclList(new ArrayList<>() {{
                                add(MethodDecl.builder()
                                        .identifier("main")
                                        .formals(new FormalList(new ArrayList<>()))
                                        .varDecls(varList)
                                        .statements(new StatementList(new ArrayList<>() {{
                                            add(assign);
                                        }}))
                                        .type(new IntegerType())
                                        .returnExpression(new IntegerLiteral(1))
                                        .build());
                            }}))
                            .build());
                }}))
                .build();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertFalse(typeVisitor.getErrors().isEmpty());
    }

    @DisplayName("Should check for a valid Assign Array Statement")
    @Test
    void shouldCheckForAValidAssignArray() {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(mockedMainClass())
                .classes(new ClassDeclList(new ArrayList<>() {{
                    add(ClassDeclSimple.builder()
                            .className(new Identifier("Gabrigas"))
                            .methods(new MethodDeclList(new ArrayList<>() {{
                                add(MethodDecl.builder()
                                        .identifier("main")
                                        .formals(new FormalList(new ArrayList<>()))
                                        .varDecls(new VarDeclList(new ArrayList<>() {{
                                            add(VarDecl.builder().name("x").type(new IntArrayType()).build());
                                        }}))
                                        .statements(new StatementList(new ArrayList<>() {{
                                            add(ArrayAssign.builder()
                                                    .identifier(new Identifier("x"))
                                                    .index(new IntegerLiteral(1))
                                                    .value(new IntegerLiteral(2))
                                                    .build());
                                        }}))
                                        .type(new IntegerType())
                                        .returnExpression(new IntegerLiteral(1))
                                        .build());
                            }}))
                            .build());
                }}))
                .build();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertTrue(typeVisitor.getErrors().isEmpty());
    }

    static Stream<Arguments> shouldCheckForAInvalidAssignArray() {
        return Stream.of(
                Arguments.of(
                        new VarDeclList(new ArrayList<>() {{
                            add(VarDecl.builder().name("x").type(new IntArrayType()).build());
                        }}),
                        ArrayAssign.builder()
                                .identifier(new Identifier("x"))
                                .index(new IntegerLiteral(2))
                                .value(new False())
                                .build()
                ),
                Arguments.of(
                        new VarDeclList(new ArrayList<>() {{
                            add(VarDecl.builder().name("x").type(new IntArrayType()).build());
                        }}),
                        ArrayAssign.builder()
                                .identifier(new Identifier("x"))
                                .index(new False())
                                .value(new IntegerLiteral(2))
                                .build()
                ),
                Arguments.of(
                        new VarDeclList(new ArrayList<>() {{
                            add(VarDecl.builder().name("x").type(new IntegerType()).build());
                        }}),
                        ArrayAssign.builder()
                                .identifier(new Identifier("x"))
                                .index(new IntegerLiteral(1))
                                .value(new IntegerLiteral(2))
                                .build()
                )
        );
    }

    @ParameterizedTest
    @DisplayName("Should check for a invalid Assign Array Statement")
    @MethodSource
    void shouldCheckForAInvalidAssignArray(VarDeclList varList, ArrayAssign assign) {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(mockedMainClass())
                .classes(new ClassDeclList(new ArrayList<>() {{
                    add(ClassDeclSimple.builder()
                            .className(new Identifier("Gabrigas"))
                            .methods(new MethodDeclList(new ArrayList<>() {{
                                add(MethodDecl.builder()
                                        .identifier("main")
                                        .formals(new FormalList(new ArrayList<>()))
                                        .varDecls(varList)
                                        .statements(new StatementList(new ArrayList<>() {{
                                            add(assign);
                                        }}))
                                        .type(new IntegerType())
                                        .returnExpression(new IntegerLiteral(1))
                                        .build());
                            }}))
                            .build());
                }}))
                .build();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertFalse(typeVisitor.getErrors().isEmpty());
    }

    static Stream<Arguments> shouldCheckForAValidBooleanExpression() {
        return Stream.of(
                Arguments.of(
                        And.builder()
                                .lhe(new True())
                                .rhe(new True())
                                .build()
                ),
                Arguments.of(
                        And.builder()
                                .lhe(And.builder()
                                        .lhe(new False())
                                        .rhe(new True())
                                        .build()
                                )
                                .rhe(And.builder()
                                        .lhe(new False())
                                        .rhe(new True())
                                        .build())
                                .build()
                ),
                Arguments.of(
                        new Not(new False())
                ),
                Arguments.of(
                        new Not(
                                And.builder()
                                        .lhe(new True())
                                        .rhe(new True())
                                        .build()
                        )
                ),
                Arguments.of(
                        LessThan.builder()
                                .lhe(new IntegerLiteral(2))
                                .rhe(new IntegerLiteral(1))
                                .build()
                )
        );

    }

    @ParameterizedTest
    @DisplayName("Should check for a valid boolean expression")
    @MethodSource
    void shouldCheckForAValidBooleanExpression(Expression exp) {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(MainClass.builder()
                        .className(new Identifier("Main"))
                        .argsName(new Identifier("args"))
                        .statements(
                                new StatementList(new ArrayList<>() {{
                                    add(
                                            new If(exp, new Sout(new IntegerLiteral(1)), new Sout(new IntegerLiteral(1)))
                                    );
                                }})
                        )
                        .build())
                .build();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertTrue(typeVisitor.getErrors().isEmpty());
    }

    static Stream<Arguments> shouldCheckForAInvalidBooleanExpression() {
        return Stream.of(
                Arguments.of(
                        And.builder()
                                .lhe(new IntegerLiteral(1))
                                .rhe(new True())
                                .build()
                ),
                Arguments.of(
                        And.builder()
                                .lhe(And.builder()
                                        .lhe(new False())
                                        .rhe(new True())
                                        .build()
                                )
                                .rhe(new IntegerLiteral(2))
                                .build()
                ),
                Arguments.of(
                        new Not(new IntegerLiteral(2))
                ),
                Arguments.of(
                        LessThan.builder()
                                .lhe(new IntegerLiteral(2))
                                .rhe(new True())
                                .build()
                )
        );

    }

    @ParameterizedTest
    @DisplayName("Should check for a valid boolean expression")
    @MethodSource
    void shouldCheckForAInvalidBooleanExpression(Expression exp) {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(MainClass.builder()
                        .className(new Identifier("Main"))
                        .argsName(new Identifier("args"))
                        .statements(
                                new StatementList(new ArrayList<>() {{
                                    add(
                                            new If(exp, new Sout(new IntegerLiteral(1)), new Sout(new IntegerLiteral(1)))
                                    );
                                }})
                        )
                        .build())
                .build();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertFalse(typeVisitor.getErrors().isEmpty());
    }

    static Stream<Arguments> shouldCheckForAValidBinaryExpression() {
        return Stream.of(
                Arguments.of(
                        new Plus(
                                new IntegerLiteral(1),
                                new IntegerLiteral(1)
                        )
                ),
                Arguments.of(
                        new Plus(
                                new Plus(
                                        new IntegerLiteral(1),
                                        new IntegerLiteral(1)
                                ),
                                new IntegerLiteral(1)
                        )
                ),
                Arguments.of(
                        new Minus(
                                new IntegerLiteral(1),
                                new IntegerLiteral(1)
                        )
                ),
                Arguments.of(
                        new Minus(
                                new Minus(
                                        new IntegerLiteral(1),
                                        new IntegerLiteral(1)
                                ),
                                new IntegerLiteral(1)
                        )
                ),
                Arguments.of(
                        new Times(
                                new IntegerLiteral(1),
                                new IntegerLiteral(1)
                        )
                ),
                Arguments.of(
                        new Times(
                                new Times(
                                        new IntegerLiteral(1),
                                        new IntegerLiteral(1)
                                ),
                                new IntegerLiteral(1)
                        )
                ),
                Arguments.of(
                        new ArrayLength(
                                new NewArray(new IntegerLiteral(1))
                        )
                )
        );
    }

    @ParameterizedTest
    @DisplayName("Should check for a valid binary expression")
    @MethodSource
    void shouldCheckForAValidBinaryExpression(Expression exp) {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(mockedMainClass())
                .classes(new ClassDeclList(new ArrayList<>() {{
                    add(ClassDeclSimple.builder()
                            .className(new Identifier("Gabrigas"))
                            .methods(new MethodDeclList(new ArrayList<>() {{
                                add(MethodDecl.builder()
                                        .identifier("main")
                                        .formals(new FormalList(new ArrayList<>()))
                                        .varDecls(new VarDeclList(new ArrayList<>() {{
                                            add(VarDecl.builder().name("x").type(new IntegerType()).build());
                                        }}))
                                        .statements(new StatementList(new ArrayList<>() {{
                                            add(Assign.builder()
                                                    .identifier(new Identifier("x"))
                                                    .value(exp)
                                                    .build());
                                        }}))
                                        .type(new IntegerType())
                                        .returnExpression(new IntegerLiteral(1))
                                        .build());
                            }}))
                            .build());
                }}))
                .build();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertTrue(typeVisitor.getErrors().isEmpty());
    }

    static Stream<Arguments> shouldCheckForAInvalidBinaryExpression() {
        return Stream.of(
                Arguments.of(
                        new Plus(
                                new True(),
                                new IntegerLiteral(1)
                        )
                ),
                Arguments.of(
                        new Plus(
                                new Plus(
                                        new True(),
                                        new IntegerLiteral(1)
                                ),
                                new IntegerLiteral(1)
                        )
                ),
                Arguments.of(
                        new Minus(
                                new IntegerLiteral(1),
                                new True()
                        )
                ),
                Arguments.of(
                        new Minus(
                                new Minus(
                                        new True(),
                                        new IntegerLiteral(1)
                                ),
                                new IntegerLiteral(1)
                        )
                ),
                Arguments.of(
                        new Times(
                                new True(),
                                new IntegerLiteral(1)
                        )
                ),
                Arguments.of(
                        new Times(
                                new Times(
                                        new True(),
                                        new IntegerLiteral(1)
                                ),
                                new IntegerLiteral(1)
                        )
                )
        );
    }

    @ParameterizedTest
    @DisplayName("Should check for a valid binary expression")
    @MethodSource
    void shouldCheckForAInvalidBinaryExpression(Expression exp) {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(mockedMainClass())
                .classes(new ClassDeclList(new ArrayList<>() {{
                    add(ClassDeclSimple.builder()
                            .className(new Identifier("Gabrigas"))
                            .methods(new MethodDeclList(new ArrayList<>() {{
                                add(MethodDecl.builder()
                                        .identifier("main")
                                        .formals(new FormalList(new ArrayList<>()))
                                        .varDecls(new VarDeclList(new ArrayList<>() {{
                                            add(VarDecl.builder().name("x").type(new IntegerType()).build());
                                        }}))
                                        .statements(new StatementList(new ArrayList<>() {{
                                            add(Assign.builder()
                                                    .identifier(new Identifier("x"))
                                                    .value(exp)
                                                    .build());
                                        }}))
                                        .type(new IntegerType())
                                        .returnExpression(new IntegerLiteral(1))
                                        .build());
                            }}))
                            .build());
                }}))
                .build();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertFalse(typeVisitor.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should check for a invalid NewArray expression")
    void shouldCheckForAInvalidNewArray() {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(mockedMainClass())
                .classes(new ClassDeclList(new ArrayList<>() {{
                    add(ClassDeclSimple.builder()
                            .className(new Identifier("Gabrigas"))
                            .methods(new MethodDeclList(new ArrayList<>() {{
                                add(MethodDecl.builder()
                                        .identifier("main")
                                        .formals(new FormalList(new ArrayList<>()))
                                        .varDecls(new VarDeclList(new ArrayList<>() {{
                                            add(VarDecl.builder().name("x").type(new IntArrayType()).build());
                                        }}))
                                        .statements(new StatementList(new ArrayList<>() {{
                                            add(Assign.builder()
                                                    .identifier(new Identifier("x"))
                                                    .value(new NewArray(new True()))
                                                    .build());
                                        }}))
                                        .type(new IntegerType())
                                        .returnExpression(new IntegerLiteral(1))
                                        .build());
                            }}))
                            .build());
                }}))
                .build();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertFalse(typeVisitor.getErrors().isEmpty());
    }

    static Stream<Arguments> shouldCheckForAInvalidArrayLookup() {
        return Stream.of(
                Arguments.of(
                        new VarDeclList(new ArrayList<>() {{
                            add(VarDecl.builder().name("x").type(new IntArrayType()).build());
                            add(VarDecl.builder().name("y").type(new IntegerType()).build());
                        }}),
                        new StatementList(new ArrayList<>() {{
                            add(new Assign(new Identifier("y"), new ArrayLookup(new IdentifierExpression("x"), new True())));
                        }})
                ),
                Arguments.of(
                        new VarDeclList(new ArrayList<>() {{
                            add(VarDecl.builder().name("x").type(new IntArrayType()).build());
                            add(VarDecl.builder().name("y").type(new IntegerType()).build());
                        }}),
                        new StatementList(new ArrayList<>() {{
                            add(new Assign(new Identifier("y"), new ArrayLookup(new IdentifierExpression("x"), new NewArray(new IntegerLiteral(1)))));
                        }})
                ),
                Arguments.of(
                        new VarDeclList(new ArrayList<>() {{
                            add(VarDecl.builder().name("x").type(new IntArrayType()).build());
                            add(VarDecl.builder().name("y").type(new IntegerType()).build());
                        }}),
                        new StatementList(new ArrayList<>() {{
                            add(new Assign(new Identifier("y"), new ArrayLookup(new IdentifierExpression("x"), new NewObject(mockedMainClass().getClassName()))));
                        }})
                ),
                Arguments.of(
                        new VarDeclList(new ArrayList<>() {{
                            add(VarDecl.builder().name("x").type(new IntegerType()).build());
                            add(VarDecl.builder().name("y").type(new IntegerType()).build());
                        }}),
                        new StatementList(new ArrayList<>() {{
                            add(new Assign(new Identifier("y"), new ArrayLookup(new IdentifierExpression("x"), new IntegerLiteral(1))));
                        }})

                )
        );
    }

    @ParameterizedTest
    @DisplayName("Should check for a invalid Array Lookup expression")
    @MethodSource
    void shouldCheckForAInvalidArrayLookup(VarDeclList varList, StatementList stmList) {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(mockedMainClass())
                .classes(new ClassDeclList(new ArrayList<>() {{
                    add(ClassDeclSimple.builder()
                            .className(new Identifier("Gabrigas"))
                            .methods(new MethodDeclList(new ArrayList<>() {{
                                add(MethodDecl.builder()
                                        .identifier("main")
                                        .formals(new FormalList(new ArrayList<>()))
                                        .varDecls(varList)
                                        .statements(stmList)
                                        .type(new IntegerType())
                                        .returnExpression(new IntegerLiteral(1))
                                        .build());
                            }}))
                            .build());
                }}))
                .build();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertFalse(typeVisitor.getErrors().isEmpty());
    }

    static Stream<Arguments> shouldCheckForAInvalidCall() {
        return Stream.of(
                Arguments.of(
                        new MethodDeclList(new ArrayList<>() {{
                            add(MethodDecl.builder()
                                    .identifier("main")
                                    .formals(new FormalList(new ArrayList<>() {{
                                        add(Formal.builder().name("x").type(new IntegerType()).build());
                                    }}))
                                    .type(new IntegerType())
                                    .returnExpression(new IntegerLiteral(1))
                                    .build());
                            add(MethodDecl.builder()
                                    .identifier("main")
                                    .formals(new FormalList(new ArrayList<>()))
                                    .statements(new StatementList(new ArrayList<>() {{
                                        add(new Sout(Call.builder()
                                                .owner(new This())
                                                .method(new Identifier("main"))
                                                .expressionList(new ExpressionList(new ArrayList<>() {{
                                                    add(new False());
                                                }}))
                                                .build()));
                                    }}))
                                    .type(new IntegerType())
                                    .returnExpression(new IntegerLiteral(1))
                                    .build());
                        }})
                ),
                Arguments.of(
                        new MethodDeclList(new ArrayList<>() {{
                            add(MethodDecl.builder()
                                    .identifier("main")
                                    .formals(new FormalList(new ArrayList<>() {{
                                        add(Formal.builder().name("x").type(new IntegerType()).build());
                                    }}))
                                    .type(new IntegerType())
                                    .returnExpression(new IntegerLiteral(1))
                                    .build());
                            add(MethodDecl.builder()
                                    .identifier("main")
                                    .formals(new FormalList(new ArrayList<>()))
                                    .statements(new StatementList(new ArrayList<>() {{
                                        add(new Sout(Call.builder()
                                                .owner(new This())
                                                .method(new Identifier("main"))
                                                .expressionList(new ExpressionList(new ArrayList<>()))
                                                .build()));
                                    }}))
                                    .type(new IntegerType())
                                    .returnExpression(new IntegerLiteral(1))
                                    .build());
                        }})
                ),
                Arguments.of(
                        new MethodDeclList(new ArrayList<>() {{
                            add(MethodDecl.builder()
                                    .identifier("main")
                                    .formals(new FormalList(new ArrayList<>() {{
                                        add(Formal.builder().name("x").type(new IntegerType()).build());
                                    }}))
                                    .type(new IntegerType())
                                    .returnExpression(new IntegerLiteral(1))
                                    .build());
                            add(MethodDecl.builder()
                                    .identifier("main")
                                    .formals(new FormalList(new ArrayList<>()))
                                    .statements(new StatementList(new ArrayList<>() {{
                                        add(new Sout(Call.builder()
                                                .owner(new This())
                                                .method(new Identifier("main"))
                                                .expressionList(new ExpressionList(new ArrayList<>() {{
                                                    add(new IntegerLiteral(1));
                                                    add(new IntegerLiteral(1));
                                                }}))
                                                .build()));
                                    }}))
                                    .type(new IntegerType())
                                    .returnExpression(new IntegerLiteral(1))
                                    .build());
                        }})
                ),
                Arguments.of(
                        new MethodDeclList(new ArrayList<>() {{
                            add(MethodDecl.builder()
                                    .identifier("main")
                                    .formals(new FormalList(new ArrayList<>() {{
                                        add(Formal.builder().name("x").type(new IntegerType()).build());
                                        add(Formal.builder().name("y").type(new BooleanType()).build());
                                    }}))
                                    .type(new IntegerType())
                                    .returnExpression(new IntegerLiteral(1))
                                    .build());
                            add(MethodDecl.builder()
                                    .identifier("main")
                                    .formals(new FormalList(new ArrayList<>()))
                                    .statements(new StatementList(new ArrayList<>() {{
                                        add(new Sout(Call.builder()
                                                .owner(new This())
                                                .method(new Identifier("main"))
                                                .expressionList(new ExpressionList(new ArrayList<>() {{
                                                    add(new IntegerLiteral(1));
                                                    add(new IntegerLiteral(1));
                                                }}))
                                                .build()));
                                    }}))
                                    .type(new IntegerType())
                                    .returnExpression(new IntegerLiteral(1))
                                    .build());
                        }}))
        );
    }

    @ParameterizedTest
    @DisplayName("Should check for a invalid Call expression")
    @MethodSource
    void shouldCheckForAInvalidCall(MethodDeclList methods) {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(mockedMainClass())
                .classes(new ClassDeclList(new ArrayList<>() {{
                    add(ClassDeclSimple.builder()
                            .className(new Identifier("Gabrigas"))
                            .methods(methods)
                            .build());
                }}))
                .build();

        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertFalse(typeVisitor.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should allow a subclass to be used as a superclass on method call")
    void shouldAllowASubclassToBeUsedAsASuperclassOnMethodCall() {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(MainClass.builder()
                        .argsName(new Identifier("args"))
                        .className(new Identifier("Main"))
                        .statements(new StatementList(new ArrayList<>() {{
                            add(new Sout(Call.builder()
                                    .owner(new NewObject(new Identifier("Caller")))
                                    .method(new Identifier("accept"))
                                    .expressionList(new ExpressionList(new ArrayList<>() {{
                                        add(new NewObject(new Identifier("MyVisitorExtends")));
                                    }}))
                                    .build()));
                        }}))
                        .build())
                .classes(new ClassDeclList(new ArrayList<>() {{
                    add(ClassDeclSimple.builder()
                            .className(new Identifier("Visitor"))
                            .build());
                    add(ClassDeclExtends.builder()
                            .className(new Identifier("MyVisitorExtends"))
                            .parent(new Identifier("Visitor"))
                            .build());
                    add(ClassDeclSimple.builder()
                            .className(new Identifier("Caller"))
                            .methods(new MethodDeclList(new ArrayList<>() {{
                                add(MethodDecl.builder()
                                        .identifier("accept")
                                        .formals(new FormalList(new ArrayList<>() {{
                                            add(Formal.builder().name("v").type(new IdentifierType("Visitor")).build());
                                        }}))
                                        .type(new IntegerType())
                                        .returnExpression(new IntegerLiteral(1))
                                        .build());
                            }}))
                            .build());
                }}))
                .build();

        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertTrue(typeVisitor.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should allow a subclass to be used as a superclass type on assign")
    void shouldAllowASuperclassToBeUsedAsASuperClassTypeOnAssign() {
        // ARRANGE
        Program prog = Program.builder()
                .mainClass(mockedMainClass())
                .classes(new ClassDeclList(new ArrayList<>() {{
                    add(ClassDeclSimple.builder()
                            .className(new Identifier("Visitor"))
                            .build());
                    add(ClassDeclExtends.builder()
                            .className(new Identifier("MyVisitorExtends"))
                            .parent(new Identifier("Visitor"))
                            .build());
                    add(ClassDeclSimple.builder()
                            .className(new Identifier("Caller"))
                            .methods(new MethodDeclList(new ArrayList<>() {{
                                add(MethodDecl.builder()
                                        .identifier("accept")
                                        .varDecls(new VarDeclList(new ArrayList<>() {{
                                            add(VarDecl.builder().name("v").type(new IdentifierType("Visitor")).build());
                                        }}))
                                        .statements(new StatementList(new ArrayList<>() {{
                                            add(Assign.builder()
                                                    .identifier(new Identifier("v"))
                                                    .value(new NewObject(new Identifier("MyVisitorExtends")))
                                                    .build());
                                        }}))
                                        .type(new IntegerType())
                                        .returnExpression(new IntegerLiteral(1))
                                        .build());
                            }}))
                            .build());
                }}))
                .build();

        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        prog.accept(symbolTableVisitor);
        TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(symbolTableVisitor.getMainTable());

        // ACT
        prog.accept(typeVisitor);

        // ASSERT
        assertTrue(typeVisitor.getErrors().isEmpty());
    }
}
