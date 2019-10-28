package com.example.myapplication.Adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.Models.ConnectionDetails;
import com.example.myapplication.Models.User;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.AllUsersViewholder> {

    Activity context;
    ArrayList<User> userArrayList;
    FirebaseUser firebaseUser;
    FirebaseAuth auth;
    boolean busy  = false;

    DatabaseReference reference3 = FirebaseDatabase.getInstance().getReference().child("Calls");
    DatabaseReference ref_call_user = FirebaseDatabase.getInstance().getReference().child("Calls");
    public AllUsersAdapter(Activity context ,ArrayList<User> userArrayList){
        this.context = context;
        this.userArrayList = userArrayList;
    }

    @Override
    public AllUsersViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users,parent,false);
        AllUsersViewholder allUsersAdapter = new AllUsersViewholder(view);

        return allUsersAdapter;
    }

    @Override
    public void onBindViewHolder(AllUsersViewholder holder, int position) {
        User user = userArrayList.get(position);

        holder.user_ka_naam.setText(user.getName());
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public class AllUsersViewholder extends RecyclerView.ViewHolder{
        TextView user_ka_naam;
        Button calling;
        String my_name ,my_id;


        public AllUsersViewholder(View itemView) {
            super(itemView);
            user_ka_naam = itemView.findViewById(R.id.itemName);
            calling = itemView.findViewById(R.id.callButton);

            calling.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = userArrayList.get(getAdapterPosition());


                    auth = FirebaseAuth.getInstance();
                    firebaseUser = auth.getCurrentUser();

                    String name_conn_to = user.getName();
                    String uska_id = user.getUserid();
                    Toast.makeText(context, "Conn to : " + name_conn_to, Toast.LENGTH_SHORT).show();

                    //get my email
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child(firebaseUser.getUid()).child("email");
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            my_name = dataSnapshot.getValue().toString();

                            Toast.makeText(context, "My name : " + my_name, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    //get my uid
                    DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child(firebaseUser.getUid()).child("userid");
                    reference2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            my_id = dataSnapshot.getValue().toString();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    //set call details ke tu usse call karta...

                    //idhar tere call details me usko ring jaata ye pratit hota
                    boolean ringo = true;
                    boolean conn = false;

                    ConnectionDetails details = new ConnectionDetails(ringo , conn ,uska_id) ;
                    reference3.child(firebaseUser.getUid()).child("Call details").child("to")
                            .setValue(details).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                            }
                            else{
                                Toast.makeText(context, "Fb error on set call details from sender", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    //idhar uske call details me aata
                    boolean ringo2 = true;
                    boolean conn2 = false;

                    ConnectionDetails details2 = new ConnectionDetails(ringo2 , conn2 ,firebaseUser.getUid()) ;
                    //check kar wo dusre call pe h ?
                    DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Calls").child(uska_id)
                            .child("Call details");
                    UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.hasChild("from")) {
                                //Toast.makeText(context, "call busy", Toast.LENGTH_SHORT).show();
                                busy = true;
                            }
                            UsersRef.child("from")
                                    .setValue(details2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                    }
                                    else{
                                        Toast.makeText(context, "Fb error on set call details on receiver side", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }) ;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



                    ((MainActivity)context).callUser(user ,busy);
                }
            });
        }
    }
}
