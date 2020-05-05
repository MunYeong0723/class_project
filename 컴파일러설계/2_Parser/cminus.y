/****************************************************/
/* File: tiny.y                                     */
/* The TINY Yacc/Bison specification file           */
/* Compiler Construction: Principles and Practice   */
/* Kenneth C. Louden                                */
/****************************************************/
%{
#define YYPARSER /* distinguishes Yacc output from other code files */

#include "globals.h"
#include "util.h"
#include "scan.h"
#include "parse.h"

#define YYSTYPE TreeNode *
static char * savedName; /* for use in assignments */
static int savedNumber;
static int savedLineNo;  /* ditto */
static TreeNode * savedTree; /* stores syntax tree for later return */
static int yylex(void); // added 11/2/11 to ensure no conflict with lex

%}

%token IF ELSE WHILE RETURN INT VOID
%token ID NUM 
%token ASSIGN EQ NE LT LE GT GE PLUS MINUS TIMES OVER SEMI COMMA
%token LPAREN RPAREN LBRACE RBRACE LCURLY RCURLY
%token ERROR

%% /* Grammar for C-MINUS */

program     : declaration_list
                 { savedTree = $1;} 
            ;
declaration_list : declaration_list declaration
                    { YYSTYPE t = $1;
                     if (t != NULL)
                     { while (t->sibling != NULL)
                          t = t->sibling;
                       t->sibling = $2;
                       $$ = $1; }
                       else $$ = $2;
                 }
                  | declaration { $$ = $1; }
              ;
declaration : var_declaration { $$ = $1; }
              | fun_declaration { $$ = $1; }
            ;
name         : ID { savedName = copyString(tokenString); }
            ;
number      : NUM { savedNumber = atoi(tokenString); }
            ;
arrSize     : NUM
              { $$ = newExpNode(ConstK);
                $$->type = Integer;
                $$->attr.val = atoi(tokenString);
              }
            ;
var_declaration : type_specifier name SEMI
                 { $$ = newExpNode(VarK);
                   $$->attr.name = savedName;
                   $$->type = (ExpType)$1;
                 }
                 | type_specifier name LBRACE arrSize RBRACE SEMI
                 { $$ = newExpNode(VarArrayK);
                   $$->attr.name = savedName;
                   $$->type = (ExpType)$1;
                   $$->child[0] = $4;
                 }
              ;
type_specifier : INT { $$ = Integer; }
                  | VOID { $$ = Void; }
                  ;
fun_declaration : type_specifier name
                 { $$ = newExpNode(FuncK);
                   $$->attr.name = savedName;
                 }
                LPAREN params RPAREN compound_stmt 
                { $$ = $3;
                  $$->type = (ExpType)$1;
                  $$->child[0] = $5;
                  $$->child[1] = $7;
                }
              ;
params      : param_list { $$ = $1; }
            | VOID
              { $$ = newExpNode(SingleParamK);
                $$->type = Void;
              }
          ;
param_list : param_list COMMA param
              { YYSTYPE t = $1;
                   if (t != NULL)
                   { while (t->sibling != NULL)
                        t = t->sibling;
                     t->sibling = $3;
                     $$ = $1; }
                     else $$ = $3;
                 }
            | param { $$ = $1; }
            ;
param       : type_specifier name
               { $$ = newExpNode(SingleParamK);
                 $$->attr.name = savedName;
                 $$->type = (ExpType)$1;
               }
            | type_specifier name LBRACE RBRACE
               { $$ = newExpNode(ArrayParamK);
                 $$->attr.name = savedName;
                 $$->type = (ExpType)$1;
               }
            ;
compound_stmt : LCURLY local_declarations stmt_list RCURLY
                  { $$ = newExpNode(CompoundK);
                    $$->child[0] = $2;
                    $$->child[1] = $3;
                  }
                ;
local_declarations : local_declarations var_declaration
                        { YYSTYPE t = $1;
                           if (t != NULL)
                           { while (t->sibling != NULL)
                                t = t->sibling;
                             t->sibling = $2;
                             $$ = $1; }
                             else $$ = $2;
                         }
                      | /* empty */ { $$ = NULL; }
                    ;
stmt_list   : stmt_list stmt
              { YYSTYPE t = $1;
                 if (t != NULL)
                 { while (t->sibling != NULL)
                      t = t->sibling;
                   t->sibling = $2;
                   $$ = $1; }
                   else $$ = $2;
               }
            | /* empty */ { $$ = NULL; }
          ;
stmt        : open_stmt { $$ = $1; }
            | closed_stmt { $$ = $1; }
            ;
simple_stmt : return_stmt { $$ = $1; }
              | exp_stmt { $$ = $1; }
              | compound_stmt { $$ = $1; }
              ;
exp_stmt    : exp SEMI { $$ = $1; }
            | SEMI { $$ = NULL; }
          ;
open_stmt   : IF LPAREN exp RPAREN stmt
              { $$ = newStmtNode(IfK);
               $$->child[0] = $3;
               $$->child[1] = $5;
               $$->attr.thereElse = FALSE;
             }
            | IF LPAREN exp RPAREN closed_stmt ELSE open_stmt
            { $$ = newStmtNode(IfK);
              $$->child[0] = $3;
              $$->child[1] = $5;
              $$->child[2] = $7;
              $$->attr.thereElse = TRUE;
            }
            | WHILE LPAREN exp RPAREN open_stmt
            { $$ = newStmtNode(WhileK);
              $$->child[0] = $3;
              $$->child[1] = $5;
            }
            ;
