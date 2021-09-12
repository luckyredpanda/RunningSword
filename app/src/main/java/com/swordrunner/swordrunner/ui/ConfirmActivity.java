package com.swordrunner.swordrunner.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.swordrunner.swordrunner.R;

public class ConfirmActivity extends AppCompatActivity {

    private Button conformButton;
    private TextView conformTextView;
    int type;// 0 create a game, 1 added in a game, 2 some one left this game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        type = getIntent().getIntExtra("type",0);
        initView();
        initConformButton();
    }
    private void initView(){
        conformButton = findViewById(R.id.button_confirm);
        conformTextView = findViewById(R.id.textViewConfirm);
        if(type == 0)
            conformTextView.setText("You have created a game");
        else if(type == 1)
            conformTextView.setText("You are added in a new MultiGame");
        else if(type == 2)
            conformTextView.setText("Someone has left your MultiGame");
        else if(type == 3)
            conformTextView.setText("Someone has finished a run in your MultiGame");
    }
    private void initConformButton(){
        conformButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConfirmActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}