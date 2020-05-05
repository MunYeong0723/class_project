	lw 0 1 data1	load data1 to reg1
	lw 0 2 data2	load data2 to reg2
	add 1 2 3		reg3 = reg2 + reg1 -> in here, data1 and data2 not loaded in reg1, reg2 yet.(data hazard) so need to stall 1 cycle and using forwarding, we can know reg1, reg2. 
	halt
data1 .fill 10
data2 .fill 20
