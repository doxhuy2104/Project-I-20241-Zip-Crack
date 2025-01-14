package multithreadbruteforce;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main extends Application {
    public boolean started = false;
    private boolean canStart = false;
    public boolean isRunning = false;


    public double startTime;
    public double time = 0;

    private TextField filePathField;
    private Spinner<Integer> threadSpinner, maxLenSpinner;
    private Label statusLabel;
    public Button startButton, fileButton;
    public ComboBox<String> comboBox, tryMethodSelection;
    public CheckBox lowerCase, upperCase, numbers, specialChars;
    public ProgressBar progressBar;
    public Label timeLabel;

    private static Main mainApp;

    private Thread passwordGeneratorThread;
    private Thread[] checkPassThreads;

    private boolean changeNumThreads = false;
    public String tryMethod = "Brute Force";


    //debug
    private Label numThreadLabel;

    @Override
    public void start(Stage primaryStage) {
        mainApp = this;
        primaryStage.setTitle("Bẻ khoá file zip");
        Image icon = new Image("files//unlock.png");

        primaryStage.getIcons().add(icon);
        filePathField = new TextField();
        filePathField.setPromptText("Chọn tệp zip");
        filePathField.setPrefWidth(300);
        fileButton = new Button("Chọn tệp");
        fileButton.setOnAction(_ -> selectFile(primaryStage));

        Label tryMethodLabel = new Label("Chọn phương pháp thử mật khẩu:");
        VBox tryMethodLabelBox = new VBox(10, tryMethodLabel);


        HBox fileSection = new HBox(10, filePathField, fileButton);

        Label charsetLabel = new Label("Chọn bộ ký tự:");

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


        threadSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue) && started) {
//                storeData();
                stopAllThreads();
                changeNumThreads = true;
            }
        });


        Label maxLenLabel = new Label("Độ dài mật khẩu tối đa:");
        maxLenSpinner = new Spinner<>(1, 16, 1);
        maxLenSpinner.setEditable(true);
        maxLenSpinner.setPrefWidth(70);
        HBox maxLenSection = new HBox(10, maxLenLabel, maxLenSpinner);

        //Lấy dữ liệu từ file store.txt
        getData();

        tryMethodSelection = new ComboBox<>();
        tryMethodSelection.setItems(FXCollections.observableArrayList("Brute Force Attack", "Dictionary Attack"));
        tryMethodSelection.setValue("Brute Force Attack");
        tryMethodSelection.setOnAction(_ -> {
            if (tryMethodSelection.getValue().equals("Dictionary Attack")) {
                tryMethod = "Dictionary";
                started = false;
//                resetIndex();
                updateProgress(0);
                stopAllThreads();
                setCharsetAndMaxLenDisabled(false);
                startButton.setText("Bắt đầu");
            } else {
                tryMethod = "Brute Force";
                started = false;
//                resetIndex();
                updateProgress(0);
                stopAllThreads();
                setCharsetAndMaxLenDisabled(false);
                startButton.setText("Bắt đầu");
            }
        });

        comboBox = new ComboBox<>();
        comboBox.setItems(FXCollections.observableArrayList("Thử từ đầu", "Tiếp tục từ lần thử trước"));
        if (getIndex() != 0) {
            comboBox.setValue("Tiếp tục từ lần thử trước");
            setCharsetAndMaxLenDisabled(true);
        } else {
            comboBox.setValue("Thử từ đầu");
            comboBox.setDisable(true);
        }
        comboBox.setOnAction(_ -> {
            if (comboBox.getValue().equals("Tiếp tục từ lần thử trước")) {
                setCharsetAndMaxLenDisabled(true);
                startButton.setText("Tiếp tục");
            } else {
                started = false;
//                resetIndex();
                updateProgress(0);
                stopAllThreads();
                setCharsetAndMaxLenDisabled(false);
                startButton.setText("Bắt đầu");
//                startButton.setDisable(true);
//                new Thread(() -> {
//                    try {
//                        Thread.sleep(1000);
//                        Platform.runLater(() -> startButton.setDisable(false));
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }).start();
            }
        });


        startButton = new Button("Bắt đầu");
        if (comboBox.getValue().equals("Thử từ đầu")) {
            startButton.setText("Bắt đầu");
        } else {
            startButton.setText("Tiếp tục");
        }
        startButton.setOnAction(_ -> {
            if (!canStart) {
                statusLabel.setText("Vui lòng chọn tệp zip");
                return;
            }

            if (!isRunning) {
                if (started) {
                    if (!changeNumThreads)
                        resumeCracking();
                    else {
                        CheckPass.isRunning = true;
                        PasswordQueue.isRunning = true;
                        startCracking();
                        changeNumThreads = false;
                    }
                    updateNumThreads(Thread.activeCount() + "");
                } else {
                    updateNumThreads(Thread.activeCount() + "");
                    startCracking();
                    time = 0;
                }
//                storeData();
                startButton.setText("Tạm dừng");
            } else {
                setFileCountinueDisabled(false);
                pauseCracking();
                startButton.setText("Tiếp tục");
            }
            isRunning = !isRunning;
        });
        filePathField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                CheckPass.passwordFound = false;
                updateProgress(0);
                statusLabel.setText("");
                stopAllThreads();
                started = false;
                isRunning = false;
                comboBox.setValue("Thử từ đầu");
                startButton.setText("Bắt đầu");
                setControlsDisabled(false);
                statusLabel.setText("");
                timeLabel.setText("");
            }
        });


        statusLabel = new Label("");
        numThreadLabel = new Label("");

        progressBar = new ProgressBar((double) getIndex() / calculateTotalPassword());
        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: #00FF00;");

        timeLabel = new Label("");
        Separator separator1 = new Separator();
        Separator separator2 = new Separator();
        Separator separator3 = new Separator();
        Separator separator4 = new Separator();
        Separator separator5 = new Separator();


        VBox layout = new VBox(15, fileSection, separator1, tryMethodLabelBox, tryMethodSelection, separator2, threadSection, separator3, maxLenSection, charsetLabelBox, charsetSection, separator4, startButton, separator5, progressBar, statusLabel, timeLabel, numThreadLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-spacing: 15;-fx-font-size: 16px;");

        Scene scene = new Scene(layout, 450, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setResizable(false);

        //cập nhật index khi thoát
        primaryStage.setOnCloseRequest(_ -> {
//            if (threadGroup != null) {
//                threadGroup.interrupt();
//            }
            storeData();
            stopAllThreads();

            Platform.exit();
            if (isRunning) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("files\\index.txt"))) {
                    writer.write(PasswordQueue.index + "");
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
            System.exit(0);
        });
    }

    public void startCracking(String zipFilePath, int numThreads, String charset, int maxPasswordLength, String tryMethod) {
        updateNumThreads(Thread.activeCount() + "");
        BlockingQueue<String> passwordQueue = new LinkedBlockingQueue<>(PasswordQueue.MAX_SIZE);

        PasswordQueue passwordGenerator = new PasswordQueue(charset.toCharArray(), maxPasswordLength, tryMethod);
        passwordGenerator.queue = passwordQueue;
        if (comboBox.getValue().equals("Thử từ đầu")) {
            passwordGenerator.index = 0;
            comboBox.setValue("Tiếp tục từ lần thử trước");
        } else {
            passwordGenerator.index = getIndex();
        }
        passwordGeneratorThread = passwordGenerator;
        passwordGenerator.start();

        checkPassThreads = new Thread[numThreads];
        startTime = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            CheckPass checkPass = new CheckPass(passwordQueue, passwordGenerator, zipFilePath);
            Thread thread = new Thread(checkPass);
            checkPassThreads[i] = thread;
            thread.start();
        }
    }


    public void stopAllThreads() {
        if (passwordGeneratorThread != null) {
            passwordGeneratorThread.interrupt();
        }

        if (checkPassThreads != null) {
            for (Thread thread : checkPassThreads) {
                if (thread != null) {
                    thread.interrupt();
                }
            }
        }
    }

    private void updateNumThreads(String numThreads) {
        Platform.runLater(() -> {
            if (numThreadLabel != null) {
                numThreadLabel.setText(numThreads);
            }
        });
    }

    public void updateButton() {
        Platform.runLater(() -> {
            startButton.setText("Bắt đầu");
            comboBox.setValue("Thử từ đầu");
        });
    }

    private void startCracking() {
//        CPUAffinity.setCPUAffinity(0, 1, 2, 3);
        CheckPass.passwordFound = false;
        CheckPass.isRunning = true;
        PasswordQueue.isRunning = true;
        updateNumThreads(Thread.activeCount() + "");
        started = true;
        statusLabel.setText("");
        timeLabel.setText("");
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

        startCracking(filePath, numThreads, charset, maxPasswordLength, tryMethod);
    }

    private void resumeCracking() {
        setControlsDisabled(true);
        CheckPass.isRunning = true;
        PasswordQueue.isRunning = true;
        startTime = System.currentTimeMillis();
    }

    private void pauseCracking() {
        Platform.runLater(() -> {
            threadSpinner.setDisable(false);
        });

        time += System.currentTimeMillis() - startTime;
        storeData();

        System.out.print(time);
        expectedTime();
        CheckPass.isRunning = false;
        PasswordQueue.isRunning = false;

        if (!CheckPass.passwordFound) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("files\\index.txt"))) {
                long currentIndex = getCurrentIndex();
                writer.write(currentIndex + "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void foundedPopup(String password, double time) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Đã tìm thấy mật khẩu: " + password + " trong " + time + "s");
            alert.showAndWait();
        });
    }

    public void updateProgress(double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress));
    }

    private void selectFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn tệp zip");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Files", "*.zip"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
            storeData();
            canStart = true;
        }
    }

    //lưu trữ path, số luồng, độ dài mật khẩu
    public void storeData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("files\\store.txt"))) {
            writer.write(filePathField.getText() + "\n");
            writer.write(threadSpinner.getValue() + "\n");
            writer.write(maxLenSpinner.getValue() + "\n");
            writer.write((lowerCase.isSelected() ? "1" : "0") + (upperCase.isSelected() ? "1" : "0") + (numbers.isSelected() ? "1" : "0") + (specialChars.isSelected() ? "1" : "0") + "\n");
            writer.write(time + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long getIndex() {
        File indexFile = new File("files\\index.txt");
        if (!indexFile.exists()) {
            return 0;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("files\\index.txt"))) {
            long index = 0;
            if (reader.ready()) {
                index = Long.parseLong(reader.readLine());
            }
            return index;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long getCurrentIndex() {
        return PasswordQueue.getCurrentIndex();
    }

    private void getData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("files\\store.txt"))) {
            String zipPath = reader.readLine();
            filePathField.setText(zipPath);
            if (!zipPath.isEmpty()) canStart = true;

            int numThreads = Integer.parseInt(reader.readLine());
            threadSpinner.getValueFactory().setValue(numThreads);

            int maxPasswordLength = Integer.parseInt(reader.readLine());
            maxLenSpinner.getValueFactory().setValue(maxPasswordLength);

            String charset = reader.readLine();
            char[] charsetArray = charset.toCharArray();

            lowerCase.setSelected(charsetArray[0] == '1');
            upperCase.setSelected(charsetArray[1] == '1');
            numbers.setSelected(charsetArray[2] == '1');
            specialChars.setSelected(charsetArray[3] == '1');
            time = Double.parseDouble(reader.readLine());
            System.out.println(time);
        } catch (Exception e) {
            e.printStackTrace();
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

    public static Main getMainApp() {
        return mainApp;
    }

    public void setFileCountinueDisabled(boolean disabled) {
        Platform.runLater(() -> {
            fileButton.setDisable(disabled);
            filePathField.setDisable(disabled);
            comboBox.setDisable(disabled);
        });
    }

    public void setControlsDisabled(boolean disabled) {
        Platform.runLater(() -> {
            fileButton.setDisable(disabled);
            filePathField.setDisable(disabled);
            threadSpinner.setDisable(disabled);
            maxLenSpinner.setDisable(disabled);
            comboBox.setDisable(disabled);
            lowerCase.setDisable(disabled);
            upperCase.setDisable(disabled);
            numbers.setDisable(disabled);
            specialChars.setDisable(disabled);

        });
    }

    private void setCharsetAndMaxLenDisabled(boolean disabled) {
        lowerCase.setDisable(disabled);
        upperCase.setDisable(disabled);
        numbers.setDisable(disabled);
        specialChars.setDisable(disabled);
        maxLenSpinner.setDisable(disabled);
    }

    private long calculateTotalPassword() {
        long totalPasswords = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader("files\\store.txt"))) {
            reader.readLine();
            reader.readLine();
            int maxPasswordLength = Integer.parseInt(reader.readLine());
            String charset = reader.readLine();
            char[] charsetArray = charset.toCharArray();
            StringBuilder charsetBuilder = new StringBuilder();
            if (charsetArray[0] == '1') {
                charsetBuilder.append("abcdefghijklmnopqrstuvwxyz");
            }
            if (charsetArray[1] == '1') {
                charsetBuilder.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
            }
            if (charsetArray[2] == '1') {
                charsetBuilder.append("0123456789");
            }
            if (charsetArray[3] == '1') {
                charsetBuilder.append("!@#$%^&*()_+[]{}|;:,.<>?");
            }


            for (int i = 1; i <= maxPasswordLength; i++) {
                totalPasswords += Math.pow(charsetBuilder.length(), i);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalPasswords;
    }

    public void disableComboBox(boolean disabled) {
        Platform.runLater(() -> {
            comboBox.setDisable(disabled);
        });
    }

    private void resetIndex() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("files\\index.txt"))) {
            writer.write("0");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void expectedTime() {
        double totalPasswords = calculateTotalPassword();
        double expTime = time * totalPasswords / PasswordQueue.index;
        int hours = (int) (expTime / 3600000);
        int minutes = (int) (expTime % 3600000 / 60000);
        int seconds = (int) (expTime % 60000 / 1000);
        Platform.runLater(() -> {
            statusLabel.setText("Dự kiến: " + hours + "h " + minutes + "m " + seconds + "s");
        });
    }


    public void updateTimeLabel(String s) {
        Platform.runLater(() -> {
            timeLabel.setText(s);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}