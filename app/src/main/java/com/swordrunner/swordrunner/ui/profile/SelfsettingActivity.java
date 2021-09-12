package com.swordrunner.swordrunner.ui.profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.MapRes;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.GenericResponse;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.databinding.ActivitySelfsettingBinding;
import com.swordrunner.swordrunner.ui.CreateGameActivity;
import com.swordrunner.swordrunner.ui.home.HomeFragment;
import com.swordrunner.swordrunner.ui.login.LoginActivity;
import com.swordrunner.swordrunner.ui.profile.emailchange.EmailChangeActivity;
import com.swordrunner.swordrunner.ui.profile.emailchange.EmailChangeViewModel;
import com.swordrunner.swordrunner.ui.profile.pwdchange.PasswordChangeActivity;

public class SelfsettingActivity extends AppCompatActivity {

    private CircleImageView avatar;
    private TextView username, email;
    private TextView changeAvatar, changeUsername, changePassword, changeEmail;
    private String id;
    private BroadcastReceiver broadcastReceiver;
    private ActivitySelfsettingBinding binding;
    private EmailChangeViewModel emailChangeViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfsetting);

        binding= DataBindingUtil.setContentView(this,R.layout.activity_selfsetting);
        initializeView();
        emailChangeViewModel=new ViewModelProvider(this).get(EmailChangeViewModel.class);
        emailChangeViewModel.setContext(getApplicationContext());
        emailChangeViewModel.getEmail().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.vtMyemail.setText(emailChangeViewModel.getEmail().getValue());
            }
        });


        id = getUserId();
        getData(id);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action.refreshName");
        intentFilter.addAction("action.refreshAvatar");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals("action.refreshName")) {
                    getData(id);
                }else if(action.equals("action.refreshAvatar")){
                    Log.d("refreshAvatar", "received message");
                    getData(id);
                }
            }
        };
        registerReceiver(broadcastReceiver,intentFilter);
    }
    protected void onResume() {
        SharedPreferences spf=getSharedPreferences(getString(R.string.User_Data),Context.MODE_PRIVATE);
        binding.vtMyemail.setText(spf.getString("email",""));
        super.onResume();
    }
    private void initializeView(){
        username = findViewById(R.id.username_set);
        email = findViewById(R.id.vt_myemail);
        changeAvatar = findViewById(R.id.vt_changeusr);
        changeEmail = findViewById(R.id.vt_Email);
        changePassword = findViewById(R.id.vt_password);
        changeUsername = findViewById(R.id.vt_username);
        avatar = findViewById(R.id.userImagesetting);
        changeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startChangeAvatarActivity();
            }
        });
        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEmailChangeActivity();
            }
        });
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startChangePwdActivity();
                }
        });
        changeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(SelfsettingActivity.this, "change Username selected", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),ChangeUserNameActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("username",username.getText().toString());
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });
        binding.vtSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        binding.vtDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SelfsettingActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                builder.setTitle("This will delete your account and all your data irreversibly.");

                builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAccount();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    /**
     * deletes the current account and all its data
     */
    private void deleteAccount() {
        Call<Void> call = Client.get(this).create(UserRes.class).wipe();
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                logout();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }

    /**
     * get user data from server
     * @param id
     */
    private void getData(String id) {
        User user = new User("","","","");
        Call<User> call = Client.get(this).create(UserRes.class).get(id);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                user.setName(response.body().getName());
                user.setEmail(response.body().getEmail());
                user.setAvatarUrl(response.body().getAvatarUrl());
                if(!user.getAvatarUrl().equals("")){
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(user.getAvatarUrl())
                            .into(avatar);
                }
                email.setText(user.getEmail());
                username.setText(user.getName());

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }

        });
    }
    //get current user id from shared preference
    private String getUserId(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("swordrunnerpref", Context.MODE_PRIVATE);
        String currentUserId = pref.getString("id","").toString();
        String currentName = pref.getString("name","").toString();
        return currentUserId;
    }
    private void startEmailChangeActivity() {
        Intent intent = new Intent(this, EmailChangeActivity.class);
        startActivity(intent);
    }

    private void startChangePwdActivity() {
        Intent intent=new Intent(this, PasswordChangeActivity.class);
        startActivity(intent);
        finish();
    }
    private void startChangeAvatarActivity(){
        Intent intent = new Intent(this,AvatarChangeActivity.class);
        startActivity(intent);
    }

    /**
     * signs out user by removing his data on the phone
     */
    private void logout() {
        SharedPreferences pref = this.getSharedPreferences("swordrunnerpref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        if (getParent() != null)
            getParent().finish();
        finish();
    }
}