package com.example.communityforum;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {
    private EditText inputTitle, inputContent;
    private Switch switchAnonymous;
    private FirebaseFirestore db;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        inputTitle = findViewById(R.id.inputPostTitle);
        inputContent = findViewById(R.id.inputPostContent);
        switchAnonymous = findViewById(R.id.switchAnonymous);


        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btnSubmitPost).setOnClickListener(this::submitPost);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());
    }

    @SuppressLint("SuspiciousIndentation")
    private void submitPost(View view) {
        String title = inputTitle.getText()
                .toString().trim();
        String content = inputContent
                .getText().toString().trim();
        boolean isAnonymous = switchAnonymous
                .isChecked();

        if (title.isEmpty() || content
                .isEmpty()) {
            Toast.makeText(this,
                    "All fields are required",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String posterId = null;
        User currentUser = UserManager.getCurrentUser(); // Get the current user
        if (currentUser != null) {
            if(isAnonymous)
                posterId = "anonymous";
            else
            posterId = currentUser.getUserId(); // Get the user's ID
        } else {
            // Handle the case where there is no current user
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Date timestamp = new Date();

        Map<String, Object> post = new HashMap<>();
        post.put("title", title);
        post.put("content", content);
        post.put("anonymous", isAnonymous);
        post.put("upvotes", 0);
        post.put("posterId", posterId);
        post.put("timestamp", timestamp);

        db.collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
