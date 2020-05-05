public class Main {
    public static void main(String[] args) throws CloneNotSupportedException{
        // input
        int N = Integer.parseInt(args[0]);
        String print = args[1];

        N_Queens queens = new N_Queens(N, print);
        queens.DFS();
        queens.BFS();
        queens.DFID();
    }
}
