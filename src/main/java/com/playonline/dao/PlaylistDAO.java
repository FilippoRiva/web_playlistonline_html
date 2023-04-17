package com.playonline.dao;

import com.playonline.beans.*;
import com.playonline.utils.GenreEnum;

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
        String getPlaylistInfoQuery ="SELECT p_title,p_date,s.s_ID,a_ID,s_title,s_genre,a_name,ar_name, ar_ID ,a_date " +
                "FROM playlist " +
                "LEFT JOIN playlistsong p on playlist.p_ID = p.p_ID " +
                "JOIN song s on s.s_ID = p.s_ID " +
                "JOIN album a on a.a_ID = s.s_album_ID " +
                "JOIN artist a2 on a.a_artist_ID = a2.ar_ID " +
                "WHERE playlist.p_ID=? AND playlist.p_owner=?" +
                "ORDER BY a_date DESC, ps_position ASC";
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

    public Playlist getPlaylistPage(int playlistID,String owner, int page) throws SQLException{
        Playlist playlist = new Playlist();
        ResultSet queryResult;
        getPlaylistInfoStatement.setInt(1,playlistID);
        getPlaylistInfoStatement.setString(2,owner);
        queryResult = getPlaylistInfoStatement.executeQuery();
        if(!queryResult.next()){
            return null;
        }
        int beginRow = (page-1)*5;
        int endRow = (page)*5;
        while(beginRow!=0){
            if(!queryResult.next()) return null;
            beginRow--;
        }
        playlist.setName(queryResult.getString("p_title"));
        playlist.setId(playlistID);
        playlist.setDate(new Date(queryResult.getDate("p_date").getTime()));
        List<Song> songs = new ArrayList<>();
        do{
            Song song = new Song();
            song.setId(queryResult.getInt("s_ID"));
            song.setTitle(queryResult.getString("s_title"));
            GenreEnum genreEnum = GenreEnum.valueOf(queryResult.getString("s_genre").toUpperCase());
            Genre genre = new Genre();
            genre.setName(genreEnum.getName());
            song.setGenre(genre);
            Album album = new Album();
            album.setId(queryResult.getInt("a_ID"));
            album.setName(queryResult.getString("a_name"));
            album.setDate(new Date(queryResult.getDate("a_date").getTime()));
            Artist artist = new Artist();
            artist.setName(queryResult.getString("ar_name"));
            artist.setID(queryResult.getInt("ar_ID"));
            album.setArtist(artist);
            song.setAlbum(album);
            songs.add(song);
        }while(queryResult.next() && queryResult.getRow()!=endRow+2);
        playlist.setSongs(songs);
        return playlist;
    }
}
