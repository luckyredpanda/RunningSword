package com.swordrunner.swordrunner.ui.profile;



import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;

public class ProfileViewModel extends ViewModel {

    private MutableLiveData<String> comment01;
    private MutableLiveData<String> comment02;
    private MutableLiveData<String> date;
    private MutableLiveData<Integer> distance;

    private MutableLiveData<String> game01;
    private MutableLiveData<String> game02;
    private MutableLiveData<String> game03;
    private MutableLiveData<String> game04;
    private MutableLiveData<String> username;

    public MutableLiveData<String> getUsername() {
        if(username==null){
            username = new MutableLiveData<>();
            username.setValue("Habara Ai");
        }
        return username;
    }

    public void setUsername(String s) {
        this.username.setValue(s);
    }

    public MutableLiveData<String> getDate() {
        if(date==null){
            date=new MutableLiveData<>();
            long currentTime = System.currentTimeMillis();
            String timeNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentTime);
            date.setValue(timeNow);
        }
        return date;
    }

    public MutableLiveData<Integer> getDistance() {
        if(distance ==null){
            distance=new MutableLiveData<>();
            distance.setValue(0);
        }
        return distance;
    }

    public MutableLiveData<String> getComment01() {
        if (comment01 == null){
            comment01 = new MutableLiveData<>();
            comment01.setValue("You are so nice ");
        }
        return comment01;
    }



    public MutableLiveData<String> getComment02() {
        if (comment02 == null){
            comment02 = new MutableLiveData<>();
            comment02.setValue("You are so nice ");
        }
        return comment02;
    }



    public MutableLiveData<String> getGame01() {
        if (game01 == null){
            game01 = new MutableLiveData<>();
            game01.setValue("GAME single ");
        }
        return game01;
    }



    public MutableLiveData<String> getGame02() {
        if (game02 == null){
            game02 = new MutableLiveData<>();
            game02.setValue("GAME group");
        }
        return game02;
    }


    public MutableLiveData<String> getGame03() {
        if (game03== null){
            game03 = new MutableLiveData<>();
            game03.setValue("GAME group");
        }
        return game03;
    }


    public MutableLiveData<String> getGame04() {
        if (game04== null){
            game04 = new MutableLiveData<>();
            game04.setValue("GAME group");
        }
        return game04;
    }

 /*
    private MutableLiveData<Image> userPicture01;
    public MutableLiveData<Image> getUserPicture() {
        if(userPicture01 == null){
            userPicture01 = new MutableLiveData<>();
            userPicture.setValue();
        }
        return userPicture01;
    }*/

    /*
    private MutableLiveData<Image> userPicture02;
    public MutableLiveData<Image> getUserPicture() {
        if(userPicture02 == null){
            userPicture02 = new MutableLiveData<>();
            userPicture.setValue();
        }
        return userPicture02;
    }*/

    /*
   private MutableLiveData<Image> userPicture03;
   public MutableLiveData<Image> getUserPicture() {
       if(userPicture03 == null){
           userPicture03 = new MutableLiveData<>();
           userPicture.setValue();
       }
       return userPicture03;

    public void Fertig(){

    }

}
  }*/
    public void edit(){

    }

    public void Fertig(){

    }


}