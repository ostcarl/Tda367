package com.CEYMChatClient.Model;

import com.CEYMChatClient.View.FriendListItem;
import com.CEYMChatLib.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Model for the client */
public class ClientModel {
    private String username;
    private List<UserDisplayInfo> userList = new ArrayList<>();
    private List<Message> receivedMessages = new ArrayList<>();
    private List<Message> sentMessages = new ArrayList<>();
    private File selectedFile;
    private String serverIP;
    private List<String> mutedFriends = new ArrayList<>();
    private List<FriendListItem> blockedFriends = new ArrayList<>();
    private List<UserDisplayInfo> friendList = new ArrayList<>();

    /** Getters, setters and adders **/
    public void setUserList(List<UserDisplayInfo> userList) {
        this.userList = userList;
    }
    public List<UserDisplayInfo> getUserList() {
        return userList;
    }
    public void setUsername(String user){
        this.username = user;
    }
    public String getUsername(){
        return username;
    }
    public void addReceivedMessage(Message message){
        receivedMessages.add(message);
    }
    public void addSentMessage (Message message){
        sentMessages.add(message);
    }
    public File getSelectedFile() {
        return selectedFile;
    }
    public void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
    }

    /** Saves all sent and received messages into a file */
    void saveArrayListToFile(List<Message> list, String filename) throws IOException {
        FileWriter writer = new FileWriter(filename);

        for(Message message: list) {
            if(message.getSender().equals(username)) {
                writer.write("Me: " + "," + message.getData().toString() + ",");
            }
            else{
                writer.write(message.getSender() + "," + message.getData().toString() + ",");
            }
        }
        writer.close();
    }

    /** Calls saveArrayListToFile to save all Received messages */
    public void saveReceivedMessages(String filename) {
        try {
            saveArrayListToFile(receivedMessages, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Calls saveArrayListToFile to save all sent messages */
    public void saveSendMessages(String filename) {
        try {
            saveArrayListToFile(sentMessages, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Loads messages that were saved during the last session */
    public List<String> loadSavedMessages(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line = "";
        String cvsSplitBy = ",";
        String [] savedMessages = {};
        while((line = reader.readLine())!=null){
            savedMessages = line.split(cvsSplitBy);
        }
        return new ArrayList<String>(Arrays.asList(savedMessages));
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void addMuted(String friendUsername) {
        mutedFriends.add(friendUsername);
    }

    private List<String> getMutedFriends() {
        return this.mutedFriends;
    }

    public void removeMuted(String text) {
        mutedFriends.remove(text);
    }
    public boolean isMuted(String userName ) {
        for (String s : getMutedFriends()) {
            if (s.equals(userName)){
                return true;
            }
        }
        return false;
    }

    private List<FriendListItem> getBlockedFriends() {
        return this.blockedFriends;
    }
    public boolean isBlocked(FriendListItem friendListItem) {
        for (FriendListItem blocked : getBlockedFriends()) {
            if (blocked.getFriendUsername().getText().equals(friendListItem.getFriendUsername().getText())) {
                return true;
            }
        }
        return false;
    }
    public void addBlockedFriend(FriendListItem item) {
        blockedFriends.add(item);
    }


    public void removeFriends(UserDisplayInfo uInfo){
        if(!uInfo.getIsFriend() && friendList.contains(uInfo)){
            friendList.remove(uInfo);
        }
    }

    public void addFriends(UserDisplayInfo uInfo){
        if (uInfo.getIsFriend() && !friendList.contains(uInfo)){
            friendList.add(uInfo);
        }
    }

    public List<UserDisplayInfo> getFriendList() {
        return friendList;
    }
}
