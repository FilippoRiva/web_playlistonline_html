package com.playonline.controllers;

import com.playonline.beans.Album;
import com.playonline.beans.Artist;
import com.playonline.beans.User;
import com.playonline.dao.AlbumDAO;
import com.playonline.dao.ArtistDAO;
import com.playonline.dao.SongDAO;
import com.playonline.utils.GenreEnum;
import com.playonline.utils.MySQLConnections;
import com.playonline.utils.exceptions.SongNameNotAvailable;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/song-add")
@MultipartConfig
public class SongAdd extends HttpServlet {

    Connection connection;
    SongDAO songDAO;
    AlbumDAO albumDAO;
    ArtistDAO artistDAO;

    @Override
    public void init() throws ServletException {
        this.connection = MySQLConnections.getConnection(getServletContext());
        String filesDirectory = getServletContext().getInitParameter("filesDirectory");
        try {
            connection.setAutoCommit(false);
            artistDAO = new ArtistDAO(connection);
            songDAO = new SongDAO(connection,filesDirectory);
            albumDAO = new AlbumDAO(connection,filesDirectory);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //auth check
        User user = (User) req.getSession().getAttribute("user");
        if(req.getSession().getAttribute("user")==null) {
            resp.sendRedirect("login");
            return;
        }
        //input parsing
        Part song = req.getPart("s_file");
        Part cover = req.getPart("a_cover");
        String s_title = req.getParameter("s_title");
        String s_genre = req.getParameter("s_genre");
        String a_name = req.getParameter("a_name");
        String a_ID_str = req.getParameter("a_ID");
        String ar_name = req.getParameter("ar_name");
        String a_date_str = req.getParameter("a_date");
        if(a_ID_str==null && (song==null || s_title == null || s_title.equals("") ||
                a_name==null || a_name.equals("") || ar_name==null || ar_name.equals("") ||
                a_date_str == null || cover==null || s_genre==null)){
            //TODO compila i campi per favore;
            return;
        }
        if( a_ID_str!=null && (song==null || s_title == null || s_title.equals(""))){
            //TODO compila i campi per favore;
            return;
        }
        int a_ID = -1;
        if(a_ID_str!=null){
            try {
                a_ID = Integer.parseInt(a_ID_str);
            }catch (NumberFormatException e){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);                
                return;
            }
        }
        GenreEnum genre;
        try {
            genre = GenreEnum.valueOf(s_genre.toUpperCase());
        } catch (IllegalArgumentException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Date a_date;
        try {
            a_date = new SimpleDateFormat("yyyy-dd-MM").parse(a_date_str);
        } catch (ParseException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //query
        Album album = null;
        Artist artist;
        try {
            artist = artistDAO.addArtist(user.getUsername(),ar_name);
            if(a_ID_str==null) {
                album = albumDAO.addAlbum(a_name, artist, a_date, cover, user.getUsername());
            } else {
                album = albumDAO.getAlbum(a_ID);
            }
            songDAO.addSong(user.getUsername(), s_title,genre, song, album);
            connection.commit();
        } catch (InvalidParameterException e){
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e){
            try {
                //sql rollback
                connection.rollback();
                //cover deletion when song query or song file save fails
                albumDAO.deleteAlbumCover(user.getUsername(),album);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (SongNameNotAvailable e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            //TODO reinserire nome per favore
        }
    }
}
