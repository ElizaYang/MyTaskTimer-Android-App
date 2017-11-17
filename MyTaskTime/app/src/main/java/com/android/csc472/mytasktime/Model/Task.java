package com.android.csc472.mytasktime.Model;

/**
 * Created by Shuonan on 11/7/2017.
 */

public class Task {

    private String taskName;
    private String taskDetail;
    private String targetHours;
    private Long completeHours;
    private String dateTaskAdded;
    private int id;


    public Task() {

    }

    public Task(String taskName, String taskDetail, String targetHours, Long completeHours,
                String dateTaskAdded, int id) {
        this.taskName = taskName;
        this.taskDetail = taskDetail;
        this.targetHours = targetHours;
        this.completeHours = completeHours;
        this.dateTaskAdded = dateTaskAdded;
        this.id = id;
    }

    public Long getCompleteHours() {
        return completeHours;
    }

    public void setCompleteHours(Long completeHours) {
        this.completeHours = completeHours;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setTargetHours(String targetHours) {
        this.targetHours = targetHours;
    }

    public void setDateTaskAdded(String dateTaskAdded) {
        this.dateTaskAdded = dateTaskAdded;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskName() {

        return taskName;
    }

    public String getTargetHours() {
        return targetHours;
    }

    public String getDateTaskAdded() {
        return dateTaskAdded;
    }

    public int getId() {
        return id;
    }

    public String getTaskDetail() {
        return taskDetail;
    }

    public void setTaskDetail(String taskDetail) {
        this.taskDetail = taskDetail;
    }
}
