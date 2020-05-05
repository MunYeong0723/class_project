import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class ElectionSim {
	//read, write data
	
	String departmentPath;
	String studentPath;
	String outfilePath;
	
	ArrayList<Department> departments = new ArrayList<Department>();
	ArrayList<Candidate> elected = new ArrayList<Candidate>();
	
	BufferedReader department = null;
	BufferedReader student = null;
	BufferedWriter write = null;
	
	public ElectionSim(String departmentPath, String studentPath, String outfilePath) {
		this.departmentPath = departmentPath;
		this.studentPath = studentPath;
		this.outfilePath = outfilePath;
		
		try {
			department = new BufferedReader(new InputStreamReader(new FileInputStream(departmentPath),"UTF8"));
			student = new BufferedReader(new InputStreamReader(new FileInputStream(studentPath),"UTF8"));
			
			String line = department.readLine();
			//read data from "input1.csv", add them into Collection of Department
			while((line = department.readLine()) != null) {
				String[] splitData = line.split(",");
				int id = Integer.parseInt(splitData[0]);
				Department dep = new Department(splitData[1]);
				
				departments.add(dep);
			}
			
			//read data from "input2.csv", add them into Collection of Student
			line = student.readLine();
			while((line = student.readLine()) != null) {
				String[] splitData = line.split(",");
				int id = Integer.parseInt(splitData[0]);
				int depNum = Integer.parseInt(splitData[1]) - 1;
				String name = splitData[2];
				String candidate = splitData[3];
				
				Department department = departments.get(depNum);
				Student st = new Student(name, id, department);
				department.studentAdd(st);
				
				if(candidate.equals("TRUE")) {
					Candidate cd = new Candidate(name, id, department);
					department.candidateAdd(cd);
				}
				
			}
		}
		catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void saveData() {
		//sort collection of elected candidate
		//득표수를 기준으로  하여 오름차순으로 정렬
		Collections.sort(elected);
		
		//write information
		try {
			write = new BufferedWriter(new FileWriter(outfilePath));
			
			for(int i = 0; i < elected.size(); i++) {
				write.write("=======Elected Candidate=======\r\n");
				Candidate c = elected.get(i);
				write.write(c.toString());	
				write.write("===============================\r\n");
			}
			
			write.close();
		}
		catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void runSimulation() {
		//학생 한명씩 투표를 한다.
		Iterator<Department> it = departments.iterator();
		while(it.hasNext()) {
			Department d = it.next();
			d.studentVote();
			elected.add(d.elected());
		}
		//call saveData method to save
		saveData();
	}
}
