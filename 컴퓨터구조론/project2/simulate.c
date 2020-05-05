#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define NUMMEMORY 65536 /* maximum number of data words in memory */
#define NUMREGS 8 /* number of machine registers */
#define MAXLINELENGTH 1000

#define ADD 0
#define NOR 1
#define LW 2
#define SW 3
#define BEQ 4
#define JALR 5 /* JALR will not implemented for this project */
#define HALT 6
#define NOOP 7

#define NOOPINSTRUCTION 0x1c00000

typedef struct IFIDStruct {
	int instr;
	int pcPlus1;
} IFIDType;

typedef struct IDEXStruct {
	int instr;
	int pcPlus1;
	int readRegA;
	int readRegB;
	int offset;
} IDEXType;

typedef struct EXMEMStruct {
	int instr;
	int branchTarget;
	int aluResult;
	int readRegB;
} EXMEMType;

typedef struct MEMWBStruct {
	int instr;
	int writeData;
} MEMWBType;

typedef struct WBENDStruct {
	int instr;
	int writeData;
} WBENDType;

typedef struct stateStruct {
	int pc;
	int instrMem[NUMMEMORY];
	int dataMem[NUMMEMORY];
	int reg[NUMREGS];
	int numMemory;
	IFIDType IFID;
	IDEXType IDEX;
	EXMEMType EXMEM;
	MEMWBType MEMWB;
	WBENDType WBEND;
	int cycles; /* number of cycles run so far */
} stateType;

int field0(int);
int field1(int);
int field2(int);
int opcode(int);
void printInstruction(int);
void printState(stateType*);
int convertNum(int);

void run(stateType);

int
main(int argc, char *argv[])
{
	char line[MAXLINELENGTH];
	stateType state;
	FILE *filePtr;

	if (argc != 2) {
		printf("error: usage: %s <machine-code file>\n", argv[0]);
		exit(1);
	}

	filePtr = fopen(argv[1], "r");
	if (filePtr == NULL) {
		printf("error: can't open file %s", argv[1]);
		perror("fopen");
		exit(1);
	}

	/* read in the entire machine-code file into memory */
	for (state.numMemory = 0; fgets(line, MAXLINELENGTH, filePtr) != NULL;
		state.numMemory++) {
		if (sscanf(line, "%d", state.instrMem+state.numMemory) != 1) {
			printf("error in reading address %d\n", state.numMemory);
			exit(1);
		}
		printf("memory[%d]=%d\n", state.numMemory, state.instrMem[state.numMemory]);
		state.dataMem[state.numMemory] = state.instrMem[state.numMemory];
	}
	printf("%d memory words\n", state.numMemory);
	printf("\tinstruction memory:\n");
	for(int i = 0; i < state.numMemory; i++){
		printf("\t\tinstrMem[%d] ", i);
		printInstruction(state.instrMem[i]);
	}

	run(state);

	return(0);
}

int
field0(int instruction)
{
	return( (instruction>>19) & 0x7);
}
int
field1(int instruction)
{
	return( (instruction>>16) & 0x7);
}
int
field2(int instruction)
{
	return(instruction & 0xFFFF);
}
int
opcode(int instruction)
{
	return(instruction>>22);
}
void
printInstruction(int instr)
{
	char opcodeString[10];

	if (opcode(instr) == ADD) {
		strcpy(opcodeString, "add");
	} else if (opcode(instr) == NOR) {
		strcpy(opcodeString, "nor");
	} else if (opcode(instr) == LW) {
		strcpy(opcodeString, "lw");
	} else if (opcode(instr) == SW) {
		strcpy(opcodeString, "sw");
	} else if (opcode(instr) == BEQ) {
		strcpy(opcodeString, "beq");
	} else if (opcode(instr) == JALR) {
		strcpy(opcodeString, "jalr");
	} else if (opcode(instr) == HALT) {
		strcpy(opcodeString, "halt");
	} else if (opcode(instr) == NOOP) {
		strcpy(opcodeString, "noop");
	} else {
		strcpy(opcodeString, "data");
	}
	printf("%s %d %d %d\n", opcodeString, field0(instr), field1(instr),
	field2(instr));
}
void
printState(stateType *statePtr)
{
	int i;
	printf("\n@@@\nstate before cycle %d starts\n", statePtr->cycles);
	printf("\tpc %d\n", statePtr->pc);

	printf("\tdata memory:\n");
	for (i=0; i<statePtr->numMemory; i++) {
		printf("\t\tdataMem[ %d ] %d\n", i, statePtr->dataMem[i]);
	}

	printf("\tregisters:\n");
	for (i=0; i<NUMREGS; i++) {
		printf("\t\treg[ %d ] %d\n", i, statePtr->reg[i]);
	}

	printf("\tIFID:\n");
	printf("\t\tinstruction ");
	printInstruction(statePtr->IFID.instr);
	printf("\t\tpcPlus1 %d\n", statePtr->IFID.pcPlus1);

	printf("\tIDEX:\n");
	printf("\t\tinstruction ");
	printInstruction(statePtr->IDEX.instr);
	printf("\t\tpcPlus1 %d\n", statePtr->IDEX.pcPlus1);
	printf("\t\treadRegA %d\n", statePtr->IDEX.readRegA);
	printf("\t\treadRegB %d\n", statePtr->IDEX.readRegB);
	printf("\t\toffset %d\n", statePtr->IDEX.offset);

	printf("\tEXMEM:\n");
	printf("\t\tinstruction ");
	printInstruction(statePtr->EXMEM.instr);
	printf("\t\tbranchTarget %d\n", statePtr->EXMEM.branchTarget);
	printf("\t\taluResult %d\n", statePtr->EXMEM.aluResult);
	printf("\t\treadRegB %d\n", statePtr->EXMEM.readRegB);

	printf("\tMEMWB:\n");
	printf("\t\tinstruction ");
	printInstruction(statePtr->MEMWB.instr);
	printf("\t\twriteData %d\n", statePtr->MEMWB.writeData);

	printf("\tWBEND:\n");
	printf("\t\tinstruction ");
	printInstruction(statePtr->WBEND.instr);
	printf("\t\twriteData %d\n", statePtr->WBEND.writeData);
}
int convertNum(int num){
	/* convert a 16-bit number into a 32-bit Linux integer*/
	if(num & (1<<15)){
		num -= (1<<16);
	}
	return(num);
}