closed_stmt : simple_stmt { $$ = $1; }
              | IF LPAREN exp RPAREN closed_stmt ELSE closed_stmt
                { $$ = newStmtNode(IfK);
                  $$->child[0] = $3;
                  $$->child[1] = $5;
                  $$->child[2] = $7;
                  $$->attr.thereElse = TRUE;
                }
              | WHILE LPAREN exp RPAREN closed_stmt
                { $$ = newStmtNode(WhileK);
                  $$->child[0] = $3;
                  $$->child[1] = $5;
                }
                ;
return_stmt : RETURN SEMI { $$ = newStmtNode(ReturnK); }
            | RETURN exp SEMI
                { $$ = newStmtNode(ReturnK);
                  $$->child[0] = $2;
                }
            ;
exp         : var ASSIGN exp 
              { $$ = newStmtNode(AssignK);
                $$->child[0] = $1;
                $$->child[1] = $3;
              }
            | simple_exp { $$ = $1; }
            ;
var          : name
                { $$ = newExpNode(IdK);
                   $$->attr.name = savedName;
                 }
              | name 
                 { $$ = newExpNode(IdK);
                   $$->attr.name = savedName;
                 } LBRACE exp RBRACE
                 {  $$ = $2;
                    $$->child[0] = $4;
                 }
            ;
simple_exp  : addictive_exp LT addictive_exp 
                 { $$ = newExpNode(OpK);
                   $$->child[0] = $1;
                   $$->child[1] = $3;
                   $$->attr.op = LT;
                 }
            | addictive_exp LE addictive_exp 
                 { $$ = newExpNode(OpK);
                   $$->child[0] = $1;
                   $$->child[1] = $3;
                   $$->attr.op = LE;
                 }
            | addictive_exp GT addictive_exp 
                 { $$ = newExpNode(OpK);
                   $$->child[0] = $1;
                   $$->child[1] = $3;
                   $$->attr.op = GT;
                 }
            | addictive_exp GE addictive_exp 
                 { $$ = newExpNode(OpK);
                   $$->child[0] = $1;
                   $$->child[1] = $3;
                   $$->attr.op = GE;
                 }
            | addictive_exp EQ addictive_exp 
                 { $$ = newExpNode(OpK);
                   $$->child[0] = $1;
                   $$->child[1] = $3;
                   $$->attr.op = EQ;
                 }
            | addictive_exp NE addictive_exp 
                 { $$ = newExpNode(OpK);
                   $$->child[0] = $1;
                   $$->child[1] = $3;
                   $$->attr.op = NE;
                 }
            | addictive_exp { $$ = $1; }
            ;
addictive_exp  : addictive_exp PLUS term 
                 { $$ = newExpNode(OpK);
                   $$->child[0] = $1;
                   $$->child[1] = $3;
                   $$->attr.op = PLUS;
                 }
            | addictive_exp MINUS term
                 { $$ = newExpNode(OpK);
                   $$->child[0] = $1;
                   $$->child[1] = $3;
                   $$->attr.op = MINUS;
                 } 
            | term { $$ = $1; }
            ;
term        : term TIMES factor 
                 { $$ = newExpNode(OpK);
                   $$->child[0] = $1;
                   $$->child[1] = $3;
                   $$->attr.op = TIMES;
                 }
            | term OVER factor
                 { $$ = newExpNode(OpK);
                   $$->child[0] = $1;
                   $$->child[1] = $3;
                   $$->attr.op = OVER;
                 }
            | factor { $$ = $1; }
            ;
factor      : LPAREN exp RPAREN { $$ = $2; }
            | var { $$ = $1; }
            | call { $$ = $1; }
            | number
             { $$ = newExpNode(ConstK);
               $$->type = Integer;
               $$->attr.val = savedNumber;
             }
            ;
call       : name 
              { $$ = newExpNode(CallK);
                $$->attr.name = savedName;
             } LPAREN args RPAREN 
             { $$ = $2;
              $$->child[0] = $4;
             }
            ;
args        : args_list { $$ = $1; }
              | /* empty */ { $$ = NULL; }
            ;
args_list   : args_list COMMA exp
              { YYSTYPE t = $1;
                 if (t != NULL)
                 { while (t->sibling != NULL)
                      t = t->sibling;
                   t->sibling = $3;
                   $$ = $1; }
                   else $$ = $3;
               }
              | exp { $$ = $1; }
            ;
%%

int yyerror(char * message)
{ fprintf(listing,"Syntax error at line %d: %s\n",lineno,message);
  fprintf(listing,"Current token: ");
  printToken(yychar,tokenString);
  Error = TRUE;
  return 0;
}

/* yylex calls getToken to make Yacc/Bison output
 * compatible with ealier versions of the TINY scanner
 */
static int yylex(void)
{ return getToken(); }

TreeNode * parse(void)
{ yyparse();
  return savedTree;
}