/****************************************************/
/* File: symtab.c                                   */
/* Symbol table implementation for the TINY compiler*/
/* (allows only one symbol table)                   */
/* Symbol table is implemented as a chained         */
/* hash table                                       */
/* Compiler Construction: Principles and Practice   */
/* Kenneth C. Louden                                */
/****************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "globals.h"
#include "symtab.h"

/* SHIFT is the power of two used as multiplier
   in hash function  */
#define SHIFT 4

char *typePrint[] = {"void", "int", "int[]"};
static ScopeList scopeStack[1000];
static ScopeList totalScope[1000];
static int totalSize = 0;
static int location[1000];
static int stackSize = 0;

ScopeList stack_top() { return scopeStack[stackSize-1]; }
ScopeList stack_node(char * name)
{ ScopeList new;
  new = (ScopeList)malloc(sizeof(struct ScopeListRec));
  new->name = name;
  new->parent = stack_top();

  totalScope[totalSize++] = new;
  return new;
}
ScopeList stack_push(ScopeList new) 
{ scopeStack[stackSize] = new;
  location[stackSize++] = 0;
}
void stack_pop() { stackSize = stackSize - 1; }
int addLocation() { return location[stackSize-1]++; }

/* the hash function */
static int hash ( char * key )
{ int temp = 0;
  int i = 0;
  while (key[i] != '\0')
  { temp = ((temp << SHIFT) + key[i]) % SIZE;
    ++i;
  }
  return temp;
}

/* the hash table */
//static BucketList hashTable[SIZE];

/* Procedure st_insert inserts line numbers and
 * memory locations into the symbol table
 * loc = memory location is inserted only the
 * first time, otherwise ignored
 */
void st_insert( char * scope, char * name, ExpType type, int lineno, int loc, TreeNode *treenode  )
{ int h = hash(name);
  ScopeList sc = stack_top();

  while(sc != NULL)
  { if(strcmp(scope, sc->name) == 0) break;
    sc = sc->parent;
  }
  BucketList l =  sc->bucket[h];
  while ((l != NULL) && (strcmp(name,l->name) != 0))
    l = l->next;
  if (l == NULL) /* variable not yet in table */
  { l = (BucketList) malloc(sizeof(struct BucketListRec));
    l->name = name;
    l->type = type;
    l->lines = (LineList) malloc(sizeof(struct LineListRec));
    l->lines->lineno = lineno;
    l->memloc = loc;
    l->lines->next = NULL;
    l->next = sc->bucket[h];
    l->t = treenode;
    sc->bucket[h] = l; }
  else /* found in table, so just add line number */
  { LineList t = l->lines;
    while (t->next != NULL) t = t->next;
    t->next = (LineList) malloc(sizeof(struct LineListRec));
    t->next->lineno = lineno;
    t->next->next = NULL;
  }
} /* st_insert */

void just_add_lineno(char * name, int lineno)
{ BucketList l = st_lookup(name);
  LineList t = l->lines;
  while (t->next != NULL) t = t->next;
  t->next = (LineList) malloc(sizeof(struct LineListRec));
  t->next->lineno = lineno;
  t->next->next = NULL;
}

/* Function st_lookup returns the memory 
 * location of a variable or -1 if not found
 */
BucketList st_lookup ( char * name )
{ int h = hash(name);
  ScopeList sc = stack_top();

  while(sc != NULL)
  { BucketList l =  sc->bucket[h];
    while(l != NULL)
    { if(strcmp(name, l->name) == 0) return l;
      l = l->next;
    }
    sc = sc->parent;
  }
  return NULL;
}

BucketList st_lookup_excluding_parent(char * scope, char * name)
{ int h = hash(name);
  ScopeList sc = stack_top();

  if(strcmp(sc->name, scope) != 0)
  { BucketList l =  sc->bucket[h];
    while(l != NULL)
    { if(strcmp(name, l->name) == 0) return l;
      l = l->next;
    }
  }
  return NULL;
}

/* Procedure printSymTab prints a formatted 
 * listing of the symbol table contents 
 * to the listing file
 */
void printSymTab(FILE * listing)
{ int i, j;
  fprintf(listing,"Variable Name  Variable Type  Scope Name  Location   Line Numbers\n");
  fprintf(listing,"-------------  -------------  ----------  --------   ------------\n");
  
  for(j = 0; j < totalSize; j++){
    ScopeList sc = totalScope[j];

    for (i=0;i<SIZE;++i)
    { if (sc->bucket[i] != NULL)
      { BucketList l = sc->bucket[i];
        while (l != NULL)
        { LineList t = l->lines;
          fprintf(listing,"%-14s ",l->name);
          fprintf(listing,"%-14s ",typePrint[l->type]);
          fprintf(listing,"%-14s ",sc->name); // scope name
          fprintf(listing,"%-8d  ",l->memloc);
          while (t != NULL)
          { fprintf(listing,"%4d ",t->lineno);
            t = t->next;
          }
          fprintf(listing,"\n");
          l = l->next;
        }
      }
  }
  }
} /* printSymTab */
