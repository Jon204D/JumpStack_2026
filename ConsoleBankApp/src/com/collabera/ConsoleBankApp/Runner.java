package com.collabera.ConsoleBankApp;
import java.util.*;

public class Runner {
	static Scanner scr = new Scanner(System.in);
	static final List<User> users = new ArrayList<>();

	static {
		Admin admin = new Admin();
		admin.setUsername("admin");
		admin.setPassword("admin123");

		Customer customer1 = new Customer();
		customer1.setUsername("customer1");
		customer1.setPassword("customer123");
		admin.openCheckingAccount(customer1, 1, "CHK2345678", 1000.0);
		admin.openSavingsAccount(customer1, 1, "SAV002345678", 500.0);

		Customer customer2 = new Customer();
		customer2.setUsername("customer2");
		customer2.setPassword("customer123");
		admin.openCheckingAccount(customer2, 2, "CHK003456789", 2000.0);
		admin.openSavingsAccount(customer2, 2, "SAV003456789", 1000.0);

		Customer customer3 = new Customer();
		customer3.setUsername("customer3");
		customer3.setPassword("customer123");
		admin.openCheckingAccount(customer3, 3, "CHK004567891", 3000.0);
		admin.openSavingsAccount(customer3, 3, "SAV004567891", 1500.0);

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

		
		printMessage("What would you like to do? \n1. View balance \n2. Withdraw \n3. Transfer \n4. Deposit \n");
		String choice = scr.nextLine().trim();

		switch (choice) {
			case "1":
				displayAccountDetails(uname);
				break;
			case "2":
				withdrawFromAccount(uname);
				break;
			case "3":
				transferFromAccount(uname);
				break;
			case "4":
				depositToAccount(uname);
				break;
			default:
				printMessage("Invalid menu option.");
		}
	}

	static Customer findCustomerByUsername(String uname) {
		for (User user : users) {
			if (user instanceof Customer customer
					&& customer.getUsername().equals(uname)) {
				return customer;
			}
		}

		return null;
	}

	static void displayAccountDetails(String uname) {
		Customer customer = findCustomerByUsername(uname);

		if (customer == null) {
			printMessage("Customer was not found.");
			return;
		}

		if (customer.getAccounts().isEmpty()) {
			printMessage("You do not have any accounts.");
			return;
		}

		for (Account account : customer.getAccounts()) {
			String accountType = "Unknown";
			double interestRate = 0.0;

			if (account instanceof SavingsAccount savings) {
				accountType = "Savings";
				interestRate = savings.getSavingsInterestRate();
			} else if (account instanceof CheckingAccount checking) {
				accountType = "Checking";
				interestRate = checking.getCheckingInterestRate();
			}

			String details = String.format(
					"%s Account:%n"
							+ "\tAccount Number: %s%n"
							+ "\tBalance: $%.2f%n"
							+ "\tInterest Rate: %.2f%%",
					accountType,
					account.getAccountNumber(),
					account.getBalance(),
					interestRate * 100);

			printMessage(details);
		}
	}

	static void withdrawFromAccount(String uname) {
		Customer customer = findCustomerByUsername(uname);
		Account account = selectOwnedAccount(customer, "withdraw from");
		if (account == null) {
			return;
		}

		Double amount = readPositiveAmount("Enter the amount to withdraw:");
		if (amount == null) {
			return;
		}

		if (amount > account.getBalance()) {
			printMessage("Insufficient balance.");
			return;
		}

		account.withdraw(amount);
		printMessage(String.format(
				"Withdrawal successful. New balance: $%.2f",
				account.getBalance()));
	}

	static void depositToAccount(String uname) {
		Customer customer = findCustomerByUsername(uname);
		Account account = selectOwnedAccount(customer, "deposit into");
		if (account == null) {
			return;
		}

		Double amount = readPositiveAmount("Enter the amount to deposit:");
		if (amount == null) {
			return;
		}

		account.deposit(amount);
		printMessage(String.format(
				"Deposit successful. New balance: $%.2f",
				account.getBalance()));
	}

	static void transferFromAccount(String uname) {
		Customer customer = findCustomerByUsername(uname);
		Account sourceAccount = selectOwnedAccount(customer, "transfer from");
		if (sourceAccount == null) {
			return;
		}

		printMessage("Enter the destination account number:");
		String destinationAccountNumber = scr.nextLine().trim();
		Account destinationAccount = findAccountByNumber(destinationAccountNumber);

		if (destinationAccount == null) {
			printMessage("Destination account was not found.");
			return;
		}

		if (destinationAccount == sourceAccount) {
			printMessage("Source and destination accounts must be different.");
			return;
		}

		Double amount = readPositiveAmount("Enter the amount to transfer:");
		if (amount == null) {
			return;
		}

		if (amount > sourceAccount.getBalance()) {
			printMessage("Insufficient balance.");
			return;
		}

		sourceAccount.transfer(amount, destinationAccount);
		printMessage(String.format(
				"Transfer successful. New balance: $%.2f",
				sourceAccount.getBalance()));
	}

