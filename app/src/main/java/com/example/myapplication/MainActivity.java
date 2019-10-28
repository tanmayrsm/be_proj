package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapters.AllUsersAdapter;
import com.example.myapplication.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    SinchClient sinchClient;
    RecyclerView recyclerView;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    Call call;
    ArrayList<User> userArrayList;
    DatabaseReference reference;
    TextView usrname;

    String my_id ,my_name ,uska_id ,uska_name ,usrname_string ,usrname_string_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerview);
        usrname = findViewById(R.id.username);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        userArrayList = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        usrname_string = firebaseUser.getUid();
        DatabaseReference usr = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(usrname_string).child("email");
        usr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usrname_string_email = dataSnapshot.getValue().toString();
                usrname.setText(usrname_string_email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(firebaseUser.getUid())
                .applicationKey("07ef8714-79aa-4193-be6c-70d7eec6ed6a")
                .applicationSecret("kUL0vXIk+EOEx4f2OjS/TA==")
                .environmentHost("clientapi.sinch.com")
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();

        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener(){

        });
        sinchClient.start();

        fetchAllUsers();

    }

    private void fetchAllUsers() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userArrayList.clear();
                for(DataSnapshot dss : dataSnapshot.getChildren()){
                    User user = dss.getValue(User.class);
                    userArrayList.add(user);
                }

                AllUsersAdapter adapter = new AllUsersAdapter(MainActivity.this ,userArrayList);
                recyclerView.setAdapter(adapter);

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "error h :"+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class SinchCallListener implements CallListener{

        @Override
        public void onCallProgressing(Call call) {
            Toast.makeText(MainActivity.this, "Call is ringing", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCallEstablished(Call call) {
            Toast.makeText(MainActivity.this, "Call established and I am : " + auth.getCurrentUser(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this,CallGoingActivity.class);
            startActivity(intent);
        }

        @Override
        public void onCallEnded(Call endedCall) {
            Toast.makeText(MainActivity.this, "Call ended", Toast.LENGTH_SHORT).show();
            call = null;
            endedCall.hangup();

        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_logout){
            if(firebaseUser != null){
                auth.signOut();
                finish();
                startActivity(new Intent(MainActivity.this ,LoginActivity.class));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient,final Call incomingcalll ) {


            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child("Calls").child(firebaseUser.getUid()).child("Call details");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.hasChild("from")){
                        DatabaseReference ref2 = ref.child("from").child("uid");
                        ref2.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                String phone_karne_wale_ka_id = dataSnapshot.getValue().toString();

                                DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child(phone_karne_wale_ka_id).child("name");
                                ref2.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String nameo = dataSnapshot.getValue().toString();
                                        /////////////alert ka dialog


                                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                        alertDialog.setTitle("Incoming");
                                        alertDialog.setMessage("call mail id : " + nameo);
                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Reject", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                call.hangup();
                                            }
                                        });
                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Pick", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                call = incomingcalll;
                                                call.answer();
                                                call.addCallListener(new SinchCallListener());
                                            }
                                        });
                                        alertDialog.show();



                                        /////////////
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    else{
                        Toast.makeText(MainActivity.this, "busy hai", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });






        }
    }


    public void callUser(User user ,boolean busy){
        if(call == null){
           boolean busy2 ;
            call = sinchClient.getCallClient().callUser(user.getUserid());

            call.addCallListener(new SinchCallListener());

            if(busy == true){
                Toast.makeText(this, "busy h : " + user.getName(), Toast.LENGTH_SHORT).show();
                //call.hangup();
            }
            else
                openCallerDialog(call ,user);
        }
    }

    private void openCallerDialog(final Call call ,final User user) {
        AlertDialog alertDialogCall = new AlertDialog.Builder(MainActivity.this).create();
        alertDialogCall.setTitle("Alert");
        alertDialogCall.setMessage("Calling to :"  +user.getName());
        alertDialogCall.setButton(AlertDialog.BUTTON_NEUTRAL, "Hang Up", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                call.hangup();
            }
        });
        alertDialogCall.show();
    }


}
