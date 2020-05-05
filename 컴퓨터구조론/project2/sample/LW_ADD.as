	lw 0 1 data1	load Data to reg1
	lw 0 2 data2	load Data to reg2
	noop
	noop
	noop
	add 1 2 3		add reg1, reg2 -> reg3
	halt
data1 .fill 10
data2 .fill 20
