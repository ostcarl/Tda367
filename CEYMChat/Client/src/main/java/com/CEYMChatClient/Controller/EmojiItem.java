package com.CEYMChatClient.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;

/**
 * emojiItem is a controller to emojiItem fxml file
 */
public class EmojiItem implements IFXMLController {

    private IClientController clientController;

    @FXML
    private Label emojiCharLabel;
    @FXML
    private AnchorPane emojiPane;

    public EmojiItem (String emojiChar, IClientController clientController) {
        load();
        this.emojiCharLabel.setText(emojiChar);
        this.clientController = clientController;
    }

    public AnchorPane getPane() {
        return emojiPane;
    }

    /**
     * handles click event on an emoji. Sets the clicked emoji in the text box
     */
    @FXML
    public void onClick(){
        clientController.chatBoxAppendText(emojiCharLabel.getText());
    }

    @Override
    public void load() {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("View/emojiItem.fxml"));
            loader.setController(this);
            try {
                loader.load();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
}
