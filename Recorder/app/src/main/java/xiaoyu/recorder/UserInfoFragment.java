package xiaoyu.recorder;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import xiaoyu.recorder.AudioAdapter.*;

/**
 * Created by Xiaoyu on 9/15/2016.
 */
public class UserInfoFragment extends Fragment implements ResultCallback, AudioCallback {
    private ListView listView;
    private TextView tv_userName;
    private TextView tv_email;
    private Button btn_logout;
    private String audioDir;
    private ArrayList<String> audioList;
    private String username;
    private String email;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_info_fragment, container, false);
        listView = (ListView) view.findViewById(R.id.audioList);
        tv_userName = (TextView) view.findViewById(R.id.tv_uname);
        tv_email = (TextView)view.findViewById(R.id.tv_email);
        btn_logout = (Button)view.findViewById(R.id.btn_logout);

        //read file to get user info
        getUserInfo();
        tv_userName.setText(username);
        tv_email.setText("Email: " + email);

        //put file name into listview
        HashMap<String, BirdInfo> birdInfoHash = getBirdInfo();
        audioDir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/birdRec/birdSong";
        getAudioList();

        AudioAdapter audioAdapter= new AudioAdapter(getActivity(), R.layout.upload_item, audioList, this, this, birdInfoHash);
        listView.setAdapter(audioAdapter);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/birdRec";
                deleteFile(new File(dir + "/birdSong"));
                deleteFile(new File(dir + "/log"));
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
    public void deleteFile(File file)
    {
        if (!file.exists())
        {
            return;
        }else {
            if (file.isFile()) {
                file.delete();
                return;
            }
            if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0) {
                    file.delete();
                    return;
                }
                for (File f : childFile) {
                    deleteFile(f);
                }
                file.delete();
            }
        }
    }

    public void getUserInfo()
    {
        Scanner file = null;
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/birdRec/log/userLog.txt";
        try {
            file=new Scanner(new FileInputStream(dir));
            String userInfo[] = file.nextLine().split("##");
            username = userInfo[0];
            email = userInfo[1];
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public HashMap<String, BirdInfo> getBirdInfo()
    {
        HashMap<String, BirdInfo> birdInfoHash = new HashMap<String, BirdInfo>();
        Scanner file = null;
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/birdRec/log/resultLog.txt";
        try {
            FileInputStream fileInputStream = new FileInputStream(dir);
            file=new Scanner(fileInputStream);
            while(file.hasNextLine())
            {
                String line = file.nextLine();
                String birdInfo[] = line.split("##");
                if(birdInfo != null) {
                    //System.out.println("Hahahahah"+line+"  length:"+ birdInfo.length);
                    BirdInfo bird = new BirdInfo();
                    if (birdInfo.length > 2) {
                        bird.setRecognised(true);
                        bird.setDate(birdInfo[1]);
                        bird.setId(Integer.parseInt(birdInfo[2]));
                        bird.setName(birdInfo[3]);
                        bird.setConfident(Double.parseDouble(birdInfo[4]));
                        bird.setDescription(birdInfo[5]);
                    } else {
                        bird.setRecognised(false);
                        //bird.setDate(birdInfo[1]);
                    }
                    // in form <fileName, BirdInfo>
                    birdInfoHash.put(birdInfo[0], bird);
                }

            }
            fileInputStream.close();
            file.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return birdInfoHash;
    }

    public void getAudioList()
    {
        audioList = new ArrayList<String>();
        File file = new File(audioDir);
        File audios[] = file.listFiles();
        if(audios != null)
        {
            for(int i = 0; i<audios.length; i++)
            {
                audioList.add(audios[i].getName());
            }
        }
    }

    @Override
    public void audioClick(View v, String filename) {
        String filedir = audioDir+"/"+filename;
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filedir);
            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Toast.makeText(getActivity(), "click item", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void resultClick(View v, String filename, BirdInfo birdInfo) {

        popup(v, birdInfo);
        //Toast.makeText(getActivity(), "click item 2", Toast.LENGTH_SHORT).show();
    }

    public void popup(View view, BirdInfo birdInfo)
    {
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_result, null);
        TextView tv_name = (TextView) contentView.findViewById(R.id.tv_pop_name);
        ImageView img_bird = (ImageView) contentView.findViewById(R.id.img_pop);
        TextView tv_confident = (TextView) contentView.findViewById(R.id.tv_pop_confidence);
        TextView tv_description = (TextView) contentView.findViewById(R.id.tv_pop_description);

        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.HALF_UP);
        String percent = nf.format(birdInfo.getConfident());

        tv_name.setText(birdInfo.getName());
        tv_confident.setText(percent);

        tv_description.setText(birdInfo.getDescription());
        tv_description.setMovementMethod(ScrollingMovementMethod.getInstance());

        img_bird.setImageBitmap(getImage("b"+ birdInfo.getId()+".jpg"));

        final PopupWindow popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // if return true, can't dismiss
                return false;
            }
        });
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_login_div));
        //popupWindow.showAsDropDown(view);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 100);
    }

    public Bitmap getImage(String fileName)
    {
        Bitmap img = null;
        AssetManager am = getActivity().getAssets();
        try {
            InputStream is = am.open(fileName);
            img = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }
}
