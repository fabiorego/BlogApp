package com.example.yusuf.simpleblog;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private RecyclerView mBlogList;
    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseCurrentUser;
    private Query mProfileQuery;

    private DatabaseReference mDatabaseLike;

    // For check like
    private boolean mProcessLike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");

        // Get userId first
        String mUserId = mAuth.getCurrentUser().getUid();
        Log.v("ProfileActivity", "User Id is : " + mUserId);

        mDatabaseCurrentUser = FirebaseDatabase.getInstance().getReference().child("Blog");
        // Query for getting user post
        mProfileQuery = mDatabaseCurrentUser.orderByChild("uid").equalTo(mUserId);

        mBlogList = (RecyclerView) findViewById(R.id.rvBlog);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();


        // RecyclerView Adapter
        FirebaseRecyclerAdapter<Blog, MainActivity.BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, MainActivity.BlogViewHolder>(
                Blog.class,
                R.layout.blog_row,
                MainActivity.BlogViewHolder.class,
                mProfileQuery
        ) {
            @Override
            protected void populateViewHolder(MainActivity.BlogViewHolder viewHolder, Blog model, int position) {

                final String postKey = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setUsername(model.getUsername());

                viewHolder.setLikeBtn(postKey);

                // For recyclerview item click

                // Reference of our views
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v("postKey", "Clicked Post Key is : " + postKey);

                        // Sent post id to new intent
                        Intent singleBlogIntent = new Intent(ProfileActivity.this, BlogSingleActivity.class);
                        singleBlogIntent.putExtra("postId", postKey);
                        startActivity(singleBlogIntent);
                    }
                });

                // For like button click
                viewHolder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProcessLike = true;

                        mDatabaseLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (mProcessLike) {
                                    // Check like already exist or not
                                    if (dataSnapshot.child(postKey).hasChild(mAuth.getCurrentUser().getUid())) {

                                        mDatabaseLike.child(postKey).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mProcessLike = false;

                                    } else {
                                        // Add to likes node with current user id and a value
                                        mDatabaseLike.child(postKey).child(mAuth.getCurrentUser().getUid()).setValue("Random Value");
                                        mProcessLike = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

            }
        };

        mBlogList.setAdapter(firebaseRecyclerAdapter);
    }
}
