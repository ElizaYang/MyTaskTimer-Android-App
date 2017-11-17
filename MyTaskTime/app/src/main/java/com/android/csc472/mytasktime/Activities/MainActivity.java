package com.android.csc472.mytasktime.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.android.csc472.mytasktime.Data.DatabaseHandler;
import com.android.csc472.mytasktime.Model.Task;
import com.android.csc472.mytasktime.R;

public class MainActivity extends AppCompatActivity {
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText taskName;
    private EditText targetHour;
    private EditText taskDetail;
    private Button saveButton;
    private Button cancelButton;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHandler(this);

        byPassActivity();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPopDialog();
            }
        });
    }

    //pop up edit task screen
    private void createPopDialog() {

        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.edit_task, null);
        taskName = view.findViewById(R.id.task_name);
        targetHour = view.findViewById(R.id.hourTarget);
        taskDetail = view.findViewById(R.id.hint_taskDetail);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTaskToDB(v);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void saveTaskToDB(View v) {

        Task task = new Task();

        String newTask = taskName.getText().toString();
        String newTargetHour = targetHour.getText().toString();
        String newTaskDetail = taskDetail.getText().toString();

        task.setTaskName(newTask);
        task.setTargetHours(newTargetHour);
        task.setTaskDetail(newTaskDetail);
        task.setCompleteHours(0L);
        if (!newTask.isEmpty()
                && !targetHour.getText().toString().isEmpty()) {
            //Save to DB
            db.addTask(task);
            Snackbar.make(v, "Task Saved!", Snackbar.LENGTH_LONG).show();
            Log.d("Item Added ID:", String.valueOf(db.getTaskCount()));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    //start a new activity
                    startActivity(new Intent(MainActivity.this, ListActivity.class));
                }
            }, 600); // 0.5 second.
        } else {
            Snackbar.make(v, "Add Task Name and Target Hours", Snackbar.LENGTH_LONG).show();
        }
    }

    public void byPassActivity() {
        //Checks if database is empty; if not, then we just
        //go to ListActivity and show all added items

        if (db.getTaskCount() > 0) {
            startActivity(new Intent(MainActivity.this, ListActivity.class));
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
