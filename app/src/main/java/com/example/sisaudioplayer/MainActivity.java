package com.example.sisaudioplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sisaudioplayer.adapter.AudioAdapter;
import com.example.sisaudioplayer.data.ModelAudio;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    ArrayList<ModelAudio> audioArrayList;
    MediaPlayer mediaPlayer;
    RecyclerView recyclerView;
    double current_pos, total_duration;
    TextView current, total, audio_name;
    ImageView prev, next, pause;
    SeekBar seekBar;
    int audio_index = 0;
    public static final int PERMISSION_READ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(checkPermission()) {
            setAudio();
        }

    }

    private void setAudio() {
        recyclerView = (RecyclerView) findViewById(R.id.audio_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        current = (TextView) findViewById(R.id.current);
        total = (TextView) findViewById(R.id.total);
        audio_name = (TextView) findViewById(R.id.audio_name);
        prev = (ImageView) findViewById(R.id.prev);
        next = (ImageView) findViewById(R.id.next);
        pause = (ImageView) findViewById(R.id.pause);
        seekBar = (SeekBar) findViewById(R.id.seekbar);

        audioArrayList = new ArrayList<ModelAudio>();
        mediaPlayer = new MediaPlayer();

        getAudioFiles();

        // seekbar change listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                current_pos = seekBar.getProgress();
                mediaPlayer.seekTo((int) current_pos);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audio_index++;
                if(audio_index < audioArrayList.size()) {
                    playAudio(audio_index);
                } else {
                    audio_index = 0;
                    playAudio(audio_index);
                }
            }
        });

        if(!audioArrayList.isEmpty()) {
            playAudio(audio_index);
            prevAudio();
            pauseAudio();
            nextAudio();
        }
    }

    // play audio file
    private void playAudio(int pos) {
        try {
            mediaPlayer.reset();
            // set file path
            mediaPlayer.setDataSource(this, audioArrayList.get(pos).getaudioUri());
            mediaPlayer.prepare();
            mediaPlayer.start();
            pause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
            audio_name.setText(audioArrayList.get(pos).getaudioTitle());
            audio_index = pos;
        } catch (Exception e) {
            e.printStackTrace();
        }

        setAudioProgress();
    }

    //set audio progress
    private void setAudioProgress() {
        //get the audio duration
        current_pos = mediaPlayer.getCurrentPosition();
        total_duration = mediaPlayer.getDuration();

        // display audio duration
        total.setText(timerConversion((long) total_duration));
        current.setText(timerConversion((long) current_pos));
        seekBar.setMax((int) total_duration);

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    current_pos = mediaPlayer.getCurrentPosition();
                    current.setText(timerConversion((long) current_pos));
                    seekBar.setProgress((int) current_pos);
                    handler.postDelayed(this, 1000);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        };

        handler.postDelayed(runnable, 1000);
    }

    private void prevAudio() {
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(audio_index > 0) {
                    audio_index--;
                    playAudio(audio_index);
                } else {
                    audio_index = audioArrayList.size() - 1;
                    playAudio(audio_index);
                }
            }
        });
    }

    private void nextAudio() {
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(audio_index < audioArrayList.size() - 1) {
                    audio_index++;
                    playAudio(audio_index);
                } else {
                    audio_index  = 0;
                    playAudio(audio_index);
                }
            }
        });
    }

    private void pauseAudio() {
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    pause.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                } else {
                    mediaPlayer.start();
                    pause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                }
            }
        });
    }

    // time conversion
    private String timerConversion(long total_duration) {
        String audioTime;
        int dur = (int) total_duration;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if(hrs > 0) {
            audioTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            audioTime = String.format("%02d:%02d", mns, scs);
        }

        return audioTime;
    }

    // fetch the audio file from the storage
    private void getAudioFiles() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        // looping through all rows and adding to list
        if(cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                ModelAudio modelAudio = new ModelAudio();
                modelAudio.setaudioTitle(title);
                modelAudio.setaudioArtist(artist);
                modelAudio.setaudioUri(Uri.parse(url));
                modelAudio.setaudioDuration(duration);
                audioArrayList.add(modelAudio);
            } while (cursor.moveToNext());
        }

        Log.d(TAG, ""+audioArrayList.size());

        AudioAdapter adapter = new AudioAdapter(this, audioArrayList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new AudioAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos, View v) {
                playAudio(pos);
            }
        });
    }

    private boolean checkPermission() {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_READ
            );
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_READ: {
                if(grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(getApplicationContext(), "Please allow storage permission", Toast.LENGTH_LONG).show();
                    } else {

                    }
                }
            }
        }
    }

    //release mediaplayer
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer!=null){
            mediaPlayer.release();
        }
    }
}