void run(stateType state){
	stateType newState;
	int writeData = 0;
	int aluResult = 0;

	int i = 0;
	//initialize pc, cycles and register
	state.pc = 0;
	state.cycles = 0;
	for(i = 0; i < NUMREGS; i++){
		state.reg[i] = 0;
	}
	// initialize instruction field to NOOP(0x1c00000)
	state.IFID.instr = 29360128;
	state.IDEX.instr = 29360128;
	state.EXMEM.instr = 29360128;
	state.MEMWB.instr = 29360128;
	state.WBEND.instr = 29360128;

	while (1) {
		int forward0 = 0;
		int forward1 = 0;
		printState(&state);

		/* check for halt */
		if (opcode(state.MEMWB.instr) == HALT) {
			printf("machine halted\n");
			printf("total of %d cycles executed\n", state.cycles);
			exit(0);
		}
		newState = state;
		newState.cycles++;

		/* --------------------- IF stage --------------------- */
		newState.pc = state.pc + 1;

		newState.IFID.instr = state.instrMem[state.pc];
		newState.IFID.pcPlus1 = state.pc + 1;

		/* --------------------- ID stage --------------------- */
		newState.IDEX.instr = state.IFID.instr; // to transfer in instruction in the IFID register to the IDEX register
		newState.IDEX.pcPlus1 = state.IFID.pcPlus1;

		int instr = state.IFID.instr;
		newState.IDEX.readRegA = state.reg[field0(instr)];
		newState.IDEX.readRegB = state.reg[field1(instr)];
		newState.IDEX.offset = convertNum(field2(instr));

		// handling Load-use data hazard
		if(opcode(state.IDEX.instr) == LW){
			if(field1(state.IDEX.instr) == field0(state.IFID.instr) || field1(state.IDEX.instr) == field1(state.IFID.instr)){
				// stall one cycle
				newState.IDEX.instr = 29360128; //NOOP
				newState.pc -= 1;
				newState.IFID.instr = state.IFID.instr;
				newState.IFID.pcPlus1 -= 1;
				newState.IDEX.offset = 0;
				newState.IDEX.pcPlus1 = 0;
			}
		}

		/* --------------------- EX stage --------------------- */
		switch(opcode(state.IDEX.instr)){
		case 0 : //ADD
			aluResult = state.IDEX.readRegA + state.IDEX.readRegB;
			break;
		case 1 : //NOR
			aluResult = ~(state.IDEX.readRegA | state.IDEX.readRegB);
			break;
		case 2 : //LW
			aluResult = state.IDEX.readRegA + state.IDEX.offset;
			break;
		case 3 : //SW
			aluResult = state.IDEX.readRegA + state.IDEX.offset;
			break;
		case 4 : //BEQ
			aluResult = state.IDEX.readRegA - state.IDEX.readRegB;
			break;
		case 5 : //JALR
			// do nothing
			break;
		case 6 : //HALT
			break;
		case 7 : //NOOP
			break;
		default :
			printf("error : unrecognized opcode\n");
			exit(1);
			break;
		}

		newState.EXMEM.instr = state.IDEX.instr;
		newState.EXMEM.branchTarget = state.IDEX.pcPlus1 + state.IDEX.offset;
		newState.EXMEM.aluResult = aluResult;
		newState.EXMEM.readRegB = state.IDEX.readRegB;

		// forwarding
		if(opcode(state.IDEX.instr) <= 1){
			if(field2(state.IDEX.instr) != 0 && field2(state.IDEX.instr) == field0(state.IFID.instr)){
				if(!forward0){
					newState.IDEX.readRegA = aluResult;
					forward0 = 1;
				}
			}
			if(field2(state.IDEX.instr) != 0 && field2(state.IDEX.instr) == field1(state.IFID.instr)){
				if(!forward1){
					newState.IDEX.readRegB = aluResult;
					forward1 = 1;
				}
			}	
		}

		/* --------------------- MEM stage --------------------- */		
		switch(opcode(state.EXMEM.instr)){
		case 0 : //ADD
			writeData = state.EXMEM.aluResult;
			break;
		case 1 : //NOR
			writeData = state.EXMEM.aluResult;
			break;
		case 2 : //LW
			writeData = state.dataMem[state.EXMEM.aluResult];
			break;
		case 3 : //SW
			newState.dataMem[state.EXMEM.aluResult] = state.EXMEM.readRegB;
			break;
		case 4 : //BEQ
			// branch hazard
			//branch taken
			if(state.EXMEM.aluResult == 0){
				//discard instruction
				newState.IFID.instr = 29360128; //NOOP
				newState.IDEX.instr = 29360128; //NOOP
				newState.EXMEM.instr = 29360128; //NOOP
				newState.pc = state.EXMEM.branchTarget;
			}
			break;
		case 5 : //JALR
			break;
		case 6 : //HALT
			break;
		case 7 : //NOOP
			break;
		default :
			printf("error : unrecognized opcode\n");
			exit(1);
			break;
		}
		newState.MEMWB.instr = state.EXMEM.instr;
		newState.MEMWB.writeData = writeData;

		// forwarding
		if(opcode(state.EXMEM.instr) <= 1){
			if(field2(state.EXMEM.instr) != 0 && field2(state.EXMEM.instr) == field0(state.IFID.instr)){
				if(!forward0){
					newState.IDEX.readRegA = state.EXMEM.aluResult;
					forward0 = 1;
				}
			}
			if(field2(state.EXMEM.instr) != 0 && field2(state.EXMEM.instr) == field1(state.IFID.instr)){
				if(!forward1){
					newState.IDEX.readRegB = state.EXMEM.aluResult;
					forward1 = 1;
				}
			}	
		}
		if(opcode(state.EXMEM.instr) == LW){
			if(field1(state.EXMEM.instr) != 0 && field1(state.EXMEM.instr) == field0(state.IFID.instr)){
				if(!forward0){
					newState.IDEX.readRegA = state.dataMem[state.EXMEM.aluResult];
					forward0 = 1;
				}
			}
			if(field1(state.EXMEM.instr) != 0 && field1(state.EXMEM.instr) == field1(state.IFID.instr)){
				if(!forward1){
					newState.IDEX.readRegB = state.dataMem[state.EXMEM.aluResult];
					forward1 = 1;
				}
			}
		}

		/* --------------------- WB stage --------------------- */
		switch(opcode(state.MEMWB.instr)){
		case 0 : //ADD
			newState.reg[field2(state.MEMWB.instr)] = state.MEMWB.writeData;
			break;
		case 1 : //NOR
			newState.reg[field2(state.MEMWB.instr)] = state.MEMWB.writeData;
			break;
		case 2 : //LW
			newState.reg[field1(state.MEMWB.instr)] = state.MEMWB.writeData;
			break;
		case 3 : //SW
			break;
		case 4 : //BEQ
			break;
		case 5 : //JALR
			break;
		case 6 : //HALT
			break;
		case 7 : //NOOP
			break;
		default :
			printf("error : unrecognized opcode\n");
			exit(1);
			break;
		}	
		newState.WBEND.instr = state.MEMWB.instr;
		newState.WBEND.writeData = state.MEMWB.writeData;

		// forwarding
		if(opcode(state.MEMWB.instr) <= 1){
			if(field2(state.MEMWB.instr) != 0 && field2(state.MEMWB.instr) == field0(state.IFID.instr)){
				if(!forward0){
					newState.IDEX.readRegA = state.MEMWB.writeData;
					forward0 = 1;
				}
			}
			if(field2(state.MEMWB.instr) != 0 && field2(state.MEMWB.instr) == field1(state.IFID.instr)){
				if(!forward1){
					newState.IDEX.readRegB = state.MEMWB.writeData;
					forward1 = 1;
				}
			}	
		}
		if(opcode(state.MEMWB.instr) == LW){
			if(field1(state.MEMWB.instr) != 0 && field1(state.MEMWB.instr) == field0(state.IFID.instr)){
				if(!forward0){
					newState.IDEX.readRegA = state.MEMWB.writeData;
					forward0 = 1;
				}
			}
			if(field1(state.MEMWB.instr) != 0 && field1(state.MEMWB.instr) == field1(state.IFID.instr)){
				if(!forward1){
					newState.IDEX.readRegB = state.MEMWB.writeData;
					forward1 = 1;
				}
			}
		}

		state = newState; /* this is the last statement before end of the loop.
		It marks the end of the cycle and updates the
		current state with the values calculated in this
		cycle */
	}
}