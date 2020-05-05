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
	//students arrayList�� student�� �߰���.
	public void studentAdd(Student std) {
		students.add(std);
	}
	//candidates arrayList�� candidate�� �߰���.
	public void candidateAdd(Candidate cad) {
		candidates.add(cad);
	}
	//students arrayList�� ����Ǿ��ִ� student �Ѹ� ��ǥ�� �Ѵ�.
	public void studentVote() {	
		Iterator<Student> it = students.iterator();
		while(it.hasNext()) {
			Student s = it.next();
			s.vote(candidates.size(), candidates);
		}
	}
	//method that return candidate with the most votes in the department
	public Candidate elected() {
		//ǥ�� ���� ���� ���� �������� ������������ ������.
		Collections.sort(candidates);
		return candidates.get(candidates.size() - 1);
	}
}
