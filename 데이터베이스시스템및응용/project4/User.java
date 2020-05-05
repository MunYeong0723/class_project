package project4;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class User {
	Connection con = null;
	Statement stmt = null;
	ResultSet rs = null;
	Scanner s = new Scanner(System.in);
	
	public User(Connection con){
		try {
			this.con = con;
			this.stmt = con.createStatement();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void finish() {
		try {
			stmt.close();
			rs.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			while(true) {				
				//who are you?
				System.out.print("Input your User_Num : ");
				int userNum = s.nextInt();
				
				//query
				rs = stmt.executeQuery("select User_Num from User where User_Num = " + userNum + "");
				
				//�Է¹��� userNum�� database�� ���ٸ�
				if(!rs.first()) {
					System.out.println("There is no UserNum.");
					System.out.println("0. Return to previous menu.");
					System.out.println("1. Try again.");
					System.out.println("2. Make new ID");
					System.out.print("Input : ");
					
					int menu = s.nextInt();
					switch(menu) {
					case 0 :
						finish();
						return;
					case 1 :
						continue;
					case 2 :
						register();
						continue;
					default :
						System.out.println("There is no " + menu + " menu. Return to previous menu.");
						finish();
						return;
					}
				}
				//�Է¹��� userNum�� database�� �ִٸ�
				else {
					if(!showAccount(userNum)) {
						finish();
						return;
					}
				}
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finish();
	}
	
	//�ش� user�� ���� ��� �����ְ� �� �� ���ϴ� account�� ���ؼ� insert, delete, update�� �� �ֵ��� ��
	public boolean showAccount(int userNum) {
		boolean debug = true;
		try {
			while(true) {
				//�ش� user�� ���� ��� �����ֱ�
				rs = stmt.executeQuery("select Account_Num, OpenDate from User, Account "
						+ "where Owner_Num = User_Num and User_Num = " + userNum);
				
				int i = 0;
				ArrayList<Integer> account_have = new ArrayList<>();
				while(rs.next()) {
					int account_Num = rs.getInt("Account_Num");
					account_have.add(account_Num);
					
					System.out.println("< " + i++ + " >");
					System.out.println("Account_Num : " + rs.getInt("Account_Num"));
					System.out.println("OpenDate : " + rs.getDate("OpenDate"));
					System.out.println();
				}
				//�ڼ��� ���� ���� ������ index�� �Է¹ް� �� ���¿� ���� ���� �����ֱ�
				System.out.println("What do you want to see detail? Please input account's index.\n"
						+ "If you want to leave, input -1.");
				
				System.out.print("Input : ");
				int index = s.nextInt();
				
				//return to previous menu.
				if(index == -1) {
					debug = false;
					break;
				}
				
				//arrayList index �ܿ� �ִ� ���� �Է� �� ���� handling
				if(index >= account_have.size()) {
					System.out.println("There is no index " + index + ". Try again.");
					s.nextLine();
					continue;
				}
				
				int wantSeeAccNum = account_have.get(index);
				
				rs = stmt.executeQuery("select Account_Num, OpenDate, RecentUseDate, WithdrawalLimit, Money "
						+ "from Account where Account_Num = " + wantSeeAccNum);
				
				//�ش� account�� ���� ���� print
				while(rs.next()) {
					int account_Num = rs.getInt("Account_Num");
					Date openDate = rs.getDate("OpenDate");
					Date recentUseDate = rs.getDate("RecentUseDate");
					int withdrawalLimit = rs.getInt("WithdrawalLimit");
					int money = rs.getInt("Money");
					
					System.out.println("< account_Num : " + account_Num + " >");
					System.out.println("(1) open date : " + openDate);
					System.out.println("(2) recentUseDate : " + recentUseDate);
					System.out.println("(3) withdrawal limit : " + withdrawalLimit);
					System.out.println("(4) total money : " + money);
					System.out.println();
				}
				
				//�ش� ���¿� ���ؼ� ������ ���� ������(�Աݳ��� / ��ݳ��� / �ڵ�������ü����) ����
				System.out.println("You can see more detail. If you want, please input menu.");
				while(true) {
					System.out.println("0. Return to previous menu.");
					System.out.println("1. Deposit lists");
					System.out.println("2. Withdrawal lists");
					System.out.println("3. Auto Send Money lists");
					System.out.print("Input : ");
					index = s.nextInt();
					
					if(index == 0) break;
					
					switch(index) {
					case 1 :
						deposit(wantSeeAccNum);
						break;
					case 2 :
						withdrawal(wantSeeAccNum);
						break;
					case 3 :
						auto_sendmoney(wantSeeAccNum);
						break;
					default :
						System.out.println("There is no index " + index + ". Try again.");
						break;
					}
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return debug;
	}
	
	//�Աݳ����̸� �Աݳ��� �����ֱ� (��ȸ�� ����)
	public void deposit(int wantSeeAccNum) {
		try {
			rs = stmt.executeQuery("select Deposit_Num from Deposit where DepositAcc_Num = " + wantSeeAccNum);
			
			//�Աݳ����� ���� ���
			if(!rs.first()) {
				System.out.println("There is no Deposit lists about this account\n");
				return;
			}
			
			//�Աݳ��� �����ֱ�
			rs = stmt.executeQuery("select Date, HowmuchIN, SendMsg, TotalMoney, Memo"
					+ " from Deposit where DepositAcc_Num = " + wantSeeAccNum);
			
			int num = 0;
			System.out.println("\nDeposit lists---------------------------------------------");
			while(rs.next()) {
				System.out.println("< " + num++ + " >");
				System.out.println("(1) Date : " + rs.getDate("Date"));
				System.out.println("(2) HowmuchIN : " + rs.getInt("HowmuchIN"));
				System.out.println("(3) SendMsg : " + rs.getString("sendMsg"));
				System.out.println("(4) TotalMoney : " + rs.getInt("TotalMoney"));
				System.out.println("(5) Memo : " + rs.getString("Memo"));
			}
			System.out.println("---------------------------------------------------------");
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	//��ݳ����̸� ��ݳ��� �����ֱ� (��ȸ�� ����)
	public void withdrawal(int wantSeeAccNum) {
		try {
			rs = stmt.executeQuery("select Withdrawal_Num from Withdrawal where WithdrawalAcc_Num = " + wantSeeAccNum);
			
			//��ݳ����� ���� ���
			if(!rs.first()) {
				System.out.println("There is no Withdrawal lists about this account\n");
				return;
			}
			
			//��ݳ��� �����ֱ�
			rs = stmt.executeQuery("select Date, HowmuchOUT, RecvMsg, TotalMoney, Memo"
					+ " from Withdrawal where WithdrawalAcc_Num = " + wantSeeAccNum);
			
			int num = 0;
			System.out.println("\nWithdrawal lists------------------------------------------");
			while(rs.next()) {
				System.out.println("< " + num++ + " >");
				System.out.println("(1) Date : " + rs.getDate("Date"));
				System.out.println("(2) HowmuchIN : " + rs.getInt("HowmuchOUT"));
				System.out.println("(3) SendMsg : " + rs.getString("RecvMsg"));
				System.out.println("(4) TotalMoney : " + rs.getInt("TotalMoney"));
				System.out.println("(5) Memo : " + rs.getString("Memo"));
			}
			System.out.println("---------------------------------------------------------");
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//�ڵ�������ü�����̸� ���� ����� �����ְ�
	//insert, delete, update �����ϵ��� ���ֱ�
	public void auto_sendmoney(int wantSeeAccNum) {
		try {
			rs = stmt.executeQuery("select ASM_Num from Auto_sendMoney where SendAcc_Num = " + wantSeeAccNum);
			
			//�ڵ�������ü������ ���� ���
			if(!rs.first()) {
				System.out.println("There is no Auto_sendMoney lists about this account\n");
				System.out.println("If you want to insert new one, input 0 or if you want to leave, input -1.");
				System.out.print("Input : ");
				int what = s.nextInt();
				
				if(what == -1) return;
				else if(what == 0) {
					//insert
					//RecvAcc_Num�� �Է¹ް�
					System.out.print("Input account_num that you want to send money : ");
					int recvAcc_Num = s.nextInt();
					
					//RecvAcc�� ������ ����ó�����ֱ�
					rs = stmt.executeQuery("select Account_Num from Account where Account_Num = " + recvAcc_Num);
					if(!rs.first()) {
						System.out.println("There is no account number " + recvAcc_Num + ". Return to previous menu.\n");
						return;
					}
					
					//SendDate, SendMoney�� �Է¹���.
					System.out.print("Input send date you want to send money (yyyy-mm-dd) : ");
					s.nextLine();
					String sendDate = s.nextLine();
					System.out.print("Input money you want to send : ");
					int sendMoney = s.nextInt();
					
					//insert query �����ֱ�
					stmt.executeUpdate("insert into Auto_sendMoney(SendAcc_Num, RecvAcc_Num, SendDate, SendMoney) "
							+ "values(" + wantSeeAccNum + ", " + recvAcc_Num + ", '" + sendDate + "', " + sendMoney + ")");
					
					System.out.println("Insert succeed!");
					return;
				}
				else {
					System.out.println("Wrong number. Return to previous menu.");
					return;
				}
			}
			
			//���� ��� �����ֱ�
			rs = stmt.executeQuery("select distinct ASM_Num, R.Name, SendDate, SendMoney"
					+ " from Auto_sendMoney, User as S, User as R, Account "
					+ "where Account_Num = RecvAcc_Num and Owner_Num = R.User_Num and SendAcc_Num = " + wantSeeAccNum);
			
			System.out.println("\nAuto Send Money lists-------------------------------------");
			while(rs.next()) {
				int asm_Num = rs.getInt("ASM_Num");
				String recvName = rs.getString("R.Name");
				Date sendDate = rs.getDate("SendDate");
				int sendMoney = rs.getInt("SendMoney");
				
				System.out.println("< ASM_Num : " + asm_Num + " >");
				System.out.println("(1) Send to : " + recvName);
				System.out.println("(2) SendDate : " + sendDate);
				System.out.println("(3) sendMoney : " + sendMoney);
				System.out.println();
			}
			System.out.println("---------------------------------------------------------");
			
			boolean db = false;
			while(!db) {
				//insert, delete, update �����ϵ��� ���ֱ�
				System.out.println("If you want to more, then input menu.");
				System.out.println("0. Return to previous menu.");
				System.out.println("1. Insert new one.");
				System.out.println("2. Delete one.");
				System.out.println("3. Update one.");
				System.out.print("Input : ");
				int menu = s.nextInt();
				
				switch(menu) {
				case 0 :
					return;
				case 1 :
					//insert
					//RecvAcc_Num�� �Է¹ް�
					System.out.print("Input account_num that you want to send money : ");
					int recvAcc_Num = s.nextInt();
					
					//RecvAcc�� ������ ����ó�����ֱ�
					rs = stmt.executeQuery("select Account_Num from Account where Account_Num = " + recvAcc_Num);
					if(!rs.first()) {
						System.out.println("There is no account number " + recvAcc_Num + ". Return to previous menu.\n");
						continue;
					}
					
					//SendDate, SendMoney�� �Է¹���.
					System.out.print("Input send date you want to send money (yyyy-mm-dd) : ");
					s.nextLine();
					String sendDate = s.nextLine();
					System.out.print("Input money you want to send : ");
					int sendMoney = s.nextInt();
					
					//insert query �����ֱ�
					stmt.executeUpdate("insert into Auto_sendMoney(SendAcc_Num, RecvAcc_Num, SendDate, SendMoney) "
							+ "values(" + wantSeeAccNum + ", " + recvAcc_Num + ", '" + sendDate + "', " + sendMoney + ")");
					
					System.out.println("Insert succeed!");
					db = true;
					break;
				case 2 :
					//delete
					//������� �ϴ� ASM_Num�� �Է¹ް�
					System.out.print("Input ASM_Num you want to delete : ");
					int delete_asm_Num = s.nextInt();
					
					//�ڽ��� list�� ���� ASM_Num�̸� ����ó�����ֱ�
					rs = stmt.executeQuery("select ASM_Num from Auto_sendMoney where SendAcc_Num = " + wantSeeAccNum +" and ASM_Num = " + delete_asm_Num);
					if(!rs.first()) {
						System.out.println("There is no ASM_Num " + delete_asm_Num + " in your account. Return to previous menu.\n");
						continue;
					}
					
					//delete���ֱ�
					stmt.executeUpdate("delete from Auto_sendMoney where ASM_Num = " + delete_asm_Num);
					System.out.println("Delete succeed!");
					db = true;
					break;
				case 3 :
					//update
					//update�ϰ��� �ϴ� ASM_Num�� �Է¹ް�
					System.out.print("Input ASM_Num you want to update : ");
					int update_asm_Num = s.nextInt();
					
					//�ڽ��� list�� ���� ASM_Num�̸� ����ó�����ֱ�
					rs = stmt.executeQuery("select ASM_Num from Auto_sendMoney where SendAcc_Num = " + wantSeeAccNum +" and ASM_Num = " + update_asm_Num);
					if(!rs.first()) {
						System.out.println("There is no ASM_Num " + update_asm_Num + " in your account. Return to previous menu.\n");
						continue;
					}
					
					//update�ϰ����ϴ� ���� �Է¹ޱ� (RecvAcc_Num? sendDate? sendMoney?)
					System.out.println("What do you want to update? Please input menu.");
					System.out.println("0. Receive Account");
					System.out.println("1. send date");
					System.out.println("2. send money");
					System.out.print("Input : ");
					int updateNum = s.nextInt();
					
					//update �ϰ��� �ϴ� ������ RecvAcc_Num�� ���
					if(updateNum == 0) {
						//������Ʈ�ϰ��� �ϴ� recvacc_Num�� �ް�
						System.out.print("Input receive account number you want to change : ");
						updateNum = s.nextInt();
						
						//���� account number�̸� ����ó�����ֱ�
						rs = stmt.executeQuery("select Account_Num from Account where Account_Num = " + updateNum);
						if(!rs.first()) {
							System.out.println("There is no account number " + updateNum + ". Return to previous menu.\n");
							continue;
						}
						
						//update ���ֱ�
						stmt.executeUpdate("update Auto_sendMoney set RecvAcc_Num = " + updateNum + " where ASM_Num = " + update_asm_Num);
						System.out.println("Update succeed!");
					}
					//update �ϰ��� �ϴ� ������ sendDate�� ���
					else if(updateNum == 1) {
						//date�Է¹ް�
						System.out.print("Input send date you want to change(yyyy-mm-dd) : ");
						s.nextLine();
						String date = s.nextLine();
						
						//update���ֱ�
						stmt.executeUpdate("update Auto_sendMoney set SendDate = '" + date + "' where ASM_Num = " + update_asm_Num);
						System.out.println("Update succeed!");
					}
					//update �ϰ��� �ϴ� ������ sendMoney�� ���
					else if(updateNum == 2) {
						//money�Է¹ް�
						System.out.print("Input send money you want to change : ");
						updateNum = s.nextInt();
						
						//update���ֱ�
						stmt.executeUpdate("update Auto_sendMoney set SendMoney = " + updateNum + " where ASM_Num = " + update_asm_Num);
						System.out.println("Update succeed!");
					}
					else {
						System.out.println("There is no menu " + updateNum + ". Return to previous menu.");
						continue;
					}
					db = true;
					break;
				default :
					System.out.println("There is no menu " + menu + ". Try again.");
					break;
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//���ο� user ���
	public void register() {
		//user�� �����ޱ�
		System.out.print("Input your name : ");
		s.nextLine();
		String name = s.nextLine();
		
		System.out.print("Input your Ssn(9) : ");
		String ssn = s.nextLine();
	
		System.out.print("Input your gender (M or F) : ");
		String sex = s.nextLine();
		
		System.out.print("Input your address : ");
		String address = s.nextLine();
		
		System.out.print("Input your birth date(yyyy-mm-dd) : ");
		String bDate = s.nextLine();
		
		System.out.print("Input your phoneNumber(***-****-****) : ");
		String ph = s.nextLine();
		
		System.out.println("If you want to add more phoneNumber, input 0, else input any number without 0.");
		System.out.print("Input : ");
		int addMore = s.nextInt();
		ArrayList<String> phNums = new ArrayList<>();
		phNums.add(ph);
		
		//�ڵ�����ȣ�� ������ ����ϴ� ���
		if(addMore == 0) {
			System.out.print("How many phoneNumbers do you want to add more? ");
			int num = s.nextInt();
			int b = 2;
			s.nextLine();
			
			while(num > 0) {
				System.out.print("Input your phoneNumber " + b++ + " (***-****-****) : ");
				String pN = s.nextLine();
				
				phNums.add(pN);
				num--;
			}
		}
		
		try {
			//���� database�� update ���ֱ�
			stmt.executeUpdate("insert into User(Name, Ssn, Sex, Address, Bdate, Manage_Num) "
					+ "values('" + name + "', '" + ssn + "', '" + sex + "', '" + address + "', '" + bDate + "', 1)");
			
			rs = stmt.executeQuery("select last_insert_id()");
			rs.next();
			int user_Num = rs.getInt("last_insert_id()");
			//phone number�� �� ���� ���
			if(phNums.size() == 1) {
				stmt.executeUpdate("insert into User_PhoneNumbers values(" + user_Num + ", '" + ph + "')");
			}
			//phone number�� ���� ���� ���
			else if(phNums.size() > 1){
				int insert = 0;
				//phone number ������ŭ data�� ����.
				while(insert < phNums.size()) {
					stmt.executeUpdate("insert into User_PhoneNumbers values(" + user_Num + ", '" + phNums.get(insert++) + "')");
				}
			}
			System.out.println("Your data is update successfully.");
			
			//account �ϳ� �߰����ֱ�
			Manager mg = new Manager(con);
			mg.addAccount(user_Num);
			
			System.out.println("Register successfully. Your User_Num is " + user_Num + ".");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
