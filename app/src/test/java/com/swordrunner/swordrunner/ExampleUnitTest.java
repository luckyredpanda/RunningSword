package com.swordrunner.swordrunner;

import android.content.Context;

import com.swordrunner.swordrunner.Utils.UserOwnInfo;
import com.swordrunner.swordrunner.data.model.User;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    Context;
    @Test
    public void addition_isCorrect() {
        User user=initUser();
        UserOwnInfo userOwnInfo=new UserOwnInfo();
    }

    private User initUser(){
        String id="oneTest";
        String email="djfoai@gmail.com";
        String avatar="http://www.google.com";
        String name="Bob";
        return new User(id,email,name,avatar);
    }
}