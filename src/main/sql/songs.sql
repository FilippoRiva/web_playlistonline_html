SELECT s_title,s_genre,a_name,ar_name
FROM user JOIN song JOIN album JOIN artist
WHERE user.u_name='pippo' AND u_name=song.s_owner AND s_album_ID=album.a_ID AND a_artist_ID=artist.ar_ID