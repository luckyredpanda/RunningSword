package com.swordrunner.swordrunner.ui.profile.pwdchange;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.material.snackbar.Snackbar;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.Credentials;
import com.swordrunner.swordrunner.data.model.User;

import com.swordrunner.swordrunner.databinding.ActivityPasswordChangeBinding;
import com.swordrunner.swordrunner.ui.login.LoginActivity;
import com.swordrunner.swordrunner.ui.profile.ChangeUserNameActivity;


public class PasswordChangeActivity extends AppCompatActivity {
    private PasswordViewModel passwordViewModel;
    private User currentuser;
    private static final int MINPASSWORDLENGTH = 5;
    private static final String TAG="WritetoDB";
    ActivityPasswordChangeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_password_change);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Change Password");
        passwordViewModel= new ViewModelProvider(this).get(PasswordViewModel.class);
        currentuser=getData(getUserId());
        initButton();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_change,menu);
        return super.onCreateOptionsMenu(menu);
    }
    private void initButton(){
        binding.btnPasswordChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String TAG="STPWD";
                boolean isequal=passwordViewModel.changePWD(binding.editTextTextPassword.getText().toString(),binding.editTextTextPasswordConfim.getText().toString());
                if(isequal){
                    Log.d(TAG,"NewPassword:"+passwordViewModel.getPwd().getValue());
                    Credentials credentials=new Credentials(currentuser.getEmail(),passwordViewModel.getPwd().getValue());
                    postData(credentials);
                    startLoginActivity();
                }else
                {
                    Toast.makeText(getApplicationContext(), "Plesase enter same password!", Toast.LENGTH_SHORT).show();
                    binding.editTextTextPassword.setText("");
                    binding.editTextTextPasswordConfim.setText("");
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_save:
                closesoftKeyboard();
                String newPassword=binding.editTextTextPassword.getText().toString();
                String newPasswordConfirm=binding.editTextTextPasswordConfim.getText().toString();
                boolean isequal = passwordViewModel.changePWD(newPassword, newPasswordConfirm);
                if(isequal&&!newPassword.isEmpty()){
                    if (checkPasswordFormat(newPassword)){
                        Credentials credentials=new Credentials(currentuser.getEmail(),passwordViewModel.getPwd().getValue());
                        postData(credentials);
                        startLoginActivity();
                    }
                }else {
                    Toast.makeText(this, "Plesase enter same and vaild password!", Toast.LENGTH_SHORT).show();
                    binding.editTextTextPassword.setText("");
                    binding.editTextTextPasswordConfim.setText("");
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private boolean checkPasswordFormat(String password) {
        if (password.length()<MINPASSWORDLENGTH){
            String msg="The length of your new password must above "+String.valueOf(MINPASSWORDLENGTH);
            Snackbar.make(findViewById(android.R.id.content),msg,Snackbar.LENGTH_LONG).show();
            return false;
        }else return true;
    }


    private void postData(Credentials credentials) {
        Call<User> call= Client.get(this,false).create(UserRes.class).change(credentials);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code()!=200){
                    Snackbar.make(findViewById(android.R.id.content),"Failed to change!",Snackbar.LENGTH_LONG);
                }else {
                    startLoginActivity();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Snackbar.make(findViewById(android.R.id.content), "Something went wrong", Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    private User getData(String id) {
        User user = new User("","","","");
        Call<User> call = Client.get(this).create(UserRes.class).get(id);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                user.setName(response.body().getName());
                user.setEmail(response.body().getEmail());
                user.setAvatarUrl(response.body().getAvatarUrl());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "onFailure: ",t);
                String msg=getResources().getString(R.string.PasswordChangeFailed);
                Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
            }

        });
        return user;
    }

    //get current user id from shared preference
    private String getUserId(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("swordrunnerpref", Context.MODE_PRIVATE);
        String currentUserId = pref.getString("id","").toString();
        String currentName = pref.getString("username","").toString();
        return currentUserId;
    }

    //TODO  There will be an potential cache leak problem. Some activities called before will not be destroyed.
    public void startLoginActivity(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("swordrunnerpref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
        Intent intent=new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    private void closesoftKeyboard(){
        View view=this.getCurrentFocus();
        if(view!=null){
            InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

}