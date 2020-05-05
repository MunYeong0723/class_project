package ftp;

import java.io.*;
import java.net.*;

public class threadedClient {

	public static void main(String[] args) {
		int cPortNum = 2020;
		String serverIP = "127.0.0.1";
		String clientIP = "127.0.0.1";
		System.out.println("서버에 연결중입니다. 서버 IP : " + serverIP);
		String response;
		String sentence;
		
		if(args.length != 0) {
			cPortNum = Integer.parseInt(args[0]);
		}
		Socket clientSocket = null;
		BufferedReader inFromUser = null;
		DataOutputStream outToServer = null;
		BufferedReader inFromServer = null;
		
		try {
			//socket을 생성해서 연결 요청
			clientSocket = new Socket(serverIP, cPortNum);
			//create input stream
			inFromUser = new BufferedReader(new InputStreamReader(System.in));
			
			//create output stream attached to socket
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			//create input stream attached to socket
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			FileInputStream fin = null;
			
			while(true) {
				sentence = inFromUser.readLine();
				
				if(sentence.contains("PUT")) {
					String[] sp = sentence.split(" ");
					File input = new File(sp[1]);
					
					if(input.exists()) {
						int inputLen = (int)input.length();
						sentence += ("\n" + inputLen + "\n");
						
						fin = new FileInputStream(input);
						byte[] bytes = new byte[inputLen];
						fin.read(bytes);
						
						String contents = new String(bytes);
						sentence += contents + "\n";
						sentence += "fileEnd\n";
					
						//System.out.println("sentence : " + sentence + "\n");
						outToServer.writeBytes(sentence);
						
						fin.close();
					}
					else {
						System.out.println("No such file exist");
						continue;
					}
				}
				else {
					//send line to server
					outToServer.writeBytes(sentence + "\n");
				}

				if(sentence.equals("QUIT")) {
					inFromUser.close();
					outToServer.close();
					inFromServer.close();
					clientSocket.close();
					break;
				}
				
				//read line from server
				response = inFromServer.readLine();
				int state = Integer.parseInt(response);
				String result = null;
				switch(state) {
				case 1 :
					//PUT command
					if(sentence.contains("PUT")) {
						result = inFromServer.readLine();
						System.out.println(result);
					}
					else {
						int length = Integer.parseInt(inFromServer.readLine());
						
						result = inFromServer.readLine();
						
						//LIST command
						if(sentence.contains("LIST")) {
							String[] lists = result.split(",");
							if(lists.length == 1) {
								System.out.println("Directory is empty!");
							}
							else {
								for(int i = 0; i < lists.length;) {
									System.out.print(lists[i++] + ",");
									System.out.println(lists[i++]);
								}
							}
						}
						//GET command
						else if(sentence.contains("GET")) {
							String fileName = result;
							File f = new File(fileName);
							BufferedWriter fw = new BufferedWriter(new FileWriter(f));
							String tmp = null;
							tmp = inFromServer.readLine();
							while(!(tmp.equals("fileEnd"))) {
								fw.write(tmp);
								tmp = inFromServer.readLine();
								if(!(tmp.equals("fileEnd"))) {
									fw.newLine();
								}
								fw.flush();
							}
							
							if((int)f.length() == length) {
								System.out.println("Received " + fileName + " / " + length + " bytes");
							}
							else {
								fw.close();
								f.delete();
								System.out.println("Failed for unknown reason");
							}
							
						}
						//CD command, PUT command
						else {
							System.out.println(result);
						}
						
					}
					break;
				//error : 입력한 path가 존재하지 않는 directory일 때
				case -1 :
					result = inFromServer.readLine();
					
					System.out.println(result);
					break;
				//error : 입력한 path가 경로가 아닐 때
				case -2 :
					result = inFromServer.readLine();
					
					System.out.println(result);
					break;
				//error : PUT unknown reason
				case -10 :
					result = inFromServer.readLine();
					
					System.out.println(result);
					break;
				//command error
				case -20 :
					result = inFromServer.readLine();
					System.out.println(result);
					break;
				default :
					//status code error
					System.out.println("status code error");
					break;
				}
			}
			
		}
		catch (ConnectException ce){
			ce.printStackTrace();
		}
		catch(IOException ie) {
			ie.printStackTrace();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
