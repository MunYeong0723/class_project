import java.util.ArrayList;
import java.util.Random;

public class Student implements Voter {
	private String name;
	private int identificationNum;
	private Department dep;
	
	public Student(String name, int id, Department dep) {
		this.name = name;
		this.identificationNum = id;
		this.dep = dep;
	}
	//랜덤으로 투표를 함.
	public void vote(int candidateNum, ArrayList<Candidate> c) {
		Random generator = new Random();
		int vote = generator.nextInt(candidateNum);
		
		Candidate elect = c.get(vote);
		elect.addVoteNum();
	}
	
	@Override
	public int compareTo(Student other) {
		if(name.compareTo(other.name) > 0) return 1;
		else if(name.compareTo(other.name) < 0) return -1;
		else return 0;
	}

	@Override
	public String toString() {		
		String s = new String();
		s += "Department name : " + dep.getDepName();
		s += "\r\nname : " + name;
		s += "\r\nStudent_id : " + identificationNum;
		
		return s;
	}
}
