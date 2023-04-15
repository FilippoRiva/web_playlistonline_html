SELECT ar_name, ar_ID, a_name, a_date, a_ID, s_title, s_ID
FROM artist
         JOIN album a on artist.ar_ID = a.a_artist_ID
         JOIN song s on a.a_ID = s.s_album_ID
WHERE s_owner= ?
ORDER BY ar_name, a_name, s_title