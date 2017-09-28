package com.example.kinny.snapchat_clone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class UserList extends AppCompatActivity {

    RelativeLayout relativeLayout;
    ListView userList;
    ArrayAdapter arrayAdapter;
    ArrayList<String> usernames;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("users");
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef;
    ProgressBar progressBar;
    DatabaseReference ref;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null){
            Uri selectedImage = data.getData();

            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                String path = MainActivity.user.getEmail() + "/" + UUID.randomUUID() + ".png";
                mStorageRef = storage.getReference(path);
                byte[] byteArray = stream.toByteArray();

                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setCustomMetadata("text", MainActivity.user.getEmail()).build();

                final UploadTask uploadTask = mStorageRef.putBytes(byteArray, metadata);

                uploadTask.addOnSuccessListener(UserList.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressBar.setVisibility(View.GONE);;

//                        ref.addListenerForSingleValueEvent(
//                                new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                        for(DataSnapshot data: dataSnapshot.getChildren()){
//                                            if(String.valueOf(data.getKey()).equals(currentUser)) {
//                                                ref.child(String.valueOf(currentUser)).child("images").push().setValue(String.valueOf(mStorageRef));
//                                            }
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancelled(DatabaseError databaseError) {
//                                        //handle databaseError
//                                        Log.w("data", "Failed to read value.", databaseError.toException());
//                                    }
//                                });
                        Toast.makeText(getApplicationContext(), "Photo Uploaded!", Toast.LENGTH_SHORT).show();
                    }
                });

            }catch(Exception e){

            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        ref = FirebaseDatabase.getInstance().getReference("users");

        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        userList = (ListView) findViewById(R.id.userList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        usernames = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames);
        userList.setAdapter(arrayAdapter);

        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, position);
            }
        });

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    usernames.add(String.valueOf(data.child("username").getValue()));
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.Logout){
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }
}
