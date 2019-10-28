package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.myapplication.Models.Chats;
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
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CallGoingActivity extends AppCompatActivity implements RecognitionListener {
    Button end ,view_conv_text;
    Call call;
    User user;

    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    TextView connected_name ,first_txtvu;

    SinchClient sinchClient;

    private static final int REQUEST_RECORD_PERMISSION = 100;
    private int maxLinesInput = 10;
    private TextView returnedText ,result;
    private ToggleButton toggleButton;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    boolean listening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_going);

        end = findViewById(R.id.end_call_btn);
        connected_name = findViewById(R.id.conn_name);
        first_txtvu = findViewById(R.id.ram);

        result = findViewById(R.id.hi);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        view_conv_text = findViewById(R.id.view_text_btn);

        returnedText = (TextView) findViewById(R.id.textView1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);

        DatabaseReference usr = FirebaseDatabase.getInstance().getReference()
                .child("Calls").child(firebaseUser.getUid()).child("Call details");


        usr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("from")){
                    /////from ka naam
                    DatabaseReference ref2 = usr.child("from").child("uid");
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
                                    /////////////set conn name
                                    connected_name.setText(nameo);
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

                    /////
                }
                else if(dataSnapshot.hasChild("to")){
                    /////to ka naam
                    DatabaseReference ref2 = usr.child("to").child("uid");

                    result.setVisibility(View.INVISIBLE);

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
                                    /////////////text view name set
                                    connected_name.setText(nameo);
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

                    /////
                }
                else{
                    Toast.makeText(CallGoingActivity.this, "call error", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CallGoingActivity.this ,MainActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        view_conv_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (result.getVisibility() == View.INVISIBLE){
                    result.setVisibility(View.VISIBLE);
                    view_conv_text.setText("Hide converted text");
                }
                else if(result.getVisibility() == View.VISIBLE){
                    result.setVisibility(View.INVISIBLE);
                    view_conv_text.setText("View converted text");
                }
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CallGoingActivity.this, "Call ended", Toast.LENGTH_SHORT).show();
                if (call != null)
                    call.hangup();
                else if (call == null){
                    Intent intent = new Intent(CallGoingActivity.this ,MainActivity.class);
                    startActivity(intent);

                    call = sinchClient.getCallClient().callUser(user.getUserid());
                    call.addCallListener(new CallGoingActivity.SinchCallListener());
                    call.hangup();
                }

                Intent intent = new Intent(CallGoingActivity.this ,MainActivity.class);
                startActivity(intent);
            }
        });

        ///toggle listener

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    listening = true;
                    start();
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    ActivityCompat.requestPermissions
                            (CallGoingActivity.this,
                                    new String[]{Manifest.permission.RECORD_AUDIO},
                                    REQUEST_RECORD_PERMISSION);
                } else {
                    listening = false;
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    turnOf();
                }
            }
        });

        ///on text change
        returnedText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                ////////chng text and add to fb

                DatabaseReference usr2 = FirebaseDatabase.getInstance().getReference()
                        .child("Calls").child(firebaseUser.getUid()).child("Call details");


                usr2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("from")){
                            /////from ka naam
                            DatabaseReference ref3 = usr.child("from").child("uid");
                            ref3.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    String phone_karne_wale_ka_id = dataSnapshot.getValue().toString();
                                    final String so = s.toString();
                                    if(!so.equals(" ") && !so.equals("Say something") && !so.equals("\n")) {
                                        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                                        String res = currentDate + " " + currentTime;

                                        DatabaseReference hammand = FirebaseDatabase.getInstance().getReference()
                                                .child("Chats");

                                        Chats chat = new Chats(so, res);

                                        hammand.child(firebaseUser.getUid()).setValue(chat)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (!task.isSuccessful()) {
                                                            Toast.makeText(CallGoingActivity.this, "Speech gya nhi dB me", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                        hammand.child(phone_karne_wale_ka_id).setValue(chat)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (!task.isSuccessful()) {
                                                            Toast.makeText(CallGoingActivity.this, "Speech gya nhi dB me", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    }



                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            /////
                        }
                        else if(dataSnapshot.hasChild("to")){
                            /////to ka naam
                            DatabaseReference ref3 = usr.child("to").child("uid");
                            ref3.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    String phone_karne_wale_ka_id = dataSnapshot.getValue().toString();

                                    final String so = s.toString();
                                    if(!so.equals(" ") && !so.equals("Say something") && !so.equals("\n")) {
                                        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                                        String res = currentDate + " " + currentTime;

                                        DatabaseReference hammand = FirebaseDatabase.getInstance().getReference()
                                                .child("Chats");

                                        Chats chat = new Chats(so, res);

                                        hammand.child(firebaseUser.getUid()).setValue(chat)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (!task.isSuccessful()) {
                                                            Toast.makeText(CallGoingActivity.this, "Speech gya nhi dB me", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                        DatabaseReference hammand2 = FirebaseDatabase.getInstance().getReference()
                                                .child("Chats");
                                        hammand2.child(phone_karne_wale_ka_id).setValue(chat)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (!task.isSuccessful()) {
                                                            Toast.makeText(CallGoingActivity.this, "Speech gya nhi dB me", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            /////
                        }
                        else{
                            Toast.makeText(CallGoingActivity.this, "call error", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CallGoingActivity.this ,MainActivity.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                ///////added

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });

        DatabaseReference Chato = FirebaseDatabase.getInstance().getReference("Chats");
        Chato.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(firebaseUser.getUid())){{
                        DatabaseReference chats = Chato.child(firebaseUser.getUid());

                        DatabaseReference chudail = chats.child("chat");


                        chudail.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                result.setText(dataSnapshot.getValue().toString());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(CallGoingActivity.this, "Cant display from firebase", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
    private class SinchCallListener implements CallListener {
        @Override
        public void onCallProgressing(Call call) {
            Toast.makeText(CallGoingActivity.this, "Call is ringing", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCallEstablished(Call call) {
            Toast.makeText(CallGoingActivity.this, "Call established", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCallEnded(Call endedCall) {
            Toast.makeText(CallGoingActivity.this, "Call ended", Toast.LENGTH_SHORT).show();
            call = null;
            endedCall.hangup();

        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }

    public void start(){
        progressBar.setVisibility(View.INVISIBLE);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxLinesInput);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);
    }

    public void turnOf(){
        speech.stopListening();
        speech.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(CallGoingActivity.this, "start talk...", Toast
                            .LENGTH_SHORT).show();
                    speech.startListening(recognizerIntent);
                } else {
                    Toast.makeText(CallGoingActivity.this, "Permission Denied!", Toast
                            .LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (speech != null) {
//            speech.destroy();
//            Log.i(LOG_TAG, "destroy");
//        }
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.i(LOG_TAG, "onReadyForSpeech");

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
        if(!listening){
            turnOf();
        }
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        Log.i(LOG_TAG, "onBufferReceived: " + bytes);

    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        Toast.makeText(this, "End of speech", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        returnedText.setText(errorMessage);
        speech.startListening(recognizerIntent);

    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text += result + "\n";
        Log.i(LOG_TAG, "onResults="+text);
        //returnedText.setText(text);
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onPartialResults(Bundle results) {
        Log.i(LOG_TAG, "onPartialResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text += result + "\n";
        Log.i(LOG_TAG, "onPartialResults="+text);
        returnedText.setText(text);

    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        Log.i(LOG_TAG, "onEvent");

    }

    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "Say something";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                turnOf();
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}
