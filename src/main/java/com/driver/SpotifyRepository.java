package com.driver;

import java.util.*;

import org.springframework.aop.target.dynamic.AbstractRefreshableTargetSource;
import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        users.add(new User(name, mobile));
        return users.get(users.size()-1);
    }

    public Artist createArtist(String name) {
        artists.add(new Artist(name));
        return artists.get(artists.size()-1);
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = null;
        for(Artist a : artists){
            // if artist exists
            if(a.getName().equals(artistName)){
                artist = a;
                break;
            }
        }
        if(artist == null) {
            artists.add(new Artist(artistName));
            artist = artists.get(artists.size()-1);
            // HashMap<Artist, List<Album>> artistAlbumMap;
        }
        albums.add(new Album(title));
        Album album = albums.get(albums.size()-1);
        boolean albumAdded = false;
        for(Artist a : artistAlbumMap.keySet()){
            if(a.getName().equals(artist.getName())){
                List<Album> oldlist = artistAlbumMap.get(a);
                oldlist.add(album);
                albumAdded = true;
            }
        }
        if(!albumAdded){
            List<Album> newlist = new ArrayList<>();
            newlist.add(album);
            artistAlbumMap.put(artist, newlist);
        }
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        // Find album
        Album album = null;
        for (Album a : albums) {
            if (a.getTitle().equals(albumName)) {
                album = a;
                break;
            }
        }
        if(album == null){
            throw new Exception("Album does not exist");
        }

        songs.add(new Song(title, length));
        Song s = songs.get(songs.size() - 1);
        // HashMap<Album, List<Song>> albumSongMap
        boolean songAdded = false;
        for(Album a : albumSongMap.keySet()) {
            if(a.getTitle().equals(album.getTitle())){
                List<Song> oldList = albumSongMap.get(a);
                oldList.add(s);
                songAdded = true;
            }
        }
        if(!songAdded){
            List<Song> newList = new ArrayList<>();
            newList.add(s);
            albumSongMap.put(album, newList);
        }
        return s;

    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        playlists.add(new Playlist(title));
        Playlist playlist = playlists.get(playlists.size()-1);
        List<Song> songlist = new ArrayList<>();
        for(Song s : songs){
            if(s.getLength() == length){
                songlist.add(s);
            }
        }
        playlistSongMap.put(playlist, songlist);
        boolean userFound = false;
        for(User u : users){
            if(u.getMobile().equals(mobile)){
                creatorPlaylistMap.put(u, playlist);
                userFound = true;
                break;
            }
        }
        if(userFound) return playlist;
        throw new Exception("User does not exist");
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        playlists.add(new Playlist(title));
        Playlist playlist = playlists.get(playlists.size()-1);
        List<Song> songlist = new ArrayList<>();
        for(String s : songTitles){
            Song song = new Song(s);
            songlist.add(song);
        }
        playlistSongMap.put(playlist, songlist);
        boolean userFound = false;
        for(User u : users){
            if(u.getMobile().equals(mobile)){
                creatorPlaylistMap.put(u, playlist);
                userFound = true;
                break;
            }
        }
        if(userFound) return playlist;
        throw new Exception("User does not exist");
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        // Find playlist
        Playlist playlist = null;
        for(Playlist p : playlists){
            if(p.getTitle().equals(playlistTitle)){
                playlist = p;
                break;
            }
        }
        if(playlist == null){
            throw new Exception("Playlist does not exist");
        }
        // Find user
        User user = null;
        for(User u : users){
            if(u.getMobile().equals(mobile)){
                user = u;
            }
        }
        if(user == null){
            throw new Exception("User does not exist");
        }
        // Check if user is already a listen or creator to that playlist
        for(User u : creatorPlaylistMap.keySet()){
            if(u.getMobile().equals(user.getMobile())){
                break;
            }
        }
        for(Playlist p : playlistListenerMap.keySet()){
            if(p.getTitle().equals(playlistTitle)){
                List<User> userlist = playlistListenerMap.get(p);
                boolean listenerFound = false;
                for(User u : userlist){
                    if(u.getMobile().equals(mobile)){
                        listenerFound = true;
                        break;
                    }
                }
                if(!listenerFound){
                    playlistListenerMap.put(playlist, userlist);
                }
            }
        }
        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        // Find the user
        User user = null;
        for(User u : users){
            if(u.getMobile().equals(mobile)){
                user = u;
                break;
            }
        }
        if(user == null){
            throw new Exception("User does not exist");
        }
        // Find the song
        Song song = null;
        for(Song s : songs){
            if(s.getTitle().equals(songTitle)){
                song = s;
                break;
            }
        }
        if(song == null){
            throw new Exception("Song does not exist");
        }
        //The user likes the given song. The corresponding artist of the song gets auto-liked
        // HashMap<Song, List<User>> songLikeMap;
        boolean alreadyLikedByUser = false;
        for(Song s : songLikeMap.keySet()){
            if(s.getTitle().equals(song.getTitle())){
                List<User> userlist = songLikeMap.get(s);
                for(User u : userlist){
                    if(u.getMobile().equals(mobile)){
                        alreadyLikedByUser = true;
                        break;
                    }
                }
            }
        }
        if(!alreadyLikedByUser){
            song.setLikes(1);
            // HashMap<Artist, List<Album>> artistAlbumMap;
            // HashMap<Album, List<Song>> albumSongMap;
            Album album = null;
            for(Album a : albumSongMap.keySet()){
                for(Song s : albumSongMap.get(a)){
                    if(s.getTitle().equals(songTitle)){
                        album = a;
                        break;
                    }
                }
            }
            for(Artist a : artistAlbumMap.keySet()){
                boolean likeGiven = false;
                for(Album al : artistAlbumMap.get(a)){
                    if(al.getTitle().equals(album.getTitle())){
                        a.setLikes(1);
                        likeGiven = true;
                        break;
                    }
                }
                if(likeGiven){
                    break;
                }
            }
        }
        return song;
    }

    public String mostPopularArtist() {
        int maxLike = Integer.MIN_VALUE;
        Artist artistWithMostLikes = null;
        for(Artist a : artists){
            if(a.getLikes() > maxLike){
                maxLike = a.getLikes();
                artistWithMostLikes = a;
            }
        }
        return artistWithMostLikes.getName();
    }

    public String mostPopularSong() {
        int maxLike = Integer.MIN_VALUE;
        Song songWithMostLikes = null;
        for(Song s : songs){
            if(s.getLikes() > maxLike){
                maxLike = s.getLikes();
                songWithMostLikes = s;
            }
        }
        return songWithMostLikes.getTitle();
    }
}
