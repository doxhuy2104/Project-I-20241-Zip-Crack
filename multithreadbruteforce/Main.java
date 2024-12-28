package multithreadbruteforce;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

public class Main extends Application {
    private boolean started = false;
    private boolean canStart = false;
    public boolean isRunning = false;

    private TextField filePathField;
    private Spinner<Integer> threadSpinner, maxLenSpinner;
    private Label maxLenLabel;
    private Label statusLabel;
    private Button startButton, fileButton;
    public ComboBox<String> comboBox;
    public CheckBox lowerCase, upperCase, numbers, specialChars;
    private Label charsetLabel;
    private ProgressBar progressBar;

    private ThreadGroup threadGroup;
//    public double time=0;

    //debug
    private Label numThreadLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Bẻ khoá file zip");

        filePathField = new TextField();
        filePathField.setPromptText("Chọn tệp zip");
        filePathField.setPrefWidth(300);
        fileButton = new Button("Chọn");
        fileButton.setOnAction(event -> selectFile(primaryStage));

        HBox fileSection = new HBox(10, filePathField, fileButton);

        charsetLabel = new Label("Chọn bộ ký tự:");

        lowerCase = new CheckBox("Chữ thường");
        lowerCase.setSelected(true);
        upperCase = new CheckBox("Chữ hoa");
        upperCase.setSelected(true);
        numbers = new CheckBox("Số");
        numbers.setSelected(true);
        specialChars = new CheckBox("Ký tự đặc biệt");
        specialChars.setSelected(false);

        VBox lowerUperSection = new VBox(10, lowerCase, upperCase);
        VBox numberSpecSection = new VBox(10, numbers, specialChars);
        HBox charsetSection = new HBox(10, lowerUperSection, numberSpecSection);

        VBox charsetLabelBox = new VBox(charsetLabel);
        charsetLabelBox.setStyle("-fx-alignment: center-left;");

        Label threadLabel = new Label("Số luồng:");
        threadSpinner = new Spinner<>(1, 16, 1);
        threadSpinner.setEditable(true);
        threadSpinner.setPrefWidth(70);

        HBox threadSection = new HBox(10, threadLabel, threadSpinner);

        maxLenLabel = new Label("Độ dài mật khẩu tối đa:");
        maxLenSpinner = new Spinner<>(1, 16, 1);
        maxLenSpinner.setEditable(true);
        maxLenSpinner.setPrefWidth(70);
        HBox maxLenSection = new HBox(10, maxLenLabel, maxLenSpinner);

        //Lấy dữ liệu từ file store.txt
        getStore();


        comboBox = new ComboBox<>();
        comboBox.setItems(FXCollections.observableArrayList("Thử từ đầu", "Tiếp tục từ lần thử trước"));
        if (MultiThread.getIndex() != 0) {
            comboBox.setValue("Tiếp tục từ lần thử trước");
            setCharsetAndMaxLenDisabled(true);
        } else {
            comboBox.setValue("Thử từ đầu");
        }
        comboBox.setOnAction(event -> {
            if (comboBox.getValue().equals("Tiếp tục từ lần thử trước")) {
                setCharsetAndMaxLenDisabled(true);
                startButton.setText("Tiếp tục");
            } else {
                setCharsetAndMaxLenDisabled(false);
                startButton.setText("Bắt đầu");
            }
        });


        startButton = new Button("Bắt đầu");
        if (comboBox.getValue().equals("Thử từ đầu")) {
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
                if (started)
                    resumeCracking();
                else
                    startCracking();
                startButton.setText("Tạm dừng");
            } else {
                pauseCracking();
                startButton.setText("Tiếp tục");
            }
            isRunning = !isRunning;
        });

        progressBar = new ProgressBar();
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setVisible(false);

        statusLabel = new Label("");
        numThreadLabel = new Label("");

        VBox layout = new VBox(15, fileSection, threadSection, comboBox, maxLenSection, charsetLabelBox, charsetSection, startButton, statusLabel, numThreadLabel, progressBar);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-spacing: 15;-fx-font-size: 16px;");


        Scene scene = new Scene(layout, 410, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setResizable(false);

        //cập nhật index khi thoát
        primaryStage.setOnCloseRequest(event -> {
//            if (threadGroup != null) {
//                threadGroup.interrupt();
//            }
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
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("files\\store.txt"))) {
                writer.write(selectedFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            canStart = true;
        }
    }

    private void getStore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("files\\store.txt"))) {
            String zipPath = reader.readLine();
            filePathField.setText(zipPath);
            if (!zipPath.isEmpty()) canStart = true;

            int numThreads = Integer.parseInt(reader.readLine());
            threadSpinner.getValueFactory().setValue(numThreads);

            int maxPasswordLength = Integer.parseInt(reader.readLine());
            maxLenSpinner.getValueFactory().setValue(maxPasswordLength);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setControlsDisabled(boolean disabled) {
        fileButton.setDisable(disabled);
        filePathField.setDisable(disabled);
        threadSpinner.setDisable(disabled);
        maxLenSpinner.setDisable(disabled);
        comboBox.setDisable(disabled);
        lowerCase.setDisable(disabled);
        upperCase.setDisable(disabled);
        numbers.setDisable(disabled);
        specialChars.setDisable(disabled);
    }

    private void setCharsetAndMaxLenDisabled(boolean disabled) {
        lowerCase.setDisable(disabled);
        upperCase.setDisable(disabled);
        numbers.setDisable(disabled);
        specialChars.setDisable(disabled);
        maxLenSpinner.setDisable(disabled);
    }


    private void startCracking() {
        started = true;
        setControlsDisabled(true);

        String filePath = filePathField.getText();
        int numThreads = threadSpinner.getValue();
        int maxPasswordLength = maxLenSpinner.getValue();

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

        StringBuilder charsetBuilder = new StringBuilder();
        if (lowerCase.isSelected()) {
            charsetBuilder.append("abcdefghijklmnopqrstuvwxyz");
        }
        if (upperCase.isSelected()) {
            charsetBuilder.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        }
        if (numbers.isSelected()) {
            charsetBuilder.append("0123456789");
        }
        if (specialChars.isSelected()) {
            charsetBuilder.append("!@#$%^&*()_+[]{}|;:,.<>?");
        }

        String charset = charsetBuilder.toString();
        if (charset.isEmpty()) {
            statusLabel.setText("Vui lòng chọn ít nhất một bộ ký tự.");
            return;
        }

        progressBar.setVisible(true);

        MultiThread.startCracking(filePath, numThreads, this, charset, maxPasswordLength);
    }

    private void resumeCracking() {
        setControlsDisabled(true);
        CheckPass.isRunning = true;
        PasswordQueue.isRunning = true;
    }

    private void pauseCracking() {
        progressBar.setVisible(false);
        CheckPass.isRunning = false;
        PasswordQueue.isRunning = false;

        if (!CheckPass.passwordFound) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("files\\index.txt"))) {
                long currentIndex = MultiThread.getCurrentIndex();
                writer.write(currentIndex + "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void updateStatus(String status) {
        Platform.runLater(() -> {
            statusLabel.setText(status);
            if (status.startsWith("Đã tìm thấy mật khẩu:")) {
                isRunning = false;
                startButton.setText("Bắt đầu");
            }
        });

    }

    public void updateThreadStatus(String status) {
        Platform.runLater(() -> {
            numThreadLabel.setText(status);

        });

    }

    public static void main(String[] args) {
        launch(args);
    }
}