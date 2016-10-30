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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Xiaoyu on 10/4/2016.
 */
public class RegisterActivity extends Activity {
    private EditText et_username;
    private EditText et_email;
    private EditText et_password;
    private EditText et_repass;
    private TextView tv_info;
    private Button button;
    private String reg_message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        et_username = (EditText)findViewById(R.id.reg_uname);
        et_email = (EditText)findViewById(R.id.reg_email);
        et_password = (EditText)findViewById(R.id.reg_repass);
        et_repass = (EditText)findViewById(R.id.reg_repass);
        tv_info = (TextView)findViewById(R.id.tv_reg_info);
        button = (Button)findViewById(R.id.btn_reg);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = et_username.getText().toString().trim();
                String email = et_email.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                String repass = et_repass.getText().toString().trim();

                Thread thread = new Thread(new register(username, email, password, repass));
                thread.start();

                while(true)
                {
                    if(reg_message == null)
                    {
                        continue;
                    }else if(reg_message.equals("success"))
                    {
                        writeFile();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        break;
                    }else{
                        tv_info.setText(reg_message);
                        break;
                    }
                }
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
            outputStream.print(et_username.getText().toString().trim() + "##");
            outputStream.print(et_email.getText().toString().trim());
            outputStream.println();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean checkEmailFormat(String email)
    {
        String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(email);
        boolean isMatched = matcher.matches();
        if(isMatched)
        {
            return true;
        }
        return false;
    }
    public boolean checkUsername(String uname)
    {
        String check = "^[a-zA-Z]\\w{0,31}$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(uname);
        boolean isMatched = matcher.matches();
        if(isMatched)
        {
            return true;
        }
        return false;
    }
    public boolean checkPassword(String password)
    {
        String check = "^[0-9a-zA-Z]{6,16}$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(password);
        boolean isMatched = matcher.matches();
        if(isMatched)
        {
            return true;
        }
        return false;
    }

    public void checkRegister(String uname, String email, String password, String repass)
    {
        if(uname == null||uname.equals("")||email == null||email.equals("")||password == null||password.equals("")||repass == null ||repass.equals(""))
        {
            reg_message = "Please fill all items!";
            return;
        }

        // check user name
        if(checkUsername(uname)== false)
        {
            reg_message = "Please enter a correct user name";
            return;
        }

        // check email format
        if(checkEmailFormat(email)==false)
        {
            reg_message = "Please write a correct email";
            return;
        }
        //check password
        if(password.equals(repass)== false)
        {
            reg_message = "password and re-password should be same";
            return;
        }
        if(checkPassword(password) == false)
        {
            reg_message = "Please write a correct password";
            return;
        }

        //Post to server
        Map<String, String> params = new HashMap<String,String>();
        params.put("username", uname);
        params.put("email", email);
        params.put("password",password);
        params.put("expert", "false");
        params.put("client", "android");

        //115.146.90.254:5001
        //192.168.31.236:5001
        String request_URL="http://115.146.90.254:5001/register";
        String uploadResponse = UploadUtil.uploadForm(request_URL, params);

        if(uploadResponse== null)
        {
            reg_message = "Network error";
        }else{
            System.out.println("response:" + uploadResponse);
            try {
                JSONParser p =new JSONParser();
                JSONObject obj = (JSONObject)p.parse(uploadResponse);
                if((boolean)obj.get("status")== true)
                {
                    reg_message = "success";
                }else
                {
                    reg_message = (String)obj.get("message");
                }


            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

    class register implements Runnable
    {
        private String uname;
        private String email;
        private String password;
        private String repass;

        public register(String uname, String email, String password, String repass)
        {
            this.uname = uname;
            this.email = email;
            this.password = password;
            this.repass = repass;
        }

        @Override
        public void run() {
            checkRegister(uname, email, password, repass);
        }
    }
}
