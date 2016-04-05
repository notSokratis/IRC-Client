package com.resende.basicircclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    boolean canSend = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText hostText = (EditText) findViewById(R.id.hostname), portText = (EditText) findViewById(R.id.port),
                nickText = (EditText) findViewById(R.id.nickname), userText = (EditText) findViewById(R.id.username),
                nameText = (EditText) findViewById(R.id.realname), chanText = (EditText) findViewById(R.id.autoJoin);
        Button sendBtn = (Button) findViewById(R.id.sendLoginButton);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String settings[] = new String[6];
                canSend = true;

                settings[0] = hostText.getText().toString();
                settings[1] = portText.getText().toString();
                settings[2] = nickText.getText().toString();
                settings[3] = userText.getText().toString();
                settings[4] = nameText.getText().toString();
                settings[5] = chanText.getText().toString();

                for (int i = settings.length - 2; i > 0; i--) {
                    if (settings[i].equals("")) {
                        canSend = false;
                    }
                }

                if (canSend == true) {
                    Intent i = new Intent(MainActivity.this, ChatActivity.class);
                    i.putExtra(ChatActivity.USER_PARAMS, settings);
                    startActivity(i);
                } else {
                    Toast.makeText(MainActivity.this, "Must fill every field", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}