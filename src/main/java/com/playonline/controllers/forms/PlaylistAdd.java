package com.playonline.controllers.forms;

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

    Connection connection;
    @Override
    public void init() throws ServletException {
        this.connection = MySQLConnections.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // auth check
        User user = (User) req.getSession().getAttribute("user");
        if(user == null){
            resp.sendRedirect("login");
            return;
        }
        // input parsing
        Map<String,String[]> parameters = req.getParameterMap();
        String playlistID = req.getParameter("p_ID");
        String pageString = req.getParameter("page");
        int p_ID;
        int page;
        try{
            p_ID = Integer.parseInt(playlistID);
            page = Integer.parseInt(pageString);
        } catch (NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if(!parameters.containsKey("s_ID")){ //doesn't contains songs
            resp.sendRedirect("/PlaylistOnline/playlist?id="+p_ID+"&page="+page);
            return;
        }

        // database update
        String[] songIDs = parameters.get("s_ID");
        try {
            SongDAO songDAO = new SongDAO(connection,"");
            PlaylistDAO playlistDAO = new PlaylistDAO(connection);
            for (String s_ID : songIDs) {
                try {
                    int id = Integer.parseInt(s_ID);
                    if(songDAO.isOwner(user.getUsername(),id)) playlistDAO.addSongToPlaylist(user.getUsername(), p_ID, id);
                } catch (NumberFormatException ignored) { //ignoring non parsable IDs
                }
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        resp.sendRedirect("/PlaylistOnline/playlist?id="+p_ID+"&page=1");
    }

}
