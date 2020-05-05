/****************************************************/
/* File: analyze.c                                  */
/* Semantic analyzer implementation                 */
/* for the TINY compiler                            */
/* Compiler Construction: Principles and Practice   */
/* Kenneth C. Louden                                */
/****************************************************/

#include "globals.h"
#include "symtab.h"
#include "analyze.h"

/* counter for variable memory locations */
static int location = 0;
static char * scopeName = "global";

int isFunck = FALSE;

/* Procedure traverse is a generic recursive 
 * syntax tree traversal routine:
 * it applies preProc in preorder and postProc 
 * in postorder to tree pointed to by t
 */
static void traverse( TreeNode * t,
               void (* preProc) (TreeNode *),
               void (* postProc) (TreeNode *) )
{ if (t != NULL)
  { preProc(t);
    { int i;
      for (i=0; i < MAXCHILDREN; i++)
        traverse(t->child[i],preProc,postProc);
    }
    postProc(t);
    traverse(t->sibling,preProc,postProc);
  }
}

/* nullProc is a do-nothing procedure to 
 * generate preorder-only or postorder-only
 * traversals from traverse
 */
// static void nullProc(TreeNode * t)
// { if (t==NULL) return;
//   else return;
// }

static void scope_pop(TreeNode *t){
  if(t->nodekind == ExpK){
    if(t->kind.exp == CompoundK){
      stack_pop();
      scopeName = stack_top()->name;
    }
  }
}

static void symbolError(TreeNode * t, char * message)
{ fprintf(listing,"symbol error at line %d: %s\n",t->lineno,message);
  Error = TRUE;
}

/* Procedure insertNode inserts 
 * identifiers stored in t into 
 * the symbol table 
 */
static void insertNode( TreeNode * t)
{ switch (t->nodekind)
  { case StmtK:
      switch (t->kind.stmt)
      {
        default:
          break;
      }
      break;
    case ExpK:
      switch (t->kind.exp)
      { case IdK:
        case ArrIdK:
        case CallK:
        { if(st_lookup(t->attr.name) != NULL)
            just_add_lineno(t->attr.name, t->lineno);
          else
            symbolError(t, "did not declare");
          break;
        }
        case VarK:
        { if(st_lookup_excluding_parent(scopeName, t->attr.name) != NULL)
            symbolError(t, "variable already declare in same scope or parent scope");
          else
            st_insert(scopeName, t->attr.name, t->type, t->lineno, addLocation(), t);
          break;
        }
        case VarArrayK:
        { if(st_lookup_excluding_parent(scopeName, t->attr.name) != NULL)
            symbolError(t, "variable already declare in same scope or parent scope");
          else
            st_insert(scopeName, t->attr.name, Array, t->lineno, addLocation(), t);
          break;
        }
        case FuncK:
        { isFunck = TRUE;
          if(st_lookup_excluding_parent(scopeName, t->attr.name) != NULL)
            symbolError(t, "function already declare in same scope or parent scope");
          else
          { st_insert(scopeName, t->attr.name, t->type, t->lineno, addLocation(), t);
            stack_push(stack_node(t->attr.name));
            scopeName = t->attr.name;
          }
          break;
        }
        case SingleParamK:
        { if (t->type != Void){
            if(st_lookup(t->attr.name) != NULL) { symbolError(t, "redefinition of parameter"); }
            else st_insert(scopeName, t->attr.name, t->type, t->lineno, addLocation(), t);
          }
          break;
        }
        case ArrayParamK:
        { if (t->type != Void){
            if(st_lookup(t->attr.name) != NULL) { symbolError(t, "redefinition of parameter"); }
            else st_insert(scopeName, t->attr.name, Array, t->lineno, addLocation(), t);
          }
          else symbolError(t, "array type should not void");
          break;
        }
        case CompoundK:
        { if(isFunck){
            isFunck = FALSE;
          }
          else{
            stack_push(stack_node(scopeName));
          }
          t->scope = stack_top();
          break;
        }
        default:
          break;
      }
      break;
    default:
      break;
  }
}

void built_in_Func()
{ TreeNode *input;
  input = newExpNode(FuncK);
  input->type = Integer;
  input->attr.name = (char*)malloc(strlen("input")+1);
  strcpy(input->attr.name, "input");
  input->child[0] = newExpNode(SingleParamK);
  input->child[0]->type = Void;
  input->child[1] = NULL;

  TreeNode *output;
  output = newExpNode(FuncK);
  output->type = Void;
  output->attr.name = (char*)malloc(strlen("output")+1);
  strcpy(output->attr.name, "output");
  output->child[0] = newExpNode(SingleParamK);
  output->child[0]->type = Integer;
  output->child[1] = NULL;

  st_insert(scopeName, input->attr.name, input->type, 0, addLocation(), input);
  st_insert(scopeName, output->attr.name, output->type, 0, addLocation(), output);
}

