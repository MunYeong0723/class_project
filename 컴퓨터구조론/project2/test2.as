	lw 0 1 data1	load data1 to reg1
	lw 0 2 data2	load data2 to reg2
	noop
	noop
	noop			noop 3 times not to have data hazard. because data writes back to register in WB stage. 
	nor 1 2 3		reg3 = reg1 nor reg2
	halt
data1 .fill 1
data2 .fill 5
