	lw 0 2 mcand		load reg2 with 32766
	lw 0 3 mplier		load reg3 with 10383
	lw 0 4 index		load reg4 with index 1
	lw 0 6 maxbit		load reg6 with maxbit 15
	lw 0 7 count		load reg7 with count 0
start nor 3 3 3			complement mplier
	nor 4 4 4			complement index
	nor 3 4 5			compute mplier & index and store in reg5
	nor 3 3 3			return back mplier
	nor 4 4 4			return back index
	beq 0 5 next 		if(mplier & index == 0) then go next
	add 1 2 1			mcand + result
next add 4 4 4			index * 2
	add 2 2 2			mcand * 2
	beq	7 6 done		if(count == maxbit) then go done
	lw 0 3 one			load reg3 with 1
	add 3 7 7			count++
	lw 0 3 mplier		load reg3 with mplier
	beq 0 0 start		loop
done halt				end program
mcand .fill 32766
mplier .fill 10383
index .fill 1
count .fill 0
maxbit .fill 15
one .fill 1
