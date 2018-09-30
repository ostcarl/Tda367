package com.CEYMChat;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerMain extends Application {

    ServerModel model = new ServerModel();
    SocketHandler socketHandler = new SocketHandler(model);

    @Override
    public void start(Stage primaryStage) throws Exception {
        socketHandler.start();
        System.out.println("Server running");
    }
}