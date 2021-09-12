package com.swordrunner.swordrunner.ui.register;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.Credentials;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.ui.login.LoginActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPwd, etConfPwd;
    private Button registerBt;
    private TextView hint;
    private static final int MINPASSWORDLENGTH = 175;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("Register");
        initialView();

    }
    private void initialView(){
        etEmail = findViewById(R.id.ed_register_email);
        etConfPwd = findViewById(R.id.ed_conform_register_password);
        etPwd = findViewById(R.id.ed_register_password);
        registerBt = findViewById(R.id.bt_register);
        hint = findViewById(R.id.tv_hint);
        registerBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closesoftKeyboard();
                hint.setVisibility(View.INVISIBLE);
                String email = etEmail.getText().toString();
                String password = etPwd.getText().toString();
                String passwordConf = etConfPwd.getText().toString();
                if(email.equals("")||password.equals("")||passwordConf.equals("")){
                    hint.setVisibility(View.VISIBLE);
                    hint.setText("Please fill in all the text above");
                }else if (!isEmail(email)){
                    hint.setVisibility(View.VISIBLE);
                    hint.setText("Please enter an vaild email address");
                }
                else{
                    if(!password.equals(passwordConf)){
                        hint.setVisibility(View.VISIBLE);
                        hint.setText("Please fill in the same password");
                    }else if(password.length()<5||passwordConf.length()<5){
                        hint.setVisibility(View.VISIBLE);
                        hint.setText("Password should be no less than 5 characters");
                    }
                    else{
                        register(email, password, new RegisterCallback() {
                            @Override
                            public void onSuccess(boolean status) {
                                if(status) {
                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("email",email);
                                    bundle.putString("password",password);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure() {

                            }
                        });
                    }
                }
            }
        });
    }
    private void register(String email,String password,RegisterCallback registerCallback){
        Credentials credentials = new Credentials(email,password);
        Call<User> call = Client.get(this,false).create(UserRes.class).register(credentials);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code()==200){
                    Toast.makeText(RegisterActivity.this, "success", Toast.LENGTH_SHORT).show();
                    registerCallback.onSuccess(true);
                }
                if(response.code()==404){
                    Snackbar.make(findViewById(android.R.id.content),"This email address has been used. Please use a new one",Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "failure", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public interface RegisterCallback{
        void onSuccess(boolean status);
        void onFailure();
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