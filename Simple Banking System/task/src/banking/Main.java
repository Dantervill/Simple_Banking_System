package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank();
        while (true) {
            bank.showMainMenu();
        }
    }
}

class Bank {
    private static final String url = "jdbc:sqlite:card.s3db";
    private static final String BIN = "400000";
    private static final Scanner scn = new Scanner(System.in);
    private String cardNumber = "";
    private String pin = "";

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getPin() {
        return pin;
    }

    /**
     * @return boolean flag of the database connection.
     *
     * If connection is valid, the "card" table will be created.
     */

    private static boolean connectToDb() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        try (Connection con = dataSource.getConnection()) {
            if (con.isValid(5)) {
                Statement statement = con.createStatement();
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ," +
                        "number TEXT NOT NULL ," +
                        "pin TEXT NOT NULL ," +
                        "balance INTEGER DEFAULT 0)");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param number - the first argument which will be passed to the SQL query.
     * @param pin - the second argument which will be passed to the SQL query.
     * Method stores card's data into the database table.
     */

    private static void storeAccountInfo(String number, String pin) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        try (Connection con = dataSource.getConnection()) {
            if (con.isValid(5)) {
                String insertInto = "INSERT INTO card (number, pin) VALUES (?,?)";
                PreparedStatement preparedStatement = con.prepareStatement(insertInto);
                preparedStatement.setString(1, number);
                preparedStatement.setString(2, pin);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void showMainMenu() {
        if (connectToDb()) {
            System.out.println("1. Create an account\n" +
                    "2. Log into account\n" +
                    "0. Exit");
            readUserInput(scn.nextInt());
        } else {
            System.out.println("Connection is invalid.");
        }
    }

    private void readUserInput(int userInput) {
        switch (userInput) {
            case 0:
                System.out.println("\nBye!");
                System.exit(0);
                break;
            case 1:
                createAccount();
                break;
            case 2:
                logIntoAccount();
                break;
        }
    }

    /**
     * Method creates user's card number and pin and passes them as parameters to
     * storeAccountInfo() method. Also, a random generated card number goes through
     * the Luhn Algorithm, so the card number will be always valid.
     */

    private static void createAccount() {
        Random random = new Random();
        int checkSum;
        boolean isValid = false;
        while (!isValid) {
            checkSum = random.nextInt(9);
            String userCardNumber = BIN + (100000000 + random.nextInt(999999999 - 100000000)) + checkSum;
            String[] array = userCardNumber.split("");
            String[] modifiedArray = modifyArray(array);
            int sumOfDigits = 0;
            for (int i = 0; i < modifiedArray.length - 1; i++) {
                if (Integer.parseInt(modifiedArray[i]) > 9) {
                    modifiedArray[i] = String.valueOf(Integer.parseInt(modifiedArray[i]) - 9);
                }
                sumOfDigits += Integer.parseInt(modifiedArray[i]);
            }
            if ((sumOfDigits + checkSum) % 10 == 0) {
                String userPin = String.valueOf((1000 + random.nextInt(9999 - 1000)));
                storeAccountInfo(userCardNumber, userPin);
                System.out.printf("\nYour card has been created%n" +
                        "Your card number: %n%s%nYour card PIN:%n%s%n%n", userCardNumber, userPin);
                isValid = true;
            }
        }
    }

    /**
     * @param array that will be modified
     * @return modified array where even indexes are multiplied by 2
     */

    private static String[] modifyArray(String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (i % 2 == 0) {
                array[i] = String.valueOf(Integer.parseInt(array[i]) * 2);
            }
        }
        return array;
    }

    /**
     * The method reads user's input and passes it to authenticateUsersData()
     * if entered data is correct the method allows a user to log into his account.
     */

    private void logIntoAccount() {
        Scanner scn = new Scanner(System.in);
        System.out.println("\nEnter your card number:");
        cardNumber = scn.nextLine();
        setCardNumber(cardNumber);
        System.out.println("Enter your PIN:");
        pin = scn.nextLine();
        setPin(pin);
        if (authenticateUsersData(getCardNumber(), getPin())) {
            System.out.println("\nYou have successfully logged in!\n");
            showAccountMenu();
        } else {
            System.out.println("\nWrong card number or PIN!\n");
        }
    }

    /**
     * @param userCardNumber - entered card number.
     * @param userPin - entered PIN.
     * @return a boolean flag (true if entered data has been found in card table and
     * false if it hasn't been found).
     */

    private static boolean authenticateUsersData(String userCardNumber, String userPin) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        try (Connection con = dataSource.getConnection()) {
            if (con.isValid(5)) {
                String selectFrom = "SELECT id FROM card WHERE number LIKE ? AND pin LIKE ?";
                PreparedStatement pstmt = con.prepareStatement(selectFrom);
                pstmt.setString(1, userCardNumber);
                pstmt.setString(2, userPin);
                ResultSet resultSet = pstmt.executeQuery();
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Shows users balance by selecting it with the help
     * of entered and stored user's card number.
     */

    private void showBalance() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        String selectBalance = "SELECT balance FROM card WHERE number LIKE ?";
        int balance = 0;
        try (Connection con = dataSource.getConnection()) {
            PreparedStatement preparedStatement = con.prepareStatement(selectBalance);
            preparedStatement.setString(1, getCardNumber());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                balance = resultSet.getInt("balance");
            }
            System.out.println("\nBalance: " + balance + "\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param income - user's entered amount of money he wants to deposit.
     * The method adds money to user's account.
     */

    private void addIncome(int income) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        String updateBalance = "UPDATE card SET balance = balance + ? WHERE number LIKE ?";
        try (Connection con = dataSource.getConnection()) {
            PreparedStatement preparedStatement = con.prepareStatement(updateBalance);
            preparedStatement.setInt(1, income);
            preparedStatement.setString(2, getCardNumber());
            preparedStatement.executeUpdate();
            System.out.println("Income was added!\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * The method deletes an existing account from "card" table.
     */

    private void closeAccount() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        String deleteAccount = "DELETE FROM card WHERE number LIKE ?";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(deleteAccount);
            preparedStatement.setString(1, getCardNumber());
            preparedStatement.executeUpdate();
            System.out.println("\nThe account has been closed!\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param receiversCardNumber entered card number of a person who will
     * receive the money.
     */

    private void doTransfer(String receiversCardNumber) {
        String[] array = receiversCardNumber.split("");
        int checkSum = Integer.parseInt(array[array.length - 1]);
        String[] modifiedArray = modifyArray(array);
        int sumOfDigits = 0;
        for (int i = 0; i < modifiedArray.length - 1; i++) {
            if (Integer.parseInt(modifiedArray[i]) > 9) {
                array[i] = String.valueOf(Integer.parseInt(modifiedArray[i]) - 9);
            }
            sumOfDigits += Integer.parseInt(modifiedArray[i]);
        }
        if ((sumOfDigits + checkSum) % 10 != 0) {
            System.out.println("Probably you made a mistake in the card number. Please try again!\n");
            showAccountMenu();
        } else {
            SQLiteDataSource dataSource = new SQLiteDataSource();
            dataSource.setUrl(url);
            String selectID = "SELECT id FROM card WHERE number LIKE ?";
            try (Connection con = dataSource.getConnection()) {
                PreparedStatement preparedStatement = con.prepareStatement(selectID);
                preparedStatement.setString(1, receiversCardNumber);
                ResultSet resultSet = preparedStatement.executeQuery();
                String id = "";
                while (resultSet.next()) {
                    id = resultSet.getString("id");
                }
                if (id.equals("")) {
                    System.out.println("Such a card does not exist.\n");
                    showAccountMenu();
                } else {
                    System.out.println("Enter how much money you want to transfer:");
                    int transferMoney = scn.nextInt();
                    String selectBalance = "SELECT balance FROM card WHERE number LIKE ?";
                    try (PreparedStatement preparedStatement1 = con.prepareStatement(selectBalance)) {
                        preparedStatement1.setString(1, getCardNumber());
                        ResultSet resultSet1 = preparedStatement1.executeQuery();
                        int balance = 0;
                        while (resultSet1.next()) {
                            balance = resultSet1.getInt("balance");
                        }
                        if (balance < transferMoney) {
                            System.out.println("Not enough money!\n");
                            showAccountMenu();
                        } else {
                            con.setAutoCommit(false);
                            String updateMyAccount = "UPDATE card SET balance = balance - ? WHERE number LIKE ?";
                            String updateReceiversAccount = "UPDATE card SET balance = balance + ? WHERE number LIKE ?";
                            try (PreparedStatement preparedStatement2 = con.prepareStatement(updateMyAccount)) {
                                PreparedStatement preparedStatement3 = con.prepareStatement(updateReceiversAccount);
                                preparedStatement2.setInt(1, transferMoney);
                                preparedStatement2.setString(2, getCardNumber());
                                preparedStatement2.executeUpdate();
                                preparedStatement3.setInt(1, transferMoney);
                                preparedStatement3.setString(2, receiversCardNumber);
                                preparedStatement3.executeUpdate();
                                con.commit();
                                con.setAutoCommit(true);
                                System.out.println("Success!\n");
                            } catch (SQLException e) {
                                e.printStackTrace();
                                try {
                                    con.rollback();
                                } catch (SQLException exception) {
                                    exception.printStackTrace();
                                }
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showAccountMenu() {
        System.out.println("1. Balance\n2. Add income\n3. Do transfer\n4. Close account\n5. Log out\n0. Exit");
        readUserInputInAccount(scn.nextInt());
    }

    private void readUserInputInAccount(int userAccountInput) {
        switch (userAccountInput) {
            case 0:
                System.out.println("\nBye!");
                System.exit(0);
                break;
            case 1:
                showBalance();
                showAccountMenu();
                break;
            case 2:
                System.out.println("\nEnter income:");
                addIncome(scn.nextInt());
                showAccountMenu();
                break;
            case 3:
                System.out.println("\nTransfer\nEnter card number:");
                doTransfer(scn.next());
                showAccountMenu();
                break;
            case 4:
                closeAccount();
                showMainMenu();
                break;
            case 5:
                System.out.println("\nYou have successfully logged out!\n");
                showMainMenu();
                break;
        }
    }
}