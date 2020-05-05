import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Department {
	private String depName;
	private ArrayList<Candidate> candidates = new ArrayList<Candidate>();
	private ArrayList<Student> students = new ArrayList<Student>();
	
	public Department(String depName) {
		this.depName = depName;
	}
	public String getDepName() {
		return depName;
	}
	//students arrayList에 student를 추가함.
	public void studentAdd(Student std) {
		students.add(std);
	}
	//candidates arrayList에 candidate를 추가함.
	public void candidateAdd(Candidate cad) {
		candidates.add(cad);
	}
	//students arrayList에 저장되어있는 student 한명씩 투표를 한다.
	public void studentVote() {	
		Iterator<Student> it = students.iterator();
		while(it.hasNext()) {
			Student s = it.next();
			s.vote(candidates.size(), candidates);
		}
	}
	//method that return candidate with the most votes in the department
	public Candidate elected() {
		//표를 많이 받은 수를 기준으로 오름차순으로 정렬함.
		Collections.sort(candidates);
		return candidates.get(candidates.size() - 1);
	}
}
