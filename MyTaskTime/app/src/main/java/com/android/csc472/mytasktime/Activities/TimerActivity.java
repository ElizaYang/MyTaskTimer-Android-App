package com.android.csc472.mytasktime.Activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.csc472.mytasktime.Data.DatabaseHandler;
import com.android.csc472.mytasktime.Model.Task;
import com.android.csc472.mytasktime.R;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

/**
 * Created by Shuonan on 11/9/2017.
 */

public class TimerActivity extends AppCompatActivity {

    private DatabaseHandler db;
    private Task task;
    private TextView taskName;
    private TextView targetHours;
    private TextView doneHours;
    private TextView taskDetails;
    private TextView timerValue;
    private TextView progressNum;
    private int taskId;
    private ImageButton backB;
    private ImageButton startB;
    private ImageButton stopB;
    private ImageButton forwardB;
    private ImageView trophy;
    private Button dimissB;
    private Button editB;
    private Button deleteB;
    private CircularProgressBar progressBar;
    private Handler customHandler = new Handler();
    private long startTime;
    long timeInMilliseconds;
    long timeSwapBuff;
    long updatedTime;
    int totalTime;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        Log.d("On Create", "ON CREATE");
        loadPageViews();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) { //pass data from previous page
            taskId = bundle.getInt("id");
            taskName.setText(bundle.getString("taskName"));
            targetHours.setText(bundle.getString("targetHours"));
            taskDetails.setText(bundle.getString("detail"));
        }
        db = new DatabaseHandler(getApplicationContext());
        task = db.getTask(taskId);
        updateViewData(task);
        showProgressBar(task);
        buttonsAction();
    }

    private void loadPageViews(){
        setContentView(R.layout.activity_timer);
        taskName = findViewById(R.id.taskNameTimer);
        targetHours = findViewById(R.id.hoursTextTimer);
        taskDetails = findViewById(R.id.taskDetailTextTimer);
        timerValue = findViewById(R.id.timerValue);
        doneHours = findViewById(R.id.doneHoursTextTimer);
        progressBar = findViewById(R.id.round_progress_bar);
        trophy = findViewById(R.id.trophy);
        progressNum = findViewById(R.id.progressNum);

        backB = findViewById(R.id.backButton);
        startB = findViewById(R.id.startButton);
        stopB = findViewById(R.id.stopButton);
        forwardB = findViewById(R.id.forwardButton);
        backB.setImageResource(R.drawable.ic_action_replay);
        startB.setImageResource(R.drawable.ic_action_start);

        stopB.setImageResource(R.drawable.ic_action_stop);
        forwardB.setImageResource(R.drawable.ic_action_forward);
        editB = findViewById(R.id.editButtonTimer);
        deleteB = findViewById(R.id.deleteButtonTimer);
    }

    private void updateViewData(Task task) {
        taskName.setText(task.getTaskName());
        taskDetails.setText(task.getTaskDetail());
        Log.d("UPDATE DB", timeLongtoString(task.getCompleteHours()));

        timerValue.setText(timeLongtoString(task.getCompleteHours()));//
        doneHours.setText(timeLongtoString(task.getCompleteHours()));//
        targetHours.setText(task.getTargetHours());//
        totalTime = Integer.valueOf(task.getTargetHours()) * 60 * 60 * 10;
        progressNum.setText(task.getCompleteHours().intValue()/totalTime +"% Done!");//
        showProgressBar(task);

    }

    private void showProgressBar(Task task) {
        timerValue.setText(timeLongtoString(task.getCompleteHours()));
        doneHours.setText(timeLongtoString(task.getCompleteHours()));
        progressBar.setMinimumHeight(20);

        totalTime = Integer.valueOf(task.getTargetHours()) * 60 * 60 * 10;
        int progress = task.getCompleteHours().intValue()/totalTime;
        progressBar.setProgress(progress);
        if (progress <= 10){
            trophy.setImageAlpha(80);
        }
        else if (progress <= 50) {
            trophy.setImageAlpha(120);
        }
        else if (progress < 90) {
            trophy.setImageAlpha(160);
        }
        else if (progress < 100) {
            trophy.setImageAlpha(200);
        }
        else if (progress == 100){
            trophy.setImageAlpha(255);
            createPopDialog();
        }
        progressNum.setText(progress+"% Done!");
    }

    private void buttonsAction() {
        startB.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                startB.setVisibility(View.INVISIBLE);
                stopB.setVisibility(View.VISIBLE);
                timeSwapBuff = task.getCompleteHours();
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
            }
        });
        stopB.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startB.setVisibility(View.VISIBLE);
                stopB.setVisibility(View.INVISIBLE);
                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);

                task.setTaskName(taskName.getText().toString());
                task.setTaskDetail(taskDetails.getText().toString());

                updateDB();
            }
        });
        backB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startB.setVisibility(View.VISIBLE);
                stopB.setVisibility(View.INVISIBLE);
                customHandler.removeCallbacks(updateTimerThread);
                updatedTime = task.getCompleteHours();
                updatedTime -= 30 * 60 * 1000;
                if (updatedTime < 0) {
                    updatedTime = 0;
                }
                updateDB();
            }
        });
        forwardB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startB.setVisibility(View.VISIBLE);
                stopB.setVisibility(View.INVISIBLE);
                customHandler.removeCallbacks(updateTimerThread);
                updatedTime = task.getCompleteHours();
                updatedTime += 30 * 60 * 1000;

                updateDB();
            }
        });
        editB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                editTask(task);
                updatedTime = task.getCompleteHours();
                task.setCompleteHours(updatedTime);
                updateDB();
            }
        });
        deleteB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                deleteTask(taskId);
                updateDB();
            }
        });
    }

    public void deleteTask(final int id) {

        //create an AlertDialog
        final Context context = TimerActivity.this;
        dialogBuilder = new AlertDialog.Builder(context);

        inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.comfirm_dialog, null);

        Button noButton = view.findViewById(R.id.noButton);
        Button yesButton = view.findViewById(R.id.yesButton);

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete the item.
                DatabaseHandler db = new DatabaseHandler(context);
                //delete item
                db.deleteTask(id);
                dialog.dismiss();
                onBackPressed();
            }
        });
    }

    public void editTask(final Task task) {
        Log.d("Edit Task timer Value:", timeLongtoString(task.getCompleteHours()));//work fine
        final Context context = TimerActivity.this;
        dialogBuilder = new AlertDialog.Builder(context);

        inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.edit_task_timer, null);
        //new edit View view UI items
        final TextView taskName = view.findViewById(R.id.task_name);
        final TextView taskDetail = view.findViewById(R.id.hint_taskDetail);
        final TextView targetHours = view.findViewById(R.id.hourTarget);
        final TextView title = view.findViewById(R.id.new_task);
        final TextView hourInput = view.findViewById(R.id.hour_input);
        final TextView minsInput = view.findViewById(R.id.min_input);
        final TextView secInput = view.findViewById(R.id.sec_input);

        Button saveButton = view.findViewById(R.id.saveButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        title.setText("Edit Task");
        taskName.setText(task.getTaskName());
        taskDetail.setText(task.getTaskDetail());
        targetHours.setText(task.getTargetHours());
        int secs = (int) (task.getCompleteHours() / 1000);
        int mins = secs / 60;
        int hours = mins / 60;
        mins = mins % 60;
        secs = secs % 60;
        hourInput.setText(String.format("%02d", hours));
        minsInput.setText(String.format("%02d", mins));
        secInput.setText(String.format("%02d", secs));

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHandler db = new DatabaseHandler(context);
                long updatedDoneHours = Integer.valueOf(hourInput.getText().toString()) * 60 * 60 * 1000
                        + Integer.valueOf(minsInput.getText().toString()) * 60 * 1000
                        + Integer.valueOf(secInput.getText().toString()) * 1000 ;
                db.updateCompleteHours(task,updatedDoneHours);
                task.setTaskName(taskName.getText().toString());
                task.setTargetHours(targetHours.getText().toString());
                task.setTaskDetail(taskDetail.getText().toString());
                task.setCompleteHours(updatedDoneHours);

                if (!taskName.getText().toString().isEmpty()
                        && !targetHours.getText().toString().isEmpty()) {
                    Snackbar.make(v, "Task Saved!", Snackbar.LENGTH_LONG).show();
                    db.updateTask(task);
                    updateViewData(task);
                    dialog.dismiss();
                }else {
                    Snackbar.make(view, "Add Task Name and Target Hours", Snackbar.LENGTH_LONG).show();
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });
    }

    public void updateDB() {
        timerValue.setText(timeLongtoString(updatedTime));
        doneHours.setText(timeLongtoString(updatedTime));
        task.setCompleteHours(updatedTime);//updatedTime init 0
        db.updateTask(task);
        db.updateCompleteHours(task, updatedTime);

        showProgressBar(task);

    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            //find original task time in db, add timeInMilliseconds to  timeSwapBuff
            timerValue.setText(timeLongtoString(updatedTime));
            customHandler.postDelayed(this, 0);
        }

    };
    private void createPopDialog() {

        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.congratulation_dialog, null);
        dimissB = view.findViewById(R.id.dimissButton);

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

        dimissB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    //reformat mills sec to readable 00:00:00
    private String timeLongtoString(Long timeValue) {
        int secs = (int) (timeValue / 1000);
        int mins = secs / 60;
        int hours = mins / 60;
        mins = mins % 60;
        secs = secs % 60;
        String stringTimeValue = String.format("%02d", hours) + ":" +
                String.format("%02d", mins) + ":" + String.format("%02d", secs);
        return stringTimeValue;
    }

}
