package com.slavamashkov;

import java.sql.*;

public class Main {
    private static Connection connection;
    private static Statement statement;

    public static void main(String[] args) throws SQLException {
        //createStudent("Max", 55);
        printAllStudents();
    }

    private static void createStudent(String studentName, int studentScore) {
        String sqlStatement = "INSERT INTO students (name, score) VALUES (?, ?);";

        try (Connection connection = connect();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, studentName);
            preparedStatement.setInt(2, studentScore);

            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            System.out.println("Creation error");
        }
    }

    private static void printAllStudents() {
        String sqlStatement = "SELECT * FROM students;";

        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                System.out.printf("id: %d, name: %s, score %d%n",
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("score"));
            }

        } catch (SQLException exception) {
            System.out.println("Read error");
        }
    }

    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:main.db");
        } catch (ClassNotFoundException | SQLException e) {
            throw new SQLException("Unable to connect");
        }
    }

    public static void disconnect() {
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
