package com.example.kinny.snapchat_clone;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.example.kinny.snapchat_clone.R.id.image;
import static com.example.kinny.snapchat_clone.R.id.imageView;

public class UserList extends AppCompatActivity {

    RelativeLayout relativeLayout;
    ListView userList;
    ArrayAdapter arrayAdapter;
    ArrayList<String> usernames;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("users");
    DatabaseReference imageReference = database.getReference("images");
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef;
    ProgressBar progressBar;
    String currentUser;


    public void checkForImages(){
        imageReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> imagesList = new HashMap<String, String>();
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    if((String.valueOf(data.child("recipientName").getValue()).equals(currentUser))){
                        imagesList.put(String.valueOf(data.child("imageUrl").getValue()), String.valueOf(data.child("senderName").getValue()));
                    }
                }

                for(Map.Entry<String, String> entry: imagesList.entrySet()){
                    final String imageStorageUrl = entry.getKey();
                    String senderName = entry.getValue();

                    Log.i("ImageStorageUlr", imageStorageUrl);

                    AlertDialog.Builder builder = new AlertDialog.Builder(UserList.this);
                    builder.setTitle("You have a new Snap!!");
                    TextView content = new TextView(UserList.this);
                    content.setText(senderName  + " has sent you an snap!!");
                    content.setGravity(Gravity.CENTER_HORIZONTAL);
                    builder.setView(content);

                    builder.setPositiveButton("View", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(UserList.this);

                            builder.setTitle("Your message");
                            final ImageView content = new ImageView(UserList.this);
                            String exactStorageLocation = imageStorageUrl.replace("%40", "@");
                            Log.i("exact", exactStorageLocation);
                            StorageReference current = storage.getReferenceFromUrl(exactStorageLocation);
                            current.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.with(UserList.this).load(uri.toString()).placeholder(R.drawable.placeholder).into(content);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Log.i("Error", exception.getLocalizedMessage());
                                }
                            });

                            builder.setView(content);

                            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    imageReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for(DataSnapshot data: dataSnapshot.getChildren()){
                                                if((String.valueOf(data.child("imageUrl").getValue()).equals(imageStorageUrl))){
                                                    mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageStorageUrl);
                                                    mStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.i("Removed", "Removed from storage!");
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.i("Removed", "Failure!!");
                                                        }
                                                    });
                                                    data.getRef().removeValue();

                                                }
                                            }
                                            arrayAdapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });

                            builder.show();
                        }
                    });

                    builder.show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Database Error", databaseError.getDetails());
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, final Intent data) {
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
                        @SuppressWarnings("VisibleForTests") String downloadUrl = String.valueOf(taskSnapshot.getDownloadUrl());
                        String senderName = currentUser;
                        String recipientName = usernames.get(requestCode);

                        Images newImage = new Images(downloadUrl, senderName, recipientName);

                        imageReference.push().setValue(newImage);
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

        checkForImages();

        mStorageRef = FirebaseStorage.getInstance().getReference();

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
                    if(!(String.valueOf(data.child("email").getValue()).equals(MainActivity.user.getEmail()))){
                        usernames.add(String.valueOf(data.child("username").getValue()));
                    }else{
                        currentUser = String.valueOf(data.child("username").getValue());
                    }
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
