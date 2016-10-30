package xiaoyu.recorder;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Xiaoyu on 10/6/2016.
 */
public class DisplayResultActivity extends Activity {
    private int top_bird_id;
    private String top_bird_name;
    private LinearLayout linear_confident;
    private LinearLayout linear_unconfident;
    private Button expert_button;
    private ImageView img_top_bird;
    private TextView tv_top_bird_name;
    private TextView tv_bird_confidence;
    private TextView tv_other_possible;
    private ListView other_result;
    private String fileName;
    private ArrayList<BirdInfo> resultList = new ArrayList<BirdInfo>();
    private boolean state;
    //private Context context = this;
    //private String url;
    //private double confidence;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        String message = getIntent().getStringExtra("result");
        fileName = getIntent().getStringExtra("fileName");

        img_top_bird = (ImageView)findViewById(R.id.img_top_bird);
        tv_top_bird_name = (TextView)findViewById(R.id.tv_bird_name);
        tv_bird_confidence = (TextView)findViewById(R.id.tv_confidence);
        tv_other_possible = (TextView)findViewById(R.id.tv_otherPossible);
        linear_confident = (LinearLayout)findViewById(R.id.linear_confident);
        linear_unconfident = (LinearLayout)findViewById(R.id.linear_unconfident);
        other_result = (ListView)findViewById(R.id.birdList);
        expert_button = (Button)findViewById(R.id.expert);

        try {
            JSONParser parser =new JSONParser();
            JSONObject obj = (JSONObject)parser.parse(message);
            JSONObject codeDict = (JSONObject)obj.get("bird_code_dictionary");
            double confidence = (Double)obj.get("confidence");
            final String description =(String) ((JSONObject)obj.get("wiki_brief")).get("summary");
            //System.out.println(description);

            if(confidence > 0.52)
            {
                state = true;
                JSONArray rank = (JSONArray)obj.get("estimation_rank");
                displayConfidentResult(rank, codeDict, confidence,description);

                img_top_bird.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        showDescription(view, description);
                    }
                });

            }else
            {
                state = false;
                linear_unconfident.setVisibility(View.VISIBLE);
                expert_button.setVisibility(View.VISIBLE);
                //expert_button.setOnClickListener();
            }

            writeFile(confidence,description, state);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
    public void displayConfidentResult(JSONArray rank, JSONObject codeDict, Double confidence, String description)
    {
        linear_confident.setVisibility(View.VISIBLE);
        tv_other_possible.setVisibility(View.VISIBLE);
        other_result.setVisibility(View.VISIBLE);
        for(int i =0; i<rank.size();i++)
        {
            if(i ==0)
            {
                top_bird_id = (int)((long)((JSONArray)rank.get(i)).get(0));
                top_bird_name = (String)codeDict.get(String.valueOf(top_bird_id));
            }else
            {
                BirdInfo bird = new BirdInfo();
                JSONArray item= (JSONArray) rank.get(i);
                int bird_id = (int)((long)item.get(0));
                String bird_name = (String)codeDict.get(String.valueOf(bird_id));
                bird.setId(bird_id);
                bird.setName(bird_name);
                resultList.add(bird);
            }
        }

        //img_top_bird.setImageResource(R.drawable.b0);
        img_top_bird.setImageBitmap(getImage("b" + top_bird_id + ".jpg"));

        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.HALF_UP);
        String percent = nf.format(confidence);

        tv_top_bird_name.setText(top_bird_name);
        tv_bird_confidence.setText(percent+" confident");

        //display other result
        other_result = (ListView)findViewById(R.id.birdList);
        BirdAdapter birdAdapter= new BirdAdapter(DisplayResultActivity.this, R.layout.possible_item, resultList);

        other_result.setAdapter(birdAdapter);
    }

    public void showDescription(View view, String description)
    {
        View contentView = LayoutInflater.from(this).inflate(R.layout.popup_description, null);
        TextView textView = (TextView) contentView.findViewById(R.id.tv_description);
        textView.setText(description);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
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
        AssetManager am = getAssets();
        try {
            InputStream is = am.open(fileName);
            img = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    public void writeFile(Double confident, String description, boolean state)
    {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/birdRec/log";
        File file = new File(dir);
        if(!file.exists())
        {
            file.mkdirs();
        }

        Date date = new Date(Long.parseLong(fileName));
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
        PrintWriter outputStream = null;
        try {
            outputStream = new PrintWriter(new FileOutputStream(dir+"/resultLog.txt",true),true);
            outputStream.print(fileName + "##");
            if(state)
            {
                // confident
                outputStream.print(formater.format(date) + "##");
                outputStream.print(top_bird_id + "##");
                outputStream.print(top_bird_name + "##");
                outputStream.print(confident + "##");
                outputStream.print(description.replace("\n", ""));
            }else
            {
                // not confident
                outputStream.print(formater.format(date));
            }
            outputStream.println();
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    class BirdAdapter extends ArrayAdapter<BirdInfo>
    {
        private int resourceId;
        private ImageView img_bird;
        private TextView tv_name;
        public BirdAdapter(Context context, int resource, List<BirdInfo> objects) {
            super(context, resource, objects);
            this.resourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            BirdInfo bird = getItem(position);
            ViewHolder viewHolder;
            View view;
            if(convertView == null)
            {
                viewHolder = new ViewHolder();
                view = LayoutInflater.from(getContext()).inflate(resourceId,null);
                viewHolder.img_bird = (ImageView)view.findViewById(R.id.img_other);
                viewHolder.tv_name = (TextView) view.findViewById(R.id.tv_other_name);
                view.setTag(viewHolder);

            }else
            {
                view = convertView;
                viewHolder = (ViewHolder)view.getTag();
            }
            //viewHolder.img_bird.setImageResource(R.drawable.a2);
            viewHolder.img_bird.setImageBitmap(getImage("b"+bird.getId()+".jpg"));
            viewHolder.tv_name.setText(bird.getName());
            return view;
        }

    }
    class ViewHolder
    {
        ImageView img_bird;
        TextView tv_name;
    }
}
