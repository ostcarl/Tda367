package com.CEYMChatClient.Controller;

import com.CEYMChatClient.Services.IOutput;
import com.CEYMChatClient.Services.outputService;
import com.CEYMChatClient.View.*;
import javafx.application.Platform;
import com.CEYMChatClient.Model.ClientModel;
import com.CEYMChatClient.Services.IInput;
import com.CEYMChatLib.*;
import com.CEYMChatClient.Services.inputService;
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Client and ClientMain .
 */
public class ClientController implements IController {

    private ClientModel model;
    private IOutput outService;
    private IInput inService;
    private List<FriendListItem> friendItemList = new ArrayList<>();
    private String currentChatName;
    private String userName;

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
    private TextField passwordTextField;
    @FXML
    private Button loginButton;
    @FXML
    private Button fileSend;
    @FXML
    private TextField ipField;
    @FXML
    private Button recordStopButton;
    @FXML
    private Button recordPlayButton;
    @FXML
    private Text fileName;
    @FXML
    private ImageView emojis;
    @FXML
    private FlowPane emojisFlowPane;

    private Stage disconnectPopup = new Stage();
    private Parent disconnect;

    /** FXML methods**/
    @FXML
    public void loginClick() throws IOException {
        this.userName = userNameTextField.getText();
        login();
        mainPane.toFront();
    }

