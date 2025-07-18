package utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import model.Task;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;

public class ReminderService {
    private final PriorityQueue<Task> taskQueue;
    private final Timer timer;

    public ReminderService(PriorityQueue<Task> taskQueue) {
        this.taskQueue = taskQueue;
        this.timer = new Timer(true);
    }

    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LocalDate today = LocalDate.now();
                LocalTime now = LocalTime.now();

                for (Task task : taskQueue) {
                    if (task.isDone()) continue; //  Skip tasks marked as done

                    if (task.getDeadline().equals(today)) {
                        Platform.runLater(() -> showReminder(task, "due today"));

                        if (task.getTime() != null && now.isBefore(task.getTime())
                                && now.plusMinutes(30).isAfter(task.getTime())) {
                            Platform.runLater(() -> showReminder(task, "starts in 30 minutes!"));
                        }
                    }
                }
            }
        }, 0, 60000);
    }

    private void showReminder(Task task, String type) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reminder");
        alert.setHeaderText("Task: " + task.getTitle());
        alert.setContentText("\uD83D\uDD52 " + type + " — Deadline: " + task.getDeadline() +
                (task.getTime() != null ? " " + task.getTime() : ""));
        alert.show();
    }

    public void stop() {
        timer.cancel();
    }
}