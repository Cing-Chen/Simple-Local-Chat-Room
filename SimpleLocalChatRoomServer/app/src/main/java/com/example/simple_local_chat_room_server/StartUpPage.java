package com.example.simple_local_chat_room_server;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class StartUpPage extends AppCompatActivity {
    EditText start_up_page_typeServerNameBlock;
    Button start_up_page_connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_start_up);

        /* Initial all objects */
        initViewElement();

        /* If start_up_page_connectButton is clicked, send the content in *
         * start_up_page_typeServerNameBlock to ChatRoomPage.class        */
        start_up_page_connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("serverName", start_up_page_typeServerNameBlock.getText().toString());

                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(StartUpPage.this, ChatRoomPage.class);
                startActivity(intent);
            }
        });
    }

    private void initViewElement() {
        start_up_page_typeServerNameBlock = (EditText) findViewById(R.id.start_up_page_typeServerNameBlock);
        start_up_page_connectButton = (Button) findViewById(R.id.start_up_page_connectButton);
    }
}
