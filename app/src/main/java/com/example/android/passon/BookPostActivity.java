package com.example.android.passon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class BookPostActivity extends AppCompatActivity {

    private FirebaseDatabase mfirebaseDatabase;
    public static DatabaseReference mPostDatabaseReference;
    public static DatabaseReference mUserDatabaseReference;
    private FirebaseStorage mFirebaseStorage;

    private EditText bookName, filter1, filter2;
    private Button postButton;
    private Boolean bookNameEnable = false, filter1Enable = false, filter2Enable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_post);

        bookName = (EditText) findViewById(R.id.edit1);
        filter1 = (EditText) findViewById(R.id.edit2);
        filter2 = (EditText) findViewById(R.id.edit3);
        postButton = (Button) findViewById(R.id.postButton);
        postButton.setEnabled(false);
//        requestButton = (Button) findViewById(R.id.requestButton);

        mfirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mPostDatabaseReference = mfirebaseDatabase.getReference().child("post");
        mUserDatabaseReference = mfirebaseDatabase.getReference().child("user");

        bookName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    bookNameEnable = true;
                } else {
                    bookNameEnable = false;
                }
                if (bookNameEnable && filter1Enable && filter2Enable) {
                    postButton.setEnabled(true);
                } else {
                    postButton.setEnabled(false);
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        filter1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    filter1Enable = true;

                } else {
                    filter1Enable = false;
                }
                if (bookNameEnable && filter1Enable && filter2Enable) {
                    postButton.setEnabled(true);
                } else {
                    postButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        filter2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    filter2Enable = true;
                } else {
                    filter2Enable = false;
                }
                if (bookNameEnable && filter1Enable && filter2Enable) {
                    postButton.setEnabled(true);
                } else {
                    postButton.setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pust post object to database
                Post post = new Post(1, "a", calculateTime(), bookName.getText().toString(), "d","k", filter1.getText().toString(), filter2.getText().toString(),true);
//                posts.add(post);
                BooksFragment.mPostDatabaseReference.push().setValue(post);
                bookName.setText("");
                filter1.setText("");
                filter2.setText("");
                Toast.makeText(BookPostActivity.this, "Book added successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(BookPostActivity.this,Main2Activity.class));
            }
        });


    }

    public String calculateTime() {
        return android.text.format.DateFormat.format("MMM dd, yyyy hh:mm:ss aaa", new java.util.Date()).toString();

    }

}