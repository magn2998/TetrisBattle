package Client;

import Client.UI.Board;
import Main.Main;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SpaceRepository;


import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.SQLOutput;
import java.util.UUID;


// Right now a lot of crap is included - this is only used for debugging end development purposes - Magn.
public class Client extends Application {
  public String userName, UUID, roomUUID;
  public RemoteSpace mainServer, room;
  public boolean gameActive = false;
  public String uri = "tcp://LocalHost:6969/";

  @Override
  public void start(Stage primaryStage) {

  }
  public static void main(String[] args) throws IOException {
    SpaceRepository repo = new SpaceRepository();
    repo.addGate("tcp://server:6969/?keep");

    launch(args);
  }

  public void login() {
    try {
      this.mainServer = new RemoteSpace("tcp://LocalHost:6969/main?mainServer");

      Object[] loginResponse = new Object[0];
      mainServer.put(userName, "login");
      loginResponse = mainServer.get(new ActualField(userName), new FormalField(String.class), new FormalField(String.class));
      if (loginResponse[1] == "ok") {
        UUID = (String) loginResponse[2];
      } else {
        //Error message
        System.out.println(loginResponse[1]);
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }

  }

  public void hostRoom() {

    try {
      Object[] roomResponse = new Object[0];
      mainServer.put(UUID, "create");
      roomResponse = mainServer.get(new ActualField(UUID), new FormalField(String.class), new FormalField(String.class));
      if (roomResponse[1] == "ok") {
        roomUUID = (String) roomResponse[2];
        //Room can be started by UI
      } else {
        //Error message
        System.out.println(roomResponse[1]);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void TryJoinRoom(String roomName) {
    try {
      Object[] roomResponse = new Object[0];
      mainServer.put(UUID, "join", roomName);
      roomResponse = mainServer.get(new ActualField(UUID), new FormalField(String.class), new FormalField(String.class));

      if (roomResponse[1] == "ok") {
        roomUUID = (String) roomResponse[2];
        //Room can be started by UI

        //New thread waiting for game to start
      } else {
        //Error message
        System.out.println(roomResponse[1]);
      }

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void TryStartGame() {
    try {
      Object[] gameResponse = new Object[0];
      room.put(UUID, "start");
      gameResponse = mainServer.get(new ActualField(UUID), new FormalField(String.class), new FormalField(String.class));

      if (gameResponse[1] == "ok") {
        gameActive = true;
        //Game can be started by UI

      } else {
        //Error message
        System.out.println(gameResponse[1]);
      }

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
