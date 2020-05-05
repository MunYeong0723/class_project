/* LC-2K Instruction-level simulator */
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define NUMMEMORY 65536 /* maximum number of words in memory */
#define NUMREGS 8 /* number of machine registers */
#define MAXLINELENGTH 1000

typedef struct stateStruct {
	int pc;
	int mem[NUMMEMORY];
	int reg[NUMREGS];
	int numMemory;
}stateType;

void printState(stateType *);
int convertNum(int);
char* decToBits(int);
int bitsToDec(char*);
void runSimulator(stateType *);

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
		if (sscanf(line, "%d", state.mem+state.numMemory) != 1) {
			printf("error in reading address %d\n", state.numMemory);
			exit(1);
		}
		printf("memory[%d]=%d\n", state.numMemory, state.mem[state.numMemory]);
	}

	runSimulator(&state);

	return(0);
}

void
printState(stateType *statePtr)
{
	int i;
	printf("\n@@@\nstate:\n");
	printf("\tpc %d\n", statePtr->pc);
	printf("\tmemory:\n");
	for (i=0; i<statePtr->numMemory; i++) {
		printf("\t\tmem[ %d ] %d\n", i, statePtr->mem[i]);
	}
	printf("\tregisters:\n");
	for (i=0; i<NUMREGS; i++) {
		printf("\t\treg[ %d ] %d\n", i, statePtr->reg[i]);
	}
	printf("end state\n");
}
int convertNum(int num){
	/* convert a 16-bit number into a 32-bit Linux integer*/
	if(num & (1<<15)){
		num -= (1<<16);
	}
	return(num);
}

