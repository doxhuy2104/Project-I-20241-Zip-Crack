package multithreadbruteforce;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Main extends Application {
    private boolean canStart = false;
    public boolean isRunning = false;
    private TextField filePathField;
    private Spinner<Integer> threadSpinner;
    private Label statusLabel;
    public ChoiceBox<String> choiceBox;
    private ThreadGroup threadGroup;
//    public double time=0;


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Bẻ khoá file zip");

        filePathField = new TextField();
        filePathField.setPromptText("Chọn tệp zip");
        filePathField.setPrefWidth(300);
        Button fileButton = new Button("Chọn");
        fileButton.setOnAction(event -> selectFile(primaryStage));

        HBox fileSection = new HBox(10, filePathField, fileButton);

        Label threadLabel = new Label("Số luồng:");
        threadSpinner = new Spinner<>(1, 16, 1);
        threadSpinner.setEditable(true);
        threadSpinner.setPrefWidth(70);

        HBox threadSection = new HBox(10, threadLabel, threadSpinner);

        choiceBox = new ChoiceBox<>();
        choiceBox.setItems(FXCollections.observableArrayList("Thử từ đầu", "Tiếp tục từ lần thử trước"));
        if (MultiThread.getIndex() != 0) {
            choiceBox.setValue("Tiếp tục từ lần thử trước");

        } else {
            choiceBox.setValue("Thử từ đầu");
        }


        Button startButton = new Button("Bắt đầu");
        if (choiceBox.getValue().equals("Thử từ đầu")) {
            startButton.setText("Bắt đầu");
        } else {
            startButton.setText("Tiếp tục");
        }
        startButton.setOnAction(event -> {
            if (!canStart) {
                statusLabel.setText("Vui lòng chọn tệp zip");
                return;
            }

            if (!isRunning) {
                startCracking();
                startButton.setText("Tạm dừng");
            } else {
                stopCracking();
                startButton.setText("Tiếp tục");
            }
            isRunning = !isRunning;
        });

        statusLabel = new Label("");

        VBox layout = new VBox(15, fileSection, threadSection, choiceBox, startButton, statusLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-spacing: 15;-fx-font-size: 16px;");


        Scene scene = new Scene(layout, 410, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> {
            if (threadGroup != null) {
                threadGroup.interrupt();
            }
            Platform.exit();
            if (isRunning) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("files\\index.txt"))) {
                    writer.write(PasswordQueue.index + "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.exit(0);
        });
    }

    private void selectFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn tệp zip");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Files", "*.zip"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
            canStart = true;
        }
    }

    private void startCracking() {
        String filePath = filePathField.getText();
        int numThreads = threadSpinner.getValue();


        if (numThreads > 16) {
            statusLabel.setText("Vui lòng nhập số luồng nhỏ hơn hoặc bằng 16.");
            return;
        }

        if (filePath.isEmpty()) {
            statusLabel.setText("Vui lòng chọn tệp zip.");
            return;
        }

        if (numThreads <= 0) {
            statusLabel.setText("Vui lòng nhập số lồng lớn hơn 0.");
            return;
        }


        threadGroup = new ThreadGroup("CrackingGroup");

        MultiThread.startCracking(filePath, numThreads, this, threadGroup);
    }

    private void stopCracking() {
        if (threadGroup != null) {
            threadGroup.interrupt();
        }
        double endTime = System.currentTimeMillis();
        MultiThread.time += endTime - MultiThread.startTime;

        if (!CheckPass.passwordFound) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("files\\index.txt"))) {
                long currentIndex = MultiThread.getCurrentIndex();
                writer.write(currentIndex + "");
                statusLabel.setText("Đã tạm dừng tại index: " + currentIndex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void updateStatus(String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }

    public static void main(String[] args) {
        launch(args);
    }
}