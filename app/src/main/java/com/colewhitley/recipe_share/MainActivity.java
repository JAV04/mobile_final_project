package com.colewhitley.recipe_share;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    //constant for on actvity result. Tells us signInIntent called $finish
    private static final int RC_SIGN_IN = 1;

    //current user
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions gso;
    private Button add_btn;
    private Button recipes_btn;
    private Button find_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Configure Google Sign In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //api client. GIves us access to google apis
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,null /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //firebase auth instance
        mAuth = FirebaseAuth.getInstance();
        //simple listener that checks if the user signs in/out while already in app
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d("SignIn", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d("SignIn", "onAuthStateChanged:signed_out");
                }
            }
        };

        add_btn = findViewById(R.id.add_btn);
        recipes_btn = findViewById(R.id.recipes_btn);
        find_btn = findViewById(R.id.find_btn);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        user = mAuth.getCurrentUser();

        //sign user in if they are not
        if(user == null) {
            signIn();

//            Toast.makeText(MainActivity.this, "Welcome Back, " + user.getDisplayName(),
//                    Toast.LENGTH_SHORT).show();
        }
        add_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("add in main", "HERE");
                Intent intent = new Intent(MainActivity.this, AddRecipe.class);
                intent.putExtra("useremail", user.getEmail());
                intent.putExtra("username", user.getDisplayName());
                MainActivity.this.startActivity(intent);
            }
        });

        recipes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SWITCH", "GOING TO MY RECIPES");
                Intent intent = new Intent(MainActivity.this, MyRecipes.class);
                intent.putExtra("useremail", user.getEmail());
                intent.putExtra("username", user.getDisplayName());
                MainActivity.this.startActivity(intent);
            }
        });

        find_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SWITCH", "GOING TO FIND RECIPES");
                Intent intent = new Intent(MainActivity.this, FindRecipe.class);
                intent.putExtra("useremail", user.getEmail());
                intent.putExtra("username", user.getDisplayName());
                MainActivity.this.startActivity(intent);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void signIn() {
        //googles sign in page
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("HERE","WE ARE HERE");
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d("SIGNIN", result.getStatus().toString());
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Log.d("google sign in", "FAILED");
                // Google Sign In failed, update UI appropriately
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("AuthGoogle", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("AuthSuccess", "signInWithCredential:success");
                            user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("AuthFailed", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                        // ...
                    }
                });
    }
}