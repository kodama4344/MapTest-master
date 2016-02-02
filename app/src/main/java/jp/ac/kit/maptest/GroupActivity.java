package jp.ac.kit.maptest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;

import java.util.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;



public class GroupActivity extends AppCompatActivity implements OnClickListener {

    private TextView gid;
    private EditText inputText;
    private String password;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_activity);

        Intent intent = getIntent();
        if(intent != null){
            password = intent.getStringExtra("password");
            userID = intent.getStringExtra("userID");

            //グループID入力領域
            inputText = (EditText) findViewById(R.id.groupInput);

            Button btn = (Button) findViewById(R.id.button);
            btn.setOnClickListener(GroupActivity.this);
        }

    }


    @Override
    protected void onResume(){
        super.onResume();
        android.util.Log.v("GroupAct", "onResume");

        //グループIDの生成と表示(新規グループ作成用)
        gid = (TextView)findViewById(R.id.groupID);
        String rndGID = getRandomString(8);
        gid.setText(rndGID);
    }


    @Override
    protected void onPause() {
        super.onPause();
        android.util.Log.v("GroupAct", "onPause");
    }


    public static String getRandomString(int cnt) {
        final String CHARS ="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random rnd=new Random();
        StringBuffer buf = new StringBuffer();

        for(int i=0;i<cnt;i++){
            int val=rnd.nextInt(CHARS.length());
            buf.append(CHARS.charAt(val));
        }

        return buf.toString();
    }


    private void startMapsActivity(byte[] k, String t){
        byte[] key = k;
        String topic = t;

        Intent intent = new Intent();
        intent.setClassName("jp.ac.kit.maptest", "jp.ac.kit.maptest.MapsActivity");

        intent.putExtra("key", key);
        intent.putExtra("topic", topic);
        intent.putExtra("password", password);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }



    //共通鍵生成
    public byte[] generatedKey(String id){
        char[] password = id.toCharArray();
        byte[] salt = new byte[] {1};

        try{
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password, salt, 65536, 128); // 128Bit
            SecretKey tmp = factory.generateSecret(spec);
            return tmp.getEncoded();

        }catch(NoSuchAlgorithmException ne){
            ne.printStackTrace();
        }catch(InvalidKeySpecException ie){
            ie.printStackTrace();
        }

        //KeyGenerator gen = KeyGenerator.getInstance("AES");
        //gen.init(128);
        //SecretKey key=gen.generateKey();

        return null;

    }



    @Override
    public void onClick(View v){
        String groupID = null;
        byte[] aes = null;
        String topic;

        //System.out.println("group_id: " + inputText.getText().toString());

        groupID = inputText.getText().toString();

        //グループ共通鍵生成
        aes = generatedKey(groupID);
        //Topic生成
        topic = "mqtt_track/groupID";


        startMapsActivity(aes, topic);


    }
}
