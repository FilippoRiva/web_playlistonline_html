package com.playonline.controllers;

import com.playonline.beans.User;
import com.playonline.utils.TemplateEngines;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet("/home")
public class Home extends HttpServlet {
    TemplateEngine templateEngine;
    JavaxServletWebApplication application;

    @Override
    public void init() throws ServletException {
        this.templateEngine = TemplateEngines.getTemplateEngine(getServletContext());
        this.application = JavaxServletWebApplication.buildApplication(getServletContext());
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if(user == null) {
            resp.sendRedirect("/PlaylistOnline/login");
            return;
        }
        IWebExchange webExchange = application.buildExchange(req,resp);
        WebContext context = new WebContext(webExchange);
        templateEngine.process("home",context,resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req,resp);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
