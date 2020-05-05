package project4;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Manager {
	Connection con = null;
	Statement stmt = null;
	ResultSet rs = null;
	Scanner s = new Scanner(System.in);
	
	public Manager(Connection con) {
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
				System.out.print("Input your Manager_Num : ");
				int managerNum = s.nextInt();
				
				//query
				rs = stmt.executeQuery("select Manager_Num from Manager where Manager_Num = " + managerNum);
				
				//입력한 manager_num이 없는 경우 처리해주기
				//입력받은 manager number가 database에 없다면
				if(!rs.first()) {
					System.out.println("There is no ManagerNum " + managerNum);
					System.out.println("0. Return to previous menu.");
					System.out.println("1. Try again.");
					System.out.print("Input : ");
					
					int menu = s.nextInt();
					switch(menu) {
					case 0 :
						finish();
						return;
					case 1 :
						continue;
					default : 
						System.out.println("There is no " + menu + " menu. Return to previous menu.");
						finish();
						return;
					}
				}
				//입력받은 manager number가 database에 있다면
				else {
					while(true) {
						//user에서 manage_num이 who are you?에서 받은 manager_num과 일치하는 user 목록 보여주기
						rs = stmt.executeQuery("select User_Num, Name, Sex, Bdate, Address from User where Manage_Num = " + managerNum);
						
						int i = 0;
						ArrayList<Integer> who_manage = new ArrayList<>();
						while(rs.next()) {
							int user_Num = rs.getInt("User_Num");
							String name = rs.getString("Name");
							String sex = rs.getString("Sex");
							Date bDate = rs.getDate("Bdate");
							String address = rs.getString("Address");
							who_manage.add(user_Num);
							
							System.out.println("< " + i++ + " >");
							System.out.println("(1) User Num : " + user_Num);
							System.out.println("(2) Name : " + name);
							System.out.println("(3) Gender : " + sex);
							System.out.println("(4) Birthday : " + bDate);
							System.out.println("(5) Address : " + address);
							System.out.println();
						}
						
						//account 정보에 대해 알고 싶은 user가 들어있는 index를 입력하시오
						System.out.println("Input index of user_num you want to see more."
								+ "\nIf you want to leave, input -1.");
						System.out.print("Input : ");
						int index = s.nextInt();
						
						//return to previous menu.
						if(index == -1) {
							finish();
							return;
						}
						
						//arrayList index 외에 있는 숫자 입력 시 오류 handling
						if(index >= who_manage.size()) {
							System.out.println("There is no index " + index + ". Try again.");
							continue;
						}
						int wantSeeUser = who_manage.get(index);
						
						boolean db = true;
						while(db) {
							//해당 user의 계좌 목록 보여주기
							rs = stmt.executeQuery("select Account_Num, OpenDate from User, Account "
									+ "where Owner_Num = User_Num and User_Num = " + wantSeeUser);
							
							int k = 0;
							ArrayList<Integer> account_have = new ArrayList<>();
							while(rs.next()) {
								int account_Num = rs.getInt("Account_Num");
								account_have.add(account_Num);
								
								System.out.println("< " + k++ + " >");
								System.out.println("Account_Num : " + rs.getInt("Account_Num"));
								System.out.println("OpenDate : " + rs.getDate("OpenDate"));
								System.out.println();
							}
							
							System.out.println("0. Return to previous menu.");
							System.out.println("1. Add account");
							System.out.println("2. Delete account");
							System.out.println("3. Show detail about account.");
							System.out.print("Input : ");
							int menu = s.nextInt();
							
							switch(menu) {
							case 0:
								db = false;
								break;
							case 1 :
								s.nextLine();
								addAccount(wantSeeUser);
								break;
							case 2 : 
								//delete하고 싶은 account 입력받고 method에 넘겨주기
								System.out.print("Input index of account_Num you want to delete : ");
								int m = s.nextInt();
								
								if(m >= account_have.size()) {
									System.out.println("There is no index " + m + ". Return to previous menu.");
									break;
								}
								int wantDeleteAccount = account_have.get(m);
								
								if(!deleteAccount(wantDeleteAccount)) {
									//error
									System.out.println("Something didn't delete! Please check. Return to first menu.");
									finish();
									return;
								}
								else System.out.println("Delete Succeed!");
								break;
							case 3 :
								//detail을 보고싶은 account의 index 입력받기
								System.out.println("Please input index of account_num you want to see more.");
								System.out.print("Input : ");
								int a = s.nextInt();
								
								if(a >= account_have.size()) {
									System.out.println("There is no index " + a + ". Return to previous menu.");
									break;
								}
								int wantSeeAccount = account_have.get(a);
								
								User managed = new User(con);
								showDetail(managed, wantSeeAccount);
								break;
							default :
								System.out.println("There is no " + menu + " menu. Return to previous menu.");
								db = false;
								break;
							}
						}
						
					}
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finish();
	}
	
	//3. show detail about account
	public void showDetail(User managed, int wantSeeAccount) {
		while(true) {
			try {
				rs = stmt.executeQuery("select Account_Num, OpenDate, RecentUseDate, WithdrawalLimit, Money "
						+ "from Account where Account_Num = " + wantSeeAccount);
				
				//해당 account에 대한 정보 print
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
				
				//해당 계좌에 대해서 무엇을 보고 싶은지(입금내역 / 출금내역 / 자동계좌이체내역) 선택
				System.out.println("You can see more detail. If you want, please input menu.");
				
				System.out.println("0. Return to previous menu.");
				System.out.println("1. Depoist lists");
				System.out.println("2. Withdrawal lists");
				System.out.println("3. Auto Send Money lists");
				System.out.print("Input : ");
				int index = s.nextInt();
				
				if(index == 0) break;
				
				switch(index) {
				case 1 :
					managed.deposit(wantSeeAccount);
					break;
				case 2 :
					managed.withdrawal(wantSeeAccount);
					break;
				case 3 :
					managed.auto_sendmoney(wantSeeAccount);
					break;
				default :
					System.out.println("There is no index " + index + ". Try again.");
					break;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	//1. add account
	public void addAccount(int owner_Num) {
		//account의 정보입력
		System.out.println("Input your account's data.");
		System.out.print("Input Account Password : ");
		String pw = s.nextLine();
		System.out.print("Input Withdrawal limit. If you don't want, input -1 : ");
		int withdrawalLimit = s.nextInt();
		
		if(withdrawalLimit == -1) withdrawalLimit = 0;
		
		try {
			//입력받은 정보로 insert해주기
			stmt.executeUpdate("insert into Account(Password, OpenDate, WithdrawalLimit, Money, Owner_Num) "
					+ "values('" + pw + "', CURDATE(), " + withdrawalLimit + ", 0, " + owner_Num + ")");
		
			System.out.println("Add account successfully!");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//2. delete account
	public boolean deleteAccount(int wantDelete) {
		boolean debug = true;
		try {
			//인자로 입력받은 account num을 지우기
			stmt.executeUpdate("delete from Account where Account_Num = " + wantDelete);
			
			//deposit에서 지워졌는지 확인
			rs = stmt.executeQuery("select Deposit_Num from Deposit where DepositAcc_Num = " + wantDelete);
			if(rs.first()) debug = false;
			
			//withdrawal에서 지워졌는지 확인
			rs = stmt.executeQuery("select Withdrawal_Num from Withdrawal where WithdrawalAcc_Num = " + wantDelete);
			if(rs.first()) debug = false;
			
			//auto_sendMoney 지워졌는지 확인
			//sendAcc_Num 없는지 체크
			rs = stmt.executeQuery("select ASM_Num from Auto_sendMoney where SendAcc_Num = " + wantDelete);
			if(rs.first()) debug = false;
			
			//RecvAcc_Num 없는지 체크
			rs = stmt.executeQuery("select ASM_Num from Auto_sendMoney where RecvAcc_Num = " + wantDelete);
			if(rs.first()) debug = false;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return debug;
	}
}
