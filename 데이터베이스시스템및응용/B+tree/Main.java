import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


public class Main {

	public static void main(String[] args) {
		BpTree bp = new BpTree();
		/*ObjectOutputStream oos = null;
		ObjectInputStream ois = null;*/
		/*FileInputStream fis = null;
		FileOutputStream fos = null;
		ByteArrayOutputStream baos = null;
		ByteArrayInputStream bais = null;*/
		BufferedReader readKey = null;
		BufferedWriter writeKey = null;
		
		//command의 위치 or 길이가 예상과 다를 때의 예외 처리 해주기!
		if(args[0].equals("-c")) {	
			//create bptree
			int degree = Integer.parseInt(args[2]);
			bp = new BpTree(args[1], degree);
			bp.create();
			
			try {
				
				/*fos = new FileOutputStream(args[1]);
				baos = new ByteArrayOutputStream();
				oos = new ObjectOutputStream(baos);*/
				/*oos = new ObjectOutputStream(new FileOutputStream(args[1]));
				oos.writeObject(bp);*/
			//	byte[] bytes = baos.toByteArray();
				writeKey = new BufferedWriter(new FileWriter(args[1]));
				bp.saveData(writeKey);
			}
			catch(IOException e) {
				e.printStackTrace();
			}finally {
				try {
				//	if(oos != null) oos.close();
					if(writeKey != null) writeKey.close();
				}catch(IOException e) {}
			}

		}
		else if(args[0].equals("-i")) {
			//insertion
			try {
				/*fis = new FileInputStream(args[1]);
				bais = new ByteArrayInputStream();*/
				
				/*ois = new ObjectInputStream(new FileInputStream(args[1]));
				bp = (BpTree)ois.readObject();*/
				
				readKey = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
				String keyLine = null;
				int degree = -1;
				keyLine = readKey.readLine();
				degree = Integer.parseInt(keyLine);
				bp = new BpTree(args[1], degree);
				bp.create();
				
				int keyRead = -1, valueRead = -1;
				while((keyLine = readKey.readLine()) != null) {
					String[] splitData = keyLine.split(",");
					keyRead = Integer.parseInt(splitData[0]);
					valueRead = Integer.parseInt(splitData[1]);
					
					bp.insert(keyRead, valueRead);
				}
			//	System.out.println("1");
				BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(args[2])));
				String line = null;
				int key = -1, value = -1;
				while((line = read.readLine()) != null ){
					String[] splitData = line.split(",");
					key = Integer.parseInt(splitData[0]);
					value = Integer.parseInt(splitData[1]);
					
					bp.insert(key, value);
				}
				read.close();
				
			//	read = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
			}
			catch(FileNotFoundException e) {
				System.out.println("file not found\n");
			}
			/*catch(ClassNotFoundException e) {
				System.out.println("class not found\n");
			}*/
			catch(IOException e) {
				e.printStackTrace();
			}finally {
				try {
					/*if(ois != null) ois.close();
					//bpTree 저장
					oos = new ObjectOutputStream(new FileOutputStream(args[1]));
					oos.writeObject(bp);	
					oos.close();*/
					if(readKey != null) readKey.close();
					
					writeKey = new BufferedWriter(new FileWriter(args[1]));
					bp.saveData(writeKey);
					writeKey.close();
				}catch(IOException e) {}
			}
		}
		else if(args[0].equals("-d")) {
			//deletion
			try {
				/*ois = new ObjectInputStream(new FileInputStream(args[1]));
				bp = (BpTree)ois.readObject();*/
				readKey = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
				String keyLine = null;
				int degree = -1;
				keyLine = readKey.readLine();
				degree = Integer.parseInt(keyLine);
				bp = new BpTree(args[1], degree);
				bp.create();
				
				int keyRead = -1, valueRead = -1;
				while((keyLine = readKey.readLine()) != null) {
					String[] splitData = keyLine.split(",");
					keyRead = Integer.parseInt(splitData[0]);
					valueRead = Integer.parseInt(splitData[1]);
					
					bp.insert(keyRead, valueRead);
				}
				
				BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(args[2])));
				String line = null;
				int key = -1;
				while((line = read.readLine()) != null){
					key = Integer.parseInt(line);
					
					bp.delete(key);
				}
				read.close();
			}
			catch(FileNotFoundException e) {
				System.out.println("file not found\n");
			}
			/*catch(ClassNotFoundException e) {
				System.out.println("class not found\n");
			}*/
			catch(IOException e) {
				e.printStackTrace();
			}finally {
				try {
					/*if(ois != null) ois.close();
					//bpTree 저장
					oos = new ObjectOutputStream(new FileOutputStream(args[1]));
					oos.writeObject(bp);	
					oos.close();*/
					if(readKey != null) readKey.close();
					
					writeKey = new BufferedWriter(new FileWriter(args[1]));
					bp.saveData(writeKey);
					writeKey.close();
				}catch(IOException e) {}
			}
		}
		else if(args[0].equals("-s")) {
			//single key search
			try {
				/*ois = new ObjectInputStream(new FileInputStream(args[1]));
				bp = (BpTree)ois.readObject();*/
				readKey = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
				String keyLine = null;
				int degree = -1;
				keyLine = readKey.readLine();
				degree = Integer.parseInt(keyLine);
				bp = new BpTree(args[1], degree);
				bp.create();
				
				int keyRead = -1, valueRead = -1;
				while((keyLine = readKey.readLine()) != null) {
					String[] splitData = keyLine.split(",");
					keyRead = Integer.parseInt(splitData[0]);
					valueRead = Integer.parseInt(splitData[1]);
					
					bp.insert(keyRead, valueRead);
				}
				
				int find = Integer.parseInt(args[2]);
				
				bp.singleSearch(find);
			}
			catch(FileNotFoundException e) {
				System.out.println("file not found\n");
			}/*
			catch(ClassNotFoundException e) {
				System.out.println("class not found\n");
			}*/
			catch(IOException e) {
				e.printStackTrace();
			}/*finally {
				try {
					if(ois != null) ois.close();
				}catch(IOException e) {}
			}*/
			
		}
		else if(args[0].equals("-r")) {
			//ranged search
			try {
				/*ois = new ObjectInputStream(new FileInputStream(args[1]));
				bp = (BpTree)ois.readObject();*/
				readKey = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
				String keyLine = null;
				int degree = -1;
				keyLine = readKey.readLine();
				degree = Integer.parseInt(keyLine);
				bp = new BpTree(args[1], degree);
				bp.create();
				
				int keyRead = -1, valueRead = -1;
				while((keyLine = readKey.readLine()) != null) {
					String[] splitData = keyLine.split(",");
					keyRead = Integer.parseInt(splitData[0]);
					valueRead = Integer.parseInt(splitData[1]);
					
					bp.insert(keyRead, valueRead);
				}
				
				int start = Integer.parseInt(args[2]);
				int end = Integer.parseInt(args[3]);
				
				bp.rangeSearch(start, end);
			}
			catch(FileNotFoundException e) {
				System.out.println("file not found\n");
			}
			/*catch(ClassNotFoundException e) {
				System.out.println("class not found\n");
			}*/
			catch(IOException e) {
				e.printStackTrace();
			}/*finally {
				try {
					if(ois != null) ois.close();
				}catch(IOException e) {}
			}*/
		}
		else {
			System.out.println("error");
		}
	}
}