import java.util.ArrayList;

public interface Voter extends Comparable<Student> {
	public void vote(int candidateNum, ArrayList<Candidate> c);
}
