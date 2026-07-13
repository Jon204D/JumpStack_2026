package com.collabera.ConsoleBankApp;
import java.util.*;

public class Runner {
	static Scanner scr = new Scanner(System.in);
	static final List<User> users = new ArrayList<>();

	static {
		User admin = new Admin();
		admin.setUsername("admin");
		admin.setPassword("admin123");

		User customer1 = new Customer();
		customer1.setUsername("customer1");
		customer1.setPassword("customer123");

		User customer2 = new Customer();
		customer2.setUsername("customer2");
		customer2.setPassword("customer123");

		User customer3 = new Customer();
		customer3.setUsername("customer");
		customer3.setPassword("customer123");

		users.add(admin);
		users.add(customer1);
		users.add(customer2);
		users.add(customer3);
	}

	static String username;

	public static void main(String[] args) {
		printMessage("Welcome to the ABC Digital Bank");

		boolean flag = true;

		while (flag) {

			String loginResult = login();

			if (loginResult.startsWith("admin")) {
				printMessage("Logged in as admin " + username);
				adminDashboard(username);
			} else if (loginResult.startsWith("customer")) {
				printMessage("Logged in as customer " + username);
				customerDashboard(username);
			} else {
				printMessage("Login failed");
			}

			printMessage("Do you want to continue? (y/n)");
			String response = scr.nextLine().trim().toLowerCase();
			if (!response.equals("y")) {
				flag = false;
			}
		}
	}

	static void printMessage(String message) {
		System.out.println(message);
	}
	
	static String login() {
		String loginType = "";
		printMessage("Please enter your username and password separated by a space...");
		String enterUsernamePassword = scr.nextLine();
		String[] parts = enterUsernamePassword.trim().split("\\s+");
		if (parts.length != 2) {
			return loginType;
		}

		String parsedUsername = parts[0];
		String parsedPassword = parts[1];

		if (parsedUsername.equals("admin") && parsedPassword.equals("admin123")) {
			loginType = "admin";
			username = parsedUsername;
		}
		else {
			for (int i = 0; i < users.size(); i++) {
				User user = users.get(i);
				if (user.getUsername().equals(parsedUsername) && user.getPassword().equals(parsedPassword)) {
					loginType = user.getUserType();
					username = user.getUsername();
					break;
				}
			}
		}

		return loginType;
	}

	public static void adminDashboard(String uname) {
		printMessage("Welcome to the admin dashboard, " + uname);
		//to-do: implement admin dashboard functionality using Switch-case
		//View client account details, withdraw, transfer, deposit
	}

	public static void customerDashboard(String uname) {
		printMessage("Welcome to the customer dashboard, " + uname);
		//to-do: implement customer dashboard functionality using Switch-case
		//View own account details, withdraw, transfer, deposit
	}
}

class Bank {
	private int id;
	private String name;
	List<Customer> customers = new ArrayList<>();

	public Bank(int id, String name, List<Customer> customers) {
		this.id = id;
		this.name = name;
		this.customers = customers;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Customer> getCustomers() {
		return customers;
	}

	public void setCustomers(List<Customer> customers) {
		this.customers = customers;
	}
}

abstract class User {
	private String username;
	private String password;

	public User() {
	}

	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	abstract String getUserType();
}

class Admin extends User {
	String getUserType() {
		return "admin";
	}
}

class Customer extends User {
	String getUserType() {
		return "customer";
	}
}

abstract class Account {
	private int id;
	private String accountNumber;
	private double balance;

	public Account(int id, String accountNumber, double balance) {
		this.id = id;
		this.accountNumber = accountNumber;
		this.balance = balance;
	}

	public int getId() {
		return id;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public double getBalance() {
		return balance;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}
}

class SavingsAccount extends Account implements AccountOperations {

	public SavingsAccount(int id, String accountNumber, double balance) {
		super(id, accountNumber, balance);
	}

	@Override
	public void deposit(double amount) {
		setBalance(getBalance() + amount);
	}

	@Override
	public void withdraw(double amount) {
		if (amount <= getBalance()) {
			setBalance(getBalance() - amount);
		} else {
			System.out.println("Insufficient balance");
		}
	}

	@Override
	public void transfer(double amount, Account toAccount) {
		if (amount <= getBalance()) {
			setBalance(getBalance() - amount);
			toAccount.setBalance(toAccount.getBalance() + amount);
		} else {
			System.out.println("Insufficient balance");
		}
	}
}

class CheckingAccount extends Account implements AccountOperations {

	public CheckingAccount(int id, String accountNumber, double balance) {
		super(id, accountNumber, balance);
	}

	@Override
	public void deposit(double amount) {
		setBalance(getBalance() + amount);
	}

	@Override
	public void withdraw(double amount) {
		if (amount <= getBalance()) {
			setBalance(getBalance() - amount);
		} else {
			System.out.println("Insufficient balance");
		}
	}

	@Override
	public void transfer(double amount, Account toAccount) {
		if (amount <= getBalance()) {
			setBalance(getBalance() - amount);
			toAccount.setBalance(toAccount.getBalance() + amount);
		} else {
			System.out.println("Insufficient balance");
		}
	}
}

interface AccountOperations {
	void deposit(double amount);
	void withdraw(double amount);
	void transfer(double amount, Account toAccount);
}
