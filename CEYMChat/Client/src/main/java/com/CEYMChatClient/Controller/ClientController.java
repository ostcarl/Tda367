package com.CEYMChatClient.Controller;

import com.CEYMChatClient.Services.FileServices.*;
import com.CEYMChatClient.IMessageObserver;
import com.CEYMChatClient.View.*;
import javafx.application.Platform;
import com.CEYMChatClient.Model.ClientModel;
import com.CEYMChatLib.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.*;
import org.apache.commons.io.FileUtils;
import javax.sound.sampled.*;
import java.io.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Client and ClientMain .
 */

public class ClientController implements IClientController, IMessageObserver {

    private ClientModel model;
    private List<FriendListItem> friendItemList = new ArrayList<>();
    private String currentChatName;

    @FXML
    private AnchorPane loginPane;
    @FXML
    private AnchorPane mainPane;
    @FXML
    private StackPane programStackPane;
    @FXML
    private Button sendButton;
    @FXML
    private Button connectButton;
    @FXML
    private TextField chatBox;
    @FXML
    private Text currentChat;
    @FXML
    private FlowPane chatPane;
    @FXML
    private TextField sendToTextField;
    @FXML
    private FlowPane friendsFlowPane;
    @FXML
    private TextField userNameTextField;
    @FXML
    private Button loginButton;
    @FXML
    private Button fileSend;
    @FXML
    private TextField ipField;
    @FXML
    private Text fileName;
    @FXML
    private ImageView emojis;
    @FXML
    private FlowPane emojisFlowPane;
    @FXML
    private MenuItem serverPath;
    @FXML
    private MenuItem historyPath;

    private Stage disconnectPopup;

    private Parent disconnect;

    private IVoice voiceService;

