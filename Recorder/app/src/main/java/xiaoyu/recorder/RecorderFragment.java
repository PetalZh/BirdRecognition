package xiaoyu.recorder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.NoiseSuppressor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Xiaoyu on 8/28/2016.
 */
public class RecorderFragment extends Fragment {
    private RecordTimerView timerView;
    private TextView textView;
    private Button button;
    private int totalProgress;
    private int currentProgress;
    private boolean isLongClick = false;
    private String dir;
    private String fileName;
    private String fileDir;
    private String target;
    private Location location;
    private GeoLocation geoLocation;
    private String uploadResponse;
    private long timestamp;
    private String uploadState;
    private String email;

    private int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_RECOGNITION;
    //private int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private int SAMPLE_RATE = 44100;
    private int CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
    private int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private int minBufferSize;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.record_timer, container, false);
        getUserInfo();
        textView = (TextView) view.findViewById(R.id.tv_record);
        button = (Button) view.findViewById(R.id.btn_record);
        geoLocation = new GeoLocation();
        location = geoLocation.getLocation(getActivity());
        //initialise the timer
        initVariable();
        initView(view);
        final ProgressRunnable progress = new ProgressRunnable();

        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                initVariable();
                textView.setText("Recording...");
                record();
                Thread thread = new Thread(progress);
                thread.start();
                isLongClick = true;
                return true;
            }
        });

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isLongClick = true) {
                    switch (motionEvent.getActionMasked()) {
                        case MotionEvent.ACTION_UP: {
                            isLongClick = false;
                            textView.setText("Audio Uploading...");
                            AudioProcess audioProcess = new AudioProcess();
                            audioProcess.pcmToWav(fileDir, target, minBufferSize);

                            Toast.makeText(getActivity(),"Uploading audio",Toast.LENGTH_LONG).show();
                            Thread thread = new Thread(new Upload());
                            thread.start();

                            Thread listener = new Thread(new StateListener());
                            listener.start();

                            textView.setText("Long click the button to record");
                        }
//                        case MotionEvent.ACTION_MOVE:
//                        {
//                            stopRecord();
//                            textView.setText("Press the Button to record");
//                            isLongClick = false;
//                        }
                    }
                }
                return false;
            }
        });
        return view;
    }
    public void getUserInfo()
    {
        Scanner file = null;
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/birdRec/log/userLog.txt";
        try {
            file=new Scanner(new FileInputStream(dir));
            String userInfo[] = file.nextLine().split("##");
            //username = userInfo[0];
            email = userInfo[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void record() {
        Recorder recorder = new Recorder();
        Thread thread = new Thread(recorder);
        thread.start();
    }

    private void writeToFile(int bufferSize, AudioRecord record) {
        dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/birdRec";
        String audioDir = dir+"/birdSong";
        File file = new File(audioDir);
        if (!file.exists()) {
            file.mkdirs();
        }

//        target = fileName + "/tweet.wav";
//        fileName = fileName + "/tweet.pcm";

        //timestamp as audio name
        timestamp = System.currentTimeMillis();
        target = audioDir + "/"+ timestamp+".wav";
        fileName = String.valueOf(timestamp);
        fileDir = audioDir +"/"+ timestamp+".pcm";

        FileOutputStream os = null;
        BufferedOutputStream bos = null;
        DataOutputStream dos = null;
        try {
            os = new FileOutputStream(fileDir);
            bos = new BufferedOutputStream(os);
            dos = new DataOutputStream(bos);

            short[] buffer = new short[bufferSize];
            while (isLongClick) {
                int bufferReadResult = record.read(buffer, 0, buffer.length);
                for (int i = 0; i < bufferReadResult; i++) {
                    dos.writeShort(Short.reverseBytes(buffer[i]));
                }
            }
            record.stop();
            dos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initVariable() {
        totalProgress = 30;
        currentProgress = 0;
    }

    private void initView(View view) {
        timerView = (RecordTimerView) view.findViewById(R.id.timer_view);
    }

    public void requestGPS(LocationManager locationManager)
    {
        if(!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
        {
            Toast.makeText(getActivity(), "Please turn on GPS", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setMessage("Please turn on GPS");
            dialog.setPositiveButton("Yes",
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            // Guide user setting GPS
                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 0); // return to original page
                        }
                    });
            dialog.setNeutralButton("No",
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                        }
                    } );
            dialog.show();
        }
    }

    class ProgressRunnable implements Runnable
    {
        @Override
        public void run() {
            while(totalProgress > currentProgress && isLongClick==true)
            {
                currentProgress ++;
                timerView.setProgress(currentProgress);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Recorder implements Runnable
    {
        @Override
        public void run() {
            minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, FORMAT);
            AudioRecord record = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL, FORMAT, minBufferSize);

            if(NoiseSuppressor.isAvailable())
            {
                NoiseSuppressor.create(record.getAudioSessionId());
            }

            //System.out.println(NoiseSuppressor.isAvailable());
            record.startRecording();

            writeToFile(minBufferSize, record);
        }
    }
    class StateListener implements Runnable
    {
        @Override
        public void run() {
            while(true)
            {
                if(uploadState != null)
                {
                    if (uploadState.equals("Upload Success!"))
                    {
                        Intent intent = new Intent(getActivity(), DisplayResultActivity.class);
                        intent.putExtra("result",uploadResponse);
                        intent.putExtra("fileName", fileName);
                        uploadState = null;
                        uploadResponse = null;
                        startActivity(intent);
                        break;
                    }
                    if(uploadState.equals("Network error!"))
                    {
                        uploadState = null;
                        //delete the no response file
                        File file = new File(target);
                        if(!file.exists())
                        {
                            try {
                                Thread.sleep((long)2000);
                                continue;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }else{
                            file.delete();
                            Looper.prepare();
                            Toast.makeText(getActivity(), "Network error!", Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }
                        break;
                    }
                }

            }

        }
    }
    class Upload implements Runnable
    {
        @Override
        public void run() {
            //get the current time
//            textView.setText("Audio Uploading...");
            long l = System.currentTimeMillis();
            Date date = new Date(l);
            SimpleDateFormat formater1 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat formater2 = new SimpleDateFormat("HH:mm:ss");
            String longitude = "";
            String latitude = "";
            String altitude = "";
            String address = "";
            if(location != null)
            {
                longitude += String.valueOf(location.getLongitude());
                latitude += String.valueOf(location.getLatitude());
                altitude += String.valueOf(location.getAltitude());
                address += geoLocation.getGeocoder(location.getLongitude(), location.getLatitude(),getActivity());
                System.out.println("address: "+address);
            }

            //set parameters of uploading
            Map<String, String> params = new HashMap<String,String>();
            params.put("longitude", longitude);
            params.put("latitude",latitude);
            params.put("evaluation",altitude);
            params.put("submitted_by",email);
            params.put("date",formater1.format(date));
            params.put("time",formater2.format(date));
            params.put("client","android");
            params.put("user_comment","This is recored in School in the spring.");
            params.put("location",address);

            //http://115.146.90.254:5001
            //http://192.168.31.236:5001
            String request_URL="http://115.146.90.254:5001/upload_audio";

            uploadResponse = UploadUtil.uploadFile(new File(target),request_URL,params,"file");
            System.out.println("response:" + uploadResponse);
            if(uploadResponse == null)
            {
                uploadState = "Network error!";
            }else
            {
                uploadState = "Upload Success!";
            }
        }
    }
}
