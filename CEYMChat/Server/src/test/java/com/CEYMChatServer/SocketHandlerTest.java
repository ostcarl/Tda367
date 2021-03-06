package com.CEYMChatServer;

import com.CEYMChatServer.Model.*;
import org.junit.Test;
import java.io.IOException;
import java.net.Socket;
import static org.junit.Assert.*;

public class SocketHandlerTest {

    private ServerModel testModel;
    private SocketHandler testHandler;

    @Test
    public void start() throws IOException {
        testModel = new ServerModel();
         testHandler = new SocketHandler(testModel);
        int expected = Thread.activeCount()+1;
        testHandler.start();
        assertEquals("A new thread has been started",expected,Thread.activeCount());
        testHandler.closeSocket();    }

    @Test
    public void connectSocket() throws IOException, InterruptedException {
        testModel = new ServerModel();
        testHandler = new SocketHandler(testModel);
        Socket socket = new Socket("localhost", 9000);
        Thread.sleep(2000);
        testHandler.start();
        Thread.sleep(2000);
        assertEquals("User added to userlist in model",1, testModel.getUserList().size());
        testHandler.closeSocket();    }
}