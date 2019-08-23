package com.example.mcr_rework;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GroupChatActivity extends AppCompatActivity {
    private static final int GalleryPick = 1;

    private Toolbar mToolbar;
    private ImageButton SendMessageButton, SendImageButton;
    private EditText userMessageInput;
    private ScrollView mScrollview;
    private TextView displayTextMessages;
    private DatabaseReference RootRef;
    ImageView userImageInput;


    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;


    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private StorageReference GroupImagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        RootRef = FirebaseDatabase.getInstance().getReference();

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        GroupImagesRef = FirebaseStorage.getInstance().getReference().child("Groups").child(currentGroupName);







        InitializeFields();

        GetUserInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SendMessageInfoToDatabase();





            }
        });
        SendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()){

                    Messages messages = dataSnapshot.getValue(Messages.class);

                    messagesList.add(messages);

                    messageAdapter.notifyDataSetChanged();
                    userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){


                Uri resultUri = result.getUri();

                Random r = new Random();
                String i1 = String.valueOf(r.nextInt(9 - 1) );
                String i2 = String.valueOf(r.nextInt(9 - 1) );
                String i3 = String.valueOf(r.nextInt(9 - 1) );

                final String messageKey = GroupNameRef.push().getKey();

                StorageReference filePath = GroupImagesRef.child(messageKey + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(GroupChatActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();

                            String downloadUrl = task.getResult().getDownloadUrl().toString();


                            Calendar calForDate = Calendar.getInstance();
                            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy");
                            currentDate = currentDateFormat.format(calForDate.getTime());

                            Calendar calForTime = Calendar.getInstance();
                            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                            currentTime = currentTimeFormat.format(calForTime.getTime());

                            HashMap<String, Object> groupMessageKey = new HashMap<>();
                            GroupNameRef.updateChildren(groupMessageKey);


                            GroupMessageKeyRef = GroupNameRef.child(messageKey);

                            HashMap<String, Object> messageInfoMap = new HashMap<>();
                            messageInfoMap.put("from", currentUserID);
                            messageInfoMap.put("type", "image");
                            messageInfoMap.put("message", downloadUrl);
                            messageInfoMap.put("date", currentDate);
                            messageInfoMap.put("time", currentTime);
                            GroupMessageKeyRef.updateChildren(messageInfoMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(GroupChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    userMessageInput.setText("");

                                }
                            });


                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(GroupChatActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();


                        }

                    }
                });
            }

        }
    }



    private void InitializeFields() {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);
        SendImageButton = (ImageButton)findViewById(R.id.group_send_Image_btn);

        SendMessageButton = (ImageButton) findViewById(R.id.group_send_message_btn);
        userMessageInput = (EditText) findViewById(R.id.group_input_message);


        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView)findViewById(R.id.private_message_list_of_Group);


        messageAdapter = new MessageAdapter(messagesList);

        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


    }

    private void GetUserInfo() {

        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendMessageInfoToDatabase() {

        String messageText = userMessageInput.getText().toString();
        String messageKey = GroupNameRef.push().getKey();

        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "First write message...", Toast.LENGTH_SHORT).show();
        }else{

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);


            GroupMessageKeyRef = GroupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("from", currentUserID);
            messageInfoMap.put("type", "text");
            messageInfoMap.put("message", messageText);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            GroupMessageKeyRef.updateChildren(messageInfoMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(GroupChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    userMessageInput.setText("");

                }
            });

        }

    }
    /*private void DisplayMessage(DataSnapshot dataSnapshot) {

        Iterator iterator = dataSnapshot.getChildren().iterator();
        while(iterator.hasNext()){

            String chatDate = (String)((DataSnapshot)iterator.next()).getValue();
            String chatMessages = (String)((DataSnapshot)iterator.next()).getValue();
            String chatName = (String)((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String)((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName + " :\n" + chatMessages + "\n" + chatTime + "    " + chatDate + "\n\n\n");

            mScrollview.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }*/

}