    /**
     * Holds the program configurations.
     */
    private IConfigurable config = Configurations.getInstance();
    /**
     *  Initiates the GUI and loading default configurations.
     */
    private void appInit() {
        disconnectPopup  = new Stage();
        config.loadProperties();
        model.register(this);
        File received = new File(config.getConfigProperty("sentTextFile"));
        File sent = new File(config.getConfigProperty("receivedTextFile"));
        voiceService = new VoiceServices(config, AudioFileFormat.Type.WAVE);
        if (received.exists() && sent.exists()) {
            try {
                loadSavedMessages();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fillEmojis();
    }
    // voice Files
    //text messages
    //Files

    /**
     * This method is to change the server ip adress
     */
    @FXML
    public void changeServer(){
        String serverIp = (String) JOptionPane.showInputDialog("Enter the new server path");
        config.setConfigProperty("serverPath", serverIp);
        JOptionPane.showMessageDialog(null, "Server path has successfully been changed. The program will shut down. Please start it again", "info", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    /**
     * This method is to change the history file path
     */
    @FXML
    public void changeHistoryPath(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose the path of the history files");
        File selectedDirectory = directoryChooser.showDialog(chatBox.getScene().getWindow());
        if(selectedDirectory == null){
            //No Directory selected
        }else{
            File newDirectoryPath = new File(selectedDirectory.getAbsolutePath());
            File oldDirectoryPath = new File(config.getConfigProperty("saveDirectoryPath"));
            config.setConfigProperty("saveDirectoryPath", selectedDirectory.getAbsolutePath());
            try {
                FileUtils.copyDirectoryToDirectory(oldDirectoryPath,newDirectoryPath);
                FileUtils.deleteDirectory(oldDirectoryPath);
                JOptionPane.showMessageDialog(null, "Server path has successfully been changed. The program will shut down. Please start it again", "info", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Couldn't change the directory path", "Failure", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * Records voice from a target line.
     */
    @FXML
    public void recordVoice (){
        voiceService.recordVoice();
    }

    /**
     * Creates a thread to play back audio files.
     */
    @FXML
    public void playBack(){
        voiceService.playBack();
    }

    /**
     * Stops the recording.
     */
    @FXML
    public void stopRecording() {
        voiceService.stopRecording();

        System.out.println("Stop recording");
        sendFile();

        //Send a string message to notify the receiver that a voice-message has been received.
        String toSend = "Sound has been received. To listen click play.";
        Message message = MessageFactory.createStringMessage(toSend, model.getUInfo(), currentChatName);
        model.addMessage(message);

    }

    /**
     * Called when a username has been chosen, notifies the
     * Server that someone has connected so that they can be
     * identified aswell as initiating the GUI
     * @throws IOException
     */
    @FXML
    public void login(){
        appInit();
        model.setUsername(userNameTextField.getText());
        UserInfo uInfo = new UserInfo();
        uInfo.setUsername(userNameTextField.getText());
        model.setUInfo(uInfo);
        model.setUsername(model.getUsername());
        model.login();
        mainPane.toFront();
    }

    /**
     * Sends the text in the chatBox to the Server
     * together with whichever user you have chosen
     * @throws IOException
     */
    public void sendString() throws IOException {
        String toSend = chatBox.getText();
        chatBox.setText("");
        Message message = MessageFactory.createStringMessage(toSend, model.getUInfo(), currentChatName);
        model.addMessage(message);
        createAddSendMessagePane("Me: " + toSend );
    }

    /**
     * creates a new Message AnchorPane and adds it to the chat flow pane
     * as a Send message
     * @param sMessage the STRING which will be sent
     */
    private void createAddSendMessagePane (final String sMessage) throws IOException {
        SentTextMessage sentTextMessage = new SentTextMessage(sMessage);
        Platform.runLater(() -> chatPane.getChildren().add(sentTextMessage.sMessagePane));
    }

    /**
     * creates a new Message AnchorPane and adds it to the chat flow pane
     * as a received message
     * @param rMessage the STRING which will be received
     */
    private void createAddReceiveMessagePane (final String rMessage) {
        ReceivedTextMessage receivedMessage = new ReceivedTextMessage(rMessage);
        receivedMessage.load();
        Platform.runLater(() -> chatPane.getChildren().add(receivedMessage.rMessagePane));
    }

    /**
     * Asks the Server for an updated active
     * userlist, called when the Refresh button is pressed
     * Not currently implemented - great for debugging.
     */
    @FXML
    public void refreshFriendList() {
        model.addMessage(MessageFactory.createCommandMessage(new Command(CommandName.REFRESH_FRIENDLIST, model.getUsername()), model.getUInfo()));
    }

    /**
     *  Checks which users have been tagged as friends
     *  and notifies the Server if any new friends have been added
     * @throws IOException
     */
    private void checkFriends() {
        for (UserInfo friendInfo : model.getFriendList()) {         // Removes friends that have been deselected
            model.removeFriends(friendInfo);
        }
        for (FriendListItem fL : friendItemList) {              // Adds all newly selected friends
            model.addFriends(fL.getUInfo());
        }
        model.addMessage(MessageFactory.createUsersDisplayInfoMessages(model.getFriendList(), model.getUInfo(), model.getUsername())); // Notifies the Server about any changes have been made to the friends list
        }

    /**
     * Updates the GUI with text from a new message
     * @param message The message to display
     */
    public void displayNewMessage(Message message) {
        if(!model.isMuted(message.getSender()) && message.getSender() != model.getUInfo()) {
            createAddReceiveMessagePane(message.getSender().getUsername() + ": " + message.getData());
        }
    }

    /**
     * Creates a list of users for the GUI to show
     * @param friendList The list of UserInfo to be made into FriendListItems
     * @throws IOException
     */
    private void createFriendListItemList(List<UserInfo> friendList) {
        for (UserInfo uInfo : friendList) {
            if (uInfo.getUsername() != null && !uInfo.getUsername().equals(model.getUsername())) {
                FriendListItem userItem = new FriendListItem(uInfo);
                if (uInfo.getIsFriend()) {
                    userItem.setFriend();
                }
                friendItemList.add(userItem);
                initFriendListItem(userItem);
            }
        }
    }

    /**
     * Updates the GUI with the new userList
     * @throws IOException
     */
    public void showOnlineFriends() {
        friendItemList.clear();
        createFriendListItemList(model.getUserList());
        friendsFlowPane.getChildren().clear();
        for (FriendListItem friendListItem : friendItemList) {
            if (!model.isBlocked(friendListItem.getUInfo())) {
                friendsFlowPane.getChildren().add(friendListItem.getPane());
            }
        }
    }

    /**
     * initialize the fxml friendListItem with data, adds methods on click and right click
     * @param item The FriendListItem to be initiated
     */
    private void initFriendListItem(FriendListItem item) {
        item.getPane().setOnMouseClicked(MouseEvent -> {
            MouseButton button = MouseEvent.getButton();
            if(button==MouseButton.PRIMARY) {
                currentChatName = item.getFriendUsername().getText();
                currentChat.setText("Currently chatting with: " + currentChatName);
                checkFriends();
            }
        });
        ContextMenu contextMenu = new ContextMenu();
        MenuItem remove = new MenuItem("Remove");
        MenuItem mute = new MenuItem("Mute");
        MenuItem unmute = new MenuItem("Unmute");
        MenuItem toggleFriend = new MenuItem("Toggle Friend");
        contextMenu.getItems().add(remove);
        contextMenu.getItems().add(mute);
        contextMenu.getItems().add(unmute);
        contextMenu.getItems().add(toggleFriend);
        toggleFriend.setOnAction(event -> {
            item.toggleFriend();
            model.addMessage(MessageFactory.createCommandMessage(new Command(CommandName.ADD_FRIEND, item.getFriendUsername().getText()), model.getUInfo()));
        });
        mute.setOnAction(event -> {
            model.addMuted(item.getUInfo());
            item.getPane().setStyle("-fx-background-color: crimson");
        });
        unmute.setOnAction(event -> {
            model.removeMuted(item.getUInfo());
            item.getPane().setStyle("-fx-background-color: white");
            });
        remove.setOnAction(event -> {
            model.addBlockedFriend(item.getUInfo());
            item.getPane().setVisible(false);
        });
        item.getPane().setOnContextMenuRequested(event -> contextMenu.show(item.getPane(), event.getScreenX(), event.getScreenY()));

    }

    /**
     * Opens a GUI window that lets the user choose a file,
     * which is then cached as a FILE object so that it can
     * be sent to the Server or another user later
     */
    public void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a file to send with your message");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
                new FileChooser.ExtensionFilter("Text Files","*.pdf","*.doc","*.docx"),
                new FileChooser.ExtensionFilter("Document Files", "*.xlsx","*.xls","*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = chooser.showOpenDialog(chatBox.getScene().getWindow());
        if (selectedFile != null) {
            model.setSelectedFile(selectedFile);
            fileName.setText("Current file: " + model.getSelectedFile().getName());
        }
    }

    /**
     * Sends a specific FILE via the service to the
     * Server (and potentially to another user)
     * @throws IOException
     */
    public void sendFile() {
        if (model.getSelectedFile() != null) {
            model.addMessage(MessageFactory.createFileMessage(new MessageFile(model.getSelectedFile()), model.getUInfo(), currentChatName));
            fileName.setText("Current file: none");
            model.setSelectedFile(null);
        }
    }

    /** Loads messages saved during previous sessions */
    private void loadSavedMessages() throws IOException {
        ILoadMessages loader = new LoadFromCSV();
        List<String> savedSentMessages = loader.loadSavedMessages(config.getConfigProperty("sentTextFile"));
        List<String> savedReceivedMessages = loader.loadSavedMessages(config.getConfigProperty("receivedTextFile"));
        List<String> allSavedMessages = new ArrayList<>();
        model.combineSavedLists(savedSentMessages,savedReceivedMessages,allSavedMessages);
        for (int i = 0; i < allSavedMessages.size(); i=i+2) {
            if (allSavedMessages.get(i).equals("Me")) {
                createAddSendMessagePane(allSavedMessages.get(i) + ": " + allSavedMessages.get(i + 1));
            }
            else if(allSavedMessages.size() > 1 && i < allSavedMessages.size()-1){
                createAddReceiveMessagePane(allSavedMessages.get(i) + ": " + allSavedMessages.get(i + 1));
            }
        }
    }

    public void fillEmojis () {
        EmojisMap emojisMap = new EmojisMap();
        Map<String, Emoji> emojiHashMap = emojisMap.createEmojiHashMap();
        for (Map.Entry<String, Emoji> entry : emojiHashMap.entrySet()) {
            IFXMLController emojiItem = new EmojiItem(entry.getValue().getEmojiChar(), this);
            emojisFlowPane.getChildren().add(emojiItem.getPane());
        }
    }
    /** Appends text of chatBox with a String
     * @param s the String to append the chatBox with
     */
    public void chatBoxAppendText(String s){
        StringBuilder stringBuilder = new StringBuilder(chatBox.getText());
        stringBuilder.append(s);
        chatBox.setText(stringBuilder.toString());
    }

    /**
     * Safely disconnects the client from the server
     */
    @Override
    public void connectionEnded() {
        Platform.runLater(
                ()->{
                    try {
                        disconnect = FXMLLoader.load(getClass().getClassLoader().getResource("View/disconnected.fxml"));
                        disconnectPopup.initModality(Modality.APPLICATION_MODAL);
                        disconnectPopup.initStyle(StageStyle.UTILITY);
                        disconnectPopup.setTitle("You've been disconnected!");
                        disconnectPopup.setScene((new Scene(disconnect)));
                        disconnectPopup.show();
                        mainPane.setDisable(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

 /*   @FXML
    public void saveMessages (){
       // model.saveMessages();
    }*/

    @Override
    public void updateNewMessage(Message message)  {
        System.out.println("Update called successfully!");
        MessageType msgType = MessageType.valueOf(message.getType().getSimpleName().toUpperCase());
        switch(msgType){
            case STRING:    displayNewMessage(message);
            break;
            case ARRAYLIST: showOnlineFriends();
            break;
        }
    }


    /*public void disconnect() {
        connectionEnded();
    }*/

    public void setModel(ClientModel model) {
        this.model = model;
    }
}



