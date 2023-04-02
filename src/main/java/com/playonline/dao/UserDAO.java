package com.playonline.dao;

import com.playonline.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private final PreparedStatement checkCredentialsStatement;
    private final PreparedStatement checkCookieStatement;
    private final PreparedStatement addUserStatement;
    private final PreparedStatement checkUserAvailableStatement;
    public UserDAO(Connection connection){
        String checkCredentialsQuery="SELECT password,user_cookie FROM users WHERE username=?";
        try{
            checkCredentialsStatement = connection.prepareStatement(checkCredentialsQuery);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        String checkCookieQuery = "SELECT username,password FROM users WHERE user_cookie=?";
        try {
            checkCookieStatement = connection.prepareStatement(checkCookieQuery);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to prepare checkCookie Statement");
        }
        String userAddQuery = "INSERT INTO users(user_cookie,username,password) VALUES(?,?,?)";
        try {
            addUserStatement = connection.prepareStatement(userAddQuery);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to prepare userGet Statement");
        }
        String checkUserAvailableQuery = "SELECT COUNT(*) FROM users WHERE username=?";
        try{
            checkUserAvailableStatement= connection.prepareStatement(checkUserAvailableQuery);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to prepare userAvailable Statement");
        }
    }

    public User checkCredentials(String username, String password){
        ResultSet result;
        try{
            checkCredentialsStatement.setString(1,username);
            result = checkCredentialsStatement.executeQuery();
            if(result.next()){
                if(result.getString(1).compareTo(password)==0){
                    User user = new User();
                    user.setUsername(username);
                    user.setCookie(result.getString(2));
                    return user;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public User checkCookie(String cookie){
        try {
            checkCookieStatement.setString(1, cookie);
            ResultSet result = checkCookieStatement.executeQuery();
            if(result.next()){
                User user = new User();
                user.setUsername(result.getString(1));
                user.setCookie(cookie);
                return user;
            } else {
                return null;
            }
        }catch (SQLException e){
            throw new RuntimeException("unable to check cookie");
        }
    }
    
    public void addUser(String username,String password, String cookie){
        try {
            addUserStatement.setString(1,cookie);
            addUserStatement.setString(2,username);
            addUserStatement.setString(3,password);
            addUserStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isUsernameAvailable(String username){
        try {
            checkUserAvailableStatement.setString(1,username);
            ResultSet result = checkUserAvailableStatement.executeQuery();
            if(result.next()){
                return result.getInt(1)==0;
            } else return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    
}
