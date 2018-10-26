package com.CEYMChatLib;
import java.io.File;
import java.util.List;
/**
 * Factory for creating Message(s) of predefined types.
 * To make sure messages only hold certain types of data
 */
public abstract class MessageFactory {


    public static Message<String> createStringMessage(String data, String user, String receiver){
        return new Message(data, user, receiver);
    }

    public static Message<File> createFileMessage(MessageFile data, String user, String receiver) {
        return new Message(data, user, receiver);
    }

    public static Message<Command> createCommandMessage(Command data, String user){
        return new Message(data, user);
    }

    public static Message<List> createUsersDisplayInfoMessages(List<UserDisplayInfo> data, String user, String receiver){
        return new Message(data, user, receiver);
    }
}