	static Account selectOwnedAccount(Customer customer, String action) {
		if (customer == null) {
			printMessage("Customer was not found.");
			return null;
		}

		if (customer.getAccounts().isEmpty()) {
			printMessage("You do not have any accounts.");
			return null;
		}

		printMessage("Available accounts:");
		for (Account account : customer.getAccounts()) {
			printMessage(String.format(
					"\t%s - Balance: $%.2f",
					account.getAccountNumber(),
					account.getBalance()));
		}

		printMessage("Enter the account number to " + action + ":");
		String accountNumber = scr.nextLine().trim();
		Account account = findAccountByNumber(customer, accountNumber);

		if (account == null) {
			printMessage("That account was not found or does not belong to you.");
		}

		return account;
	}

	static Account findAccountByNumber(Customer customer, String accountNumber) {
		for (Account account : customer.getAccounts()) {
			if (account.getAccountNumber().equalsIgnoreCase(accountNumber)) {
				return account;
			}
		}

		return null;
	}

	static Account findAccountByNumber(String accountNumber) {
		for (User user : users) {
			if (user instanceof Customer customer) {
				Account account = findAccountByNumber(customer, accountNumber);
				if (account != null) {
					return account;
				}
			}
		}

		return null;
	}

	static Double readPositiveAmount(String prompt) {
		printMessage(prompt);
		String input = scr.nextLine().trim();

		try {
			double amount = Double.parseDouble(input);
			if (!Double.isFinite(amount) || amount <= 0) {
				printMessage("Amount must be a positive number.");
				return null;
			}

			return amount;
		} catch (NumberFormatException exception) {
			printMessage("Invalid amount.");
			return null;
		}
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

	public SavingsAccount openSavingsAccount(Customer customer, int id, String accountNumber, double startingBalance) {
		SavingsAccount newAccount = new SavingsAccount(customer, id, accountNumber, startingBalance);
		customer.addAccount(newAccount);
		return newAccount;
	}

	public CheckingAccount openCheckingAccount(Customer customer, int id, String accountNumber, double startingBalance) {
		CheckingAccount newAccount = new CheckingAccount(customer, id, accountNumber, startingBalance);
		customer.addAccount(newAccount);
		return newAccount;
	}
}

class Customer extends User {
	String getUserType() {
		return "customer";
	}
	
	private final List<Account> accounts = new ArrayList<>();

	public List<Account> getAccounts() {
		return accounts;
	}

	public void addAccount(Account account) {
		accounts.add(account);
	}
}

abstract class Account implements AccountOperations {
	private Customer customer;
	private int id;
	private String accountNumber;
	private double balance;

	public Account(Customer customer, int id, String accountNumber, double balance) {
		this.customer = customer;
		this.id = id;
		this.accountNumber = accountNumber;
		this.balance = balance;
	}

	public Customer getCustomer() {
		return customer;
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

	@Override
	public void deposit(double amount) {
		if (!Double.isFinite(amount) || amount <= 0) {
			System.out.println("Amount must be a positive number.");
			return;
		}

		setBalance(getBalance() + amount);
	}

	@Override
	public void withdraw(double amount) {
		if (!Double.isFinite(amount) || amount <= 0) {
			System.out.println("Amount must be a positive number.");
		} else if (amount <= getBalance()) {
			setBalance(getBalance() - amount);
		} else {
			System.out.println("Insufficient balance");
		}
	}

	@Override
	public void transfer(double amount, Account toAccount) {
		if (!Double.isFinite(amount) || amount <= 0) {
			System.out.println("Amount must be a positive number.");
		} else if (toAccount == null || toAccount == this) {
			System.out.println("A different destination account is required.");
		} else if (amount <= getBalance()) {
			setBalance(getBalance() - amount);
			toAccount.setBalance(toAccount.getBalance() + amount);
		} else {
			System.out.println("Insufficient balance");
		}
	}
}

class SavingsAccount extends Account implements AccountOperations {
	private double savingsInterestRate = 0.015;

	public SavingsAccount(Customer customer, int id, String accountNumber, double balance) {
		super(customer, id, accountNumber, balance);
	}

	public double getSavingsInterestRate() {
		return savingsInterestRate;
	}

	public void setSavingsInterestRate(double savingsInterestRate) {
		this.savingsInterestRate = savingsInterestRate;
	}
}

class CheckingAccount extends Account implements AccountOperations {
	private double checkingInterestRate = 0.01;

	public CheckingAccount(Customer customer, int id, String accountNumber, double balance) {
		super(customer, id, accountNumber, balance);
	}

	public double getCheckingInterestRate() {
		return checkingInterestRate;
	}

	public void setCheckingInterestRate(double checkingInterestRate) {
		this.checkingInterestRate = checkingInterestRate;
	}
}

interface AccountOperations {
	void deposit(double amount);
	void withdraw(double amount);
	void transfer(double amount, Account toAccount);
}
