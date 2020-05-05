	lw 0 1 data1	load data1 to reg1
	noop
	noop
	noop			noop 3 times wait for set reg1 data (for not to have data hazard)
	beq 0 0 3		in here, have branch hazard. because use branch no taken, just do next instructions until branch instruction goes to MEM stage(so will execute next 3 instructions). if this branch taken, need to flush 3 instructions(change to noop instruction).
	add 1 1 2
	add 1 1 3
	add 1 1 4
	add 1 1 5		if branch taken, do this instruction
	add 1 1 6
	halt
data1 .fill 5
