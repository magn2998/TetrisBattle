package Client;

import Client.Logic.Controls;
import Client.Models.BoardState;
import Client.Models.I_Block;
import Client.Models.Tetromino;
import Client.UI.Board;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jspace.SpaceRepository;

import java.io.IOException;


// Right now a lot of crap is included - this is only used for debugging end development purposes - Magn.
public class Client extends Application {

  @Override
  public void start(Stage primaryStage) {
    Button btn = new Button();
    btn.setText("Say 'Hello World' to client");

    Board nBoard = new Board(30, 30, 400);

    StackPane root = new StackPane();
    root.getChildren().add(btn);
    root.getChildren().add(nBoard);
    Scene scene = new Scene(root, 750, 500);


    btn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        System.out.println("Hello World!");
      }
    });

    primaryStage.setTitle("Hello World!");
    primaryStage.setScene(scene);
    primaryStage.show();

    Clock gameEngine = new Clock(nBoard);

    scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        gameEngine.keyDownEvent(keyEvent);
      }
    });

    gameEngine.toThread().start();
  }
  public static void main(String[] args) throws IOException {
    SpaceRepository repo = new SpaceRepository();
    repo.addGate("tcp://server:6969/?keep");

    launch(args);
  }
}