/* Function buildSymtab constructs the symbol 
 * table by preorder traversal of the syntax tree
 */
void buildSymtab(TreeNode * syntaxTree)
{ ScopeList global = stack_node(scopeName);
  stack_push(global);
  built_in_Func(); // for built-in function
  traverse(syntaxTree,insertNode,scope_pop);

  if (TraceAnalyze)
  { fprintf(listing,"\nSymbol table:\n\n");
    printSymTab(listing);
  }
}

static void typeError(TreeNode * t, char * message)
{ fprintf(listing,"type error at line %d: %s\n",t->lineno,message);
  Error = TRUE;
}

/* Procedure checkNode performs
 * type checking at a single tree node
 */
static void checkNode(TreeNode * t)
{ switch (t->nodekind)
  { case ExpK:
      switch (t->kind.exp)
      { case OpK:
        { TreeNode *left = t->child[0];
          TreeNode *right = t->child[1];

          if(left->type == Void){
            typeError(t->child[0], "1 type void is only available for functions");
          }
          else if(right->type == Void){
            typeError(t->child[1], "2 type void is only available for functions");
          }
          else if(t->attr.op == PLUS && (left->type == Array && right->type == Array)){
            typeError(t, "int[] + int[] is not allowed");
          }
          else if(left->type != right->type){
            typeError(t, "operands should be same type");
          }
          else{
            t->type = Integer;
          }
          break;
        }
        case IdK:
        { if(st_lookup(t->attr.name) == NULL) break;
          else t->type = st_lookup(t->attr.name)->type;
          break;
        }
        case ArrIdK:
        { if(st_lookup(t->attr.name) == NULL) break;
          else t->type = Integer;
          break;
        }
        case ConstK:
        case NumK:
        { t->type = Integer;
          break;
        }
        case VarK:
        { if(t->type == Void){
            typeError(t, "void type is only available for functions");
            break;
          }
          break;
        }
        case VarArrayK:
        { if(st_lookup(t->attr.name) != NULL)
            break;
          else {
            if(t->child[0]->type != Integer) typeError(t->child[0], "array size should be int type");
            else t->type = Array;
          }
          break;
        }
        case ArrayParamK:
        { if(t->type == Void)
            typeError(t->child[0], "3 type void is only available for functions");
          else t->type = Array;
          break;
        }
        case CompoundK:
        { stack_pop();
          scopeName = stack_top()->name;
          break;
        }
        case CallK:
        { BucketList bucket = st_lookup(t->attr.name);
          TreeNode *args = t->child[0];
          TreeNode *params = bucket->t->child[0];

          if(bucket == NULL)
            break;

          if(params->type == Void){
            if(args != NULL){
              typeError(t, "this function's parameter type is void");
              break;
            }
          }
          else if(params->type == Integer){
            while(params != NULL){
              if(args == NULL){
                typeError(t, "check the argument number when calling function");
                break;
              }
              else if(params->type != args->type){
                typeError(t, "parameter type should match with argument type");
                break;
              }
              else{
                args = args->sibling;
                params = params->sibling;
              }
            }
            if(params == NULL && args != NULL){
              typeError(t, "check the argument number when calling function");
              break;
            }
          }
          t->type = st_lookup(t->attr.name)->type;
          break;
        }
        default:
          break;
      }
      break;
    case StmtK:
      switch (t->kind.stmt)
      { case IfK:
        { if (t->child[0]->type == Void)
            typeError(t->child[0],"4 type void is only available for functions");
          break;
        }
        case AssignK:
        { if (t->child[0]->type != t->child[1]->type)
            typeError(t,"type match of two operands when assigning");
          else
            t->type = t->child[0]->type;
          break;
        }
        case ReturnK:
        { if(st_lookup(scopeName)->type == Void){
            if(t->child[0] != NULL || t->child[0]->type != Void)
              typeError(t, "void function should return type void");
          }
          else if(st_lookup(scopeName)->type == Integer){
            if(t->child[0] == NULL || t->child[0]->type != Integer)
              typeError(t, "int function should return type int");
          }
          break;
        }
        case WhileK:
        { if (t->child[0]->type == Void)
            typeError(t->child[0],"5 type void is only available for functions");
          break;
        }
        default:
          break;
      }
      break;
    default:
      break;

  }
}

static void scope_push(TreeNode *t){
  switch(t->nodekind){
    case ExpK:
      switch(t->kind.exp){
        case FuncK:
        { scopeName = t->attr.name;
          break;
        }
        case CompoundK:
        { stack_push(t->scope);
        }
        default:
          break;
      }
    default:
      break;
  }
}

/* Procedure typeCheck performs type checking 
 * by a postorder syntax tree traversal
 */
void typeCheck(TreeNode * syntaxTree)
{ traverse(syntaxTree,scope_push,checkNode);
}
