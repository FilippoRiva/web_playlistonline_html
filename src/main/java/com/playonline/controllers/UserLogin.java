package com.playonline.controllers;

import com.playonline.beans.User;
import com.playonline.dao.UserDAO;
import com.playonline.utils.MySQLConnections;
import com.playonline.utils.TemplateEngines;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;

@WebServlet("/login")
public class UserLogin extends HttpServlet{

    private UserDAO userDAO;
    private TemplateEngine templateEngine;
    private JavaxServletWebApplication application;

    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();

        this.templateEngine = TemplateEngines.getTemplateEngine(getServletContext());
        this.application = JavaxServletWebApplication.buildApplication(getServletContext());

        Connection connection = MySQLConnections.getConnection(context);
        userDAO = new UserDAO(connection);

    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Cookie[] cookies = req.getCookies();
        if(cookies==null) {
            loginRedirect(req,resp,true);
            return;
        }
        for (Cookie c : cookies) {
            if (c.getName().equals("LOGIN_INFO")) {
                User user = userDAO.checkCookie(c.getValue());
                if(user == null) break;
                req.getSession(true).setAttribute("user", user);
                resp.sendRedirect("/PlaylistOnline/home");
                return;
            }
        }
        loginRedirect(req,resp,true);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if(username==null || password==null || password.length()<5){
            req.setAttribute("username",username);
            loginRedirect(req,resp,false);
            return;
        }
        User user = userDAO.checkCredentials(username,password);
        if(user==null){
            req.setAttribute("username",username);
            loginRedirect(req,resp,false);
            return;
        }
        req.getSession(true).setAttribute("user",user);
        Cookie cookie = new Cookie("LOGIN_INFO",user.getCookie());
        cookie.setMaxAge(31536000);
        resp.addCookie(cookie);
        resp.sendRedirect("/PlaylistOnline/home");
    }

    public void loginRedirect(HttpServletRequest req, HttpServletResponse resp,boolean valid) throws IOException {
        req.setAttribute("invalid",!valid);
        IWebContext ctx = new WebContext(application.buildExchange(req,resp));
        templateEngine.process("login",ctx,resp.getWriter());
    }
}
