package xiaoyu.recorder;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Xiaoyu on 10/10/2016.
 */
public class AudioAdapter extends ArrayAdapter<String> {
// implements View.OnClickListener
    private int resourceId;
    private ResultCallback resultCallback;
    private AudioCallback audioCallback;
    private HashMap<String, BirdInfo> birdInfoHash;
    public AudioAdapter(Context context, int resource, List<String> objects, ResultCallback resultCallback, AudioCallback audioCallback, HashMap<String, BirdInfo> birdInfoHash) {
        super(context, resource, objects);
        this.resourceId = resource;
        this.resultCallback = resultCallback;
        this.audioCallback = audioCallback;
        this.birdInfoHash = birdInfoHash;
    }

    public interface ResultCallback
    {
        public void resultClick(View v, String filename, BirdInfo birdInfo);
    }
    public interface AudioCallback
    {
        public void audioClick(View v, String filename);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String audioName = getItem(position);
        ViewHolder viewHolder;
        View view;
//        if(convertView == null)
//        {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(getContext()).inflate(resourceId,null);
            viewHolder.tv_audio_name = (TextView) view.findViewById(R.id.tv_fileName);
            viewHolder.tv_audio_state = (TextView) view.findViewById(R.id.tv_audio_state);
            view.setTag(viewHolder);

//        }else
//        {
//            view = convertView;
//            viewHolder = (ViewHolder)view.getTag();
//        }
        viewHolder.tv_audio_name.setText(audioName);

        final BirdInfo birdInfo = birdInfoHash.get(audioName.replace(".wav" , ""));
        if(!birdInfo.isRecognised())
        {
            viewHolder.tv_audio_state.setText("Expert");
            viewHolder.tv_audio_state.setBackgroundColor(Color.parseColor("#cc67f94d"));
        }else
        {
            // if bird have been recognised, set onclick listener
            viewHolder.tv_audio_state.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    resultCallback.resultClick(view, audioName, birdInfo);
                }
            });
        }


        viewHolder.tv_audio_name.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                audioCallback.audioClick(view, audioName);
            }
        });

        return view;
    }

//    @Override
//    public void onClick(View view) {
//        resultCallback.resultClick(view);
//
//    }
    class ViewHolder
    {
        TextView tv_audio_name;
        TextView tv_audio_state;
    }
}
