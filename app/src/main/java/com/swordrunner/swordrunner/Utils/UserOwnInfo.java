package com.swordrunner.swordrunner.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.data.model.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

public class UserOwnInfo {
    private Context context;
    private SharedPreferences spf;
    private SharedPreferences.Editor editor;
    private User user;
    private String prefname;

    public UserOwnInfo(Context context) {
        this.context = context;
        prefname=context.getResources().getString(R.string.User_Data);
    }

    /**
     * Warning: Perhaps return a empty user
     * @return
     */
    public User getUser() {
        spf=context.getSharedPreferences(prefname,Context.MODE_PRIVATE);
        String username=spf.getString("name","");
        String email=spf.getString("email","");
        String id=spf.getString("id","");
        String avatarurl=spf.getString("avatarurl","");

        user=new User(id,email,username,avatarurl);

        return user;
    }

    /**
     * Store Javabean to sharepreference
     */
    public void setObject(String key, Object object) {
        SharedPreferences sp = context.getSharedPreferences(prefname, Context.MODE_PRIVATE);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(baos);
            out.writeObject(object);
            String objectValue = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, objectValue);
            editor.apply();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }

                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public  <T> T getObject(String key) {
        SharedPreferences sp = context.getSharedPreferences(prefname, Context.MODE_PRIVATE);
        if (sp.contains(key)) {
            String objectValue = sp.getString(key, null);
            byte[] buffer = Base64.decode(objectValue, Base64.DEFAULT);
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(bais);
                T t = (T) ois.readObject();
                return t;
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bais != null) {
                        bais.close();
                    }

                    if (ois != null) {
                        ois.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Store List<Javabean> to sharepreference
     */
    public <T> void setDataList(String key, List<T> dataList) {
        if (null == dataList || dataList.size() < 0) {
            return;
        }
        spf = context.getSharedPreferences(prefname, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        //convert List to json, then store to shareprefence as json data
        String strJson = gson.toJson(dataList);
        SharedPreferences.Editor editor = spf.edit();
        editor.putString(key, strJson);
        editor.commit();
    }

    public <T> List<T> getDataList(String key, Class<T> cls) {
        SharedPreferences sp = context.getSharedPreferences(prefname, Context.MODE_PRIVATE);
        List<T> dataList = new ArrayList<T>();
        String strJson = sp.getString(key, null);
        if (null == strJson) {
            return dataList;
        }

        Gson gson = new Gson();

        JsonArray arry = new JsonParser().parse(strJson).getAsJsonArray();
        for (JsonElement jsonElement : arry) {
            dataList.add(gson.fromJson(jsonElement, cls));
        }

        return dataList;
    }

}
