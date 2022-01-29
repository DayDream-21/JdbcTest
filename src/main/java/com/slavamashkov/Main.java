package com.slavamashkov;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        buildTable(Student.class);

        List<Student> students = new ArrayList<>(Arrays.asList(
                new Student("Mary", 50),
                new Student("Mike", 60),
                new Student("Jim", 75),
                new Student("Kate", 70)
        ));
    }

    // TODO create method that fill generated table with related object

    private static void createStudentsTable() {
        String sqlStatement = "CREATE TABLE IF NOT EXISTS students (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "score INTEGER);";

        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {

            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            System.out.println("Table create error");
        }
    }

    private static void deleteStudentsTable() {
        String sqlStatement = "DROP TABLE IF EXISTS students";

        try (Connection connection = connect();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sqlStatement)) {

             preparedStatement.executeUpdate();

        } catch (SQLException exception) {
            System.out.println("Table delete error");
        }
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

    // In result, we must achieve and execute sql statement:
    // CREATE TABLE IF NOT EXISTS students (id INTEGER, name TEXT, score INTEGER);
    private static void buildTable(Class cl) {
        // Check if we can build the table from this class
        if (!cl.isAnnotationPresent(Table.class)) {
            throw new RuntimeException("@Table annotation missed");
        }

        Map<Class, String> map = new HashMap<>();
        map.put(int.class, "INTEGER");
        map.put(String.class, "TEXT");

        StringBuilder stringBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        // 'CREATE TABLE IF NOT EXISTS'
        stringBuilder.append(((Table) cl.getAnnotation(Table.class)).title());
        // 'CREATE TABLE IF NOT EXISTS students'
        stringBuilder.append(" (");
        // 'CREATE TABLE IF NOT EXISTS students ('
        Field[] fields = cl.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                stringBuilder.append(field.getName())
                        .append(" ")
                        .append(map.get(field.getType()))
                        .append(", ");
            }
        }
        // 'CREATE TABLE IF NOT EXISTS students (id INTEGER, name TEXT, score INTEGER, '
        stringBuilder.setLength(stringBuilder.length() - 2);
        // 'CREATE TABLE IF NOT EXISTS students (id INTEGER, name TEXT, score INTEGER'
        stringBuilder.append(");");
        // 'CREATE TABLE IF NOT EXISTS students (id INTEGER, name TEXT, score INTEGER);'
        try (Connection connection = connect();
             PreparedStatement statement =
                     connection.prepareStatement(stringBuilder.toString())) {

            statement.executeUpdate();
        } catch (SQLException exception) {
            System.out.println("Creation error");
        }
    }

    private static void printAllStudents() {
        String sqlStatement = "SELECT * FROM students;";

        try (Connection connection = connect();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sqlStatement);
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

    private static void updateStudentScoreById(int studentId, int studentScore) {
        String sqlStatement = "UPDATE students SET score = ? WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, studentScore);
            preparedStatement.setInt(2, studentId);

            preparedStatement.executeUpdate();

        } catch (SQLException exception) {
            System.out.println("Update error");
        }
    }

    private static void deleteStudentById(int studentId) {
        String sqlStatement = "DELETE FROM students WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, studentId);

            preparedStatement.executeUpdate();

        } catch (SQLException exception) {
            System.out.println("Delete error");
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
}
