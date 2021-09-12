package com.swordrunner.swordrunner.ui.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.Utils.StartOneActivities;
import com.swordrunner.swordrunner.Utils.UserOwnInfo;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.Comment;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.databinding.FragmentProfileBinding;
import com.swordrunner.swordrunner.ui.profile.mycomments.MyCommentAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FragmentProfileBinding binding;
    private Context context;
    private String id = null;
    private User currentUser = new User("","","","");
    private RecyclerView commentsRecycleView;
    private ArrayList<Comment> comments=new ArrayList<>();
    private ArrayList<User> users=new ArrayList<>();
    private MyCommentAdapter adapter;
    private String TAG = "ProfileFragment Observe";
    private BroadcastReceiver broadcastReceiver;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        commentsRecycleView=root.findViewById(R.id.commentsInProfileRecyclerView);
        adapter=new MyCommentAdapter(getContext());
        commentsRecycleView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        clickTextViewMyGame(binding.vtBtMygame.getText(),binding.vtBtMygame);
        clickTextViewMyComment(binding.tvDetails.getText(),binding.tvDetails);
        id = getUserId();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action.refreshName");
        intentFilter.addAction("action.refreshAvatar");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals("action.refreshName"))
                {
                    try {
                        id = getUserId();
                        getData(new CustomCallBackMyProfile() {
                            @Override
                            public void onSuccess(User user) {
                                adapter.InitMyCommentAdapter(user);
                                adapter.setItemCount(3);
                                commentsRecycleView.setAdapter(adapter);
                                commentsRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
                            }

                            @Override
                            public void onFailure() {

                            }
                        },id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else if(action.equals("action.refreshAvatar")){
                    try {
                        id = getUserId();
                        getData(new CustomCallBackMyProfile() {
                            @Override
                            public void onSuccess(User user) {
                                adapter.InitMyCommentAdapter(user);
                                adapter.setItemCount(3);
                                commentsRecycleView.setAdapter(adapter);
                                commentsRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
                            }

                            @Override
                            public void onFailure() {

                            }
                        }, id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        requireActivity().registerReceiver(broadcastReceiver,intentFilter);


        return root;
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        refreshCommentsList();
        super.onResume();
    }

    public void refreshCommentsList(){
        getData(new CustomCallBackMyProfile() {
            @Override
            public void onSuccess(User user) {
                adapter.InitMyCommentAdapter(user);
                adapter.setItemCount(3);
                commentsRecycleView.setAdapter(adapter);
                commentsRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
            @Override
            public void onFailure() {

            }
        },id);
    }

    //initial menu in toolbar
    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_selfsetting,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.profile_menu_add:
                startSelfsettingActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
    // get user info from backend
    private void getData(CustomCallBackMyProfile customCallBackMyProfile,String id) {
        Call<User> call = Client.get(getContext()).create(UserRes.class).get(id);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                if(response.code()==200){

                    User userRes=response.body();
                    customCallBackMyProfile.onSuccess(userRes);

                    userRes.setName(response.body().getName());
                    userRes.setEmail(response.body().getEmail());
                    userRes.setAvatarUrl(response.body().getAvatarUrl());
                    String avatarimgurl = userRes.getAvatarUrl();
                    if(avatarimgurl==null||avatarimgurl.equals("")){
                        Glide.with(getContext())
                                .asBitmap()
                                .load(R.drawable.unknown)
                                .into(binding.userImage1);
                        saveToPref(userRes);
                    }else{
                        Glide.with(getContext())
                                .asBitmap()
                                .load(avatarimgurl)
                                .into(binding.userImage1);
                        saveToPref(userRes);
                    }
                    binding.vtUsername.setText(userRes.getName());
                }

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                customCallBackMyProfile.onFailure();
            }

        });
    }

    private void saveToPref(User user){
        context=getActivity().getApplication();
        SharedPreferences spf=context.getSharedPreferences("swordrunnerpref", MODE_PRIVATE);
        SharedPreferences.Editor editor=spf.edit();
        if (!user.getEmail().isEmpty()) {
            editor.putString("email",user.getEmail());
        }
        if (!user.getAvatarUrl().isEmpty()){
            editor.putString("avatarurl",user.getAvatarUrl());
        }
        if (!user.getName().isEmpty()){
            editor.putString("name",user.getName());
        }
        editor.apply();

    }

    //get current user id from shared preference
    private String getUserId(){
        SharedPreferences pref = getContext().getSharedPreferences("swordrunnerpref", Context.MODE_PRIVATE);
        String currentUserId = pref.getString("id","").toString();
        return currentUserId;
    }
    private void startSelfsettingActivity() {
        Intent intent = new Intent(getContext(),SelfsettingActivity.class);
        startActivity(intent);
    }

    /**
     Allow textview "mygame" to be clicked, and set a listener
     **/
    private void clickTextViewMyGame(CharSequence text, TextView textView ){
        SpannableStringBuilder spannableString = new SpannableStringBuilder(text);
        ClickableSpan clickableSpan=new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(getContext(), MyGamelistActivity.class);
                intent.putExtra("userId", getUserId());
                startActivity(intent);

            }
        };
        int end =textView.getText().length();
        spannableString.setSpan(clickableSpan,0,end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

    }

    /**
     Allow textview "mycomments" to be clicked, and set a listener
     **/
    private void clickTextViewMyComment(CharSequence text, TextView textView ){
        SpannableStringBuilder spannableString = new SpannableStringBuilder(text);
        ClickableSpan clickableSpan=new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                UserOwnInfo userOwnInfo=new UserOwnInfo(getActivity().getApplication());
                User user=userOwnInfo.getUser();
                StartOneActivities startOneActivities=new StartOneActivities();
                startOneActivities.startMyCommentActivity(getContext(),user);
            }
        };
        int end =textView.getText().length();
        spannableString.setSpan(clickableSpan,0,end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

    }



    public interface CustomCallBackMyProfile{
        void onSuccess(User user);
        void onFailure();
    }
}