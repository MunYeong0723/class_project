package ftp;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class threadedServer {

	public static void main(String[] args) {
		ServerSocket welcomeSocket = null;
		
		try {
			if(args.length != 0) {
				int portNum = Integer.parseInt(args[0]);
				welcomeSocket = new ServerSocket(portNum);
			}
			else {
				welcomeSocket = new ServerSocket(2020);
			}
			
			System.out.println("서버가 준비되었습니다.");
			
		}catch (IOException e){
			e.printStackTrace();
		}
		
		while(true) {
			try {
				//client의 요청이 없으면 대기 상태에 들어감
				//client가 접속하는 순간 client와 통신할 수 있는 socket을 반환함
				Socket clientSocket = welcomeSocket.accept();
				System.out.println(clientSocket.getInetAddress() + " 로부터 연결요청이 들어왔습니다.");
				
				//접속을 계속 유지하면서 data를 송수신하기 위해서 thread 객체 생성
				Thread t = new ClientHandler(clientSocket);
				t.start();
			/*	ServerThread childThread = new ServerThread(clientSocket);
				Thread t = new Thread(childThread);
				t.start();*/
			}
			catch (IOException e){
				e.printStackTrace();
			}
		}
	}

}
class ClientHandler extends Thread{
	Socket clientSocket = null;
	File file = new File("C:\\");
	DataOutputStream outToClient = null;
	BufferedReader inFromClient = null;
	String clientSentence = null;
	String sendMessage = null;
	
	public ClientHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
		
