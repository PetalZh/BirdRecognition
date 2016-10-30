package xiaoyu.recorder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xiaoyu on 10/4/2016.
 */
public class LoginActivity extends Activity {
    private Button button;
    private EditText et_email;
    private EditText et_password;
    private TextView reg;
    private TextView info;
    private String result;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_email = (EditText) findViewById(R.id.login_email);
        et_password = (EditText) findViewById(R.id.login_pass);
        reg = (TextView) findViewById(R.id.link_register);
        info = (TextView) findViewById(R.id.tv_login_info);
        button = (Button) findViewById(R.id.btn_login);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = et_email.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                //System.out.println("email: "+email+" pass: "+password);
                //boolean checkLogin = checkLogin(email, password);
                Thread thread = new Thread(new CheckLogin(email, password));
                thread.start();
                while(true)
                {
                    if(result == null)
                    {
                        continue;
                    }else if (result.equals("success")){
                        writeFile();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        break;
                    }else if (result.equals("Email and password cannot be null!")){
                        info.setText(result);
                        break;
                    }else if(result.equals("Wrong Email or password!"))
                    {
                        info.setText(result);
                        break;
                    }else if(result.equals("Network error!"))
                    {
                        info.setText(result);
                        break;
                    }
                }
            }
        });
        reg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    public void writeFile()
    {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/birdRec/log";
        File file = new File(dir);
        if(!file.exists())
        {
            file.mkdirs();
        }
        PrintWriter outputStream = null;
        try {
            outputStream = new PrintWriter(new FileOutputStream(dir+"/userLog.txt"),true);
            outputStream.print(username + "##");
            outputStream.print(et_email.getText().toString().trim());
            outputStream.println();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void checkLogin(String email, String password)
    {
        if(email == null ||email.equals("")||password == null ||password.equals(""))
        {
            result = "Email and password cannot be null!";
            return;
        }


        Map<String, String> params = new HashMap<String,String>();
        params.put("email", email);
        params.put("password",password);

        //http://115.146.90.254:5001
        //http://192.168.31.236:5001
        String request_URL="http://115.146.90.254:5001/login";
        String uploadResponse = UploadUtil.uploadForm(request_URL, params);

        System.out.println("response:"+uploadResponse);
        if(uploadResponse == null)
        {
            result = "Network error!";
        }else{
            try {
                JSONParser p =new JSONParser();
                JSONObject obj = (JSONObject)p.parse(uploadResponse);
                //System.out.println("hahahah"+obj.get("status"));

                if((boolean)obj.get("status") == true)
                {
                    username = (String)obj.get("username");
                    result = "success";

                }else
                {
                    result = "Wrong Email or password!";
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

    class CheckLogin implements Runnable
    {
        String email;
        String password;
        public CheckLogin(String email, String password)
        {
            this.email = email;
            this.password = password;
        }
        @Override
        public void run() {
            checkLogin(email, password);
        }
    }
}
