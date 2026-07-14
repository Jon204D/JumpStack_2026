package com.collabera.ConsoleBankApp;
import java.util.*;

public class Runner {
	static final Scanner scr = new Scanner(System.in);
	static final List<User> users = new ArrayList<>();
	static final Bank bank = new Bank(1, "ABC Digital Bank");
	static int nextAccountId = 7;

	static {
		Admin admin = new Admin();
		admin.setUsername("admin");
		admin.setPassword("admin123");

		Customer customer1 = new Customer();
		customer1.setUsername("customer1");
		customer1.setPassword("customer123");
		admin.openCheckingAccount(customer1, 1, "CHK2345678", 1000.0);
		admin.openSavingsAccount(customer1, 2, "SAV002345678", 500.0);

		Customer customer2 = new Customer();
		customer2.setUsername("customer2");
		customer2.setPassword("customer123");
		admin.openCheckingAccount(customer2, 3, "CHK003456789", 2000.0);
		admin.openSavingsAccount(customer2, 4, "SAV003456789", 1000.0);

		Customer customer3 = new Customer();
		customer3.setUsername("customer3");
		customer3.setPassword("customer123");
		admin.openCheckingAccount(customer3, 5, "CHK004567891", 3000.0);
		admin.openSavingsAccount(customer3, 6, "SAV004567891", 1500.0);

		users.add(admin);
		users.add(customer1);
		users.add(customer2);
		users.add(customer3);

		bank.addCustomer(customer1);
		bank.addCustomer(customer2);
		bank.addCustomer(customer3);
	}

	public static void main(String[] args) {
		printMessage("Welcome to the ABC Digital Bank");

		boolean applicationRunning = true;

		while (applicationRunning) {
			User loggedInUser = login();

			if (loggedInUser instanceof Admin admin) {
				printMessage("Logged in as admin " + admin.getUsername());
				adminDashboard(admin);
			} else if (loggedInUser instanceof Customer customer) {
				printMessage("Logged in as customer " + customer.getUsername());
				customerDashboard(customer);
			} else {
				printMessage("Login failed");
			}

			printMessage("Do you want another user to log in? (y/n)");
			String response = scr.nextLine().trim().toLowerCase();
			if (!response.equals("y")) {
				applicationRunning = false;
			}
		}

		printMessage("Thank you for using the ABC Digital Bank.");
	}

	static void printMessage(String message) {
		System.out.println(message);
	}
	
	static User login() {
		printMessage("Please enter your username and password separated by a space...");
		String enterUsernamePassword = scr.nextLine();
		String[] parts = enterUsernamePassword.trim().split("\\s+");
		if (parts.length != 2) {
			return null;
		}

		String parsedUsername = parts[0];
		String parsedPassword = parts[1];

		for (User user : users) {
			if (user.getUsername().equals(parsedUsername)
					&& user.getPassword().equals(parsedPassword)) {
				return user;
			}
		}

		return null;
	}

	public static void adminDashboard(Admin admin) {
		boolean loggedIn = true;

		while (loggedIn) {
			printMessage("\nAdmin Dashboard - " + admin.getUsername());
			printMessage("1. View all customers\n2. View all accounts\n3. Create customer\n"
					+ "4. Create account\n5. Delete account\n6. Delete customer\n7. Log out");
			String choice = scr.nextLine().trim();

			switch (choice) {
				case "1":
					displayAllCustomers();
					break;
				case "2":
					displayAllAccounts();
					break;
				case "3":
					createCustomer();
					break;
				case "4":
					createAccount(admin);
					break;
				case "5":
					deleteAccount();
					break;
				case "6":
					deleteCustomer();
					break;
				case "7":
					loggedIn = false;
					break;
				default:
					printMessage("Invalid menu option.");
					break;
			}
		}
	}

	public static void customerDashboard(Customer customer) {
		boolean loggedIn = true;

		while (loggedIn) {
			printMessage("\nCustomer Dashboard - " + customer.getUsername());
			printMessage("1. View accounts\n2. Withdraw\n3. Transfer\n4. Deposit\n5. Log out");
			String choice = scr.nextLine().trim();

			switch (choice) {
				case "1":
					displayAccountDetails(customer);
					break;
				case "2":
					withdrawFromAccount(customer);
					break;
				case "3":
					transferFromAccount(customer);
					break;
				case "4":
					depositToAccount(customer);
					break;
				case "5":
					loggedIn = false;
					break;
				default:
					printMessage("Invalid menu option.");
					break;
			}
		}
	}

	static Customer findCustomerByUsername(String uname) {
		return bank.findCustomerByUsername(uname);
	}

