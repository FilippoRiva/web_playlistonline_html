package com.playonline.dao;

import com.playonline.beans.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO {
    PreparedStatement getUserPlaylistsStatement;
    PreparedStatement isPlaylistUserCoupleValidStatement;
    PreparedStatement getPlaylistInfoStatement;
    PreparedStatement addPlaylistStatement;
    PreparedStatement addSongToPlaylistStatement;
    public PlaylistDAO(Connection connection) throws SQLException{
        String getUserPlaylistsQuery = "SELECT p_title,p_id,p_date FROM playlist WHERE p_owner=? ORDER BY p_date";
        String isPlaylistUserValid = "SELECT COUNT(*) FROM playlist WHERE p_owner=? AND p_ID=?";
        String getPlaylistInfoQuery =
                "SELECT playlist_name,creation_date,song_id,title,author,release_date,album,genre,owner" +
                "FROM playlists " +
                "JOIN playlistsongs ON playlists.playlist_id=playlistsongs.playlist_id" +
                "JOIN songs ON playlistsongs.song_id=songs.song_id" +
                "WHERE playlists.playlist_id=?";
        String addPlaylistQuery = "INSERT INTO playlist(p_owner,p_title,p_date) VALUES (?,?,?)";
        String addSongQuery = "INSERT INTO playlistsong(s_ID,p_ID) VALUES(?,?)";
        getUserPlaylistsStatement = connection.prepareStatement(getUserPlaylistsQuery);
        isPlaylistUserCoupleValidStatement = connection.prepareStatement(isPlaylistUserValid);
        getPlaylistInfoStatement = connection.prepareStatement(getPlaylistInfoQuery);
        addPlaylistStatement = connection.prepareStatement(addPlaylistQuery,Statement.RETURN_GENERATED_KEYS);
        addSongToPlaylistStatement = connection.prepareStatement(addSongQuery);
    }

    public List<Playlist> getUserPlaylists(String username) throws SQLException{
        List<Playlist> result = new ArrayList<>();
        ResultSet queryResult;
        getUserPlaylistsStatement.setString(1,username);
        queryResult = getUserPlaylistsStatement.executeQuery();
        while(queryResult.next()){
            Playlist playlist = new Playlist();
            playlist.setName(queryResult.getString(1));
            playlist.setId(queryResult.getInt(2));
            playlist.setDate(queryResult.getDate(3));
            result.add(playlist);
        }
        return result;
    }

    public int createPlaylist(String username, String playlistName) throws SQLException {
        addPlaylistStatement.setString(1,username);
        addPlaylistStatement.setString(2,playlistName);
        addPlaylistStatement.setDate(3,new Date(System.currentTimeMillis()));
        addPlaylistStatement.executeUpdate();
        ResultSet result = addPlaylistStatement.getGeneratedKeys();
        result.next();
        return result.getInt(1);
    }

    public void addSongToPlaylist(int pId, int sId) throws SQLException {
        addSongToPlaylistStatement.setInt(1,sId);
        addSongToPlaylistStatement.setInt(2,pId);
        addSongToPlaylistStatement.executeUpdate();
    }

    public Playlist getPlaylistInfo(int playlistID) throws SQLException{
        Playlist result = new Playlist();
        ResultSet queryResult;
        getPlaylistInfoStatement.setInt(1,playlistID);
        queryResult = getPlaylistInfoStatement.executeQuery();
        queryResult.next();
        result.setName(queryResult.getString(1));
        result.setId(playlistID);
        result.setDate(queryResult.getDate(2));
        List<Song> songs = new ArrayList<>();
        do{
            Song song = new Song();
            song.setId(queryResult.getInt(3));
            song.setTitle(queryResult.getString(4));
            Album album = new Album();
            Artist artist = new Artist();
            artist.setName(queryResult.getString("ar_name"));
            album.setArtist(artist);
            album.setDate(queryResult.getDate(6));
            song.setAlbum(album);
            Genre genre = new Genre();
            genre.setName(queryResult.getString("s_genre"));
            song.setGenre(genre);
            songs.add(song);
        }while(queryResult.next());
        result.setSongs(songs);
        return result;
    }
}
