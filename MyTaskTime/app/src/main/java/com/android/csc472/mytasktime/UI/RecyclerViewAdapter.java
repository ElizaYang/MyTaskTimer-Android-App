package com.android.csc472.mytasktime.UI;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.csc472.mytasktime.Activities.TimerActivity;
import com.android.csc472.mytasktime.Data.DatabaseHandler;
import com.android.csc472.mytasktime.Model.Task;
import com.android.csc472.mytasktime.R;

import com.daimajia.numberprogressbar.NumberProgressBar;

import java.util.List;

/**
 * Created by Shuonan on 11/1/2017.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private Context context;
    private List<Task> taskItems;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog dialog;
    private LayoutInflater inflater;
    private int progress;


    public RecyclerViewAdapter(Context context, List<Task> taskItems) {
        this.context = context;
        this.taskItems =  taskItems;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
    //get current position item, and use row view holder set data
        Task task = taskItems.get(position);

        holder.taskName.setText(task.getTaskName());
        holder.dateAdded.setText(task.getDateTaskAdded());
        //reset progress bar status
        progress = task.getCompleteHours().intValue()/(Integer.valueOf(task.getTargetHours()) * 60 * 60 * 10);
        holder.progressBar.setProgress(progress);

        Log.d("progress", progress+" CompleteHours:"+task.getCompleteHours()+" target:"+Integer.valueOf(task.getTargetHours()));
    }

    @Override
    public int getItemCount() {
        return taskItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView taskName;
        public TextView targetHours;
        public TextView dateAdded;
        public TextView taskDetail;
        public Button editButton;
        public Button deleteButton;
        public Button startButton;
        private Button saveButton;
        private Button cancelButton;
        private NumberProgressBar progressBar;
        public RelativeLayout viewBackground, viewForeground;

        public int id;

        public ViewHolder(View view, Context ctx) {
            super(view);
            context = ctx;

            taskName = view.findViewById(R.id.name);
            dateAdded = view.findViewById(R.id.dateAdded);

            editButton = view.findViewById(R.id.editButton);
            deleteButton = view.findViewById(R.id.deleteButton);
            startButton = view.findViewById(R.id.startButton);
            progressBar = view.findViewById(R.id.progressBarInRow);

            editButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
            startButton.setOnClickListener(this);

            viewBackground = view.findViewById(R.id.view_background);
            viewForeground = view.findViewById(R.id.view_foreground);
            //When Click Item Card
            //send data intent to next screen timer screen
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int position = getAdapterPosition();
                    Task task = taskItems.get(position);
                    Intent intent = new Intent(context, TimerActivity.class);
                    intent.putExtra("taskName", task.getTaskName());
                    intent.putExtra("targetHours", task.getTargetHours());
                    intent.putExtra("id", task.getId());
                    intent.putExtra("detail", task.getTaskDetail());
                    intent.putExtra("completeHours", task.getCompleteHours());
                    context.startActivity(intent);
                }
            });
        }

        @Override
        //button on click methods options
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.editButton:
                    int position = getAdapterPosition();
                    //on current position item
                    Task task = taskItems.get(position);
                    editTask(task);
                    break;

                case R.id.deleteButton:
                    position = getAdapterPosition();
                    task = taskItems.get(position);
                    deleteTask(task.getId());
                    break;

                case R.id.startButton:
                    position = getAdapterPosition();
                    task = taskItems.get(position);
                    startTask(task);
                    break;
            }
        }

        public void deleteTask(final int id) {

            //create an AlertDialog
            alertDialogBuilder = new AlertDialog.Builder(context);

            inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.comfirm_dialog, null);

            Button noButton = view.findViewById(R.id.noButton);
            Button yesButton = view.findViewById(R.id.yesButton);

            alertDialogBuilder.setView(view);
            dialog = alertDialogBuilder.create();
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
                    taskItems.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    dialog.dismiss();
                }
            });
        }

        public void startTask(Task task) {
            int position = getAdapterPosition();
            task = taskItems.get(position);
            Intent intent = new Intent(context, TimerActivity.class);
            intent.putExtra("taskName", task.getTaskName());
            intent.putExtra("targetHours", task.getTargetHours());
            intent.putExtra("id", task.getId());
            intent.putExtra("detail", task.getTaskDetail());
            intent.putExtra("completeHours", task.getCompleteHours());

            context.startActivity(intent);
        }

        public void editTask(final Task task) {

            alertDialogBuilder = new AlertDialog.Builder(context);

            inflater = LayoutInflater.from(context);
            final View view = inflater.inflate(R.layout.edit_task, null);
            //new edit View view UI items
            final TextView title;
            final TextView taskName;
            final TextView targetHours;
            final TextView taskDetail;

            taskName = view.findViewById(R.id.task_name);
            taskDetail = view.findViewById(R.id.hint_taskDetail);
            targetHours = view.findViewById(R.id.hourTarget);
            title = view.findViewById(R.id.new_task);

            saveButton = view.findViewById(R.id.saveButton);
            cancelButton = view.findViewById(R.id.cancelButton);
            title.setText("Edit Task");
            taskName.setText(task.getTaskName());
            taskDetail.setText(task.getTaskDetail());
            targetHours.setText(task.getTargetHours());

            alertDialogBuilder.setView(view);
            dialog = alertDialogBuilder.create();
            dialog.show();

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseHandler db = new DatabaseHandler(context);

                    task.setTaskName(taskName.getText().toString());
                    task.setTargetHours(targetHours.getText().toString());
                    task.setTaskDetail(taskDetail.getText().toString());
                    if (!taskName.getText().toString().isEmpty()
                            && !targetHours.getText().toString().isEmpty()) {
                        Snackbar.make(v, "Task Saved!", Snackbar.LENGTH_LONG).show();
                        db.updateTask(task);
                        notifyItemChanged(getAdapterPosition(),task);
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

    }//end of ViewHolder

    public void removeItem(int position) {
        // notify the item removed by position
        DatabaseHandler db = new DatabaseHandler(context);
        //delete item
        Task task = taskItems.get(position);
        taskItems.remove(position);
        db.deleteTask(task.getId());
        notifyItemRemoved(position);
    }

    public void restoreItem(Task task, int position) {
        DatabaseHandler db = new DatabaseHandler(context);
        taskItems.add(position, task);
        // notify item added by position
        db.addTask(task);
        notifyItemInserted(position);
    }
}
