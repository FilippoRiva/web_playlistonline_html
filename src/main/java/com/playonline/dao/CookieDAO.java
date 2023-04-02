package com.playonline.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CookieDAO {
    PreparedStatement validCookieStatement;
    public CookieDAO(Connection connection){
        String checkCookieQuery = "SELECT COUNT(*) FROM users WHERE user_cookie=?";
        try{
            validCookieStatement = connection.prepareStatement(checkCookieQuery);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to prepare checkCookie Statement");
        }
    }

    public String getNewCookie(){
        ResultSet result;
        String newCookieVal;
        try {
            boolean alreadyExists;
            do {
                //this generates only Strings of length 32
                UUID randomUUID = UUID.randomUUID();
                newCookieVal = randomUUID.toString();
                validCookieStatement.setString(1, newCookieVal);
                result = validCookieStatement.executeQuery();
                result.next();
                alreadyExists = result.getInt(1) >= 1;
            } while (alreadyExists);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to query the cookie value");
        }
        return newCookieVal;
    }
}
