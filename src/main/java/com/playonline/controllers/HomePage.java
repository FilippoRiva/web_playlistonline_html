package com.playonline.controllers;

import com.playonline.beans.Playlist;
import com.playonline.beans.User;
import com.playonline.dao.AlbumDAO;
import com.playonline.dao.PlaylistDAO;
import com.playonline.utils.GenreEnum;
import com.playonline.utils.MySQLConnections;
import com.playonline.utils.TemplateEngines;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@WebServlet("/home")
@MultipartConfig
public class HomePage extends HttpServlet {
    TemplateEngine templateEngine;
    JavaxServletWebApplication application;
    PlaylistDAO playlistDAO;
    AlbumDAO albumDAO;

    @Override
    public void init() throws ServletException {
        this.templateEngine = TemplateEngines.getTemplateEngine(getServletContext());
        this.application = JavaxServletWebApplication.buildApplication(getServletContext());
        try{
            this.playlistDAO = new PlaylistDAO(MySQLConnections.getConnection(getServletContext()));
            this.albumDAO = new AlbumDAO(MySQLConnections.getConnection(getServletContext()),
                    getServletContext().getInitParameter("filesDirectory"));
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if(user == null) {
            resp.sendRedirect("/PlaylistOnline/login");
            return;
        }
        //keeping form input
        String s_title = req.getParameter("s_title");
        String s_genre = req.getParameter("s_genre");
        String a_name = req.getParameter("a_name");
        String a_artist_name = req.getParameter("a_artist_name");
        String a_date_str = req.getParameter("a_date");
        java.util.Date a_date = null;
        if(a_date_str!=null) {
            try {
                a_date = new SimpleDateFormat("yyyy-MM-dd").parse(a_date_str);
            } catch (ParseException ignored) {
            }
        }
        int selectedId=0;
        if(s_genre!=null){
            for(GenreEnum g : GenreEnum.values()){
                if(Objects.equals(g.getName(), s_genre)){
                    selectedId=g.getId();
                }
            }
        }






        Object addingNewAlbum = req.getAttribute("addingNewAlbum");
        List<Playlist> playlists = null;
        try {
            playlists = playlistDAO.getUserPlaylists(user.getUsername());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        IWebExchange webExchange = application.buildExchange(req,resp);
        WebContext context = new WebContext(webExchange);
        context.setVariable("playlists",playlists);
        context.setVariable("adding", false);
        context.setVariable("genres",GenreEnum.values());
        if(addingNewAlbum!=null) {
            context.setVariable("adding", true);
            boolean b = (boolean) addingNewAlbum;
            if(b) {
                context.setVariable("newAlbum",true);
            }
            else {
                context.setVariable("newAlbum",false);
                context.setVariable("albums",albumDAO.getUserAlbums(user.getUsername()));
            }
        }
        context.setVariable("selectedId",selectedId);
        templateEngine.process("home",context,resp.getWriter());
    }

    //New Album selector
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String att = req.getParameter("a");
        if(att ==null){
            resp.sendRedirect("/PlaylistOnline/home");
            return;
        }
        boolean addingNewAlbum = Boolean.parseBoolean(att);
        req.setAttribute("addingNewAlbum",addingNewAlbum);
        doGet(req,resp);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
