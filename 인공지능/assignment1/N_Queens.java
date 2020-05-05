import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class N_Queens {
    private int N;
    private String print; //output 파일을 저장할 절대경로
    Node state;

    public N_Queens(int N, String print){
        this.N = N;
        this.print = print + "\\result" + N + ".txt";
        state = new Node(N);
    }

    // searching by DFS
    public void DFS() throws CloneNotSupportedException{
        boolean result = false;
        // for column array initialization
        state.initialization();

        // For time checking
        long start = System.nanoTime();

        Stack<Node> s = new Stack<>();
        // expanding
        for(int j = 0; j < N; j++){
            state.columns[0] = j;
            state.depth = 1;
            s.push(state.clone());
        }

        while(!s.isEmpty()){
            // stack에서 하나 뽑고
            state = s.pop();
            // expanding
            Node tmp = state.clone();
            for(int j = 0; j < N && state.depth < N; j++){
                tmp.columns[state.depth] = j;
                tmp.depth = state.depth + 1;
                s.push(tmp.clone());
            }
            //goal test
            if(isGoal(state.depth)) {
                result = true;
                break;
            }
        }

        // For time checking
        long end = System.nanoTime();

        String toWrite = ">DFS\n";
        print(result, toWrite, (end-start) / 1000000000.0);
    }

    // searching by BFS
    public void BFS() throws CloneNotSupportedException{
        boolean result = false;
        // for column array initialization
        state.initialization();

        // For time checking
        long start = System.nanoTime();

        Queue<Node> q = new LinkedList<>();
        // expanding
        for(int j = 0; j < N; j++){
            state.columns[0] = j;
            state.depth = 1;
            q.add(state.clone());
        }

        while(!q.isEmpty()){
            // queue에서 하나 뽑고
            state = q.poll();
            // expanding
            Node tmp = state.clone();
            for(int j = 0; j < N && state.depth < N; j++) {
                tmp.columns[state.depth] = j;
                tmp.depth = state.depth + 1;
                q.add(tmp.clone());
            }
            // goal test
            if(isGoal(state.depth)){
                result = true;
                break;
            }
        }

        // For time checking
        long end = System.nanoTime();

        String toWrite = ">BFS\n";
        print(result, toWrite, (end-start) / 1000000000.0);
    }

    // searching by DFID
    public void DFID() throws CloneNotSupportedException{
        boolean result = false;
        // for column array initialization
        state.initialization();

        // For time checking
        long start = System.nanoTime();

        Stack<Node> s = new Stack<>();
        // limit을 모두 늘렸는데도 stack이 empty이면 no solution
        for(int limit = 0; limit <= N;){
            if(s.isEmpty()) {
                // limit을 하나 늘려주고 columns array 초기화한 후 depth 0부터 다시 search한다.
                limit++;
                state.initialization();
                // expanding
                for(int j = 0; j < N; j++){
                    state.columns[0] = j;
                    state.depth = 1;
                    s.push(state.clone());
                }
            }else{
                state = s.pop();
                // 현재 방문한 state의 depth가 limit와 같다면 더이상 expanding하지 않는다
                if(state.depth < limit){
                    // expanding
                    Node tmp = state.clone();
                    for(int j = 0; j < N && state.depth < N; j++){
                        tmp.columns[state.depth] = j;
                        tmp.depth = state.depth + 1;
                        s.push(tmp.clone());
                    }
                }
                // goal test
                if(isGoal(state.depth)){
                    result = true;
                    break;
                }
            }
        }

        // For time checking
        long end = System.nanoTime();

        String toWrite = ">DFID\n";
        print(result, toWrite, (end-start) / 1000000000.0);
    }

    // for goal test
    private boolean isGoal(int now){
        // 현재 방문한 state의 depth가 N이 아니라면 false를 return
        if(now == N){
            for(int i = 1; i < N; i++){
                // column을 하나씩 늘려서 check하기 때문에 현재 test하는 column(i)까지만 test해보면 된다.
                for(int j = 0; j < i; j++){
                    // j번째 column의 queen 위치와 현재 test하는 위치(i)의 queen이 일직선상에 있거나
                    // j번째 column의 queen 위치와 현재 test하는 위치(i)의 queen이 대각선상에 있는 경우
                    // false를 return
                    if(state.columns[j] == state.columns[i] || Math.abs(i - j) == Math.abs(state.columns[i] - state.columns[j])){
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    // print result in "resultN.txt"
    public void print(boolean result, String search, double time){
        String toWrite;
        // parameter result가 true이면 solution을 찾은 경우
        if(result){
            toWrite = search + state.printArr() + "\nTime : " + String.format("%.12f", time) + "\n\n";
        }else
            toWrite = search + "No solution\nTime : 0.0\n\n";

        File file = new File(this.print);
        FileWriter writer = null;

        // file에 output 입력
        try{
            // true means if file exists, continue writing to that file.
            writer = new FileWriter(file, true);
            writer.write(toWrite);
            writer.flush();
        } catch (Exception e){
            e.printStackTrace();
        } finally{
            try{
                if(writer != null) writer.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
