package com.example.simple_local_chat_room_client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StartUpPage extends AppCompatActivity {
    EditText start_up_page_clientNameBlock;
    EditText start_up_page_serverIpBlock;
    EditText start_up_page_serverPortBlock;
    Button start_up_page_connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_start_up);

        /* Initial all objects */
        initViewElement();

        /* If start_up_page_connectButton is clicked,
         * send client name, server ip, server port to ChatRoomPage.class */
        start_up_page_connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("name", start_up_page_clientNameBlock.getText().toString());
                bundle.putString("ip", start_up_page_serverIpBlock.getText().toString());
                bundle.putString("port", start_up_page_serverPortBlock.getText().toString());

                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(StartUpPage.this, ChatRoomPage.class);
                startActivity(intent);
            }
        });
    }

    private void initViewElement() {
        start_up_page_clientNameBlock = (EditText) findViewById(R.id.start_up_page_clientNameBlock);
        start_up_page_serverIpBlock = (EditText) findViewById(R.id.start_up_page_serverIpBlock);
        start_up_page_serverPortBlock = (EditText) findViewById(R.id.start_up_page_serverPortBlock);
        start_up_page_connectButton = (Button) findViewById(R.id.start_up_page_connectButton);
    }
}
