grammar MiniJava;

@header {
package org.mjc.antlr;
}

goal: program EOF;
program: mainClass ( classDeclaration )*;

mainClass: CLASS IDENTIFIER LSQUIRLY PUBLIC STATIC VOID MAIN LPAREN STRING LBRACKET RBRACKET IDENTIFIER RPAREN LSQUIRLY statement RSQUIRLY RSQUIRLY;
classDeclaration: CLASS IDENTIFIER LSQUIRLY varDeclList methodDeclList RSQUIRLY #classDecl
| CLASS IDENTIFIER EXTENDS IDENTIFIER LSQUIRLY varDeclList methodDeclList RSQUIRLY #classDecl
;

methodDeclList: ( methodDeclaration )*;
varDeclList: ( varDeclaration )*;

varDeclaration: type IDENTIFIER SEMICOLON #varDecl;
methodDeclaration: PUBLIC type IDENTIFIER LPAREN formalList RPAREN LSQUIRLY varDeclList stmList RETURN expression SEMICOLON RSQUIRLY #methodDecl;

formalList:  type IDENTIFIER ( formalRest )*
|
;

formalRest: COMMA type IDENTIFIER;

type:
  INT LBRACKET RBRACKET #typeIntArray
| BOOLEAN #typeBoolean
| INT #typeInteger
| IDENTIFIER #typeIdentifier
;

statement:
  LSQUIRLY ( statement )* RSQUIRLY #stmBlock
| IF LPAREN expression RPAREN statement ELSE statement #stmIf
| WHILE LPAREN expression RPAREN statement #stmWhile
| SOUT LPAREN expression RPAREN SEMICOLON #stmPrint
| IDENTIFIER EQ expression SEMICOLON #stmAssign
| IDENTIFIER LBRACKET expression RBRACKET EQ expression SEMICOLON #stmArrayAssign
;

expression:
  expression STAR expression #expTimes
| expression PLUS expression #expPlus
| expression MINUS expression #expMinus
| expression LT expression #expLessThan
| expression AND expression #expAnd
| expression LBRACKET expression RBRACKET #expArrayLookup
| expression DOT LENGTH #expArrayLength
| expression DOT IDENTIFIER LPAREN expList RPAREN #expCall
| INTEGER_LITERAL #expIntegerLiteral
| TRUE_LITERAL #expTrue
| FALSE_LITERAL #expFalse
| IDENTIFIER #expIdentifierExp
| THIS #expThis
| NEW INT LBRACKET expression RBRACKET #expNewArray
| NEW IDENTIFIER LPAREN RPAREN #expNewObject
| BANG expression #expNot
| LPAREN expression RPAREN #expBracket
;

expList: expression ( expRest )*
|
;

expRest: COMMA expression;

// tokens
LPAREN: '(';
RPAREN: ')';
LBRACKET: '[';
RBRACKET: ']';
LSQUIRLY: '{';
RSQUIRLY: '}';
SEMICOLON: ';';
COMMA: ',';
DOT: '.';

//keywords
CLASS: 'class';
EXTENDS: 'extends';
PUBLIC: 'public';
STATIC: 'static';
VOID: 'void';
MAIN: 'main';
INT: 'int';
BOOLEAN: 'boolean';
WHILE: 'while';
IF: 'if';
ELSE: 'else';
SOUT: 'System.out.println';
NEW: 'new';
THIS: 'this';
RETURN: 'return';
STRING: 'String';
LENGTH: 'length';


// operators
EQ: '=';
AND: '&&';
LT: '<';
PLUS: '+';
MINUS: '-';
STAR: '*';
BANG: '!';

// constants
TRUE_LITERAL: 'true';
FALSE_LITERAL: 'false';
INTEGER_LITERAL: [0-9]+;

//id
IDENTIFIER: [_a-zA-Z] [_a-zA-Z0-9]*;

COMMENT: (SINGLELINECOMMENT | MULTILINECOMMENT) -> skip;
fragment SINGLELINECOMMENT: ('//' ~('\n')*);
fragment MULTILINECOMMENT: '/*' .*? '*/';
WS: [ \t\r\n] -> skip;