	static void displayAccountDetails(Customer customer) {
		if (customer.getAccounts().isEmpty()) {
			printMessage(customer.getUsername() + " does not have any accounts.");
			return;
		}

		printMessage("Accounts for " + customer.getUsername() + ":");
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

	static void withdrawFromAccount(Customer customer) {
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

	static void depositToAccount(Customer customer) {
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

	static void transferFromAccount(Customer customer) {
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
		return bank.findAccountByNumber(accountNumber);
	}

	static void displayAllCustomers() {
		if (bank.getCustomers().isEmpty()) {
			printMessage("There are no customers.");
			return;
		}

		printMessage("Customers:");
		for (Customer customer : bank.getCustomers()) {
			printMessage(String.format(
					"\t%s - %d account(s)",
					customer.getUsername(),
					customer.getAccounts().size()));
		}
	}

	static void displayAllAccounts() {
		if (bank.getCustomers().isEmpty()) {
			printMessage("There are no customers or accounts.");
			return;
		}

		for (Customer customer : bank.getCustomers()) {
			displayAccountDetails(customer);
		}
	}

	static void createCustomer() {
		printMessage("Enter the new customer's username:");
		String username = scr.nextLine().trim();

		if (username.isEmpty() || username.contains(" ")) {
			printMessage("Username cannot be empty or contain spaces.");
			return;
		}

		if (findUserByUsername(username) != null) {
			printMessage("That username is already in use.");
			return;
		}

		printMessage("Enter the new customer's password:");
		String password = scr.nextLine().trim();
		if (password.isEmpty() || password.contains(" ")) {
			printMessage("Password cannot be empty or contain spaces.");
			return;
		}

		Customer customer = new Customer();
		customer.setUsername(username);
		customer.setPassword(password);
		users.add(customer);
		bank.addCustomer(customer);
		printMessage("Customer created successfully.");
	}

	static void createAccount(Admin admin) {
		printMessage("Enter the customer's username:");
		Customer customer = findCustomerByUsername(scr.nextLine().trim());
		if (customer == null) {
			printMessage("Customer was not found.");
			return;
		}

		printMessage("Enter a unique account number:");
		String accountNumber = scr.nextLine().trim();
		if (accountNumber.isEmpty() || findAccountByNumber(accountNumber) != null) {
			printMessage("Account number is empty or already in use.");
			return;
		}

		Double startingBalance = readNonNegativeAmount("Enter the starting balance:");
		if (startingBalance == null) {
			return;
		}

		printMessage("Choose the account type: 1. Checking  2. Savings");
		String accountType = scr.nextLine().trim();
		if (accountType.equals("1")) {
			admin.openCheckingAccount(customer, nextAccountId++, accountNumber, startingBalance);
		} else if (accountType.equals("2")) {
			admin.openSavingsAccount(customer, nextAccountId++, accountNumber, startingBalance);
		} else {
			printMessage("Invalid account type.");
			return;
		}

		printMessage("Account created successfully.");
	}

	static void deleteAccount() {
		printMessage("Enter the account number to delete:");
		Account account = findAccountByNumber(scr.nextLine().trim());
		if (account == null) {
			printMessage("Account was not found.");
			return;
		}

		account.getCustomer().removeAccount(account);
		printMessage("Account deleted successfully.");
	}

	static void deleteCustomer() {
		printMessage("Enter the customer username to delete:");
		Customer customer = findCustomerByUsername(scr.nextLine().trim());
		if (customer == null) {
			printMessage("Customer was not found.");
			return;
		}

		bank.removeCustomer(customer);
		users.remove(customer);
		printMessage("Customer and their accounts were deleted successfully.");
	}

	static User findUserByUsername(String username) {
		for (User user : users) {
			if (user.getUsername().equalsIgnoreCase(username)) {
				return user;
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

	static Double readNonNegativeAmount(String prompt) {
		printMessage(prompt);
		String input = scr.nextLine().trim();

		try {
			double amount = Double.parseDouble(input);
			if (!Double.isFinite(amount) || amount < 0) {
				printMessage("Amount cannot be negative.");
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
	private final int id;
	private final String name;
	private final List<Customer> customers = new ArrayList<>();

	public Bank(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Customer> getCustomers() {
		return Collections.unmodifiableList(customers);
	}

	public void addCustomer(Customer customer) {
		customers.add(customer);
	}

	public void removeCustomer(Customer customer) {
		customers.remove(customer);
	}

	public Customer findCustomerByUsername(String username) {
		for (Customer customer : customers) {
			if (customer.getUsername().equalsIgnoreCase(username)) {
				return customer;
			}
		}

		return null;
	}

	public Account findAccountByNumber(String accountNumber) {
		for (Customer customer : customers) {
			for (Account account : customer.getAccounts()) {
				if (account.getAccountNumber().equalsIgnoreCase(accountNumber)) {
					return account;
				}
			}
		}

		return null;
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
		return Collections.unmodifiableList(accounts);
	}

	public void addAccount(Account account) {
		accounts.add(account);
	}

	public void removeAccount(Account account) {
		accounts.remove(account);
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

class SavingsAccount extends Account {
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

class CheckingAccount extends Account {
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
