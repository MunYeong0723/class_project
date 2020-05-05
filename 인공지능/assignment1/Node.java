import java.util.Arrays;

public class Node implements Cloneable{
    private int N;
    public int[] columns; //queens의 위치를 저장할 array (i번째 column 중 몇번째 row에 있는지)
    public int depth;

    public Node(int N){
        this.N = N;
        columns = new int[N];
        Arrays.fill(columns, 0);
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
        Arrays.fill(columns, 0);
    }
}
