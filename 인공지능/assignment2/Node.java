import java.util.Arrays;
import java.util.Random;

public class Node implements Cloneable {
    private int N;
    public int[] columns; //queens의 위치를 저장할 array (i번째 column 중 몇번째 row에 있는지)
    Random ran = new Random(); //queens 위치를 랜덤하게 지정하기 위한 object

    public Node(int N){
        this.N = N;
        columns = new int[N];
        // queens의 위치를 랜덤하게 지정
        for(int i = 0; i < N; i++){
            columns[i] = ran.nextInt(N);
        }
    }

    // for deep copy
    public Node clone() throws CloneNotSupportedException{
        Node state = (Node)super.clone();
        state.columns = Arrays.copyOf(columns, N);
        return state;
    }

    // for print
    public String printArr(){
        String result = Arrays.toString(this.columns).replaceAll("[^0-9 ]", "");
        return result;
    }

    // for array initialization
    public void initialization(){
        // queens의 위치를 랜덤하게 지정
        for(int i = 0; i < N; i++){
            columns[i] = ran.nextInt(N);
        }
    }

    // heuristic function
    public int heuristic(){
        int h = 0;
        for(int i = 0; i < N; i++){
            for(int j = i+1; j < N; j++){
                if(columns[i] == columns[j])
                    h -= 1;
                if(Math.abs(j-i) == Math.abs(columns[j] - columns[i]))
                    h -=1;
            }
        }
        return h;
    }
}
