package DataBase;

import Validetion.InvalidEmailException;
import Validetion.InvalidNameException;
import Validetion.InvalidPasswordException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static DataBase.sqlQuery.*;
import static Simulation.Tournament.*;

public class LoginSingUp {

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        while (true) {
            int choice = getValidInt(sc, "1. Signup \n2. Login\n3. Exit\nEnter choice: ");

            if (choice == 1) {
                System.out.print("Enter Full Name: ");
                String fullName = getValidName(sc);

                System.out.print("Enter Email ID: ");
                String email = getValidEmail(sc);

                System.out.print("Enter Password: ");
                String password = getValidPassword(sc);

                System.out.print("Enter Role (HOST / AUDIENCE): ");
                String role = sc.nextLine().trim().toUpperCase();

                Connection con = getCon();
                String sql = "INSERT INTO Users (full_name, email, password, role) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);

                ps.setString(1, fullName);
                ps.setString(2, email);
                ps.setString(3, password);
                ps.setString(4, role);

                try {
                    ps.executeUpdate();
                    System.out.println("Signup successful!");
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Email already exists. Please login.");
                }

            } else if (choice == 2) {
                String role = "";

                System.out.print("Enter Email ID: ");
                String email = sc.nextLine();

                System.out.print("Enter Password: ");
                String password = sc.nextLine();

                Connection con = getCon();
                String sql = "SELECT * FROM Users WHERE email = ? AND password = ?";
                PreparedStatement ps = con.prepareStatement(sql);

                ps.setString(1, email);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    System.out.println("Login successful! Welcome, " + rs.getString("full_name"));
                    role = rs.getString("role");
                    System.out.println("Role: " + role);
                } else {
                    System.out.println("Invalid credentials. Enter valid input");
                }

                if (role.equalsIgnoreCase("host")) {
                    while (true) {
                        int choiceForMatch = getValidInt(sc,
                                "\nEnter 1 for new matches\nEnter 2 for saved matches\nEnter 3 for exit\nChoice: ");
                        System.out.println();

                        if (choiceForMatch == 1) {
                            if(organizeMatch(email)) {
                                startMatch(sc);
                            }
                        } else if (choiceForMatch == 2) {
                            setScheduleFromDB(email,sc);
                            try {
                                startMatch(sc);
                            }
                            catch (NoSuchElementException e){
                                System.out.println("No data found create new match");
                            }
                        } else {
                            break;
                        }

                    }
                } else if (role.equalsIgnoreCase("AUDIENCE")) {
                    Audience audience=new Audience();
                    audience.AudienceMenu(sc);
                }
            } else if (choice == 3) {
                System.out.println("Thank you.");
                break;
            } else {
                System.out.println("Invalid choice");

            }
        }
    }



    static int getValidInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    static String getValidName(Scanner sc) {
        while (true) {
            try {
                String name = sc.nextLine().replaceAll("\\s+", " ").trim().toUpperCase();
                if (name.isEmpty()) {
                    throw new InvalidNameException("Name cannot be empty");
                }
                if (name.length() < 2) {
                    throw new InvalidNameException("Name must have at least 2 characters");
                }
                if (name.length() > 17) {
                    throw new InvalidNameException("Maximum 17 characters allowed");
                }
                if (!name.matches("[A-Z ]+")) {
                    throw new InvalidNameException("Only letters and spaces are allowed");
                }

                return name;
            } catch (InvalidNameException e) {
                System.out.println(e.getMessage());
                System.out.print("Please enter again: ");
            }
        }
    }

    static String getValidEmail(Scanner sc) {
        while (true) {
            try {
                String email = sc.nextLine().trim().toLowerCase();
                if (email.isEmpty()) {
                    throw new InvalidEmailException("Email cannot be empty");
                }
                if (!email.endsWith("@gmail.com")) {
                    throw new InvalidEmailException("Email must end with @gmail.com");
                }
                if (!email.matches("[a-z0-9.@]+")) {
                    throw new InvalidEmailException("Email can only contain letters, numbers, dots, and @");
                }
                return email;
            } catch (InvalidEmailException e) {
                System.out.println(e.getMessage());
                System.out.print("Please enter again: ");
            }
        }
    }

    static String getValidPassword(Scanner sc) {
        while (true) {
            try {
                String password = sc.nextLine();

                if (password.length() < 8 || password.length() > 100) {
                    throw new InvalidPasswordException("Password must be between 8 and 100 characters long");
                }
                if (!password.matches(".*[a-z].*")) {
                    throw new InvalidPasswordException("Password must contain at least one lowercase letter");
                }
                if (!password.matches(".*[A-Z].*")) {
                    throw new InvalidPasswordException("Password must contain at least one uppercase letter");
                }
                if (!password.matches(".*[0-9].*")) {
                    throw new InvalidPasswordException("Password must contain at least one digit");
                }
                if (!password.matches(".*[!@#$%^&()_+\\-=\\[\\]{};:'\",.<>/?\\\\|`~].*")) {
                    throw new InvalidPasswordException("Password must contain at least one special character");
                }

                return password;
            } catch (InvalidPasswordException e) {
                System.out.println(e.getMessage());
                System.out.print("Please enter again: ");
            }
        }
    }

}