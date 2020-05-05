
public class Candidate extends Student {
	//ǥ�� ���� ��
	private int voteNum = 0;
	
	public Candidate(String name, int id, Department dep) {
		super(name, id, dep);
	}
	//voteNum�� +1�ϴ� �Լ�
	public void addVoteNum() {
		this.voteNum += 1;
	}

	@Override
	public int compareTo(Student other) {
		Candidate c = (Candidate)other;
		if(voteNum > c.voteNum) return 1;
		else if(voteNum < c.voteNum) return -1;
		else return 0;
	}
	
	@Override
	public String toString() {	
		String s = new String(super.toString());
		s += "\r\nVotes : " + voteNum;
		s += "\r\n";
		return s;
	}
	
	public int getVoteNum() {
		return voteNum;
	}
}
