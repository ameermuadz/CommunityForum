package com.example.communityforum;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final List<User> users = new ArrayList<>();
    private static User currentUser;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static boolean usersLoaded = false;

    static {
        addHardcodedUsers();
        usersLoaded = true;
        setCurrentUser("user456"); //PLACEHOLDER AS LOGIN AUTHENTICATION
    }

    private static void addHardcodedUsers() {
        users.add(new User(
                "user123",
                "John Doe",
                "john.doe@example.com",
                "https://example.com/john_doe.jpg"
        ));
        users.add(new User(
                "user456",
                "Jane Smith",
                "jane.smith@example.com",
                "https://example.com/jane_smith.jpg"
        ));
        users.add(new User(
                "user789",
                "Peter Pan",
                "peter.pan@example.com"
        ));

        users.add(new User(
                "anonymous",
                "Anonymous",
                "anonymous@example.com"
        ));
    }

    public static void setCurrentUser(String userId) {
        if (!usersLoaded) {
            Log.w("UserManager", "Users not loaded yet, cannot set current user.");
            return;
        }
        for (User user : users) {
            if (user.getUserId().equals(userId)) {
                currentUser = user;
                return;
            }
        }
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static User getUserById(String userId) {
        Log.d("UserManager", "getUserById called for userId: " + userId);
        if (!usersLoaded) {
            Log.w("UserManager", "Users not loaded yet, cannot get user by ID.");
            return null;
        }
        for (User user : users) {
            if (user.getUserId().equals(userId)) {
                Log.d("UserManager", "User found: " + user.getUserName());
                return user;
            }
        }
        Log.d("UserManager", "User not found for userId: " + userId);
        return null;
    }
}