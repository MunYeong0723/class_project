	lw 0 1 data1	load data1 to reg1
	lw 0 2 data2	load data2 to reg2
	noop
	noop
	noop			noop 3 times wait for set reg1, reg2 data (for not to have data hazard)
	add 1 2 3		reg3 = reg2 + reg1
	noop
	noop
	noop			because reg3(aluResult) data set in WB stage, noop 3 times to wait for set reg3 data (for not to have data hazard)
	sw 0 3 data1	dataMem[11] = reg[3]
	halt
data1 .fill 10
data2 .fill 20