char* decToBits(int num){
	int k = 0;
	char* result = malloc(sizeof(char) * 32);

	for(int c = 31; c >= 0; c--){
		k = num >> c;
		if(k & 1)
			result[c] = '1';
		else
			result[c] = '0';
	}

	return result;
}
int bitsToDec(char* bits){
	int dec = 0;
	for(int i = 31; i >= 0; ){
		if(bits[i] == '1'){
			dec |= 1;
		}
		i--;
		if(i >= 0)
			dec <<= 1;
	}
	return dec;
}
void runSimulator(stateType* state){
	int i, count = 0;
	//reset pc and register
	state->pc = 0;
	for(i = 0; i < NUMREGS; i++){
		state->reg[i] = 0;
	}

	int a = 0;
	while(1){
		count++;
		printState(state);

		char* bits;
		bits = decToBits(state->mem[state->pc]);
		//R-type
		if(bits[24] == '0' && bits[23] == '0' && bits[22] == '0'){
		//add
			char* regA = (char*)malloc(sizeof(char)*32);
			char* regB = (char*)malloc(sizeof(char)*32);
			char* destReg = (char*)malloc(sizeof(char)*32);
			memset(regA, '0', 32);
			memset(regB, '0', 32);
			memset(destReg, '0', 32);

			regA[0] = bits[19];
			regA[1] = bits[20];
			regA[2] = bits[21];

			regB[0] = bits[16];
			regB[1] = bits[17];
			regB[2] = bits[18];

			destReg[0] = bits[0];
			destReg[1] = bits[1];
			destReg[2] = bits[2];

			int A = state->reg[bitsToDec(regA)];
			int B = state->reg[bitsToDec(regB)];
			int dest = bitsToDec(destReg);

			state->reg[dest] = A+B;
		}
		else if(bits[24] == '0' && bits[23] == '0' && bits[22] == '1'){
		//nor
			char* regA = (char*)malloc(sizeof(char)*32);
			char* regB = (char*)malloc(sizeof(char)*32);
			char* destReg = (char*)malloc(sizeof(char)*32);
			memset(regA, '0', 32);
			memset(regB, '0', 32);
			memset(destReg, '0', 32);

			regA[0] = bits[19];
			regA[1] = bits[20];
			regA[2] = bits[21];

			regB[0] = bits[16];
			regB[1] = bits[17];
			regB[2] = bits[18];

			destReg[0] = bits[0];
			destReg[1] = bits[1];
			destReg[2] = bits[2];

			int A = state->reg[bitsToDec(regA)];
			int B = state->reg[bitsToDec(regB)];
			int dest = bitsToDec(destReg);

			state->reg[dest] = ~(A | B);
		}
		//I-type
		else if(bits[24] == '0' && bits[23] == '1' && bits[22] == '0'){
		//lw
			char* regA = (char*)malloc(sizeof(char)*32);
			char* regB = (char*)malloc(sizeof(char)*32);
			char* offset = (char*)malloc(sizeof(char)*32);
			memset(regA, '0', 32);
			memset(regB, '0', 32);
			memset(offset, '0', 32);

			regA[0] = bits[19];
			regA[1] = bits[20];
			regA[2] = bits[21];

			regB[0] = bits[16];
			regB[1] = bits[17];
			regB[2] = bits[18];

			for(int i = 0; i < 16; i++){
				offset[i] = bits[i];
			}
			//sign extension
			int field = convertNum(bitsToDec(offset));
			int data = state->mem[state->reg[bitsToDec(regA)] + field];
			state->reg[bitsToDec(regB)] = data;
		}
		else if(bits[24] == '0' && bits[23] == '1' && bits[22] == '1'){
		//sw
			char* regA = (char*)malloc(sizeof(char)*32);
			char* regB = (char*)malloc(sizeof(char)*32);
			char* offset = (char*)malloc(sizeof(char)*32);
			memset(regA, '0', 32);
			memset(regB, '0', 32);
			memset(offset, '0', 32);

			regA[0] = bits[19];
			regA[1] = bits[20];
			regA[2] = bits[21];

			regB[0] = bits[16];
			regB[1] = bits[17];
			regB[2] = bits[18];

			for(int i = 0; i < 16; i++){
				offset[i] = bits[i];
			}
			//sign extension
			int field = convertNum(bitsToDec(offset));
			int data = state->reg[bitsToDec(regB)];
			state->mem[state->reg[bitsToDec(regA)] + field] = data;		
		}
		else if(bits[24] == '1' && bits[23] == '0' && bits[22] == '0'){
		//beq
			char* regA = (char*)malloc(sizeof(char)*32);
			char* regB = (char*)malloc(sizeof(char)*32);
			char* offset = (char*)malloc(sizeof(char)*32);
			memset(regA, '0', 32);
			memset(regB, '0', 32);
			memset(offset, '0', 32);

			regA[0] = bits[19];
			regA[1] = bits[20];
			regA[2] = bits[21];

			regB[0] = bits[16];
			regB[1] = bits[17];
			regB[2] = bits[18];

			for(int i = 0; i < 16; i++){
				offset[i] = bits[i];
			}
			//sign extension
			int field = convertNum(bitsToDec(offset));
			if(state->reg[bitsToDec(regA)] == state->reg[bitsToDec(regB)]){
				//move to target address
				state->pc = state->pc + 1 + field;
				continue;
			}
		}
		//J-type
		else if(bits[24] == '1' && bits[23] == '0' && bits[22] == '1'){
		//jalr
			char* regA = (char*)malloc(sizeof(char)*32);
			char* regB = (char*)malloc(sizeof(char)*32);
			memset(regA, '0', 32);
			memset(regB, '0', 32);

			regA[0] = bits[19];
			regA[1] = bits[20];
			regA[2] = bits[21];

			regB[0] = bits[16];
			regB[1] = bits[17];
			regB[2] = bits[18];

			int A = bitsToDec(regA);
			int B = bitsToDec(regB);

			state->reg[B] = (state->pc)+1;
			state->pc = state->reg[A];
			continue;
		}
		//O-type
		else if(bits[24] == '1' && bits[23] == '1' && bits[22] == '0'){
		//halt
			printf("halt\n");
			state->pc++;
			printf("machine halted\n");
			printf("total of %d instructions executed\n", count);
			printf("final state of machine:\n");
			printState(state);
			break;
		}
		else if(bits[24] == '1' && bits[23] == '1' && bits[22] == '1'){
		//noop
			//do nothing
		}
		state->pc++;
	}
}
