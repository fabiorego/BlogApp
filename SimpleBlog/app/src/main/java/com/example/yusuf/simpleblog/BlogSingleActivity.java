package com.example.yusuf.simpleblog;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class BlogSingleActivity extends AppCompatActivity {

    private String mPostKey = null;

    private DatabaseReference mDatabase;

    private ImageView mBlogImage;
    private TextView mBlogTitle;
    private TextView mBlogDesc;
    private Button mRemoveBtn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);

        // Get the intent first
        mPostKey = getIntent().getExtras().getString("postId");
        Log.v("BlogSingleActivity", "Post Id is : " + mPostKey);

        mBlogImage = (ImageView) findViewById(R.id.singleBlogImage);
        mBlogTitle = (TextView) findViewById(R.id.singleBlogTitle);
        mBlogDesc = (TextView) findViewById(R.id.singeBlogDesc);
        mRemoveBtn = (Button) findViewById(R.id.singleBlogRemoveBtn);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");

        // Get datas from blog post
        mDatabase.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Get Datas
                String postTitle = (String) dataSnapshot.child("title").getValue();
                String postDesc = (String) dataSnapshot.child("desc").getValue();
                String postImage = (String) dataSnapshot.child("image").getValue();
                String postUid = (String) dataSnapshot.child("uid").getValue();

                // Set Datas
                mBlogTitle.setText(postTitle);
                mBlogDesc.setText(postDesc);
                Picasso.with(BlogSingleActivity.this).load(postImage).into(mBlogImage);

                // Check auth user posted this post or not
                if(mAuth.getCurrentUser().getUid().equals(postUid)){
                    mRemoveBtn.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // When remove button clicked
        mRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDatabase.child(mPostKey).removeValue();
                Intent mainIntent = new Intent(BlogSingleActivity.this, MainActivity.class);
                startActivity(mainIntent);
            }
        });

    }
}
