package com.example.android.passon;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Iterator;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static boolean backupCalledAlready = false;

    private FirebaseDatabase mfirebaseDatabase;
    public static DatabaseReference mMessagesDatabaseReference;
    public static ChildEventListener mChildEventListener;//to listen the changes in db
    private FirebaseStorage mFirebaseStorage;
    public static StorageReference mChatPhotosStorageReference;

    private RecyclerView mRecyclerView;
    public static RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    ArrayList<Post> posts;
    ArrayList<String> favouriteArrayList;

    private ProgressBar mProgressBar;
    private LinearLayout mInputData;
    private EditText bookName, filter1, filter2;
    private Button sendButton;
    private Boolean bookNameEnable = false, filter1Enable = false, filter2Enable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mInputData = (LinearLayout) findViewById(R.id.inputData);
        bookName = (EditText) findViewById(R.id.edit1);
        filter1 = (EditText) findViewById(R.id.edit2);
        filter2 = (EditText) findViewById(R.id.edit3);
        sendButton = (Button) findViewById(R.id.sendButton);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (!backupCalledAlready) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            backupCalledAlready = true;
            //to set up offline compatibility
        }
        mfirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mMessagesDatabaseReference = mfirebaseDatabase.getReference().child("input");
        mChatPhotosStorageReference = mFirebaseStorage.getReference("book_photos");

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mProgressBar.setVisibility(View.INVISIBLE);

        attachDatabaseListener();//take input from database

        posts = new ArrayList<>();
        favouriteArrayList = new ArrayList<>();
        favouriteArrayList.add("qwerty");

        mAdapter = new PostAdapter(posts);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
//        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
//        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //working of 3 edit text input
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
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
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
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
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
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pust post object to database
                Post post = new Post(1, "a", calculateTime(), bookName.getText().toString(), "d", filter1.getText().toString(), filter2.getText().toString(), favouriteArrayList);
//                posts.add(post);
                mMessagesDatabaseReference.push().setValue(post);
                bookName.setText("");
                filter1.setText("");
                filter2.setText("");
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseReadListener();
        posts.clear();
        mAdapter.notifyItemRangeRemoved(0, mAdapter.getItemCount());

    }

    @Override
    protected void onResume() {
        super.onResume();
        attachDatabaseListener();
        Log.i("resume", "point m323");
//        if (mChildEventListener != null) {
//            Log.i(mChildEventListener.toString(), "point m355");
    }


    private void attachDatabaseListener() {
        mMessagesDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("We're done loading the initial " + dataSnapshot.getChildrenCount() + " items");
                mProgressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


//        if (mChildEventListener != null) {
//            Log.i(mChildEventListener.toString(), "point m293");
//        }
        if (mChildEventListener == null) {
            Log.i("mChildEventListener", "standpoint 298");
            mChildEventListener = new ChildEventListener() {//working with db after authentication
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.i("onchildadded", "point M114");
                    Log.i(Integer.toString(posts.size()), "point m289");

                    //attached to all added child(all past and future child)
                    Post post = dataSnapshot.getValue(Post.class);//as Post has all the three required parameter
                    posts.add(post);
                    mAdapter.notifyDataSetChanged();
                    Log.i(Integer.toString(posts.size()), "point m295");

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    // changed content of a child
                    Log.i("child changed", "point m370");
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    // child deleted
                    Post post = dataSnapshot.getValue(Post.class);//as Post has all the three required parameter

                    for (Iterator<Post> iterator = posts.iterator(); iterator.hasNext(); ) {
                        if (iterator.next().getTimeCurrent() == post.getTimeCurrent())
                            iterator.remove();
                        Log.i(Integer.toString(posts.size()), "point m311");
                    }
                    Log.i(Integer.toString(posts.size()), "point m389");
                    mAdapter.notifyDataSetChanged();

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    //moved position of a child
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // error or permission denied
                }
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
            Log.i("child addeddd", "point m610");
        }

    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null)
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
        mChildEventListener = null;
    }

    public String calculateTime() {
        return android.text.format.DateFormat.format("MMM dd, yyyy hh:mm:ss aaa", new java.util.Date()).toString();

    }

}