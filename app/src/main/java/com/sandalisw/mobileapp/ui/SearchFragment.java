package com.sandalisw.mobileapp.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sandalisw.mobileapp.R;
import com.sandalisw.mobileapp.adapters.SearchItemAdapter;
import com.sandalisw.mobileapp.models.Song;
import com.sandalisw.mobileapp.viewmodels.SongViewModel;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;

public class SearchFragment extends Fragment implements SearchItemAdapter.SongResultSelection {


    private static final String TAG = "SearchFragment";
    private View view;
    private SongViewModel mSongViewModel;
    private SearchItemAdapter searchItemAdapter;
    private RecyclerView recyclerView;
    private List<Song> dataList;
    private IMainActivity mIMainActivity;
    private MediaMetadataCompat mSelectedMedia;
    private List<MediaMetadataCompat> mLibrary = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mSongViewModel = ViewModelProviders.of(this).get(SongViewModel.class);
        initSearchView();
        initRecyclerView(view);

    }

    private void initRecyclerView(View view){
        recyclerView = view.findViewById(R.id.search_results_recyclerview);
        searchItemAdapter = new SearchItemAdapter(getActivity(),this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(searchItemAdapter);
    }


    private void subscribeObservers(String s){
        mSongViewModel.searchSong(s).observe(this, new Observer<List<Song>>() {
            @Override
            public void onChanged(@Nullable List<Song> songs) {
                searchItemAdapter.setDataList(songs);
                dataList = songs;
            }
        });
    }
    private void initSearchView(){
        final SearchView searchview = view.findViewById(R.id.search_view);

        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG, "onQueryTextSubmit: "+s);
                subscribeObservers(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                return false;
            }
        });
    }

    private void addToMediaLibrary(Song song){
        mLibrary.clear();
        MediaMetadataCompat mData = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.getId())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,song.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,song.getThumbnailUrl())
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.getSong_url())
                .build();
        mLibrary.add(mData);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIMainActivity = (IMainActivity) getActivity();
    }

    @Override
    public void onSongClick(int position) {
        addToMediaLibrary(dataList.get(position));
        mIMainActivity.getMyApplication().setMediaItems(mLibrary);
        //adapter should highlight the selected song
        //songAdapter.setSelectedIndex(position);
        mSelectedMedia= mLibrary.get(0);
        mIMainActivity.onMediaSelected(mSelectedMedia);
    }
}
