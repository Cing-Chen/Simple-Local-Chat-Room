package com.example.simple_local_chat_room_client;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ChatRoomPage extends AppCompatActivity {
    TextView chat_room_page_clientName;
    TextView chat_room_page_chatBlock;
    EditText chat_room_page_typeBlock;
    Button chat_room_page_leaveButton;
    Button chat_room_page_sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_chat_room);

        /* Initial all objects */
        initViewElement();

        /* Declare strings of client name, server ip and server port */
        String clientNameString = "";
        String serverIpString = "";
        String serverPortString = "";

        /* Get client name, server ip and server port from StartUpPage.class */
        Intent intent = this.getIntent();
        if(intent != null) {
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                clientNameString = bundle.getString("name");
                serverIpString = bundle.getString("ip");
                serverPortString = bundle.getString("port");
            }
        }

        chat_room_page_clientName.setText(clientNameString);
        String finalServerIpString = serverIpString;
        String finalServerPortString = serverPortString;

        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                /* Set server socket address */
                SocketAddress server = new InetSocketAddress(finalServerIpString, Integer.parseInt(finalServerPortString));

                /* Open client socket */
                Socket clientSocket = new Socket();

                try {
                    /* Connect to server socket */
                    clientSocket.connect(server, 5000);

                    /* Set writer amd reader */
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    /* If client socket connect to server socket successfully, *
                     * client socket send welcome json to server socket        */
                    if(clientSocket.isConnected()) {
                        /* Create a welcome message */
                        Map map = new HashMap();
                        map.put("name", chat_room_page_clientName.getText().toString());
                        map.put("ip", clientSocket.getLocalAddress().toString());
                        map.put("port", String.valueOf(clientSocket.getLocalPort()));
                        map.put("attribute", "welcome");
                        map.put("content", "");

                        /* Change Map to JSONObject,   *
                         * change JSONObject to String */
                        JSONObject jsonObject = new JSONObject(map);
                        String jsonString = jsonObject.toString();

                        /* Send message */
                        bufferedWriter.write(jsonString);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }

                    /* Create a new thread to process sending and leaving tasking, *
                     * old thread use to receive message sent from server socket   */
                    Thread sendAndLeaveThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            /* client socket is running */
                            while(!clientSocket.isClosed()) {
                                /* If chat_room_page_sendBButton is clicked,         *
                                 * send the content in typeBlock and clear typeBlock */
                                chat_room_page_sendButton.setOnClickListener(v -> {
                                    /* Create a common message */
                                    Map map = new HashMap();
                                    map.put("name", chat_room_page_clientName.getText().toString());
                                    map.put("ip", clientSocket.getLocalAddress().toString());
                                    map.put("port", String.valueOf(clientSocket.getLocalPort()));
                                    map.put("attribute", "message");
                                    map.put("content", chat_room_page_typeBlock.getText().toString());

                                    //runOnUiThread(() -> chat_room_page_chatBlock.append("TEST\n"));

                                    /* If client socket connect to server socket successfully, *
                                     * client socket send welcome json to server socket        */
                                    JSONObject jsonObject = new JSONObject(map);
                                    String jsonString = jsonObject.toString();

                                    /* Send the message to server socket */
                                    Thread sendThread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                bufferedWriter.write(jsonString);
                                                bufferedWriter.newLine();
                                                bufferedWriter.flush();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                    sendThread.start();

                                    /* Clear typeBlock */
                                    chat_room_page_typeBlock.setText("");
                                });

                                /* If chat_room_page_leaveButton is clicked,                   *
                                 * send leave message to server socket and close client socket */
                                chat_room_page_leaveButton.setOnClickListener(v -> {
                                    Map map = new HashMap();
                                    map.put("name", chat_room_page_clientName.getText().toString());
                                    map.put("ip", clientSocket.getLocalAddress().toString());
                                    map.put("port", String.valueOf(clientSocket.getLocalPort()));
                                    map.put("attribute", "leave");
                                    map.put("content", "");

                                    /* Change Map to JSONObject,   *
                                     * change JSONObject to String */
                                    JSONObject jsonObject = new JSONObject(map);
                                    String jsonString = jsonObject.toString();

                                    /* Send the message to server socket */
                                    Thread leaveThread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                bufferedWriter.write(jsonString);
                                                bufferedWriter.newLine();
                                                bufferedWriter.flush();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            /* Close this socket(client socket) */
                                            try {
                                                clientSocket.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            /* Close this page(class) */
                                            finish();
                                        }
                                    });

                                    leaveThread.start();
                                });
                            }
                        }
                    });

                    sendAndLeaveThread.start();

                    /* Receive messages from server socket and update chatBlock */
                    while(!clientSocket.isClosed()) {
                        /* Read message from server socket */
                        String jsonString = bufferedReader.readLine();

                        /* The message is not empty */
                        if(jsonString != null) {
                            /* Change the message from String to JSONObject */
                            JSONObject jsonObject = new JSONObject(jsonString);

                            /* Get the content, then update chatBlock */
                            String content = jsonObject.getString("content");
                            runOnUiThread(() -> chat_room_page_chatBlock.append(content));
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        clientThread.start();
    }

    private void initViewElement() {
        chat_room_page_clientName = (TextView) findViewById(R.id.chat_room_page_clientName);
        chat_room_page_chatBlock = (TextView) findViewById(R.id.chat_room_page_chatBlock);
        chat_room_page_typeBlock = (EditText) findViewById(R.id.chat_room_page_typeBlock);
        chat_room_page_leaveButton = (Button) findViewById(R.id.chat_room_page_leaveButton);
        chat_room_page_sendButton = (Button) findViewById(R.id.chat_room_page_sendButton);
    }
}