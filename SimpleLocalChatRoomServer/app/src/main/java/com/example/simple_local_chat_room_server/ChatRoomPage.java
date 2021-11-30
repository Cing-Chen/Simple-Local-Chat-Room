package com.example.simple_local_chat_room_server;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ChatRoomPage extends AppCompatActivity {
    TextView chat_room_page_serverName;
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

        /* Get server name from StartUpPage.class */
        Intent intent = this.getIntent();
        if(intent != null) {
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                String serverName = bundle.getString("serverName");
                if(serverName != null && !serverName.equals("")) {
                    chat_room_page_serverName.setText(serverName);
                }
                else {
                    chat_room_page_serverName.setText("Server");
                }
            }
        }

        /* Create a new thread to process server tasks, *
         * old thread to process UI tasks               */
        Thread serverThread = new Thread(new Runnable() {
            /* A ArrayList use to store client sockets */
            ArrayList<Socket> clients = new ArrayList<>();

            /* Set server name, server ip and server port */
            final String name = chat_room_page_serverName.getText().toString();
            final String ip = getLocalIpAddress();
            final String port = "7100";

            @Override
            public void run() {
                try {
                    /* Create a server socket using port 7100 */
                    ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));

                    /* Set server start message and show the message on chatBlock */
                    String serverStartMessage = name + " started.(" + ip + ":" + port + ")\n";
                    runOnUiThread(() -> chat_room_page_chatBlock.setText(serverStartMessage));

                    /* Create a new thread to listen and accept client sockets, *
                     * old thread to process leave and send tasks               */
                    Thread listenAndAcceptThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(!serverSocket.isClosed()) {
                                try {
                                    /* Accept client socket, and add it into clients(ArrayList) */
                                    Socket clientSocket = serverSocket.accept();
                                    clients.add(clientSocket);

                                    /* Create a new thread to receive the message sent from client socket, *
                                     * old thread to keep listening and accepting new client socket        */
                                    Thread clientThread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                /* Set reader */
                                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                                                /* If client socket and server socket are both not closed, *
                                                 * keep receive the message sent from client socket        */
                                                while (!clientSocket.isClosed() && !serverSocket.isClosed()) {
                                                    /* Read the message sent from client socket */
                                                    String jsonString = bufferedReader.readLine();

                                                    /* The message is not empty */
                                                    if(jsonString != null) {
                                                        try {
                                                            /* Change the message from String to JSONObject */
                                                            JSONObject jsonObject = new JSONObject(jsonString);

                                                            /* Declare a map to create new message */
                                                            Map map = new HashMap();

                                                            /* attribute == welcome -> new client socket is connected */
                                                            if(jsonObject.getString("attribute").equals("welcome")) {
                                                                map.put("name", name);
                                                                map.put("ip", ip);
                                                                map.put("port", port);
                                                                map.put("attribute", "message");
                                                                map.put("content", name + ": Welcome " + jsonObject.getString("name") + " join us.\n");
                                                            }

                                                            /* attribute == message -> common message sent from client socket */
                                                            else if(jsonObject.getString("attribute").equals("message")) {
                                                                map.put("name", jsonObject.getString("name"));
                                                                map.put("ip", jsonObject.getString("ip"));
                                                                map.put("port", jsonObject.getString("port"));
                                                                map.put("attribute", "message");
                                                                map.put("content", jsonObject.getString("name") + ": " + jsonObject.getString("content") + "\n");
                                                            }

                                                            /* attribute == leave -> client socket is closed */
                                                            else if(jsonObject.getString("attribute").equals("leave")) {
                                                                map.put("name", name);
                                                                map.put("ip", ip);
                                                                map.put("port", port);
                                                                map.put("attribute", "message");
                                                                map.put("content", name + ": " + jsonObject.getString("name") + " has left.\n");
                                                                clients.remove(clientSocket);
                                                            }

                                                            /* Change Map to JSONObject,   *
                                                             * change JSONObject to String */
                                                            JSONObject newJsonObject = new JSONObject(map);
                                                            String newJsonString = newJsonObject.toString();

                                                            /* Send the message to every client socket */
                                                            for(int i = 0; i < clients.size(); i++) {
                                                                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(clients.get(i).getOutputStream()));
                                                                bufferedWriter.write(newJsonString);
                                                                bufferedWriter.newLine();
                                                                bufferedWriter.flush();
                                                            }

                                                            /* Update server chatBlock */
                                                            runOnUiThread(() -> chat_room_page_chatBlock.append(map.get("content").toString()));
                                                        } catch (org.json.JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                    clientThread.start();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    listenAndAcceptThread.start();

                    /* Process send and leave */
                    while(!serverSocket.isClosed()) {
                        /* Process send */
                        chat_room_page_sendButton.setOnClickListener(v -> {
                            /* Create a common message */
                            Map map = new HashMap();
                            map.put("name", name);
                            map.put("ip", ip);
                            map.put("port", port);
                            map.put("attribute", "message");
                            map.put("content", name + ": " + chat_room_page_typeBlock.getText().toString() + "\n");

                            /* Change Map to JSONObject,   *
                             * change JSONObject to String */
                            JSONObject jsonObject = new JSONObject(map);
                            String jsonString = jsonObject.toString();

                            /* Send the message to every client socket */
                            Thread sendThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    for(int i = 0; i < clients.size(); i++) {
                                        try {
                                            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(clients.get(i).getOutputStream()));
                                            bufferedWriter.write(jsonString);
                                            bufferedWriter.newLine();
                                            bufferedWriter.flush();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });

                            sendThread.start();

                            /* Update server chatBlock */
                            runOnUiThread(() -> chat_room_page_chatBlock.append(map.get("content").toString()));

                            /* Clear server typeBlock */
                            chat_room_page_typeBlock.setText("");
                        });

                        chat_room_page_leaveButton.setOnClickListener(v -> {
                            try {
                                /* Create a leave message */
                                Map map = new HashMap();
                                map.put("name", name);
                                map.put("ip", ip);
                                map.put("port", port);
                                map.put("attribute", "message");
                                map.put("content", name + ": " + "server closed. Please press 'Leave' button.\n");

                                /* Change Map to JSONObject,   *
                                 * change JSONObject to String */
                                JSONObject jsonObject = new JSONObject(map);
                                String jsonString = jsonObject.toString();

                                Thread sendThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for(int i = 0; i < clients.size(); i++) {
                                            try {
                                                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(clients.get(i).getOutputStream()));
                                                bufferedWriter.write(jsonString);
                                                bufferedWriter.newLine();
                                                bufferedWriter.flush();
                                                clients.get(i).close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });

                                sendThread.start();

                                /* Update server chatBlock */
                                runOnUiThread(() -> chat_room_page_chatBlock.append(map.get("content").toString()));

                                /* Close server socket */
                                serverSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    /* Close this page(class) */
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        serverThread.start();
    }

    private void initViewElement() {
        chat_room_page_serverName = (TextView) findViewById(R.id.chat_room_page_serverName);
        chat_room_page_chatBlock = (TextView) findViewById(R.id.chat_room_page_chatBlock);
        chat_room_page_typeBlock = (EditText) findViewById(R.id.chat_room_page_typeBlock);
        chat_room_page_leaveButton = (Button) findViewById(R.id.chat_room_page_leaveButton);
        chat_room_page_sendButton = (Button) findViewById(R.id.chat_room_page_sendButton);
    }

    public static String getLocalIpAddress() {
        try {
            for(Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces(); enumeration.hasMoreElements(); ) {
                NetworkInterface networkInterface = enumeration.nextElement();

                for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    if(!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        }
        catch (SocketException e) {
            Log.e("WifiPreference IpAddress", e.toString());
        }
        return null;
    }
}
