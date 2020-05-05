from z3 import *
import time

# Number of Queens
print("N: ")
N = int(input())

start = time.time()
# Variables
X = [Int("x_%s" % (row)) for row in range(1, N+1)]

# Constraints
domain = [Or([X[row] == i for i in range(1, N+1)]) for row in range(N)]
# queen이 같은 row에 없어야 한다.
colConst = [And([X[row] != X[i] for row in range(N) for i in range(row + 1, N)])]
# queen이 대각선상에 없어야 한다.
digConst = [And([And(X[row] - X[i] != abs(row - i), X[i] - X[row] != abs(row - i)) 
			for row in range(N) for i in range(row + 1, N)])]

eight_queens_c = domain + colConst + digConst

s = Solver()
s.add(eight_queens_c)

if s.check() == sat:
    m = s.model()
    r = [m.evaluate(X[i]) for i in range(N)]
    print_matrix(r)

print("elapsed time: ", time.time() - start, " sec")

