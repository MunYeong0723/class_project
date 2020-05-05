package ftp;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class server {
	static File file = new File("C:\\");
	static BufferedReader inFromClient;
	
	public static void main(String[] args) {
		ServerSocket welcomeSocket = null;
		String clientSentence;
		String sendMessage = null;
		
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
				System.out.println("연결 요청을 기다립니다.");
				
				Socket connectionSocket = welcomeSocket.accept();
				
				System.out.println(connectionSocket.getInetAddress() + " 로부터 연결요청이 들어왔습니다.");
				
				//create ouput stream, attached to socket
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				
				while(true) {
					//create input stream, attached to socket
					inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
					
					//read in line from socket
					clientSentence = inFromClient.readLine();
					
					if(clientSentence.equals("QUIT")) {
						inFromClient.close();
						outToClient.close();
						connectionSocket.close();
						
						break;
					}
					
					sendMessage = processResponse(clientSentence);
					outToClient.writeBytes(sendMessage);
				}
			}
			catch (IOException e){
				e.printStackTrace();
			}
		}
	}
	
	static String processResponse(String clientSentence) {
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
						file = new File(currentPath);
						
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
						
						response = "1\n" + file.getCanonicalPath().length() + "\n";
						response += file.getCanonicalPath() + "\n";
					}
					else {
						Path path = Paths.get(pathString);
						File tmp = new File(file.getPath());
						
						//절대경로
						if(path.isAbsolute()) {
							if(file.exists()) {
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
				//argument가 없는 경우. 현재 dir return
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
							else result += "\n";
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
							else result += "\n";
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
									else result += "\n";
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
									
									if(i++ < lists.length) result += ",";
									else result += "\n";
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
				//
				System.out.println("GET");
			}
			else if(arr[0].equals("PUT")) {
				String fileName = arr[1];
				clientSentence = inFromClient.readLine();
				int fileLen = Integer.parseInt(clientSentence);
				
				try {
					char[] buffer = new char[fileLen];
					inFromClient = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
				
					int read = inFromClient.read(buffer, 0, fileLen);
					String contents = new String(buffer);
					
					if(read == fileLen) {
						FileOutputStream fos = new FileOutputStream(file.getPath() + "\\" + fileName);
						fos.write(contents.getBytes());
							
						response = "1\n";
						response += fileName + "trnasferred  /" + fileLen + " bytes\n";
						fos.close();
					}
					else {
						response = "-10\n" + "Failed for unknown reason\n";
					}
				}
				catch(IOException e) {
					response = "-10\n" + "Failed for unknown reason\n";
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
