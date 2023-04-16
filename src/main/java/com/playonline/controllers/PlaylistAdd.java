package com.playonline.controllers;

import com.playonline.beans.User;
import com.playonline.dao.PlaylistDAO;
import com.playonline.dao.SongDAO;
import com.playonline.utils.MySQLConnections;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/playlist-add")
public class PlaylistAdd extends HttpServlet {
    PlaylistDAO playlistDAO;
    SongDAO songDAO;
    Connection connection;
    @Override
    public void init(){
        try {
            this.connection = MySQLConnections.getConnection(getServletContext());
            connection.setAutoCommit(false);
            this.playlistDAO = new PlaylistDAO(connection);
            this.songDAO = new SongDAO(connection,""); //no need to upload files here
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User)req.getSession().getAttribute("user");
        if(user==null){
            resp.sendRedirect("login");
            return;
        }
        String title = req.getParameter("p_title");
        String username = user.getUsername();
        Map<String,String[]> parameters = req.getParameterMap();
        String[] songIDs = parameters.get("s_ID");
        try {
            int p_ID = playlistDAO.createPlaylist(username, title);
            if (songIDs != null) {
                for (String s_ID : songIDs) {
                    try {
                        int id = Integer.parseInt(s_ID);
                        playlistDAO.addSongToPlaylist(p_ID, id);
                    } catch (NumberFormatException ignored) { //ignoring non parsable IDs
                    }
                }
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        resp.sendRedirect("home");
    }

}
