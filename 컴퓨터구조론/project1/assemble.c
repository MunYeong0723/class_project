/* Assembler code fragment for LC-2K */
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>

#define MAXLINELENGTH 1000

int readAndParse(FILE *, char *, char *, char *, char *, char *);
int isNumber(char *);
int32_t bitsToDec(char*);
char* decToBits(char*);

typedef struct _Ins* Ins;
struct _Ins{
	char label[MAXLINELENGTH];
	char opcode[MAXLINELENGTH];
	char arg0[MAXLINELENGTH];
	char arg1[MAXLINELENGTH];
	char arg2[MAXLINELENGTH];
};

int
main(int argc, char *argv[])
{
	char *inFileString, *outFileString;
	FILE *inFilePtr, *outFilePtr;
	char label[MAXLINELENGTH], opcode[MAXLINELENGTH], arg0[MAXLINELENGTH], 
		arg1[MAXLINELENGTH], arg2[MAXLINELENGTH];

	if (argc != 3) {
		printf("error: usage: %s <assembly-code-file> <machine-code-file>\n",
			argv[0]);
		exit(1);
	}

	inFileString = argv[1];
	outFileString = argv[2];

	inFilePtr = fopen(inFileString, "r");
	if (inFilePtr == NULL) {
		printf("error in opening %s\n", inFileString);
		exit(1);
	}
	outFilePtr = fopen(outFileString, "w");
	if (outFilePtr == NULL) {
		printf("error in opening %s\n", outFileString);
		exit(1);
	}

	Ins* insArray = malloc(sizeof(struct _Ins) * MAXLINELENGTH);
	int insArray_size = 0;
	while (readAndParse(inFilePtr, label, opcode, arg0, arg1, arg2) ) {
		//error handling
		if(strlen(label) > 7 || isNumber(&(label[0]))){
			printf("error : invalid label\n");
			printf("%s\n", label);
			exit(1);
		}
		if(!( !strcmp(opcode, "add") || !strcmp(opcode, "nor") || !strcmp(opcode, "lw") || !strcmp(opcode, "sw")
			|| !strcmp(opcode, "beq") || !strcmp(opcode, "jalr") || !strcmp(opcode, "halt") || !strcmp(opcode, "noop")
			|| !strcmp(opcode, ".fill") )){
			printf("error : unrecognized opcode\n%s\n", opcode);
			exit(1);
		}
		if( (!strcmp(opcode, "lw") || !strcmp(opcode, "sw") || !strcmp(opcode, "beq")) && 
			isNumber(arg2) && atoi(arg2) > 65535){
			printf("error : offsetFields that don't fit in 16 bits\n");
			printf("%s\n", arg2);
			exit(1);
		}
		for(int i = 0; i < insArray_size; i++){
			if(strcmp(label, "") && !strcmp(insArray[i]->label, label)){
				printf("error : duplicate labels\n");
				printf("%s\n", label);
				exit(1);
			}
		}
		
		Ins I = malloc(sizeof(struct _Ins));
		strcpy(I->label, label);
		strcpy(I->opcode, opcode);
		strcpy(I->arg0, arg0);
		strcpy(I->arg1, arg1);
		strcpy(I->arg2, arg2);

		insArray[insArray_size] = I;
		insArray_size++;
	}
	/* this is how to rewind the file ptr so that you start reading from the
		beginning of the file */
	rewind(inFilePtr);

	for(int j = 0; j < insArray_size; j++){
		char bits[32];
		char* buf = NULL;
		memset(bits, '0', 32);
		//R-type
		if (!strcmp(insArray[j]->opcode, "add")) {
			//opcode
			bits[24] = '0';
			bits[23] = '0';
			bits[22] = '0';
			//regA
			buf = NULL;
			buf = decToBits(insArray[j]->arg0);
			bits[19] = buf[0];
			bits[20] = buf[1];
			bits[21] = buf[2];
			//regB
			buf = NULL;
			buf = decToBits(insArray[j]->arg1);
			bits[16] = buf[0];
			bits[17] = buf[1];
			bits[18] = buf[2];
			//destReg
			buf = NULL;
			buf = decToBits(insArray[j]->arg2);
			bits[0] = buf[0];
			bits[1] = buf[1];
			bits[2] = buf[2];

			int32_t result = 0;
			result = bitsToDec(bits);

			fprintf(outFilePtr, "%d\n", result);
		}
		else if(!strcmp(insArray[j]->opcode, "nor")){
			//opcode
			bits[24] = '0';
			bits[23] = '0';
			bits[22] = '1';
			//regA
			buf = NULL;
			buf = decToBits(insArray[j]->arg0);
			bits[19] = buf[0];
			bits[20] = buf[1];
			bits[21] = buf[2];
			//regB
			buf = NULL;
			buf = decToBits(insArray[j]->arg1);
			bits[16] = buf[0];
			bits[17] = buf[1];
			bits[18] = buf[2];
			//destReg
			buf = NULL;
			buf = decToBits(insArray[j]->arg2);
			bits[0] = buf[0];
			bits[1] = buf[1];
			bits[2] = buf[2];

			int32_t result = 0;
			result = bitsToDec(bits);

			fprintf(outFilePtr, "%d\n", result);
		}
		//I-type
		else if(!strcmp(insArray[j]->opcode, "lw")){
			//opcode
			bits[24] = '0';
			bits[23] = '1';
			bits[22] = '0';
			//regA
			buf = NULL;
			buf = decToBits(insArray[j]->arg0);
			bits[19] = buf[0];
			bits[20] = buf[1];
			bits[21] = buf[2];
			//regB
			buf = NULL;
			buf = decToBits(insArray[j]->arg1);
			bits[16] = buf[0];
			bits[17] = buf[1];
			bits[18] = buf[2];

			//offsetField
			if(isNumber(insArray[j]->arg2)){
			// numberic value
				int num = atoi(insArray[j]->arg2);
				if(num < 0) num--;
				char para[MAXLINELENGTH];
				sprintf(para, "%d", num);
				buf = NULL;
				buf = decToBits(para);
				for(int i = 0; i < 16; i++){
					bits[i] = buf[i];
				}
			}
			else{
			// symbolic address
				char* symbol = insArray[j]->arg2;
				int k = 0;
				for(; k < insArray_size; k++){
					if(!strcmp(insArray[k]->label, symbol))
						break;
				}
				//error handling
				if(k == insArray_size){
					printf("error : undefined label.\n");
					printf("%s\n", symbol);
					exit(1);
				}

				char para[MAXLINELENGTH];
				sprintf(para, "%d", k);
				buf = NULL;
				buf = decToBits(para);
				for(int i = 0; i < 16; i++){
					bits[i] = buf[i];
				}
			}

			int32_t result = 0;
			result = bitsToDec(bits);

			fprintf(outFilePtr, "%d\n", result);
		}
		else if(!strcmp(insArray[j]->opcode, "sw")){
			//opcode
			bits[24] = '0';
			bits[23] = '1';
			bits[22] = '1';
			//regA
			buf = NULL;
			buf = decToBits(insArray[j]->arg0);
			bits[19] = buf[0];
			bits[20] = buf[1];
			bits[21] = buf[2];
			//regB
			buf = NULL;
			buf = decToBits(insArray[j]->arg1);
			bits[16] = buf[0];
			bits[17] = buf[1];
			bits[18] = buf[2];

			//offsetField
			if(isNumber(insArray[j]->arg2)){
			// numberic value
				int num = atoi(insArray[j]->arg2);
				if(num < 0) num--;
				char para[MAXLINELENGTH];
				sprintf(para, "%d", num);
				buf = NULL;
				buf = decToBits(para);
				for(int i = 0; i < 16; i++){
					bits[i] = buf[i];
				}
			}
			else{
			// symbolic address
				char* symbol = insArray[j]->arg2;
				int k = 0;
				for(; k < insArray_size; k++){
					if(!strcmp(insArray[k]->label, symbol))
						break;
				}
				//error handling
				if(k == insArray_size){
					printf("error : undefined label.\n");
					printf("%s\n", symbol);
					exit(1);
				}

				char para[MAXLINELENGTH];
				sprintf(para, "%d", k);
				buf = NULL;
				buf = decToBits(para);
				for(int i = 0; i < 16; i++){	
					bits[i] = buf[i];
				}
			}

			int32_t result = 0;
			result = bitsToDec(bits);

			fprintf(outFilePtr, "%d\n", result);
		}
		else if(!strcmp(insArray[j]->opcode, "beq")){
			//opcode
			bits[24] = '1';
			bits[23] = '0';
			bits[22] = '0';
			//regA
			buf = NULL;
			buf = decToBits(insArray[j]->arg0);
			bits[19] = buf[0];
			bits[20] = buf[1];
			bits[21] = buf[2];
			//regB
			buf = NULL;
			buf = decToBits(insArray[j]->arg1);
			bits[16] = buf[0];
			bits[17] = buf[1];
			bits[18] = buf[2];

			//offsetField
			if(isNumber(insArray[j]->arg2)){
			// numberic value
				int num = atoi(insArray[j]->arg2);
				if(num < 0) num--;
				char para[MAXLINELENGTH];
				sprintf(para, "%d", num);
				buf = NULL;
				buf = decToBits(para);
				for(int i = 0; i < 16; i++){
					bits[i] = buf[i];
				}
			}
			else{
			// symbolic address
				char* symbol = insArray[j]->arg2;
				int k = 0;
				for(; k < insArray_size; k++){
					if(!strcmp(insArray[k]->label, symbol))
						break;
				}
				//error handling
				if(k == insArray_size){
					printf("error : undefined label.\n");
					printf("%s\n", symbol);
					exit(1);
				}

				int target = k-j;
				target--;
				char para[MAXLINELENGTH];
				sprintf(para, "%d", target);
				buf = NULL;
				buf = decToBits(para);
				for(int i = 0; i < 16; i++){
					bits[i] = buf[i];
				}
			}

			int32_t result = 0;
			result = bitsToDec(bits);

			fprintf(outFilePtr, "%d\n", result);
		}
		//J-type
		else if(!strcmp(insArray[j]->opcode, "jalr")){
			//opcode
			bits[24] = '1';
			bits[23] = '0';
			bits[22] = '1';
			//regA
			buf = NULL;
			buf = decToBits(insArray[j]->arg0);
			bits[19] = buf[0];
			bits[20] = buf[1];
			bits[21] = buf[2];
			//regB
			buf = NULL;
			buf = decToBits(insArray[j]->arg1);
			bits[16] = buf[0];
			bits[17] = buf[1];
			bits[18] = buf[2];

			int32_t result = 0;
			result = bitsToDec(bits);

			fprintf(outFilePtr, "%d\n", result);
		}
		//O-type
		else if(!strcmp(insArray[j]->opcode, "halt")){
			//opcode
			bits[24] = '1';
			bits[23] = '1';
			bits[22] = '0';

			int32_t result = 0;
			result = bitsToDec(bits);

			fprintf(outFilePtr, "%d\n", result);
		}
		else if(!strcmp(insArray[j]->opcode, "noop")){
			//opcode
			bits[24] = '1';
			bits[23] = '1';
			bits[22] = '1';

			int32_t result = 0;
			result = bitsToDec(bits);

			fprintf(outFilePtr, "%d\n", result);
		}

		else if(!strcmp(insArray[j]->opcode, ".fill")){
			if(isNumber(insArray[j]->arg0)){
			// numberic value
				buf = NULL;
				buf = decToBits(insArray[j]->arg0);
				for(int i = 0; i < 32; i++){
					bits[i] = buf[i];
				}
			}
			else{
			// symbolic address
				char* symbol = insArray[j]->arg0;
				int k = 0;
				for(; k < insArray_size; k++){
					if(!strcmp(insArray[k]->label, symbol))
						break;
				}

				char para[MAXLINELENGTH];
				sprintf(para, "%d", k);
				buf = NULL;
				buf = decToBits(para);
				for(int i = 0; i < 32; i++){
					bits[i] = buf[i];
				}
			}

			int32_t result = 0;
			result = bitsToDec(bits);

			fprintf(outFilePtr, "%d\n", result);
		}
	}
	return(0);
}

