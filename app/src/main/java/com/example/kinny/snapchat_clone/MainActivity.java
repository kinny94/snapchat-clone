package com.example.kinny.snapchat_clone;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener  {

    EditText username;
    EditText password;
    EditText email;
    TextView changeSignupLoginMode;
    Button signupOrLogin;
    RelativeLayout relativeLayout;
    ImageView logo;
    boolean signupModeActive = true;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        email = (EditText) findViewById(R.id.email);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        changeSignupLoginMode = (TextView) findViewById(R.id.textView);
        signupOrLogin = (Button) findViewById(R.id.signupOrLogin);
        logo = (ImageView) findViewById(R.id.imageView);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent i  = new Intent(getApplicationContext(), UserList.class);
                    startActivity(i);
                } else {
                    makeToast("Please Signup or Login!");
                }
            }
        };

        changeSignupLoginMode.setOnClickListener(this);
        logo.setOnKeyListener(this);
        relativeLayout.setOnClickListener(this);

        email.setOnKeyListener(this);
        password.setOnKeyListener(this);
        username.setOnClickListener(this);

    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void signupOrLogin(View view){

        String emailText = String.valueOf(email.getText());
        String passwordText = String.valueOf(password.getText());
        String usernameText = String.valueOf(username.getText());


        if(signupModeActive){
            if(TextUtils.isEmpty(emailText)){
                makeToast("Please enter an email id.");
                return;
            }

            if(TextUtils.isEmpty(passwordText)){
                makeToast("Please enter a password.");
                return;
            }

            if(TextUtils.isEmpty(usernameText)){
                makeToast("Please enter a username.");
                return;
            }

            if(!isValidEmail(emailText)){
                makeToast("Invalid email Id.");
                return;
            }

            signupNewUser(emailText, passwordText, usernameText);

        }else{
            if(TextUtils.isEmpty(emailText)){
                makeToast("Please enter an email id.");
                return;
            }

            if(TextUtils.isEmpty(passwordText)){
                makeToast("Please enter a password.");
                return;
            }

            if(!isValidEmail(emailText)){
                makeToast("Invalid email Id.");
                return;
            }

            signinUsers(emailText, passwordText);
        }
    }

    public void signupNewUser(String email, String password, String username){

        final User newUSer = new User(email, username);

        mAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    makeToast(String.valueOf(task.getException().getLocalizedMessage()));
                    return;
                }

                if(task.isSuccessful()){
                    makeToast("User created!!, go to some other actvity");
                    myRef.push().setValue(newUSer);
                    Intent i = new Intent(getApplication(), UserList.class);
                    startActivity(i);
                }
            }
        });
    }

    public void signinUsers(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (!task.isSuccessful()) {
                    makeToast(String.valueOf(task.getException().getLocalizedMessage()));
                    return;
                }

                if(task.isSuccessful()){
                    Intent i = new Intent(getApplication(), UserList.class);
                    startActivity(i);
                    makeToast("Logged in, Go to some other activity!");
                }
            }
        });
    }

    public void makeToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.textView){
            if(signupModeActive){
                signupModeActive = false;
                changeSignupLoginMode.setText("Sign Up");
                signupOrLogin.setText("Log In");
                username.setVisibility(View.INVISIBLE);
            }else{
                signupModeActive = true;
                changeSignupLoginMode.setText("Log In");
                signupOrLogin.setText("Sign up");
                username.setVisibility(View.VISIBLE);
            }
        }else if((v.getId() == R.id.imageView || v.getId() == R.id.relativeLayout)){
            // removing keyboard form the app if clicked somewhere else
            InputMethodManager inm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            signupOrLogin(v);
        }
        return false;
    }
}
