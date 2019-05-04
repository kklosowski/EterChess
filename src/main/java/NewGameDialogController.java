import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class NewGameDialogController  {


    @FXML
    private Slider timeSlider;
    @FXML
    private Slider incrementSlider;
    @FXML
    private Label timeLabel;
    @FXML
    private Label incrementLabel;
    @FXML
    private RadioButton colorRadio;
    @FXML
    private ChoiceBox modeChoice;

    private HashMap<String, Long> boardControls;

    public void initialize(){
        modeChoice.setItems(FXCollections.observableArrayList("Game vs AI", "Game vs Human"));
        modeChoice.getSelectionModel().selectFirst();
        timeSlider.valueProperty().addListener((observable, oldValue, newValue) -> timeLabel.setText(String.valueOf(newValue.intValue())));
        incrementSlider.valueProperty().addListener((observable, oldValue, newValue) -> incrementLabel.setText(String.valueOf(newValue.intValue())));
    }

    public void startGame(){
        switch (modeChoice.getValue().toString()){
            case "Game vs AI":
                boardControls.put("mode", 0L);
                break;
            case "Game vs Human":
                boardControls.put("mode", 1L);
                break;
        }
        boardControls.put("time", TimeUnit.MINUTES.toMillis((long) timeSlider.getValue()));
        boardControls.put("increment", TimeUnit.SECONDS.toMillis((long) incrementSlider.getValue()));
        boardControls.put("color", colorRadio.isSelected() ? 1L : 0L);
        Stage stage  = (Stage) timeLabel.getScene().getWindow();
        stage.close();
    }


    public void setBoardControlsMap(HashMap<String, Long> boardControls) {
        this.boardControls = boardControls;
    }
}