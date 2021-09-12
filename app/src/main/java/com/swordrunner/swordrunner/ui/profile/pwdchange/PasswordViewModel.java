package com.swordrunner.swordrunner.ui.profile.pwdchange;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PasswordViewModel extends ViewModel {
    private MutableLiveData<String> pwd;

    public MutableLiveData<String> getPwd(){
        if(pwd==null){
            pwd=new MutableLiveData<>();
            pwd.setValue("888888");     //Default Password
        }
        return pwd;
    }
    public boolean changePWD(String newPwd,String pwdconfim){
        if(newPwd.equals(pwdconfim)){
            getPwd();
            pwd.setValue(newPwd);
            return true;
        }else return false;
    }

}
