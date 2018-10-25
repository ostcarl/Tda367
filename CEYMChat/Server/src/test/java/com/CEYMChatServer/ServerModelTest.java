package com.CEYMChatServer;

import com.CEYMChatLib.*;
import com.CEYMChatServer.Model.ServerModel;
import com.CEYMChatServer.Model.User;
import com.CEYMChatServer.Services.SocketHandler;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import static org.junit.Assert.*;

public class ServerModelTest {
    private ServerModel testModel;

    @Test
    public void performCommand() throws IOException, InterruptedException {
        testModel  = new ServerModel();
        SocketHandler testHandler = new SocketHandler(testModel);
        Socket socket = new Socket("localhost", 9000);
        Thread.sleep(2000);
        testHandler.start();
        Thread.sleep(2000);
        testModel.performCommand(new Command(CommandName.SET_USER, "true"), testModel.getUserList().get(0).getUsername());
        assertEquals("Username correctly set for user",testModel.getUserList().get(0).getUsername(), "true");
        testModel.performCommand(new Command(CommandName.REFRESH_FRIENDLIST, testModel.getUserList().get(0).getUsername()),testModel.getUserList().get(0).getUsername());
        assertEquals("Message of type 'ARRAYLIST' received","ArrayList",testModel.getUserList().get(0).getWriter().getOutMessage().getType().getSimpleName());
        testModel.performCommand(new Command(CommandName.DISCONNECT, testModel.getUserList().get(0).getUsername()),testModel.getUserList().get(0).getUsername());
        assertEquals("User successfully disconnected",0,testModel.getUserList().size());
        socket.close();
        testHandler.closeSocket();    }

    @Test
    public void addUser() throws IOException {
        testModel = new ServerModel();
        User testUser = new User();
        testModel.addUser(testUser);
        assertEquals("Correct user added to userlist",testModel.getUserList().get(0), testUser);
    }

    @Test
    public void sendMessage() throws IOException, InterruptedException {
        testModel  = new ServerModel();
        SocketHandler testHandler = new SocketHandler(testModel);
        Socket socket = new Socket("localhost", 9000);
        Thread.sleep(2000);
        testHandler.start();
        Thread.sleep(2000);
        testModel.getUserList().get(0).setUsername("testUser");
        Message testMessage = MessageFactory.createStringMessage("Hello world!", "testUser", "testUser");
        testModel.sendMessage(testMessage,"testUser");
        assertEquals("Message on outstream match expected value",testMessage,testModel.getUserByUsername("testUser").getWriter().getOutMessage());
        testHandler.closeSocket();
    }

    @Test
    public void findUserByUsername() throws IOException, InterruptedException {
        testModel = new ServerModel();
        User testUser = new User();
        testUser.setUsername("testUser");
        testModel.addUser(testUser);
        assertEquals("Retrieved user from list match expected value", testUser, testModel.getUserByUsername(testUser.getUsername()));
    }

    @Test
    public void updateUserLists() throws IOException, InterruptedException {
        testModel = new ServerModel();
        SocketHandler testHandler = new SocketHandler(testModel);
        Socket socket = new Socket("localhost", 9000);
        Thread.sleep(2000);
        testHandler.start();
        Thread.sleep(2000);
        testModel.getUserList().get(0).setUsername("testUser");
        testModel.updateUserLists();
        assertEquals("A new userlist has been put on the ooutstream","ArrayList", testModel.getUserList().get(0).getWriter().getOutMessage().getType().getSimpleName());
        testHandler.closeSocket();
    }

    @Test
    public void sendFile() throws IOException, InterruptedException {
        testModel = new ServerModel();
        SocketHandler testHandler = new SocketHandler(testModel);
        Socket socket = new Socket("localhost", 9000);
        Thread.sleep(2000);
        testHandler.start();
        Thread.sleep(2000);
        testModel.getUserList().get(0).setUsername("testUser");
        File toSend = new File("pom.xml");
        testModel.sendFile(toSend.getName(),MessageFactory.createFileMessage(new MessageFile(toSend),testModel.getUserList().get(0).getUsername(),testModel.getUserList().get(0).getUsername()));
        assertEquals("MessageFile to be sent matches expected value",toSend,((MessageFile)(testModel.getUserList().get(0).getWriter().getOutMessage().getData())).getFile());
        testHandler.closeSocket();
    }
}