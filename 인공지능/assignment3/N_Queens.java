import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class N_Queens {
    private int N;
    private String print; //output 파일을 저장할 절대경로
    Node state;
    private int maxPair;

    public N_Queens(int N, String print){
        this.N = N;
        this.print = print + "\\result" + N + ".txt";
        state = new Node(N);
        maxPair = (N*(N-1))/2;
    }

    // print result in "resultN.txt"
    private void print(boolean result, String search, double time){
        String toWrite;
        // parameter result가 true이면 solution을 찾은 경우
        if(result){
            toWrite = search + state.printArr() + "\nTotal Elasped Time : " + String.format("%.5f", time) + "\n\n";
        }else
            toWrite = search + "No solution\nTotal Elasped Time : 0.0\n\n";

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

    // sorting in order of high fitness value (내림차순)
    private void sorting(ArrayList<Node> generation){
        Collections.sort(generation, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                if(o1.fitness < o2.fitness) return 1;
                else if(o1.fitness > o2.fitness) return -1;
                return 0;
            }
        });
    }

    public void GA() throws CloneNotSupportedException {
        boolean result = true;
        Random ran = new Random();
        // For time checking
        long start = System.nanoTime();

        ArrayList<Node> generation = new ArrayList<>();
        ArrayList<Node> nextGeneration = new ArrayList<>();

        // generate initial generation
        Node init = new Node(N);
        for(int i = 0; i < 5000; i++){
            init.generateIndi();
            // fitness를 계산해줌.
            init.fitness();
            generation.add(init.clone());
        }
        // sorting in order of high fitness value
        sorting(generation);

        while(generation.get(0).fitness != maxPair){
            // fitness 가 높은 10%를 next generation 으로 그대로 copy
            for(int i = 0; i < generation.size() * 0.1; i++){
                nextGeneration.add(generation.get(i).clone());
            }

            // cross-over
            int parentSize = nextGeneration.size();
            int mom,dad;
            Node child = new Node(N);
            for(int i = 0; i < generation.size() * 0.9; i++){
                // random 하게 parent 를 뽑아
                mom = ran.nextInt(parentSize);
                dad = ran.nextInt(parentSize);
                // column 을 child 에게 유전함. division point 는 random 하게 결정
                int division = ran.nextInt(N);
                for(int j = 0; j < division; j++){
                    child.columns[j] = generation.get(mom).columns[j];
                }
                for(int j = division+1; j < N; j++){
                    child.columns[j] = generation.get(dad).columns[j];
                }
                // child의 fitness를 계산하고 nextGeneration arrayList 에 add 함.
                child.fitness();
                nextGeneration.add(child.clone());
            }

            // mutation
            for(int i = 0; i < (nextGeneration.size() - parentSize)*0.01; i++){
                // 새로 만든 child 중 random 하게 하나 select
                int k = ran.nextInt(nextGeneration.size() - parentSize) + parentSize;
                // mutate 할 child 의 columns array 에서 random index 의 value 를 random value 로 바꿈.
                nextGeneration.get(k).columns[ran.nextInt(N)] = ran.nextInt(N);
                // mutate한 child의 fitness를 다시 계산해줌.
                nextGeneration.get(k).fitness();
            }

            // sorting in order of high fitness value
            sorting(nextGeneration);
            // copy nextGeneration to generation
            generation = (ArrayList<Node>)nextGeneration.clone();
            nextGeneration.clear();
        }
        state = generation.get(0);

        // For time checking
        long end = System.nanoTime();

        String toWrite = ">Genetic Algorithm\n";
        print(result, toWrite, (end-start) / 1000000000.0);
    }
}
