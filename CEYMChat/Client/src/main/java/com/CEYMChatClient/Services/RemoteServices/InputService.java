package com.CEYMChatClient.Services.RemoteServices;

import com.CEYMChatClient.Model.ClientModel;
import com.CEYMChatLib.*;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the IInput interface.
 * It communicates via Sockets to the server.
 */
public class InputService implements IInput {
    private Socket socket;
    private ObjectInput messageInStream;
    private ClientModel model;

    public Message getMessageIn() {
        return messageIn;
    }

    private Message messageIn;
    private Message lastMsg;
    private List<UserInfo> comingFriendsList = new ArrayList();
    private int bytesRead = 0;

    private boolean running = true;

    /**
     * Constructor
     * @param model the client model
     * @param socket
     */

   public InputService(ClientModel model, Socket socket)

    {
        this.model = model;
        this.socket = socket;
    }
    /**
     * Connect client to the server and then starts the read thread
     */
    @Override
    public void connectToServer(){
        try {
            messageInStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connection started");
            read();

        } catch (IOException e) {
            System.out.println("Could not get InputStream, exiting...");
            System.exit(1);
            e.printStackTrace();
        }
    }

    /**
     * Starts a new thread constantly reading a inputstream from the Server.
     * While it is running it continuously checks the stream,
     * checks what type of message it has received and processes it appropriately
     */
    public void read() {
        new Thread(() -> {
            try {
                while (running) {
                    //checkConnectionIsLive();
                    messageIn = (Message) messageInStream.readObject();
                    if(messageIn!= null){
                        System.out.println("message received");
                        checkForType();
                        }
                    }
                } catch(SocketException e){
                    if(e.toString().contains("Connection reset")){
                        System.out.println("Connection reset!");
                        System.exit(0);

                    }
                } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public boolean checkConnectionIsLive(){
        if(!socket.isConnected()){
            disconnect();
            System.out.println("DISCONNECTED FROM SERVER");
            //System.exit(0);
            return false;
        }
        return true;
    }
    /**
     * Checks for the type of message that has been read and calls the appropriate method.
     * @throws IOException
     */
    private void checkForType() throws IOException {
        MessageType msgType = MessageType.valueOf(messageIn.getType().getSimpleName().toUpperCase());
        switch (msgType) {
            case ARRAYLIST: {   // A message with an ARRAYLIST contains information about currently active users
                if (!messageIn.equals(lastMsg) && messageIn != null) {    // The Thread updates the models state
                    receiveArrayList();
                }
                break;
            }
            case STRING: {  // A message with a STRING is a text message to be shown in the GUI
                if (!messageIn.equals(lastMsg) && messageIn != null) {
                    receiveString();
                }
                break;
            }
            case MESSAGEFILE: {    // A message with a FILE is intended to be saved to the users local device
                if(!messageIn.equals(lastMsg) && messageIn != null){  // The FILE within the message is corrupt so the Thread saves the FILE using a seperate stream of bytes
                    receiveFile();
                }
                break;
            }
            case COMMAND: {
                if(((Command)messageIn.getData()).getCommandName().equals(CommandName.DISCONNECT)){
                    disconnect();
                }
            }
        }
    }

    /**
     * handling the recieved array list by displaying the users in the list in the view via the controller
     */
    private void receiveArrayList(){
        comingFriendsList = (ArrayList) messageIn.getData();
        model.setUserList(comingFriendsList);
        lastMsg = messageIn;
        Platform.runLater(
                () -> notifyModel(messageIn)
        );
    }

    /**
     * handling the recieved STRING
     */
    private void receiveString() {
        lastMsg = messageIn;
        notifyModel(messageIn);
    }

    /**
     * handling the recieved file
     * @throws IOException
     */
    private void receiveFile() throws IOException {
        byte [] receivedFile  = new byte [((MessageFile)messageIn.getData()).getByteArray().length];
        InputStream inputStream = socket.getInputStream();

        Path path = FileSystems.getDefault().getPath("");
        String absPath;
        System.out.println(path.toAbsolutePath());
        if(path.toAbsolutePath().endsWith("Client")){
            absPath = System.getProperty("user.dir");

        }else{
            absPath = System.getProperty("user.dir") + "/Client";
        }
        FileOutputStream fileOut = new FileOutputStream(absPath + "/messages/" + ((MessageFile)messageIn.getData()).getFileName());
        BufferedOutputStream bufferedOut = new BufferedOutputStream(fileOut);
        int[] i = new int[1];
        while ((bytesRead = inputStream.read(receivedFile)) != 1) {
            i[0] = i[0] + bytesRead;
            bufferedOut.write(receivedFile, 0, bytesRead);
            if (i[0] == receivedFile.length) {
                break;
            }
        }
        bufferedOut.flush();
        fileOut.close();

    }

    /**
     * Safely stops all connections and stops the Thread
     */
    @Override
    public boolean disconnect(){
        running = false;
        try {
            messageInStream.close();
            socket.close();
            System.out.print(messageIn);
            return  true;
        } catch (IOException e) {
            System.out.println("Could not disconnect safely, exiting hard");
            System.exit(1);
            e.printStackTrace();
        }
        return false;
    }



    /**
     * Informs the controller that it should display a new message in the GUI
     * @param message the message to display
     * @throws IOException
     */
    private void notifyModel(Message message) {
        model.notify(message);
        model.addReceivedMessage(message);
    }

    /**
     * Informs the controller that it should updateNewMessage
     * @throws IOException
     */

}
