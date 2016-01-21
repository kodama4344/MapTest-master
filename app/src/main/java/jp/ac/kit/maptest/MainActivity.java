package jp.ac.kit.maptest;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;


public class MainActivity extends Activity implements OnClickListener {

    private TextView mTextView;
    private String mHost = "192.168.1.12";
    private int mPort = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        //mTextView = (TextView)findViewById(R.id.socket);
        mTextView = (TextView)findViewById(R.id.socket);
        Button btn = (Button) findViewById(R.id.button1);
        btn.setOnClickListener(MainActivity.this);
    }



    @Override
    public void onClick(View v){

        TextView textView = (TextView) findViewById(R.id.textView1);
        EditText editText = (EditText) findViewById(R.id.editText1);
        TextView textView2 = (TextView) findViewById(R.id.textView2);
        EditText editText2 = (EditText) findViewById(R.id.editText2);

        //data取得
        String str = editText.getText().toString();
        String str2 = editText2.getText().toString();

        //テキストビューに出力
        textView.setText(str);
        textView2.setText(str2);


        connect("ibis", "ibis");

            /*
            @Override
            public void onClick(View view) {
                connect("ibis", "ibis");
            }
        });
        */
    }




    public void connect(String usrID, String pass){

        //第一引数：execute()で入れるパラメータ
        //第二引数：onProgressUpdate()にいれるパラメータ
        //第三引数：onPostExecute()に入れるパラメータ
        new AsyncTask<String,Void,String>(){

            @Override
            protected String doInBackground(String... params) {
                Random rnd = new Random();
                Socket connection = null;
                BufferedReader reader = null;
                //BufferedWriter writer = null;
                String message = "result:";
                //String url = "http://" + mHost +":" + mPort + mPath;
                String result = "";
                String usrID = params[0];
                String pass = params[1];

                try {
                    //ソケット
                    connection = new Socket(mHost, mPort);
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    //writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                    PrintWriter pw =
                            new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(connection.getOutputStream()) ));

                    //nonce R'を生成
                    String serv_d = reader.readLine();
                    //hash(usrID.pass.R,R')生成

                    Double cli_d = rnd.nextDouble();
                    String hh = usrID + pass + serv_d + cli_d;
                    message += "hh: " + hh;


                    try{
                        MessageDigest md = MessageDigest.getInstance("SHA-1");
                        byte[] cli_h = md.digest(hh.getBytes());
                        message += "cli_h" + printBytes(cli_h);
                        //(usrID.serv_d.cli_d.cli_h)を送信
                        //String u_info = usrID + "/" + serv_d + "/" + cli_d + "/" + cli_h.toString();
                        String u_info = usrID + "/" + serv_d + "/" + cli_d + "/" + printBytes(cli_h);
                        pw.println(u_info);
                        pw.flush();
                    }catch (NoSuchAlgorithmException e){
                        throw new UnsupportedOperationException(e);
                    }


                    message += "reslut: " + reader.readLine();
                    //result = Integer.parseInt(tmp);


                    /*
                    //HTTPリクエスト
                    writer.write("GET " + url + " HTTP/1.1\r\n");
                    writer.write("Host: " + mHost + "\r\n");
                    writer.write("User-Agent: " + mUserAgent);
                    writer.write("Connection: close\r\n");
                    writer.write("\r\n");
                    writer.flush();

                    //HTTPレスポンス
                    String result;
                    while((result = reader.readLine()) != null) {
                        message += result;
                        message += "\n";
                    }
                    */
                } catch (IOException e) {
                    message = "IOException error: " + e.getMessage();
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                    message = "Exception: " + e.getMessage();

                } finally {
                    try{
                        reader.close();
                        connection.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(message);
                return message;
            }

            //doInBackGroundの結果を受け取る
            @Override
            protected void onPostExecute(String result){
                mTextView.setText(result);

            }
        }.execute(usrID, pass);



    }


    public static String printBytes(byte[] b){
        String s= "";
        for(int i = 0 ; i < b.length; i++){
            s = s + Integer.toHexString((0x0f&((char)b[i])>>4));
            s = s + Integer.toHexString(0x0f&(char)b[i]); }
        return s;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.socket, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


/*
public class MainActivity extends Activity {
    MqttAndroidClient mqttAndroidClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mqttAndroidClient = new MqttAndroidClient(this, "tcp://192.168.1.12:1883", "piyo"); // (1)
        try {
            mqttAndroidClient.connect();  // (2)
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        try {
            mqttAndroidClient.publish("topic/hoge", "hello world".getBytes(), 0, false); // (3)
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
*/

