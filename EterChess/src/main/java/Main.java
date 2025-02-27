import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    private final double WINDOW_WIDTH = 800;
    private final double CONTROLS_WIDTH = 250;
    private final double WINDOW_HEIGHT = 830;
    private final boolean MAXIMISED = false;
    private final boolean FULLSCREEN = false;
    private final boolean RESIZABLE = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setWidth(WINDOW_WIDTH + CONTROLS_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);
        primaryStage.setMaximized(MAXIMISED);
        primaryStage.setFullScreen(FULLSCREEN);
        primaryStage.setResizable(RESIZABLE);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/board.fxml"));
        Pane layout = fxmlLoader.load();
        primaryStage.setScene(new Scene(layout));
        primaryStage.show();
    }
}
