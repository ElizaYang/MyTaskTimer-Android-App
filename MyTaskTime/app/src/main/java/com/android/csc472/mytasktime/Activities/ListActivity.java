package com.android.csc472.mytasktime.Activities;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.csc472.mytasktime.Data.DatabaseHandler;
import com.android.csc472.mytasktime.Model.Task;
import com.android.csc472.mytasktime.R;
import com.android.csc472.mytasktime.UI.RecyclerItemTouchHelper;
import com.android.csc472.mytasktime.UI.RecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shuonan on 11/7/2017.
 */

public class ListActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener{

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private CoordinatorLayout coordinatorLayout;
    private List<Task> taskList;
    private List<Task> listItems;
    private DatabaseHandler db;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    private EditText taskItem;
    private EditText targetHour;
    private EditText taskDetail;
    private Button saveButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showListView();
    }

    @Override
    //When resume re-render list view
    public void onResume(){
        super.onResume();
        showListView();
    }

    private void showListView() {
        setContentView(R.layout.activity_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //ADD new task
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTaskDialog();
            }
        });

        db = new DatabaseHandler(this);
        recyclerView = findViewById(R.id.recyclerViewID);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        coordinatorLayout = findViewById(R.id.coordinator_layout);

        taskList = new ArrayList<>();
        listItems = new ArrayList<>();

        // Get items from database
        taskList = db.getAllTasks();

        for (Task c : taskList) {
            Task task = new Task();
            task.setTaskName(c.getTaskName());
            task.setTaskDetail(c.getTaskDetail());
            task.setTargetHours(c.getTargetHours());
            task.setCompleteHours(c.getCompleteHours());
            task.setId(c.getId());
            task.setDateTaskAdded(c.getDateTaskAdded());

            listItems.add(task);
        }

        recyclerViewAdapter = new RecyclerViewAdapter(this, listItems);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.notifyDataSetChanged();
        // adding item touch helper, swap LEFT-RIGHT gesture
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        recyclerViewAdapter.notifyDataSetChanged();
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback1 = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        // attaching the touch helper to recycler view
        new ItemTouchHelper(itemTouchHelperCallback1).attachToRecyclerView(recyclerView);

    }
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof RecyclerViewAdapter.ViewHolder) {
            // get the removed item name to display it in snack bar
            String name = taskList.get(viewHolder.getAdapterPosition()).getTaskName();
            // backup of removed item for undo purpose
            final Task deletedTask = taskList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();
            // remove the item from recycler view
            recyclerViewAdapter.removeItem(viewHolder.getAdapterPosition());
            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Task '" + name + "' removed!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // undo is selected, restore the deleted item
                    recyclerViewAdapter.restoreItem(deletedTask, deletedIndex);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    private void createTaskDialog() {
        //Create Task
        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.edit_task, null);
        taskItem = view.findViewById(R.id.task_name);
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

        String newTask = taskItem.getText().toString();
        String newTargetHour = targetHour.getText().toString();
        String newTaskDetail = taskDetail.getText().toString();

        task.setTaskName(newTask);
        task.setTargetHours(newTargetHour);
        task.setTaskDetail(newTaskDetail);

        if (!newTask.isEmpty()
                && !(targetHour.getText().toString().isEmpty())) {
            //Save to DB
            db.addTask(task);
            Snackbar.make(v, "Task Saved!", Snackbar.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    //start a new activity
                    startActivity(new Intent(ListActivity.this, ListActivity.class));
                    finish();
                }
            }, 600); //  0.5 second.
        } else {
            Snackbar.make(v, "Add Task Name and Target Hours", Snackbar.LENGTH_LONG).show();
        }
    }

}
