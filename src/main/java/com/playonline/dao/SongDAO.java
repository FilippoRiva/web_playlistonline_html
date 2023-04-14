package com.playonline.dao;

import com.playonline.beans.Album;
import com.playonline.beans.Genre;
import com.playonline.beans.Song;
import com.playonline.utils.GenreEnum;
import com.playonline.utils.exceptions.SongNameNotAvailable;

import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.sql.*;

public class SongDAO {

    PreparedStatement getSongInfoStatement;
    PreparedStatement getSongFileStatement;
    PreparedStatement getSongCoverStatement;
    PreparedStatement addSongStatement;
    AlbumDAO albumDAO;
    String folder;
    public SongDAO(Connection connection, String folder) throws SQLException {
        this.folder = folder;
        String getSongInfoQuery = "SELECT title,author,release_date,genre FROM songs WHERE song_id=?";
        String getSongFileQuery = "SELECT song FROM songs WHERE song_id=?";
        String getSongCoverQuery = "SELECT cover FROM songs WHERE song_id=?";
        String addSongQuery = "INSERT INTO song (s_title, s_album_ID, s_genre, s_owner) VALUES (?,?,?,?)";
        getSongInfoStatement = connection.prepareStatement(getSongInfoQuery);
        getSongFileStatement = connection.prepareStatement(getSongFileQuery);
        getSongCoverStatement = connection.prepareStatement(getSongCoverQuery);
        addSongStatement = connection.prepareStatement(addSongQuery,Statement.RETURN_GENERATED_KEYS);
    }

    public Song getSongInfo(int ID) throws SQLException{
        Song song = new Song();
        //TODO
        return song;
    }

    public boolean isValidUserIDCouple(String username, int ID) throws SQLException{
        //TODO
       return false;
    }

    public Song addSong(String owner, String title, GenreEnum genre, Part song, Album album) throws SQLException, SongNameNotAvailable {
        addSongStatement.setString(1,title);
        addSongStatement.setInt(2,album.getId());
        addSongStatement.setString(3, genre.name());
        addSongStatement.setString(4,owner);
        try {
            addSongStatement.executeUpdate();
        } catch (SQLException e){
            throw new SongNameNotAvailable();
        }
        ResultSet generatedKey = addSongStatement.getGeneratedKeys();
        generatedKey.next();
        int currentID = generatedKey.getInt(1);
        String fileName = song.getSubmittedFileName();
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String dir = folder+owner+"/"+album.getArtist().getID()+"/"+album.getId()+"/";
        String filepath = dir + currentID + extension;
        try {
            File f = new File(dir);
            if(!f.exists() && !f.mkdirs()){
                throw new SQLException("unable to create song dirs");
            }
            f = new File(filepath);
            song.write(f.getAbsolutePath());
        } catch (IOException e) {
            throw new SQLException(e);
        }
        Song songBean = new Song();
        songBean.setTitle(title);
        songBean.setAlbum(album);
        Genre genreBean = new Genre();
        genreBean.setName(genre.getName());
        songBean.setGenre(genreBean);
        songBean.setId(currentID);
        songBean.setAlbum(album);
        return songBean;
    }

    public void deleteSong(String username, String aName, String sTitle) {
        //TODO
    }
}
