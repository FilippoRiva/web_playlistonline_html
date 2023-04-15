package com.playonline.dao;

import com.playonline.beans.Album;
import com.playonline.beans.Artist;
import com.playonline.beans.Genre;
import com.playonline.beans.Song;
import com.playonline.utils.GenreEnum;
import com.playonline.utils.exceptions.SongNameNotAvailable;

import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SongDAO {

    PreparedStatement getSongInfoStatement;
    PreparedStatement addSongStatement;
    PreparedStatement getUserSongsStatement;
    String folder;
    public SongDAO(Connection connection, String folder) throws SQLException {
        this.folder = folder;
        String getSongInfoQuery = "SELECT title,author,release_date,genre FROM songs WHERE song_id=?";
        String addSongQuery = "INSERT INTO song (s_title, s_album_ID, s_genre, s_owner) VALUES (?,?,?,?)";
        String getUserSongs = " SELECT ar_name, ar_ID, a_name, a_date, a_ID, s_title, s_genre, s_ID " +
                "FROM artist " +
                "JOIN album a on artist.ar_ID = a.a_artist_ID " +
                "JOIN song s on a.a_ID = s.s_album_ID " +
                "WHERE s_owner= ? " +
                "ORDER BY ar_name, a_name, s_title";
        getSongInfoStatement = connection.prepareStatement(getSongInfoQuery);
        addSongStatement = connection.prepareStatement(addSongQuery,Statement.RETURN_GENERATED_KEYS);
        getUserSongsStatement = connection.prepareStatement(getUserSongs);
    }


    public void addSong(String owner, String title, GenreEnum genre, Part song, Album album) throws SQLException, SongNameNotAvailable {
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
    }

    public List<Artist> getArtists(String username) throws SQLException {
        getUserSongsStatement.setString(1,username);
        ResultSet result = getUserSongsStatement.executeQuery();
        List<Artist> artists = new ArrayList<>();
        Artist currentArtist = new Artist();
        Album currentAlbum = new Album();
        List<Album> currentAlbums = new ArrayList<>();
        Song currentSong;
        List<Song> currentSongs = new ArrayList<>();
        if(!result.next()) return artists;
        do {
            //artist changes
            if(currentArtist.getName()==null || currentArtist.getID()!=result.getInt("ar_ID")) {
                if(currentArtist.getName()!=null) {
                    currentArtist.setAlbums(currentAlbums);
                    currentAlbums = new ArrayList<>();
                    artists.add(currentArtist);
                }
                currentArtist = new Artist();
                currentArtist.setID(result.getInt("ar_ID"));
                currentArtist.setName(result.getString("ar_name"));
            }
            //album changes
            if(currentAlbum.getName()==null || currentAlbum.getId()!= result.getInt("a_ID")){
                currentAlbum.setSongs(currentSongs);
                currentSongs = new ArrayList<>();
                currentAlbum= new Album();
                currentAlbum.setId(result.getInt("a_ID"));
                currentAlbum.setArtist(currentArtist);
                currentAlbum.setDate(new Date(result.getDate("a_date").getTime()));
                currentAlbum.setName(result.getString("a_name"));
                currentAlbums.add(currentAlbum);
            }
            //new song
            currentSong = new Song();
            currentSong.setAlbum(currentAlbum);
            currentSong.setTitle(result.getString("s_title"));
            currentSong.setId(result.getInt("s_ID"));
            Genre genre = new Genre();
            genre.setName(result.getString("s_genre"));
            currentSong.setGenre(genre);
            currentSongs.add(currentSong);
        } while(result.next());
        currentAlbum.setSongs(currentSongs);
        currentArtist.setAlbums(currentAlbums);
        artists.add(currentArtist);
        return artists;
    }
}
