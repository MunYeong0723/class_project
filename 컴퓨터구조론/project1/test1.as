	lw 0 2 index		load reg2 with index 1
	lw 0 3 last			load reg3 with last 16
	lw 0 6 jump			load reg6 with where to jump 3
start add 1 2 1			result(reg1) + index(reg2)
	add 2 2 2			index * 2
	sw 0 1 11			store mem11 <- reg1
	beq 2 3 done		if(index == last) then go done
	jalr 6 7			jump to next instruction
done halt
index .fill 1
last .fill 16
jump .fill 3
