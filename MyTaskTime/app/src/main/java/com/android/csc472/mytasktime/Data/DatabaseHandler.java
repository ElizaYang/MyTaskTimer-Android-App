package com.android.csc472.mytasktime.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android.csc472.mytasktime.Model.Task;
import com.android.csc472.mytasktime.Util.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Shuonan on 11/7/2017.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private Context ctx;
    public DatabaseHandler(Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
        this.ctx = context;
    }

    @Override
    //Create SQLite db table
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TASK_TABLE = "CREATE TABLE " + Constants.TABLE_NAME + "("
                + Constants.KEY_ID + " INTEGER PRIMARY KEY,"
                + Constants.KEY_TASK_ITEM + " TEXT,"
                + Constants.KEY_TARGET_HOURS + " TEXT,"
                + Constants.KEY_DATE_ADDED + " LONG,"
                + Constants.KEY_TASK_DETAIL + " TEXT,"
                + Constants.KEY_COMPLETE_HOURS + " LONG"
                + ")";

        db.execSQL(CREATE_TASK_TABLE);
    }

    @Override
    //execSQL
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME);

        onCreate(db);
    }
    /**
     *  CRUD OPERATIONS: Create, Read, Update, Delete Methods
     */
    //CREATE Task
    public void addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.KEY_TASK_ITEM, task.getTaskName());
        values.put(Constants.KEY_TASK_DETAIL, task.getTaskDetail());
        values.put(Constants.KEY_TARGET_HOURS, task.getTargetHours());
        values.put(Constants.KEY_COMPLETE_HOURS, task.getCompleteHours());
        values.put(Constants.KEY_DATE_ADDED, java.lang.System.currentTimeMillis());

        //Insert the row
        db.insert(Constants.TABLE_NAME, null, values);

        Log.d("Saved!!", "Saved to DB");
    }

    //Get a Task
    public Task getTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(Constants.TABLE_NAME, new String[] {
                        Constants.KEY_ID,
                        Constants.KEY_TASK_ITEM,
                        Constants.KEY_TARGET_HOURS,
                        Constants.KEY_DATE_ADDED,
                        Constants.KEY_TASK_DETAIL,
                        Constants.KEY_COMPLETE_HOURS},
                Constants.KEY_ID + "=?",
                new String[] {String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();//move cursor to the first row


        Task task = new Task();
        task.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.KEY_ID))));
        task.setTaskName(cursor.getString(cursor.getColumnIndex(Constants.KEY_TASK_ITEM)));
        task.setTaskDetail(cursor.getString(cursor.getColumnIndex(Constants.KEY_TASK_DETAIL)));
        task.setTargetHours(cursor.getString(cursor.getColumnIndex(Constants.KEY_TARGET_HOURS)));
        task.setCompleteHours(cursor.getLong(cursor.getColumnIndex(Constants.KEY_COMPLETE_HOURS)));
        //convert timestamp to something readable
        java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
        String formatedDate = dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(Constants.KEY_DATE_ADDED)))
                .getTime());

        task.setDateTaskAdded(formatedDate);
        return task;
    }

    //Get all Tasks
    public List<Task> getAllTasks() {
        SQLiteDatabase db = this.getReadableDatabase();

        List<Task> taskList = new ArrayList<>();

        Cursor cursor = db.query(Constants.TABLE_NAME, new String[] {
                Constants.KEY_ID,
                Constants.KEY_TASK_ITEM,
                Constants.KEY_TARGET_HOURS,
                Constants.KEY_DATE_ADDED,
                Constants.KEY_COMPLETE_HOURS,
                Constants.KEY_TASK_DETAIL},
                null, null, null, null, Constants.KEY_DATE_ADDED + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.KEY_ID))));
                task.setTaskName(cursor.getString(cursor.getColumnIndex(Constants.KEY_TASK_ITEM)));
                task.setTaskDetail(cursor.getString(cursor.getColumnIndex(Constants.KEY_TASK_DETAIL)));
                task.setTargetHours(cursor.getString(cursor.getColumnIndex(Constants.KEY_TARGET_HOURS)));
                task.setCompleteHours(cursor.getLong(cursor.getColumnIndex(Constants.KEY_COMPLETE_HOURS)));

                //convert timestamp to something readable
                java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
                String formatedDate = dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(Constants.KEY_DATE_ADDED)))
                        .getTime());

                task.setDateTaskAdded(formatedDate);

                // Add to the taskList
                taskList.add(task);

            }while (cursor.moveToNext());
        }

        return taskList;
    }

    //Updated Task
    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.KEY_TASK_ITEM, task.getTaskName());
        values.put(Constants.KEY_TASK_DETAIL, task.getTaskDetail());
        values.put(Constants.KEY_TARGET_HOURS, task.getTargetHours());
        values.put(Constants.KEY_COMPLETE_HOURS, task.getCompleteHours());//long
        values.put(Constants.KEY_DATE_ADDED, java.lang.System.currentTimeMillis());//get system time

        //update row
        return db.update(Constants.TABLE_NAME, values, Constants.KEY_ID + "=?", new String[] { String.valueOf(task.getId())} );
    }

    //Updated Hours
    public int updateCompleteHours(Task task, Long completeHours) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.KEY_COMPLETE_HOURS, completeHours);
        values.put(Constants.KEY_TASK_ITEM, task.getTaskName());
        values.put(Constants.KEY_TASK_DETAIL, task.getTaskDetail());
        values.put(Constants.KEY_TARGET_HOURS, task.getTargetHours());
        values.put(Constants.KEY_DATE_ADDED, java.lang.System.currentTimeMillis());//get system time

        //update row
        return db.update(Constants.TABLE_NAME, values, Constants.KEY_ID + "=?", new String[] { String.valueOf(task.getId())} );
    }

    //Delete Task
    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Constants.TABLE_NAME, Constants.KEY_ID + " = ?",
                new String[] {String.valueOf(id)});

        db.close();
    }

    //Get count
    public int getTaskCount() {
        String countQuery = "SELECT * FROM " + Constants.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }
}
