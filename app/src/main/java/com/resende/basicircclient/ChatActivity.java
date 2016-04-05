package com.resende.basicircclient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatActivity extends AppCompatActivity {

    final static String USER_PARAMS = "com.resende.basicircclient.UserParams";
    Socket skt;
    DataOutputStream out;
    BufferedReader in;
    String[] connectionParams;
    String lastSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        connectionParams = getIntent().getExtras().getStringArray(USER_PARAMS);

        ConnectRead cr = new ConnectRead();
        cr.execute(connectionParams);
    }

    private class ConnectRead extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            Button sendBtn = (Button) findViewById(R.id.sendMessageBtn);
            sendBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EditText messageText = (EditText) findViewById(R.id.messageText);
                    try {
                        StringBuilder sb = new StringBuilder();
                        try {
                            String[] msgSplit = messageText.getText().toString().split(" ");
                            lastSent = msgSplit[1];
                            for (int i = 2; i < msgSplit.length; i++) {
                                sb.append(msgSplit[i]).append(" ");
                            }
                            out.writeBytes(msgSplit[0] + " " + lastSent + " :" + sb.toString() + " \r\n");
                            publishProgress("<" + lastSent + "[" + connectionParams[2] + "]> " + sb.toString());
                        } catch (Exception e) {
                            String msg = messageText.getText().toString();
                            out.writeBytes(msg + " \r\n");
                            publishProgress("<" + connectionParams[2] + ">" + msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    messageText.setText("PRIVMSG " + lastSent + " ");
                }
            });
        }

        protected String doInBackground(String ... params) {
            String res = "";
            boolean afterMOTD = false;

            try {
                skt = new Socket(params[0], Integer.parseInt(params[1]));
                out = new DataOutputStream(skt.getOutputStream());
                in = new BufferedReader(new InputStreamReader(skt.getInputStream()));

                out.writeBytes("NICK " + params[2] + " \r\n");
                out.writeBytes("USER " + params[3] + " 0 * : " + params[4] + " \r\n");
                while ((res = in.readLine()) != null) {
                    String [] msg = res.split(":"),
                            sender = msg[1].split("!"),
                            chan = msg[1].split(" ");

                    if (res.substring(0, 4).equals("PING")) {
                        out.writeBytes(res.replaceFirst("I", "O"));
                    } else if (res.contains(params[2] +" :+i")) {
                        // AFTER MOTD COMMAND
                        // FOR AUTO JOIN CHANNELS
                        afterMOTD = true;
                        publishProgress("Connected!");
                        if (! params[5].equals("")) {
                            String[] autoChans = params[5].split(" ");
                            for (int i = 0; i < autoChans.length; i++) {
                                out.writeBytes("JOIN " + autoChans[i] + " \r\n");
                                publishProgress("Joining " + autoChans[i]);
                            }
                        }
                    } else if (res.contains("PRIVMSG " + chan[2] + " :" + params[2] + ":")) {
                        // CHANNEL MESSAGE MENTIONING YOU RECEIVED
                        StringBuilder sb = new StringBuilder();
                        sb.append("{").append(chan[2]).append("<").append(sender[0]).append(">} ");
                        for (int i = 2; i < msg.length; i++){
                            sb.append(":").append(msg[i]);
                        }
                        publishProgress(sb.toString());
                    } else if (res.contains("PRIVMSG " + params[2] + " :")) {
                        // PRIVMSG RECEIVED
                        StringBuilder sb = new StringBuilder();
                        sb.append("<").append(chan[2]).append("[").append(sender[0]).append("]> ");
                        for (int i = 2; i < msg.length; i++){
                            sb.append(":").append(msg[i]);
                        }
                        publishProgress(sb.toString());
                    } else if (res.contains("PRIVMSG " + chan[2] + " :")) {
                        // CHANNEL MESSAGE RECEIVED
                        StringBuilder sb = new StringBuilder();
                        sb.append("[").append(chan[2]).append("<").append(sender[0]).append(">] ");
                        for (int i = 2; i < msg.length; i++){
                            sb.append(":").append(msg[i]);
                        }
                        publishProgress(sb.toString());
                    } else if (afterMOTD == true){
                        publishProgress(res);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return res;
        }

        protected void onProgressUpdate (String ... params) {
            TextView viewText = (TextView) findViewById(R.id.viewText);
            viewText.setText(viewText.getText() + "\n" + params[0]);
        }

        public void cancel() {
            try {
                skt.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            out = null;
            in = null;
            skt = null;
        }
    }

    @Override
    public void onBackPressed() {
        ConnectRead cr = new ConnectRead();
        cr.cancel();
        finish();
    }
}
