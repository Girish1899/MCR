package com.example.mcr_rework;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class phoneBookActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mToolbar;
    private RecyclerView PhoneBookRecyclerList;
    private DatabaseReference PhoneBookRef;
    private String currentUserID;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_book);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        PhoneBookRef = FirebaseDatabase.getInstance().getReference().child("PhoneBook").child(currentUserID);


        PhoneBookRecyclerList = (RecyclerView)findViewById(R.id.phone_book_recycler_list);
        PhoneBookRecyclerList.setLayoutManager(new LinearLayoutManager(this));


        mToolbar = (Toolbar)findViewById(R.id.phone_book_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Phone Book");
    }
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ContactsPhoneBook> options = new FirebaseRecyclerOptions.Builder<ContactsPhoneBook>()
                .setQuery(PhoneBookRef, ContactsPhoneBook.class)
                .build();

        FirebaseRecyclerAdapter<ContactsPhoneBook, PhoneBookViewHolder> adapter = new FirebaseRecyclerAdapter<ContactsPhoneBook, PhoneBookViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PhoneBookViewHolder holder, int position, @NonNull ContactsPhoneBook model) {

                holder.contactName.setText(model.getName());
                holder.contactNumber.setText(model.getMobile());
                holder.contactEmail.setText(model.getEmail());
                holder.contactAddress.setText(model.getAddress());


            }

            @NonNull
            @Override
            public PhoneBookViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_display_layout,viewGroup,false);
                PhoneBookViewHolder viewHolder = new PhoneBookViewHolder(view);
                return viewHolder;
            }
        };

        PhoneBookRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class PhoneBookViewHolder extends RecyclerView.ViewHolder
    {
        TextView contactName, contactEmail, contactNumber, contactAddress;

        public PhoneBookViewHolder(@NonNull View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.phone_book_name);
            contactEmail = itemView.findViewById(R.id.phone_book_email);
            contactNumber = itemView.findViewById(R.id.phone_book_number);
            contactAddress = itemView.findViewById(R.id.phone_book_address);


        }
    }
}
