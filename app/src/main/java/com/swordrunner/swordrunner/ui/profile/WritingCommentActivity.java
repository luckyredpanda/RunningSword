package com.swordrunner.swordrunner.ui.profile;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.databinding.ActivityWritingCommentBinding;



public class WritingCommentActivity extends AppCompatActivity {

    private EditText edcomment;
    private RatingBar rating;
    private ActivityWritingCommentBinding binding;
    private TextView usr;
    private TextView sDay;
    private TextView commnet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing_comment);
        binding=ActivityWritingCommentBinding.inflate(getLayoutInflater());
        edcomment =findViewById(R.id.editTextTextMultiLine);
        rating=findViewById(R.id.ratingBar);

        getData();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Writing Comment");





    }

    private void getData() {
        usr=findViewById(R.id.vt_username_userprofile);
        //commnet=findViewById(R.id.vt_comment_userprofile);



    }

    //initial menu in toolbar

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_change,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_save:
                if (edcomment.getText()!=null && rating.getRating()!=0){
                    storeData();
                    finish();
                }else {
                    Toast.makeText(WritingCommentActivity.this,"Warning!! Please fill all editText!!",Toast.LENGTH_SHORT);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void storeData() {

        //TODO
        String TAG="StData";
        Log.d(TAG, edcomment.getText().toString());
        Log.d(TAG, "Rating："+rating.getRating());
    }
}

