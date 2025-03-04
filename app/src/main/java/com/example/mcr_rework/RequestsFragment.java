package com.example.mcr_rework;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {
    private View RequestFragmentView;
    private RecyclerView myRequestsList;
    private DatabaseReference ChatRequestRef, UsersRef, ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RequestFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");


        myRequestsList = (RecyclerView)RequestFragmentView.findViewById(R.id.chat_request_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return RequestFragmentView;


    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatRequestRef.child(currentUserID), Contacts.class)
                        .build();


        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model) {
                        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                        final String list_user_id = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    String type = dataSnapshot.getValue().toString();
                                    if(type.equals("received")){
                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("image")){
                                                    String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                    String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                                    String requestProfileImage = dataSnapshot.child("image").getValue().toString();


                                                    holder.userName.setText(requestUserName);
                                                    holder.userStatus.setText("wants to connect with you");
                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);


                                                }else{
                                                    String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                    String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                                    holder.userName.setText(requestUserName);
                                                    holder.userStatus.setText("wants to connect with you");
                                                }

                                                holder.itemView.findViewById(R.id.request_accept_btn).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        ContactsRef.child(currentUserID).child(list_user_id).child("Contacts")
                                                                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    ContactsRef.child(list_user_id).child(currentUserID).child("Contacts")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                                            .removeValue()
                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                    if (task.isSuccessful()){
                                                                                                                        Toast.makeText(getContext(), "new contact Added", Toast.LENGTH_SHORT).show();

                                                                                                                    }
                                                                                                                }
                                                                                                            });

                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });

                                                    }

                                                });

                                                holder.itemView.findViewById(R.id.request_cancel_btn).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                .removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()){
                                                                                                Toast.makeText(getContext(), "request rejected", Toast.LENGTH_SHORT).show();

                                                                                            }
                                                                                        }
                                                                                    });

                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });








                                    }
                                    else if(type.equals("sent")){
                                        Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_btn);
                                        request_sent_btn.setText("Req Sent");

                                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);


                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("image")){
                                                    String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                    String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                                    String requestProfileImage = dataSnapshot.child("image").getValue().toString();


                                                    holder.userName.setText(requestUserName);
                                                    holder.userStatus.setText("you have sent a a request to " + requestUserName);
                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);


                                                }else{
                                                    String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                    String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                                    holder.userName.setText(requestUserName);
                                                    holder.userStatus.setText("you have sent a a request to " + requestUserName);
                                                }

                                                holder.itemView.findViewById(R.id.request_accept_btn).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                .removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()){
                                                                                                Toast.makeText(getContext(), "request rejected", Toast.LENGTH_SHORT).show();

                                                                                            }
                                                                                        }
                                                                                    });

                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });


                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        RequestsViewHolder holder = new RequestsViewHolder(view);
                        return holder;
                    }
                };
        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button AcceptButton, CancelButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);


            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            AcceptButton = itemView.findViewById(R.id.request_accept_btn);
            CancelButton = itemView.findViewById(R.id.request_cancel_btn);



        }
    }

}
