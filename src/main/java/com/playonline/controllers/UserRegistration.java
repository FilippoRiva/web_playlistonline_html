package com.playonline.controllers;

import com.playonline.dao.CookieDAO;
import com.playonline.dao.UserDAO;
import com.playonline.utils.MySQLConnections;
import com.playonline.utils.TemplateEngines;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

@WebServlet("/registration")
public class UserRegistration extends HttpServlet {

    TemplateEngine templateEngine;
    JavaxServletWebApplication application;
    private UserDAO userDAO;
    private CookieDAO cookieDAO;

    @Override
    public void init() throws ServletException {
        templateEngine = TemplateEngines.getTemplateEngine(getServletContext());
        application = JavaxServletWebApplication.buildApplication(getServletContext());
        Connection connection = MySQLConnections.getConnection(getServletContext());
        userDAO = new UserDAO(connection);
        cookieDAO = new CookieDAO(connection);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        IWebContext context = new WebContext(application.buildExchange(req,resp));
        templateEngine.process("registration",context,resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String passwordCopy = req.getParameter("passwordCopy");
        IWebContext context = new WebContext(application.buildExchange(req,resp));
        if(username==null || username.length()<1 || username.length()>30) {
            req.setAttribute("usernameInvalid",true);
            req.setAttribute("password",password);
            req.setAttribute("passwordCopy",passwordCopy);
            templateEngine.process("registration",context,resp.getWriter());
            return;
        }
        if(password==null || password.length()<5 || password.length()>100){
            req.setAttribute("username",username);
            req.setAttribute("passwordInvalid",true);
            templateEngine.process("registration",context,resp.getWriter());
            return;
        }
        if(passwordCopy==null || password.compareTo(passwordCopy)!=0){
            req.setAttribute("username",username);
            req.setAttribute("password",password);
            req.setAttribute("passwordCopyInvalid",true);
            templateEngine.process("registration",context,resp.getWriter());
            return;
        }
        if(!userDAO.isUsernameAvailable(username)){
            req.setAttribute("username",username);
            req.setAttribute("password",password);
            req.setAttribute("passwordCopy",passwordCopy);
            req.setAttribute("usernameUnavailable",true);
            templateEngine.process("registration",context,resp.getWriter());
            return;
        }
        String cookieVal = cookieDAO.getNewCookie();
        userDAO.addUser(username,password,cookieVal);
        Cookie newCookie = new Cookie("LOGIN_INFO",cookieVal);
        newCookie.setMaxAge(31536000);
        resp.addCookie(newCookie);
        resp.sendRedirect("/PlaylistOnline/login");
    }

}