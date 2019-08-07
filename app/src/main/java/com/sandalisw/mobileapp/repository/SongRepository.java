package com.sandalisw.mobileapp.repository;

import android.arch.lifecycle.LiveData;

import com.sandalisw.mobileapp.models.Artist;
import com.sandalisw.mobileapp.models.Song;
import com.sandalisw.mobileapp.models.User;
import com.sandalisw.mobileapp.requests.RequestApiClient;

import java.util.List;

public class SongRepository {
    private RequestApiClient mApiClient;
    private static SongRepository instance;

    private SongRepository(){
        mApiClient = RequestApiClient.getInstance();
    }

    public static SongRepository getInstance(){
        if(instance == null){
            instance =  new SongRepository();
        }
        return instance;
    }

    public LiveData<List<Song>> getSongs(){
        return mApiClient.getSongs();
    }

    public boolean registerUser(User user){
        return  mApiClient.registerUser(user);
    }

    public LiveData<List<Artist>> getArtists() {
        return mApiClient.getArtists();
    }
}
