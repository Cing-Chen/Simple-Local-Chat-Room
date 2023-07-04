# Simple-Local-Chat-Room

This repository is the project of the Programming Special Project on Internet of Things and Social Robot course during the autumn semester of 2021 at NCU.

## Functions
### Server
#### Start Up Page
- Enter the name of server

#### Chat Room Page
- Show server IP, the name of speakers and online members
- Close server without any error on client
- Support multiperson
- Send messages

### Client
#### Start Up Page
- Enter the name of client, IP and port of target server

#### Chat Room Page
- Show the name of speakers
- Send messages
- Close client without any error on server

## Requirements
- The screen size of phone must be 1080×2220 (Google Pixel 3a)
- Only support running on emulator
    1. Type `adb devices` to see the indexes of running server device and clients devices

    2. If server is running on emulator-5554, and the sockets of server and client are like:
        ```java
        ServerSocket serverSocket = new ServerSocket(serverPort);
        Socket clientSocket = new Socket("10.0.2.2", typePort);
        ```
        - Type `adb -s emulator-5554 forward tcp:typePort tcp:serverPort`

    3. Enjoy chatting with each other!
