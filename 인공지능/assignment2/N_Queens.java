import java.io.File;
import java.io.FileWriter;

public class N_Queens {
    private int N;
    private String print; //output 파일을 저장할 절대경로
    Node state;

    public N_Queens(int N, String print){
        this.N = N;
        this.print = print + "\\result" + N + ".txt";
        state = new Node(N);
    }

    // search by hill climbing
    public void hill_climbing() throws CloneNotSupportedException {
        boolean result = true;
        int numOfRestart = 0;
        // for column array initialization
        state.initialization();

        // For time checking
        long start = System.nanoTime();

        while(state.heuristic() != 0){
            boolean isBest = false;
            // 현재 node의 heuristic value
            int best = state.heuristic();
            Node bestNb = state.clone();

            // neighbor 중 heuristic이 제일 높은 node를 찾는다.( & First-choice hill climbing )
            for(int i = 0; i < N; i++){
                Node tmp = state.clone();
                int row = state.columns[i];
                for(int j = 1; j < N; j++){
                    tmp.columns[i] = (row + j) % N;
                    int h = tmp.heuristic();
                    if(h > best){
                        isBest = true;
                        best = h;
                        bestNb = tmp.clone();
                    }
                }
            }
            if(isBest)
                state = bestNb.clone();
            else {
                // current heuristic value보다 더 나은 state가 없으면 restart
                state.initialization();
                numOfRestart++;
            }
        }

        // For time checking
        long end = System.nanoTime();

        String toWrite = ">Hill Climbing\n";
        print(result, toWrite, (end-start) / 1000000000.0);

        System.out.println("number of restart : " + numOfRestart);
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