    /**
     *  Initiates the GUI
     */
    public void appInit() {
        model = new ClientModel();
        outService = new outputService(model, model.getServerIP());
        inService = new inputService(model, this);
        mainPane.getScene().getWindow().setOnCloseRequest(Event -> {    // Makes sure the client sends a notification to the Server that it has disconnected if the client is terminated
            try {
                saveMessages();
                outService.sendMessage(MessageFactory.createCommandMessage(new Command(CommandName.DISCONNECT, userName), userName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        File received = new File("Client/messages/received.csv");
        File sent = new File("Client/messages/received.csv");
        if (received.exists() && sent.exists()) {
            try {
                loadSavedMessages();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fillEmojis();
    }

    /**
     * Called when a username has been chosen, notifies the
     * Server that someone has connected so that they can be
     * identified aswell as initiating the GUI
     * @throws IOException
     */
    public void login() throws IOException {
        appInit();
        outService.connectToS();
        outService.login(CommandName.SET_USER, userName);
        model.setUsername(userName);
        model.setServerIP(ipField.getText());
        inService.connectToS(outService.getSocket());
    }

    /**
     * Sends the text in the chatBox to the Server
     * together with whichever user you have chosen
     * @throws IOException
     */
    public void sendString() throws IOException {
        String toSend = chatBox.getText();
        chatBox.setText("");
        Message message = MessageFactory.createStringMessage(toSend, userName, currentChatName);
        outService.sendMessage(message);
        model.addSentMessage(message);
        createAddSendMessagePane("Me: " + toSend );
    }

    /**
     * creates a new Message AnchorPane and adds it to the chat flow pane
     * as a Send message
     * @param sMessage the STRING which will be sent
     */
    public void createAddSendMessagePane (final String sMessage) throws IOException {
        SentTextMessage sentTextMessage = new SentTextMessage(sMessage);
        Platform.runLater(() -> chatPane.getChildren().add(sentTextMessage.sMessagePane));
    }

    /**
     * creates a new Message AnchorPane and adds it to the chat flow pane
     * as a received message
     * @param rMessage the STRING which will be received
     */
    public void createAddReceiveMessagePane (final String rMessage) throws IOException {
        ReceivedTextMessage receivedMessage = new ReceivedTextMessage(rMessage);
        Platform.runLater(() -> chatPane.getChildren().add(receivedMessage.rMessagePane));
    }

    /**
     * Asks the Server for an updated active
     * userlist, called when the Refresh button is pressed
     */
    @FXML
    public void refreshFriendList() {
        try {
            outService.sendMessage(MessageFactory.createCommandMessage(new Command(CommandName.REFRESH_FRIENDLIST, userName), userName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Checks which users have been tagged as friends
     *  and notifies the Server if any new friends have been added
     * @throws IOException
     */
    public void checkFriends() throws IOException {
        for (UserDisplayInfo friendInfo : model.getFriendList()) {         // Removes friends that have been deselected
            model.removeFriends(friendInfo);
        }
        for (FriendListItem fL : friendItemList) {              // Adds all newly selected friends
            model.addFriends(fL.getUInfo());
        }
        outService.sendMessage(MessageFactory.createFriendInfoList(model.getFriendList(), userName, userName)); // Notifies the Server about any changes have been made to the friends list
        }

    /**
     * Updates the GUI with text from a new message
     * @param message
     */
    public void displayNewMessage(Message message) throws IOException {
        if(!model.isMuted(message.getSender())) {
            createAddReceiveMessagePane(message.getSender() + ": " + message.getData());
        }
    }

    /**
     * Creates a list of users for the GUI to show
     * @param friendList
     * @throws IOException
     */
    public void createFriendListItemList(List<UserDisplayInfo> friendList) throws IOException {
        for (UserDisplayInfo uInfo : friendList) {
            if (!uInfo.getUsername().equals(model.getUsername())) {
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
    public void showOnlineFriends() throws IOException {
        friendItemList.clear();
        createFriendListItemList(model.getUserList());
        friendsFlowPane.getChildren().clear();
        for (FriendListItem friendListItem : friendItemList) {
            if (!model.isBlocked(friendListItem)) {
                friendsFlowPane.getChildren().add(friendListItem.getFriendPane());
            }
        }
    }

    /**
     * initialize the fxml friendListItem with data
     * @param item
     */
    public void initFriendListItem(FriendListItem item) {
        item.getFriendPane().setOnMouseClicked(MouseEvent -> {
            MouseButton button = MouseEvent.getButton();
            if(button==MouseButton.PRIMARY) {
                currentChatName = item.getFriendUsername().getText();
                currentChat.setText("Currently chatting with: " + currentChatName);
                try {
                    checkFriends();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
            try {
                outService.sendMessage(MessageFactory.createCommandMessage(new Command(CommandName.ADD_FRIEND, item.getFriendUsername().getText()), userName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        mute.setOnAction(event -> {
            model.addMuted(item.getFriendUsername().getText());
            item.getFriendPane().setStyle("-fx-background-color: crimson");
        });
        unmute.setOnAction(event -> {
            model.removeMuted(item.getFriendUsername().getText());
            item.getFriendPane().setStyle("-fx-background-color: white");
            });

        remove.setOnAction(event -> {
            model.addBlockedFriend(item);
            item.getFriendPane().setVisible(false);
        });
        item.getFriendPane().setOnContextMenuRequested(event -> contextMenu.show(item.getFriendPane(), event.getScreenX(), event.getScreenY()));

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
    public void sendFile() throws IOException {
        if (model.getSelectedFile() != null) {
            outService.sendMessage(MessageFactory.createFileMessage(model.getSelectedFile(), userName, currentChatName));
            fileName.setText("Current file: none");
            model.setSelectedFile(null);
        }
    }


    /**
     * Saves messages locally so that they can
     * be loaded the next time you load the client
     */
    public void saveMessages() {
        model.saveReceivedMessages("Client/messages/received.csv");
        model.saveSendMessages("Client/messages/sent.csv");
    }


    /** Loads messages saved during previous sessions */
    public void loadSavedMessages() throws IOException {
        List<String> savedSentMessages = model.loadSavedMessages("Client/messages/sent.csv");
        List<String> savedReceivedMessages = model.loadSavedMessages("Client/messages/received.csv");
        List<String> allSavedMessages = new ArrayList<>();
        combineSavedLists(savedSentMessages,savedReceivedMessages,allSavedMessages);
        for (int i = 0; i < allSavedMessages.size(); i=i+2) {
            if (allSavedMessages.get(i).equals("Me")) {
                createAddSendMessagePane(allSavedMessages.get(i) + ": " + allSavedMessages.get(i + 1));
            }
            else{
                createAddReceiveMessagePane(allSavedMessages.get(i) + ": " + allSavedMessages.get(i + 1));
            }

        }
    }


    /** Combines two saved lists of messages in one list */
    public void combineSavedLists (List<String> savedSentMessages, List<String> savedReceivedMessages,List<String>allSavedMessages){
        int min = Math.min(savedReceivedMessages.size(),savedSentMessages.size());
        int index = 0;
        int tmp = 0;

        for (int i = 0; i < min/2; i++){
            allSavedMessages.add(savedSentMessages.get(tmp));
            allSavedMessages.add(savedSentMessages.get(tmp + 1));
            allSavedMessages.add(savedReceivedMessages.get(tmp));
            allSavedMessages.add(savedReceivedMessages.get(tmp + 1));
            tmp = i + 2;
            index = i;
        }
        index = index * 4;
        if (min == savedSentMessages.size()){
            addElementsAfterIndex(savedReceivedMessages,allSavedMessages,index);
        }
        else if (min == savedReceivedMessages.size()) {
            addElementsAfterIndex(savedSentMessages,allSavedMessages,index);
        }
    }


    /** adds elements of a list to another list and begin from a given index */
    public void addElementsAfterIndex(List<String> savedList,List<String>allSavedMessages,int index){
        for (int i = index; i < savedList.size(); i++){
            allSavedMessages.add(savedList.get(i));
        }
    }

    public void fillEmojis () {
        EmojisMap emojisMap = new EmojisMap();
        Map<String, Emoji> emojiHashMap = emojisMap.createEmojiHashMap();

        for (Map.Entry<String, Emoji> entry : emojiHashMap.entrySet()) {
            EmojiItem emojiItem = new EmojiItem(entry.getValue().getEmojiChar(), this);
            emojisFlowPane.getChildren().add(emojiItem.getEmojiPane());
        }
    }

    public void chatBoxAppendText(String s){
        StringBuilder stringBuilder = new StringBuilder(chatBox.getText());
        stringBuilder.append(s);
        chatBox.setText(stringBuilder.toString());
    }

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

    public File getSelectedFile() {
        return model.getSelectedFile();
    }
}

