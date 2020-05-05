	lw 0 2 index		load reg2 with index 1
	lw 0 3 last			load reg3 with last 16
	lw 0 6 wherejump	load reg6 with where to jump 3
start add 1 2 4			result(reg1) + index(reg2)
	sw 0 4 1			reg1 = reg4 
	add 2 2 2			index * 2
	beq 2 3 done		if(index == last) then go done
	jalr 6 7			jump to next instruction
done noop
	halt
index .fill 1
last .fill 16
wherejump .fill 3