int
readAndParse(FILE *inFilePtr, char *label, char *opcode, char *arg0,
	char *arg1, char *arg2)
{
	char line[MAXLINELENGTH];
	char *ptr = line;

	/* delete prior values */
	label[0] = opcode[0] = arg0[0] = arg1[0] = arg2[0] = '\0';

	/* read the line from the assembly-language file */
	if (fgets(line, MAXLINELENGTH, inFilePtr) == NULL) {
		/* reached end of file */
		return(0);
	}

	/* check for line too long (by looking for a \n) */
	if (strchr(line, '\n') == NULL) {
		/* line too long */
		printf("error: line too long\n");
		exit(1);
	}

	/* is there a label? */
	ptr = line;
	if (sscanf(ptr, "%[^\t\n\r ]", label)) {
		/* successfully read label; advance pointer over the label */
		ptr += strlen(label);
	}

	sscanf(ptr, "%*[\t\n\r ]%[^\t\n\r ]%*[\t\n\r ]%[^\t\n\r ]%*[\t\n\r ]%[^\t\n\r ]%*[\t\n\r ]%[^\t\n\r ]",
		opcode, arg0, arg1, arg2);
	return(1);
}
int
isNumber(char *string)
{
	/* return 1 if string is a number */
	int i;
	return( (sscanf(string, "%d", &i)) == 1);
}

int32_t bitsToDec(char* bits){
	int32_t dec = 0;
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

char* decToBits(char* dec){
	int k = 0, num = 0;
	num = atoi(dec);
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