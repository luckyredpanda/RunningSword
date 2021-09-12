package com.swordrunner.swordrunner.ui.profile;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.Utils.WebAddress;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.FriendRes;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.Friend;
import com.swordrunner.swordrunner.data.model.User;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.google.android.gms.common.util.IOUtils.copyStream;


public class AvatarChangeActivity extends AppCompatActivity implements View.OnClickListener {

    //Declaring views
    private Button buttonChoose;
    private Button buttonUpload;
    private ImageView imageView;

    //Image request code
    private int PICK_IMAGE_REQUEST = 1;

    //storage permission code
    private static final int STORAGE_PERMISSION_CODE = 123;

    //Bitmap to get image from gallery
    private Bitmap bitmap;


    //Uri to store the image uri
    private Uri filePath;

    private static final String TAG = "AvatarChangeActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_change);

        //Requesting storage permission
        requestStoragePermission();

        //Initializing views
        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);
        imageView = (ImageView) findViewById(R.id.imageView);

        //Setting clicklistener
        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
    }



    //method to show file chooser
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
                System.out.println(bitmap);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
    private void changeFriendAvatar(Friend friend){
        Call<Friend> call = Client.get(this,false).create(FriendRes.class).updateFriendAvatar(friend);
        call.enqueue(new Callback<Friend>() {
            @Override
            public void onResponse(Call<Friend> call, Response<Friend> response) {
                if(response.code() == 200)
                    Log.d(TAG, "onResponse: change friend avatar success");
            }

            @Override
            public void onFailure(Call<Friend> call, Throwable t) {

            }
        });
    }
    private void changeUserAvatar(Friend friend){
        Call<Friend> call = Client.get(this,false).create(FriendRes.class).updateUserAvatar(friend);
        call.enqueue(new Callback<Friend>() {
            @Override
            public void onResponse(Call<Friend> call, Response<Friend> response) {
                if(response.code() == 200){
                    Log.d(TAG, "onResponse: change user avatar success");
                }
            }

            @Override
            public void onFailure(Call<Friend> call, Throwable t) {

            }
        });
    }
    private void changeUrl(String name){
        String thisUrl = WebAddress.STORAGE_ADDRESS +name;
        Log.d("newUrl", thisUrl);
        User thisUser = new User(getUserId(),"","",thisUrl);
        Call<User> call = Client.get(getApplicationContext(),false).create(UserRes.class).updateUrl(thisUser);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code()==200)
                {
                    Intent intent = new Intent();
                    intent.setAction("action.refreshAvatar");
                    sendBroadcast(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }
    private void postData(UrlCallBack urlCallBack) {

        MultipartBody.Part requestImage = null;
        File file = new File(getPath(filePath));
        RequestBody requestfile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        requestImage = MultipartBody.Part.createFormData("avatar",file.getName(),requestfile);


        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(WebAddress.RETROFIT_ADDRESS)
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();

        UserRes avatarRes = retrofit.create(UserRes.class);

        String userId = getUserId();
        User user = new User(userId,"","","");

        Call<String> uploadavatar= avatarRes.uploadavatar(requestImage);
        uploadavatar.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.code() == 200)
                {
                    Toast.makeText(getApplicationContext(),"yes",Toast.LENGTH_SHORT).show();
                    Log.d("upload", "response.body().toString()");
                    urlCallBack.onSuccess(response.body().toString());
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });


    }
    private void saveData(Bitmap bitmap){
        //File file = new File(getPath(filePath));
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File dir = cw.getDir("imageDir", Context.MODE_PRIVATE);
        //File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        dir.mkdirs();
        String fname = "myavatar" + ".jpg";
        File file = new File (dir,fname);
        System.out.println(file.getAbsolutePath());
        System.out.println(bitmap);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Toast.makeText(this, "Upload Success!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    //method to get the file path from uri
    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }


    //Requesting permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }


    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onClick(View v) {
        if (v == buttonChoose) {
            showFileChooser();
        }
        if (v == buttonUpload) {
            postData(new UrlCallBack() {
                @Override
                public void onSuccess(String name) {
                    Friend friend1 = new Friend("","","","","","","","","");
                    Friend friend2 = new Friend("","","","","","","","","");
                    String thisUrl = WebAddress.STORAGE_ADDRESS+name;
                    friend1.setUserId(getUserId());
                    friend1.setUserAvatar(thisUrl);
                    friend2.setFriendId(getUserId());
                    friend2.setFriendAvatar(thisUrl);
                    changeUserAvatar(friend1);
                    changeFriendAvatar(friend2);
                    changeUrl(name);
                }

                @Override
                public void onFailure() {

                }
            });
            //saveData(bitmap);

        }
    }
    private String getUserId(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("swordrunnerpref", Context.MODE_PRIVATE);
        String currentUserId = pref.getString("id","").toString();
        return currentUserId;
    }
    public interface UrlCallBack{
        void onSuccess(String name);
        void onFailure();
    }

}