package com.example.yusuf.simpleblog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mBlogList;

    // Database references for Firebase
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLike;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // For check like
    private boolean mProcessLike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        // Check user login or not
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() == null){
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    // User wont be able go back
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        mBlogList = (RecyclerView) findViewById(R.id.rvBlog);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));

        // We want everything in blog child
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabase.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabaseLike.keepSynced(true);

        // Check user
        checkUserExist();

    }


    @Override
    protected void onStart() {
        super.onStart();

        // For listen auth
        mAuth.addAuthStateListener(mAuthListener);

        // RecyclerView Adapter
        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class,
                R.layout.blog_row,
                BlogViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {

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
                        Intent singleBlogIntent = new Intent(MainActivity.this, BlogSingleActivity.class);
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


    //View Holder for RecyclerView
    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View mView;
//        TextView postTitle;

        // Like image
        ImageButton mLikeBtn;

        // For like post
        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        public BlogViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            /*
            // For clicking single post title, we did it generate
            postTitle = (TextView) mView.findViewById(R.id.post_title);

            postTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            */
            mLikeBtn = (ImageButton) mView.findViewById(R.id.likeBtn);

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
            mDatabaseLike.keepSynced(true);

        }

        // Methods for showing blog items, data comes from recyclerview adapter
        public void setTitle(String title){
            TextView postTitle = (TextView) mView.findViewById(R.id.post_title);
            postTitle.setText(title);
        }

        public void setDesc(String desc){
            TextView postDesc = (TextView) mView.findViewById(R.id.post_desc);
            postDesc.setText(desc);
        }

        public void setUsername(String username){
            TextView postUsername = (TextView) mView.findViewById(R.id.post_username);
            postUsername.setText(username);
        }

        public void setImage(Context contex, String image){
            ImageView postImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(contex).load(image).into(postImage);
        }

        // Check and change like icons
        public void setLikeBtn(final String postKey){
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(postKey).hasChild(mAuth.getCurrentUser().getUid())){
                        // if child exit user has liked
                        mLikeBtn.setImageResource(R.mipmap.ic_thumb_up_black_24dp);

                    } else {
                        mLikeBtn.setImageResource(R.mipmap.ic_thumb_up_gray_24dp);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }


    private void checkUserExist() {

        if (mAuth.getCurrentUser() != null) {

            final String userId = mAuth.getCurrentUser().getUid();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(userId)) {

                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        // User wont be able go back
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    // For Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    // When options item clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_add){
            startActivity(new Intent(MainActivity.this,PostActivity.class));
        }

        else if(item.getItemId() == R.id.action_logout){

            logout();

        } else if(item.getItemId() == R.id.action_profile){

            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        }

        return super.onOptionsItemSelected(item);
    }


    private void logout() {
        mAuth.signOut();
    }

}
