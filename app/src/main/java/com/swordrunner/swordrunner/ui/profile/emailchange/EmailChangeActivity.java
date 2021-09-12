package com.swordrunner.swordrunner.ui.profile.emailchange;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.FriendRes;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.Friend;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.databinding.ActivityEmailChangeBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmailChangeActivity extends AppCompatActivity {
    ActivityEmailChangeBinding binding;
    EmailChangeViewModel emailChangeViewModel;
    private static final String TAG = "EmailChangeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_email_change);
        emailChangeViewModel = new ViewModelProvider(this).get(EmailChangeViewModel.class);
        emailChangeViewModel.setContext(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Changing Email Address");

    }

    private void initButton(){
        binding.btnEmailChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newemail = binding.editTextTextEmailAddress.getText().toString();
                if (!newemail.isEmpty()) {
                    emailChangeViewModel.changeEmail(newemail);
                    postData(emailChangeViewModel.getUser());
                    Friend friend1 = new Friend("","","","","","","","","");
                    Friend friend2 = new Friend("","","","","","","","","");
                    friend1.setUserId(emailChangeViewModel.getUser().getId());
                    friend2.setFriendId(emailChangeViewModel.getUser().getId());
                    friend1.setUserEmail(newemail);
                    friend2.setFriendEmail(newemail);
                    changeUserEmail(friend1);
                    changeFriendEmail(friend2);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_change, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                closesoftKeyboard();
                showConfimdialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showConfimdialog() {
        String msg=getResources().getString(R.string.EmailChangeConfimDialogMsg);
        AlertDialog.Builder alert=new AlertDialog.Builder(this);
        alert.setMessage(msg);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeEmail();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        alert.create().show();
    }

    //Change Email and also the email address related to friend is changed. So, friend has the last
    //email address.
    private void changeEmail() {
        String newemail = binding.editTextTextEmailAddress.getText().toString();
        if (!newemail.isEmpty()&&isEmail(newemail)) {
            emailChangeViewModel.changeEmail(newemail);
            postData(emailChangeViewModel.getUser());
            Friend friend1 = new Friend("","","","","","","","","");
            Friend friend2 = new Friend("","","","","","","","","");
            friend1.setUserId(emailChangeViewModel.getUser().getId());
            friend2.setFriendId(emailChangeViewModel.getUser().getId());
            friend1.setUserEmail(newemail);
            friend2.setFriendEmail(newemail);
            changeUserEmail(friend1);
            changeFriendEmail(friend2);
        }else {
            String msg=getResources().getString(R.string.EmailCheckFailed);
            Snackbar.make(findViewById(android.R.id.content),msg,Snackbar.LENGTH_LONG).show();
        }
    }


    private void showEmptyDialog() {
        AlertDialog.Builder alert=new AlertDialog.Builder(this);
        alert.setMessage("Sorry, there is no comment to delete!");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.create().show();
    }

    private void changeUserEmail(Friend friend){
        Call<Friend> call = Client.get(this,false).create(FriendRes.class).updateUserEmail(friend);
        call.enqueue(new Callback<Friend>() {
            @Override
            public void onResponse(Call<Friend> call, Response<Friend> response) {
                if(response.code() == 200)
                    Log.d(TAG, "onResponse: change user email success");
            }
            @Override
            public void onFailure(Call<Friend> call, Throwable t) {

            }
        });
    }
    private void changeFriendEmail(Friend friend){
        Call<Friend> call = Client.get(this,false).create(FriendRes.class).updateFriendEmail(friend);
        call.enqueue(new Callback<Friend>() {
            @Override
            public void onResponse(Call<Friend> call, Response<Friend> response) {
                if(response.code() == 200){
                    Log.d(TAG, "onResponse: change friend email success");
                }
            }

            @Override
            public void onFailure(Call<Friend> call, Throwable t) {

            }
        });
    }
    private void postData(User user) {
        Call<User> call = Client.get(this, false).create(UserRes.class).changeEM(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code()==404){
                    String msg="This email address has been used.Please change another one.";
                    Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show();
                }else {
                    finish();
                    Toast.makeText(EmailChangeActivity.this,"Change email successfully!",Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_SHORT).show();
            }


        });

    }

    private void closesoftKeyboard(){
        View view=this.getCurrentFocus();
        if(view!=null){
            InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }
    private boolean isEmail(String strEmail) {
        String strPattern = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";  //Perl Regular Expression for email format check
        if (TextUtils.isEmpty(strPattern)) {
            return false;
        } else {
            return strEmail.matches(strPattern);
        }
    }
}