package com.swordrunner.swordrunner.ui.profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.data.model.Comment;
import com.swordrunner.swordrunner.data.model.User;

import java.util.Date;
import java.util.ArrayList;

public class MyCommentActivity extends AppCompatActivity {

    private ArrayList<Comment> comments=new ArrayList<>();
    private ArrayList<User> users=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_comment);
        initdata();

        Log.d("act","Comments init success");
        RecyclerView commentRV = findViewById(R.id.commentRecylerView);
        MyCommentAdapter adapter=new MyCommentAdapter(MyCommentActivity.this);
        adapter.setComments(comments);
        adapter.setUsers(users);
        Log.d("act","ItemCount: "+adapter.getItemCount());

        commentRV.setAdapter(adapter);
        commentRV.setLayoutManager(new LinearLayoutManager(this));


    }

    private void initdata(){
        // Test data
        Date date=new Date();
        comments.add(new Comment("Bob","Alice","Hello. He is a good man", (float) 4, date,"https://image.brigitte.de/10883164/t/u3/v5/w1440/r0/-/attraktive-stars--margot-robbie.jpg"));
        comments.add(new Comment("James","Helena","Hello, world", (float) 2,date,"https://image.brigitte.de/10883164/t/u3/v5/w1440/r0/-/attraktive-stars--margot-robbie.jpg"));
        comments.add(new Comment("Wang","Kim","What's up Bro", (float) 2,date,"https://image.brigitte.de/10883164/t/u3/v5/w1440/r0/-/attraktive-stars--margot-robbie.jpg"));


        users.add(new User("001", "Margot@gmail.com", "Margot Robbie", "https://image.brigitte.de/10883164/t/u3/v5/w1440/r0/-/attraktive-stars--margot-robbie.jpg"));
        users.add(new User("002", "Margot@gmail.com", "Tom Robbie", "https://de.web.img3.acsta.net/pictures/15/09/25/11/13/078050.jpg"));
        users.add(new User("003", "Margot@gmail.com", "Margot Robin", "https://upload.wikimedia.org/wikipedia/commons/2/27/Saoirse_Ronan_at_BAFTA_2016_%281%29_%28cropped%29.jpg"));


    }
}