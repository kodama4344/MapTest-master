package jp.ac.kit.maptest;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

import android.content.res.*;
import android.text.*;
import java.security.*;
import javax.net.ssl.*;


public class MainActivity extends Activity implements OnClickListener {

    //private TextView mTextView;
    private static final String HOST = "192.168.1.12";
    private static final int PORT = 10000;
    private static final String TRUSTPASS = "mqttAuth";
    private TextView mTextView;
    private EditText usridText;
    private EditText passText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView)findViewById(R.id.other);
        Button btn = (Button) findViewById(R.id.button1);
        usridText = (EditText) findViewById(R.id.editText1);
        passText = (EditText) findViewById(R.id.editText2);
        btn.setOnClickListener(MainActivity.this);
    }


    @Override
    public void onClick(View v){
        passText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        //ユーザID / パスワードを取得
        //テスト用　->  ユーザID(test3)  パスワード(test3)
        String userID = usridText.getText().toString();
        String password = passText.getText().toString();

        connect(userID, password);

    }


    public static String printBytes(byte[] b){
        String s= "";
        for(int i = 0 ; i < b.length; i++){
            s = s + Integer.toHexString((0x0f&((char)b[i])>>4));
            s = s + Integer.toHexString(0x0f&(char)b[i]); }
        return s;
    }

    private void startGroupActivity(String pass, String id){
        String password = pass;
        String userID = id;
        Intent intent = new Intent();
        intent.setClassName("jp.ac.kit.maptest", "jp.ac.kit.maptest.GroupActivity");

        intent.putExtra("password", password);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }



    public void connect(String usrID, String pass){
        //第一引数：execute()で入れるパラメータ
        //第二引数：onProgressUpdate()にいれるパラメータ
        //第三引数：onPostExecute()に入れるパラメータ
        new AsyncTask<String,Void,String>(){

            @Override
            protected String doInBackground(String... params) {
                //SSLSocket
                SSLSocketFactory SSL_SOCKET_FACTORY = null;
                SSLSocket sslSock = null;
                SSLContext sContext = null;
                KeyStore trustStore = null;
                TrustManagerFactory tmf = null;
                KeyManagerFactory kmf = null;
                Socket socket = null;

                // I/O
                DataInputStream dis = null;
                DataOutputStream dos = null;
                AssetManager as = getResources().getAssets();   //assetsフォルダ
                InputStream is = null;                          //truststore読み込み
                ByteBuffer bytebuf = null;

                String message = "";
                byte[] usrID = params[0].getBytes();
                byte[] pass = params[1].getBytes();

                // hash
                byte[] nonceR = null;
                byte[] nonceRR = null;
                byte[] hh = null;
                byte[] hash = null;
                SecureRandom rr = null;

                boolean authStat = false;

                try {
                    is = as.open("truststore");
                    char[] trustPassChar = null;
                    trustStore = KeyStore.getInstance("BKS");
                    trustPassChar = TRUSTPASS.toCharArray();
                    trustStore.load(is, trustPassChar);
                    tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init(trustStore);

                    TrustManager[] trust_manager = null;
                    trust_manager = tmf.getTrustManagers();

                    sContext = SSLContext.getInstance("TLS");
                    sContext.init(null, trust_manager, null);

                    SSL_SOCKET_FACTORY = sContext.getSocketFactory();
                    // コネクション確立
                    sslSock=(SSLSocket)SSL_SOCKET_FACTORY.createSocket(HOST, PORT);
                    sslSock.startHandshake();
                    System.out.println("サーバ認証成功");

                    socket = sslSock;



                    //nonceRを受信
                    dis = new DataInputStream(socket.getInputStream());
                    nonceR = new byte[16];
                    dis.readFully(nonceR);

                    //nonceR'を生成
                    nonceRR = new byte[16];
                    rr = new SecureRandom();
                    rr.nextBytes(nonceRR);


                    //hash値の生成
                    //hash = SHA1(usrID + pass + nonceR + nonceRR)
                    bytebuf = ByteBuffer.allocate(usrID.length + pass.length + nonceR.length + nonceRR.length);
                    bytebuf.put(usrID);
                    bytebuf.put(pass);
                    bytebuf.put(nonceR);
                    bytebuf.put(nonceRR);
                    hh = bytebuf.array();

                    MessageDigest md = MessageDigest.getInstance("SHA-1");
                    hash = md.digest(hh);

                    // ユーザID nonce cnonce ハッシュ値をサーバへ送信
                    dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeInt(usrID.length);
                    dos.write(usrID);
                    dos.write(nonceR);
                    dos.write(nonceRR);
                    dos.writeInt(hash.length);
                    dos.write(hash);
                    dos.flush();

                    //確認用
                    //System.out.println(printBytes(nonceR));
                    //System.out.println(printBytes(nonceRR));
                    //System.out.println(printBytes(hash));


                    //認証結果
                    authStat = dis.readBoolean();
                    if(authStat){
                        System.out.println("クライアント認証成功");
                        startGroupActivity(printBytes(hash), params[0]);

                    }else{
                        System.out.println("クライアント認証失敗");
                        message += "userID / passwordが正しくありません  (test3 / test3)";
                    }


                } catch (IOException e) {
                    //message = "IOException error: " + e.getMessage();
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                        if (dis != null) {
                            dis.close();
                        }
                        if (dos != null) {
                            dos.close();
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }

                }

                return message;
            }

            @Override
            protected void onPostExecute(String result){
                //System.out.println(result);
                mTextView.setText(result);

            }
        }.execute(usrID, pass);

    }

}

