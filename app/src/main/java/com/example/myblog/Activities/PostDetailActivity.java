package com.example.myblog.Activities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.myblog.Adapters.CommentAdapter;
import com.example.myblog.Models.Comment;
import com.example.myblog.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailActivity extends AppCompatActivity {

    private EditText editTextComment;
    private TextView txtPostTile, txtPostDesc, txtPostDateName;
    private ImageView imgPost;
    private CircleImageView imgCurrentUser, imgUserPost;
    private Button btnAddComment;

    private String postKey;
    private List<Comment> listComment;

    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;

    private RecyclerView RVComment;
    private CommentAdapter commentAdapter;

    static String COMMENT_KEY = "Comments";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        //set statue bar to transparent

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        getSupportActionBar().hide();

        RVComment = findViewById(R.id.rv_comment);

        editTextComment = (EditText) findViewById(R.id.post_detail_comment);
        txtPostTile = (TextView) findViewById(R.id.post_detail_title);
        txtPostDateName = (TextView) findViewById(R.id.post_detail_date_name);
        txtPostDesc = (TextView) findViewById(R.id.post_detail_desc);
        imgPost = (ImageView) findViewById(R.id.post_detail_img);
        imgCurrentUser = (CircleImageView) findViewById(R.id.post_detail_currentuser_photo);
        imgUserPost = (CircleImageView) findViewById(R.id.post_detail_user_photo);
        btnAddComment = (Button) findViewById(R.id.post_detail_add_comment_btn);

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();


        //add btn listener

        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnAddComment.setVisibility(View.INVISIBLE);
                DatabaseReference commentReference = firebaseDatabase.getReference(COMMENT_KEY).child(postKey).push();

                String comment_content = editTextComment.getText().toString();
                String uid = mAuth.getCurrentUser().getUid();
                String uname = mAuth.getCurrentUser().getDisplayName();
                String uimg = mAuth.getCurrentUser().getPhotoUrl().toString();

                Comment comment  = new Comment(comment_content, uid, uimg, uname);

                commentReference.setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        showMessage("Comment added");
                        editTextComment.setText("");
                        btnAddComment.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showMessage("Failed to add comment " + e.getMessage());
                    }
                });

            }
        });

        //load data from post

        String postImage = getIntent().getExtras().getString("postImage");
        Glide.with(this).load(postImage).into(imgPost);

        String userPostImage = getIntent().getExtras().getString("userPhoto");
        Glide.with(this).load(userPostImage).into(imgUserPost);

        String postTitle = getIntent().getExtras().getString("title");
        txtPostTile.setText(postTitle);

        String postDescription = getIntent().getExtras().getString("description");
        txtPostDesc.setText(postDescription);

        Uri currentUserImage = mAuth.getCurrentUser().getPhotoUrl();
        Glide.with(this).load(currentUserImage).into(imgCurrentUser);

        String date = timeStampToString(getIntent().getExtras().getLong("postDate"));
        txtPostDateName.setText(date);

        postKey = getIntent().getExtras().getString("postKey");

        //RVComment
        iniRVComment();
    }

    private void iniRVComment() {

        RVComment.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference commentRef = firebaseDatabase.getReference(COMMENT_KEY).child(postKey);
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                listComment = new ArrayList<>();
                for (DataSnapshot snap:dataSnapshot.getChildren()) {

                    Comment comment = snap.getValue(Comment.class);
                    listComment.add(comment);
                }

                commentAdapter = new CommentAdapter(getApplicationContext(), listComment);
                RVComment.setAdapter(commentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private String timeStampToString(long timeStamp) {

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(timeStamp);
        String date = DateFormat.format("dd-MM-yyy", calendar).toString();

        return date;
    }
}
