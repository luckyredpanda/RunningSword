package com.swordrunner.swordrunner.ui.profile.mycomments;

import androidx.lifecycle.ViewModel;

import com.swordrunner.swordrunner.data.model.User;

public class MyCommentViewModel extends ViewModel {
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
