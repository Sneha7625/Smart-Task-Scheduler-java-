// Main.java
package app;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import model.Task;
import utils.ReminderService;
import utils.TaskStorage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.PriorityQueue;

public class Main extends Application {
    private PriorityQueue<Task> taskQueue = new PriorityQueue<>();
    private ObservableList<Task> displayedTasks = FXCollections.observableArrayList();
    private ListView<Task> taskListView = new ListView<>(displayedTasks);
    private ReminderService reminderService;
    private ComboBox<String> filterBox;
    private TextField searchField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        TextField titleInput = new TextField();
        titleInput.setPromptText("Enter task title");

        TextArea notesInput = new TextArea();
        notesInput.setPromptText("Optional notes/description");
        notesInput.setPrefRowCount(3);

        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("High", "Medium", "Low");
        priorityBox.setValue("Medium");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        TextField timeInput = new TextField();
        timeInput.setPromptText("HH:mm (optional)");

        filterBox = new ComboBox<>();
        filterBox.getItems().addAll("All", "Today", "High Priority");
        filterBox.setValue("All");
        filterBox.setOnAction(e -> updateList());

        searchField = new TextField();
        searchField.setPromptText("Search by title keyword");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateList());

        Button addButton = new Button("Add Task");
        Button editButton = new Button("Edit Selected");
        Button deleteButton = new Button("Delete Selected");
        Button doneButton = new Button("Mark as Done");
        Button snoozeButton = new Button("Snooze 10 Min");

        addButton.setOnAction(e -> {
            String title = titleInput.getText().trim();
            int priority = switch (priorityBox.getValue()) {
                case "High" -> 1;
                case "Medium" -> 2;
                case "Low" -> 3;
                default -> 2;
            };
            LocalDate deadline = datePicker.getValue();
            String notes = notesInput.getText().trim();

            LocalTime time = null;
            if (!timeInput.getText().isEmpty()) {
                try {
                    time = LocalTime.parse(timeInput.getText().trim());
                } catch (Exception ex) {
                    showAlert("Time Format Error", "Please enter time as HH:mm (e.g., 14:30)");
                    return;
                }
            }

            if (!title.isEmpty() && deadline != null) {
                Task newTask = new Task(title, priority, deadline, time);
                newTask.setNotes(notes);
                taskQueue.add(newTask);
                updateList();
                titleInput.clear();
                timeInput.clear();
                notesInput.clear();
            } else {
                showAlert("Error", "Title and deadline are required.");
            }
        });

        deleteButton.setOnAction(e -> {
            Task selected = taskListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                taskQueue.remove(selected);
                updateList();
            }
        });

        editButton.setOnAction(e -> {
            Task selected = taskListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                titleInput.setText(selected.getTitle());
                priorityBox.setValue(switch (selected.getPriority()) {
                    case 1 -> "High";
                    case 2 -> "Medium";
                    case 3 -> "Low";
                    default -> "Medium";
                });
                datePicker.setValue(selected.getDeadline());
                timeInput.setText(selected.getTime() != null ? selected.getTime().toString() : "");
                notesInput.setText(selected.getNotes());
                taskQueue.remove(selected);
                updateList();
            }
        });

        doneButton.setOnAction(e -> {
            Task selected = taskListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setDone(true);
                updateList();
            }
        });

        snoozeButton.setOnAction(e -> {
            Task selected = taskListView.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getTime() != null) {
                selected.setTime(selected.getTime().plusMinutes(10));
                updateList();
            }
        });

        taskListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setText(null);
                } else {
                    String text = (task.isDone() ? "[✓] " : "") + task.toString();
                    if (task.isDone()) {
                        setStyle("-fx-text-fill: grey; -fx-strikethrough: true;");
                    } else {
                        setStyle("");
                    }
                    setText(text);
                }
            }
        });

        VBox inputBox = new VBox(10,
                new Label("Search:"), searchField,
                new Label("Filter:"), filterBox,
                new Label("Task Title:"), titleInput,
                new Label("Priority:"), priorityBox,
                new Label("Deadline:"), datePicker,
                new Label("Time (optional):"), timeInput,
                new Label("Notes:"), notesInput,
                addButton, editButton, deleteButton, doneButton, snoozeButton);
        inputBox.setPadding(new Insets(10));

        HBox root = new HBox(20, inputBox, taskListView);
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 750, 500);
        stage.setTitle("Smart Task Scheduler");
        stage.setScene(scene);
        stage.show();

        taskQueue.addAll(TaskStorage.loadTasks());
        updateList();

        reminderService = new ReminderService(taskQueue);
        reminderService.start();
    }

    private void updateList() {
        String selectedFilter = filterBox.getValue();
        LocalDate today = LocalDate.now();
        String searchText = searchField.getText().toLowerCase();

        var stream = taskQueue.stream();

        if ("Today".equals(selectedFilter)) {
            stream = stream.filter(task -> task.getDeadline().equals(today));
        } else if ("High Priority".equals(selectedFilter)) {
            stream = stream.filter(task -> task.getPriority() == 1);
        }

        if (!searchText.isEmpty()) {
            stream = stream.filter(task -> task.getTitle().toLowerCase().contains(searchText));
        }

        displayedTasks.setAll(stream.sorted().toList());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        TaskStorage.saveTasks(taskQueue.stream().toList());
        if (reminderService != null) {
            reminderService.stop();
        }
    }
}
