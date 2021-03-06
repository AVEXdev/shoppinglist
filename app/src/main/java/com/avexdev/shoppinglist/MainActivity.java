package com.avexdev.shoppinglist;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class MainActivity extends AppCompatActivity {
    private ArrayList<ListItem> arrayList;
    private RecyclerView mRecyclerView;
    private ListAdapter mAdapter;
    private TextView nrOfItemsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Oncreate", "ONCREATE");
        if(savedInstanceState != null){
            Log.d("SAVEDINSTANCE", "DATA LOADED");
            arrayList = savedInstanceState.getParcelableArrayList("Array");
        }else{
            Log.d("ONSAVEDINSTANCESTATE", "NEW ARRAY");
            arrayList = new ArrayList<>();
            loadData();
        }
        setContentView(R.layout.activity_main_list);
        mRecyclerView = findViewById(R.id.recyclerView);
        nrOfItemsTextView = findViewById(R.id.textViewItems);
        recycleSetup();
        swipetoDelete();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddNewItemActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("SAVED", "ONSAVEDINSTANCE");
        outState.putParcelableArrayList("Array", arrayList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK ){
            super.onActivityResult(requestCode, resultCode, data);
            Log.d("ONACTIVITYRESULT", "ONACTIVITYRESULT");
            String item = data.getStringExtra("Item");
            arrayList.add(new ListItem(item, false));
            recycleSetup();
            mRecyclerView.getAdapter().notifyItemInserted(arrayList.size());
            saveData();
        }
    }

    private void loadData(){
        String filename = "SaveData.json";
        FileInputStream inputStream;
        try{
            inputStream = openFileInput(filename);
            Reader reader = new BufferedReader(new InputStreamReader(inputStream));
            Gson gson = new Gson();
            Type collectionType = new TypeToken<ArrayList<ListItem>>(){}.getType();
            arrayList = gson.fromJson(reader, collectionType);
            reader.close();
            Log.d("Data loaded:", "" + arrayList);
        }catch (Exception e){
            Log.e("Can´t load data", "", e);
        }
    }

    public void saveData(){
        String filename = "SaveData.json";
        FileOutputStream outputStream;
        try{
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            Writer writer = new OutputStreamWriter(outputStream);
            Gson gson = new Gson();
            gson.toJson(arrayList, writer);
            writer.close();
            Log.d("Data saved:", "" + arrayList);
        }catch (Exception e){
            Log.e("Can´t save data", "", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    private void swipetoDelete(){
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback() {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                mAdapter.removeItem(position);
                saveData();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void recycleSetup(){
        mRecyclerView = findViewById(R.id.recyclerView);
        mAdapter = new ListAdapter(this, arrayList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        arrayList.clear();
        recycleSetup();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        saveData();

        //noinspection SimplifiableIfStatement
        if (id == R.id.remove_all) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
