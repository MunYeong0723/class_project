package project4;

import java.util.Scanner;
import java.sql.*;

public class DBProj {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		Connection con = null;
		
		User us = null;
		Manager mg = null;
		
		try {
			//mariaDB ·Îµù
			Class.forName("org.mariadb.jdbc.Driver");
			
			//open DBMS connection
			String url = "jdbc:mariadb://localhost:3306/BANK";
			String user = "ansdud0723";
			String pw = "coals0903";
			con = DriverManager.getConnection(url, user, pw);
		}
		catch(Exception e) {
			e.printStackTrace();
			try {
				con.close();
			}
			catch(Exception ei) {
				ei.printStackTrace();
			}
			return;
		}
		
		int menu = 0;
		do {
			System.out.println("0. Exit");
			System.out.println("1. User Menu");
			System.out.println("2. Manager Menu");
			System.out.print("Input : ");
			
			menu = sc.nextInt();
			
			switch(menu) {
			case 0 :
				break;
			case 1 :
				us = new User(con);
				us.run();
				break;
			case 2 :
				mg = new Manager(con);
				mg.run();
				break;
			default :
				System.out.println("There is no menu " + menu + ". Try again.");
				break;
			}
		}
		while(menu != 0);
		
		try {
			con.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
