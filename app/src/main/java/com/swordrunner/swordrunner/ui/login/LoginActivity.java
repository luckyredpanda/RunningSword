package com.swordrunner.swordrunner.ui.login;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.swordrunner.swordrunner.ui.MainActivity;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.Credentials;
import com.swordrunner.swordrunner.data.model.LoggedInUser;
import com.swordrunner.swordrunner.ui.register.RegisterActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ProgressBar loadingProgressBar = null;
    private static final int MINPASSWORDLENGTH = 5;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // already logged in?
        SharedPreferences pref = getSharedPreferences("swordrunnerpref", MODE_PRIVATE);
        String token = pref.getString("access_token", null);
        if (token != null) {
            startMainActivity();
            finish();
        }


        setContentView(R.layout.activity_login);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final TextView registerView = findViewById(R.id.tv_register);
        final TextInputLayout tilUsername=findViewById(R.id.textInputLayoutUsername);
        final TextInputLayout tilPassword=findViewById(R.id.textInputLayoutPassword);

        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");
        usernameEditText.setText(email);
        passwordEditText.setText(password);



        loadingProgressBar = findViewById(R.id.loading);

        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                tilUsername.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                tilPassword.setEndIconVisible(true);
                tilPassword.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        loginButton.setOnClickListener(v -> {
            closesoftKeyboard();
            if (usernameEditText.getText().toString().isEmpty() || passwordEditText.getText().toString().length() < MINPASSWORDLENGTH) {
                tilPassword.setErrorEnabled(true);
                tilPassword.setEndIconVisible(false);
                tilPassword.setError("The length of password should be more then " + MINPASSWORDLENGTH);
            }else if(isEmail(usernameEditText.getText().toString())){
                loadingProgressBar.setVisibility(View.VISIBLE);

                Credentials cred = new Credentials(
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString()
                );
                login(cred);
            } else {
                tilUsername.setErrorEnabled(true);
                tilUsername.setEndIconVisible(false);
                tilUsername.setError("Please enter an vaild email address!");
            }
        });
        registerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void login(Credentials credentials) {
        Call<LoggedInUser> call = Client.get(this, false).create(UserRes.class).login(credentials);
        call.enqueue(new Callback<LoggedInUser>() {
            @Override
            public void onResponse(Call<LoggedInUser> call, Response<LoggedInUser> response) {
                if (response.code() == 404) {
                    Snackbar.make(findViewById(android.R.id.content), "Account doesn't exist", Snackbar.LENGTH_SHORT).show();
                }
                else if(response.code()==403){
                    Snackbar.make(findViewById(android.R.id.content), "Password wrong", Snackbar.LENGTH_SHORT).show();
                }
                else {
                    storeLogin(response.body());
                    finish();
                    startMainActivity();
                }
                loadingProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<LoggedInUser> call, Throwable t) {
                Snackbar.make(findViewById(android.R.id.content), "Something went wrong", Snackbar.LENGTH_SHORT).show();
                loadingProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void storeLogin(LoggedInUser tokens) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("swordrunnerpref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("id", tokens.id);
        editor.putString("access_token", tokens.accessToken);
        editor.putString("refresh_token", tokens.refreshToken);
        editor.apply();
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