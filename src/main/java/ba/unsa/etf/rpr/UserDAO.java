package ba.unsa.etf.rpr;

import ba.unsa.etf.rpr.models.Employee;
import ba.unsa.etf.rpr.models.User;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.Scanner;

public class UserDAO {
    private static UserDAO instance = null;
    private Connection cnctn;
    private PreparedStatement getUserStatement, getAllUsersStatement, getAllEmployeesStatement, deleteUserStatement;
    private SimpleObjectProperty<User> selectedUser = new SimpleObjectProperty<>();

    private UserDAO() throws SQLException {
        String url = "jdbc:sqlite:bbb.db";
        try {
            cnctn = DriverManager.getConnection(url);
        }catch (SQLException e) {
            regenerateDB();
            cnctn = DriverManager.getConnection(url);
        }
        getUserStatement = cnctn.prepareStatement("SELECT * FROM user WHERE username=? AND password=?");
        getAllUsersStatement = cnctn.prepareStatement("SELECT * FROM user");
        getAllEmployeesStatement = cnctn.prepareStatement("SELECT * FROM user WHERE admin=0");
        deleteUserStatement = cnctn.prepareStatement("DELETE FROM user WHERE id=?");
    }

    private void regenerateDB() {
        Scanner in;
        try {
            in = new Scanner(new FileInputStream("resources/db/bbb.sql"));
            StringBuilder statement = new StringBuilder();
            while(in.hasNext()) {
                statement.append(in.nextLine());
                if(statement.charAt(statement.length() - 1) == ';') {
                    try {
                        Statement stmt = cnctn.createStatement();
                        stmt.execute(String.valueOf(statement));
                        stmt.close();
                        statement = new StringBuilder("");
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static UserDAO getInstance() throws SQLException {
        if (instance == null) instance = new UserDAO();
        return instance;
    }

    public User getUser(String username, String password) throws UserNotFoundException {
        User user = null;
        try {
            getUserStatement.setString(1,username);
            getUserStatement.setString(2,password);
            ResultSet rs = getUserStatement.executeQuery();
            if(rs.next())
                user = new User(rs.getInt(1), rs.getString(2),rs.getString(3),rs.getInt(4) == 1);
            else throw new UserNotFoundException("User with these credentials doesn't exist!");
        }catch (SQLException e) {
            return user;
        }
        return user;
    }

    public ObservableList<Employee> getAllEmployees() {
        ObservableList<Employee> employees = FXCollections.observableArrayList();
        try {
            ResultSet rs = getAllEmployeesStatement.executeQuery();
            while(rs.next()) {
                employees.add(new Employee(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4) == 1, rs.getString(5), rs.getString(6), rs.getString(7)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public Boolean fireEmployee() {
        try {
            deleteUserStatement.setInt(1, selectedUser.get().getId());
            deleteUserStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public User getSelectedUser() {
        return selectedUser.get();
    }

    public SimpleObjectProperty<User> selectedUserProperty() {
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser.set(selectedUser);
    }
}