		try {
			//create output stream, attached to socket
			outToClient = new DataOutputStream(clientSocket.getOutputStream());
			//create input stream, attached to socket
			inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			while(true) {
				//create input stream, attached to socket
				inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				
				//read in line from socket
				clientSentence = inFromClient.readLine();
				
				if(clientSentence.equals("QUIT")) {
					System.out.println("Closing this conncection");
					inFromClient.close();
					outToClient.close();
					clientSocket.close();
					
					break;
				}
				else if(clientSentence.contains("CD") || clientSentence.contains("LIST") || clientSentence.contains("PUT") || clientSentence.startsWith("GET")) {
					//System.out.println("clientSentence : " + clientSentence);
					sendMessage = processResponse(clientSentence);
					//System.out.println("sendMseesage : "+ sendMessage);
					outToClient.writeBytes(sendMessage);
				}
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public String processResponse(String clientSentence) {
		String response = null;
		
		String[] arr = clientSentence.split(" ");
		try {
			if(arr[0].equals("CD")) {
				//., .., 상대 경로도 지원해야 한다
				if(arr.length != 1) {
					String pathString = arr[1];
					
					if(pathString.startsWith("./")) {
						String currentPath = file.getPath();
						file = new File(currentPath);
						
						if(pathString.length() > 2) {
							String newPath = pathString.substring(2, pathString.length());
							File tmp = new File(currentPath + "\\" + newPath);
							
							if(!(tmp.exists())) {
								//not exist directory
								response = "-1\n" + "Failed - Such file does not exist\n";
								
								return response;
							}
							file = new File(tmp.getPath());
						}
						
						response = "1\n" + file.getCanonicalPath().length() + "\n";
						response += file.getCanonicalPath() + "\n";

					}
					else if(pathString.startsWith("../")) {
						String currentPath = file.getParent();
						
						if(pathString.length() > 3) {
							String newPath = pathString.substring(3, pathString.length());
							File tmp = new File(currentPath + "\\" + newPath);
							
							if(!(tmp.exists())) {
								//not exist directory
								response = "-1\n" + "Failed - Such file does not exist\n";
								
								return response;
							}
							file = new File(tmp.getPath());
						}
						else file = new File(currentPath);
						
						response = "1\n" + file.getCanonicalPath().length() + "\n";
						response += file.getCanonicalPath() + "\n";
					}
					else {
						Path path = Paths.get(pathString);
						File tmp = new File(file.getPath());
						
						//절대경로
						if(path.isAbsolute()) {
							tmp = new File(pathString);
							
							if(tmp.exists()) {
								file = new File(pathString);
								
								response = "1\n" + file.getCanonicalPath().length() + "\n";
								response += file.getCanonicalPath() + "\n";
							}
							else {
								//not exist directory
								response = "-1\n" + "Failed - Such file does not exist\n";
							}
						}
						else {
							//상대경로
							String tPath = tmp.getPath();
							tmp = new File(tPath + "\\" + pathString);
							//상대경로
							if(tmp.exists() && tmp.isDirectory()) {
								file = new File(tmp.getPath());
								
								response = "1\n" + file.getCanonicalPath().length() + "\n";
								response += file.getCanonicalPath() + "\n";
							
							}
							else if(!(tmp.exists())) {
								//not exist directory
								response = "-1\n" + "Failed - Such file does not exist\n";
							}
							else {
								response = "-2\n" + "Failed - directory name is invalid\n";
							}
						}
					}
					
				}
				//CD 입력한 경우. 현재 dir return
				else {
					response = "1\n" + file.getCanonicalPath().length() + "\n";
					response += file.getCanonicalPath() + "\n";
				}
			}
			else if(arr[0].equals("LIST")) {
				if(arr.length == 2) {
					String pathString = arr[1];
					
					String result = "";
					if(pathString.equals(".")) {
						File[] lists = file.listFiles();
						response = "1\n";
						
						for(int i = 0; i < lists.length;) {
							File f = lists[i];
							
							if(f.isDirectory()) {
								result += f.getName() + ",-";
							}
							else {
								result += f.getName() + "," + f.length();
							}
							
							if(++i < lists.length) result += ",";
						}
						
						response += result.length() + "\n" + result + "\n";
					}
					else if(pathString.equals("..")) {
						File tmp = new File(file.getParent());
						File[] lists = tmp.listFiles();
						response = "1\n";
						
						for(int i = 0; i < lists.length;) {
							File f = lists[i];
							
							if(f.isDirectory()) {
								result += f.getName() + ",-";
							}
							else {
								result += f.getName() + "," + f.length();
							}
							
							if(++i < lists.length) result += ",";
						}
						
						response += result.length() + "\n" + result + "\n";
					}
					else {
						Path path = Paths.get(pathString);
						File tmp = new File(file.getPath());
						
						//절대경로
						if(path.isAbsolute()) {
							tmp = new File(pathString);
							if(tmp.exists()) {
								File[] lists = tmp.listFiles();
								response = "1\n";
								
								for(int i = 0; i < lists.length;) {
									File f = lists[i];
									
									if(f.isDirectory()) {
										result += f.getName() + ",-";
									}
									else {
										result += f.getName() + "," + f.length();
									}
									
									if(++i < lists.length) result += ",";
								}
								
								response += result.length() + "\n" + result + "\n";
							}
							else {
								//not exist directory
								response = "-1\n" + "Failed - is not exist directory\n";
							}
						}	
						else {
							String tPath = tmp.getPath();
							tmp = new File(tPath + "\\" + pathString);
							//상대경로
							if(tmp.exists() && tmp.isDirectory()) {
								File[] lists = tmp.listFiles();
								response = "1\n";
								
								for(int i = 0; i < lists.length;) {
									File f = lists[i];
									
									if(f.isDirectory()) {
										result += f.getName() + ",-";
									}
									else {
										result += f.getName() + "," + f.length();
									}
									
									if(++i < lists.length) result += ",";
								}
								
								response += result.length() + "\n" + result + "\n";
								
							}
							else if(!(tmp.exists())) {
								response = "-1\n" + "Failed - is not exist directory\n";
							}
							else {
								//error
								response = "-2\n" + "Failed - directory name is invalid\n";
							}
						}
					}
				}
				else {
					//error
					response = "-20\n" + "command error\n";
				}
			}
			else if(arr[0].equals("GET")) {
				if(arr.length == 2) {
					String pathString = arr[1];
					Path path = Paths.get(pathString);
					File tmp = null;
					
					if(path.isAbsolute()) {
						tmp = new File(pathString);
					}
					else {
						tmp = new File(file.getPath() + "\\" + pathString);
					}
					
					//send to client
					if(tmp.exists()) {
						int sendLen = (int)tmp.length();
						response = "1\n" + sendLen + "\n";
						
						FileInputStream fin = new FileInputStream(tmp);
						byte[] bytes = new byte[sendLen];
						fin.read(bytes);
						
						response += tmp.getName() + "\n";
						String contents = new String(bytes);
						response += contents + "\n";
						response += "fileEnd\n";
						
						fin.close();
					}
					else {
						//not exist directory
						response = "-1\n" + "Failed - Such file does not exist\n";
					}
				}
				else {
					//command error
					response = "-20\n" + "command error\n";
				}
			}
			else if(arr[0].equals("PUT")) {
				//thread synchrnoized
				synchronized(this) {
					if(arr.length == 2) {
						String fileName = arr[1];
						clientSentence = inFromClient.readLine();
						int fileLen = Integer.parseInt(clientSentence);
						File f = null;
						
						try {
							f = new File(file.getPath() + "\\" + fileName);
							BufferedWriter fw = new BufferedWriter(new FileWriter(f));
							String tmp = null;
							tmp = inFromClient.readLine();
							while(!(tmp.equals("fileEnd"))) {
								fw.write(tmp);
								tmp = inFromClient.readLine();
								if(!(tmp.equals("fileEnd"))) {
									fw.newLine();
								}
								fw.flush();
							}
							
							if((int)f.length() == fileLen) {
								
								response = "1\n";
								response += fileName + " transferred/ " + fileLen + " bytes\n";
							}
							else {
								fw.close();
								f.delete();
								response = "-10\n" + "Failed for unknown reason\n";
							}
						}
						catch(IOException e) {
							f.delete();
							response = "-10\n" + "Failed for unknown reason\n";
						}
					}
					else {
						response = "-20\n" + "command error\n";
					}
				}
				
			}
			else {
				response = "-20\n" + "command error\n";
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return response;
	} 
}
