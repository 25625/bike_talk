package com.example.talk_bike;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppComponentFactory;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity {

    private static int MIC_PERMISSION = 200;

    CheckBox enable, visible;
    TextView ble_name;
    ImageView ble_image;
    ListView list_view;

    private BluetoothAdapter BA;
    private Set<BluetoothDevice> paired_devices;

    MediaPlayer player;
    MediaRecorder recoder;

    Button play,pause,stop,mic_Record,mic_Play,mic_Stop;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_main);
        enable = findViewById(R.id.enable);
        visible = findViewById(R.id.visible);
        ble_image = findViewById(R.id.bluetooth_image);
        ble_name = findViewById(R.id.bluetooth_Textview);
        list_view = findViewById(R.id.List_view_bluetooth);
        play = findViewById(R.id.play);
        stop = findViewById(R.id.stop);
        pause = findViewById(R.id.pause);
        mic_Play = findViewById(R.id.mic_play);
        mic_Record = findViewById(R.id.mic_record);
        mic_Stop = findViewById(R.id.mic_stop);

        ble_name.setText(GetLocalBluetootjName());

        BA = BluetoothAdapter.getDefaultAdapter();

        if (BA == null) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
        }

        if (BA.isEnabled()) {
            enable.setChecked(true);
        }

        if (mic_check()) {
            get_mic_grant();
        }

        enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            //@SuppressLint("MissingPermission")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    BA.isEnabled();
                    Toast.makeText(MainActivity.this, "Bluetooth is diabled", Toast.LENGTH_SHORT).show();
                } else {

                    Intent intentOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intentOn, 0);
                    Toast.makeText(MainActivity.this, "Ble is Turned On", Toast.LENGTH_SHORT).show();

                }
            }
        });

        visible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent getvisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(getvisible, 0);
                Toast.makeText(MainActivity.this, "visible for 2 min", Toast.LENGTH_SHORT).show();
                ;

            }
        });

        ble_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list();
            }
        });

    }

    private void list() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        paired_devices = BA.getBondedDevices();

        ArrayList devices = new ArrayList();

        for (BluetoothDevice bt : paired_devices) {
            devices.add(bt.getName());
        }
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, devices);
        list_view.setAdapter(adapter);
    }

    public String GetLocalBluetootjName() {

        if (BA == null) {
            BA = BluetoothAdapter.getDefaultAdapter();
        }

        @SuppressLint("MissingPermission") String name = BA.getName();

            if (name == null){
                name = BA.getAddress();
            }
            return name;
        }

        public void mus_Play(View v) {
            if (player == null){
                player = MediaPlayer.create(this,R.raw.song);
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Rel();
                    }
                });
            }
            player.start();
        }

        public void mus_Pause(View v) {
            if(player != null) {
                player.pause();
            }
        }

        public void mus_Stop(View v) {
            if(player != null) {
                player.stop();
            }
        }

        private void Rel(){
            if(player != null){
                player.stop();
                player.release();
                player = null;
            }
        }

        public void mic_Record(View v) {
            try {
                recoder = new MediaRecorder();
                recoder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recoder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recoder.setOutputFile(get_rec_file());
                recoder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                recoder.prepare();
                recoder.start();

                Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void mic_Stop(View V) {
            recoder.stop();
            recoder.release();
            recoder = null;

            Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show();
        }

    public void mic_Play(View v) {
        try {
                player = new MediaPlayer();
                player.setDataSource(get_rec_file());
                player.prepare();
                player.start();
                /*player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        player.stop();
                        player = null;
                    }
                });*/

            Toast.makeText(this, "Recording Playing", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean mic_check() {
            if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
                return true;
            }
            else {
                return false;
            }
        }

        private void get_mic_grant() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION);
            }
        }

        private String get_rec_file() {
            ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
            File voice_file = wrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            File file = new File(voice_file, "voice_record"+".mp3");
            return file.getPath();
        }

        private void Rel_mic() {
            recoder.stop();
            recoder.release();
            recoder = null;
        }

    protected void onStop() {
        super.onStop();
        Rel();
        Rel_mic();
    }
}
