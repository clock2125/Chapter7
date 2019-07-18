package com.bytedance.videoplayer;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

public class MediaPlayerActivity extends AppCompatActivity {
    private static final String TAG = MediaPlayerActivity.class.getSimpleName();
    private static final String MEDIA_PROGRESS = "media_progress";

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;

    private Handler handler = new Handler();
    private Runnable updateSeekBar;

    private TextView tv;
    private Button btn;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        surfaceView = findViewById(R.id.surface_view);
        mediaPlayer = new MediaPlayer();
        initSurfaceView();
        initSeekBar();

        tv = findViewById(R.id.tv);
        btn = findViewById(R.id.play_button);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn.getText()=="PLAY"){
                    mediaPlayer.start();
                    btn.setText("PAUSE");
                    handler.post(updateSeekBar);
                }
                else{
                    mediaPlayer.pause();
                    btn.setText("PLAY");
                }
            }
        });



        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(updateSeekBar, 100);
            }
        };

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
                handler.post(updateSeekBar);
                mediaPlayer.setLooping(false);
            }
        });
        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                System.out.println(percent);
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MEDIA_PROGRESS, mediaPlayer.getCurrentPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            int progress = savedInstanceState.getInt(MEDIA_PROGRESS);
            seekBar.setProgress(progress);
            mediaPlayer.seekTo(progress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateSeekBar);
        mediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(updateSeekBar);
        mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initSurfaceView() {
        try {
            Uri uri = getIntent().getData();
            if (uri == null) {
                mediaPlayer.setDataSource(getResources().openRawResourceFd(R.raw.bytedance));
            } else {
                mediaPlayer.setDataSource(uri.getPath());
            }


            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    mediaPlayer.setDisplay(surfaceHolder);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                }
            });
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSeekBar() {
        seekBar = findViewById(R.id.seek_bar);
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {@Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(progress);
                mediaPlayer.start();

            }
            int position = mediaPlayer.getCurrentPosition();
            int second = position/1000;
            int min = second/60;
            second-=60*min;
            tv.setText(String.format("%02d:%02d",min,second));
        }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }



}
