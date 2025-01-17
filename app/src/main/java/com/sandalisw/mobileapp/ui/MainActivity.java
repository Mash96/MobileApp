package com.sandalisw.mobileapp.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sandalisw.mobileapp.MediaApplication;
import com.sandalisw.mobileapp.R;
import com.sandalisw.mobileapp.adapters.TabAdapter;
import com.sandalisw.mobileapp.client.MediaBrowserHelper;
import com.sandalisw.mobileapp.client.MediaBrowserHelperCallback;
import com.sandalisw.mobileapp.services.MediaService;
import com.sandalisw.mobileapp.viewmodels.SongViewModel;


public class MainActivity extends AppCompatActivity implements IMainActivity, MediaBrowserHelperCallback {

    private static final String TAG = "MainActivity";

    private SeekbarBroadcastReceiver mSeekbarBroadcast;
    private TabLayout tabLayout;
    private TabAdapter tabAdapter;
    private ViewPager viewPager;
    private boolean mIsPlaying;

    private MediaBrowserHelper mMediaBrowserHelper;
    private MediaApplication mMediaApplication;
    private MediaMetadataCompat mediaMetadata;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //onetime signup check
        SharedPreferences sp = getSharedPreferences("User_Data",MODE_PRIVATE);
        Log.d(TAG, "onCreate: "+sp.getString("userId","0"));
        Log.d(TAG, "onCreate: "+sp.getBoolean("loggedIn",false));
        boolean b = sp.getBoolean("loggedIn",false);
        if(!b){
            Intent i = new Intent(this,ConsentActivity.class);
            startActivity(i);
        }

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.pager);
        tabAdapter = new TabAdapter(getSupportFragmentManager());

        tabAdapter.addFragment(new HomeFragment(), "Home");
        tabAdapter.addFragment(new SearchFragment(), "Search");
        tabAdapter.addFragment(new PlayerFragment(), "Player");
        tabAdapter.addFragment(new ProfileFragment(), "Profile");


        viewPager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(viewPager);
        setUpTabIcons();

        if (findViewById(R.id.media_player_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            MediaControllerFragment mediaControllerFragment = new MediaControllerFragment();

            mediaControllerFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.media_player_container, mediaControllerFragment).commit();
        }

        mMediaApplication = MediaApplication.getInstance();

        mMediaBrowserHelper = new MediaBrowserHelper(this, MediaService.class);
        mMediaBrowserHelper.setMediaBrowserHelperCallback(this);

    }

    private void setUpTabIcons(){
        tabLayout.getTabAt(0).setIcon(R.drawable.tab_home);
        tabLayout.getTabAt(1).setIcon(R.drawable.tab_search);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_player);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_profile);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaBrowserHelper.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaBrowserHelper.onStop();
    }

    @Override
    public void playPause() {
        if(mIsPlaying){
            mMediaBrowserHelper.getTransportControls().pause();
        }else{
            mMediaBrowserHelper.getTransportControls().play();
            mIsPlaying = true;
        }
    }

    @Override
    public MediaApplication getMyApplication() {
        return  mMediaApplication;
    }


    @Override
    public void onMediaSelected(MediaMetadataCompat mediaItem) {
        if(mediaItem != null){
            setMediadata(mediaItem);
            Log.d(TAG, "onMediaSelected: Called"+mediaItem.getDescription()+" mediaId "+mediaItem.getDescription().getMediaId());
            //mMediaBrowserHelper.subscribeToPlaylist(playlistId);
            mMediaBrowserHelper.getTransportControls().playFromMediaId(mediaItem.getDescription().getMediaId(),null);

        }else{
            Toast.makeText(this,"Select Something to play",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setMediadata(MediaMetadataCompat mData) {
        Log.d(TAG, "setMediadata: "+mData.getDescription().getTitle());
        mediaMetadata = mData;
    }

    @Override
    public MediaMetadataCompat getMediaData() {
        if(mediaMetadata == null){
            Log.d(TAG, "getMediaData: null data");
            mediaMetadata = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "aut porro officiis laborum odit ea laudantium corporis")
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,"xxxxxxxxxx x")
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,"https://i.redd.it/obx4zydshg601.jpg")
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,"https://via.placeholder.com/600/54176f" )
                    .build();
        }
        return mediaMetadata;
    }


    @Override
    public void onMetaDataChanged(MediaMetadataCompat metaData) {
        Log.d(TAG, "onMetaDataChanged: called");
        //New metadata for song
        if(metaData == null){
            return;
        }
        getMediaControllerFragment().setMediaData(metaData);
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.d(TAG, "onPlaybackStateChanged: called");
        mIsPlaying = state != null &&
                state.getState() == PlaybackStateCompat.STATE_PLAYING;
        Log.d(TAG, "onPlaybackStateChanged: iSplaying"+mIsPlaying);

        //update UI
        if(getMediaControllerFragment() != null){
            getMediaControllerFragment().setIsPlaying(mIsPlaying);
        }
        if(state.getState() == PlaybackStateCompat.STATE_STOPPED){
            getMediaControllerFragment().setIsPlaying(mIsPlaying);
        }

    }

    @Override
    public void onMediaControllerConnected(MediaControllerCompat mediaController) {
        getMediaControllerFragment().getMediaSeekbar().setMediaController(mediaController);
    }

    private MediaControllerFragment getMediaControllerFragment(){
        MediaControllerFragment mediaControllerFragment = (MediaControllerFragment)getSupportFragmentManager()
                .findFragmentById(R.id.media_player_container);
        if(mediaControllerFragment != null){
            return mediaControllerFragment;
        }
        return  null;
    }

    private void initSeekBarBroadcastReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.broadcast_seekbar_update));
        mSeekbarBroadcast = new SeekbarBroadcastReceiver();
        registerReceiver(mSeekbarBroadcast, intentFilter);
    }

    private class  SeekbarBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long seekProgress = intent.getLongExtra("SEEK_BAR_PROGRESS",0);
            long maxProgress = intent.getLongExtra("SEEK_BAR_MAX",0);
            if(!getMediaControllerFragment().getMediaSeekbar().isTracking()){
                getMediaControllerFragment().getMediaSeekbar().setProgress((int)seekProgress);
                getMediaControllerFragment().getMediaSeekbar().setMax((int)maxProgress);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSeekBarBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: called");
        super.onPause();
        if(mSeekbarBroadcast != null){
            unregisterReceiver(mSeekbarBroadcast);
        }
    }
}
