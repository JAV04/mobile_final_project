package com.colewhitley.recipe_share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddRecipe extends AppCompatActivity {

    static final int GALLERY_INTENT = 1;
    static final int CAMERA_INTENT = 2;

    Bitmap bitmap;

    Button camera_btn;
    Button gallery_btn;
    Button add_btn;

    EditText title_text;
    EditText tags_text;
    String recipe_name;

    ImageView mImageView;
    String mCurrentPhotoPath;

    String viewPage;
    String signPage;

    StorageReference storageRef;
    StorageReference imageRef;
    private GoogleSignInOptions gso;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //user info
    private String useremail;
    private String username;

    RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        queue = Volley.newRequestQueue(getApplicationContext());

        viewPage = "https://recipeshare-9444f.appspot.com";
        signPage = "https://recipeshare-9444f.appspot.com";

        camera_btn = findViewById(R.id.camera_btn);
        gallery_btn = findViewById(R.id.gallery_btn);
        add_btn = findViewById(R.id.add_btn);

        title_text = findViewById(R.id.title_text);
        tags_text = findViewById(R.id.tags_text);


        mImageView = findViewById(R.id.image_view);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            useremail = extras.getString("useremail");
            username = extras.getString("username");
            Log.d("USERNAME", username);
            Log.d("USEREMAIL", useremail);
        }


        GoogleApiClient mGoogleApiClient;

        //set up fileStore
        mAuth = FirebaseAuth.getInstance();

        //dont know if i need this...
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d("SignIn", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d("SignIn", "onAuthStateChanged:signed_out");
                }
            }
        };

        storageRef = FirebaseStorage.getInstance().getReference();

        camera_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dispatchTakePictureIntent();
            }
        });

        gallery_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("galler_btn", "CLICKING THE GALLERY BTN");
                //setPic();
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        add_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //stuff to add the image to filestore
                //need username / email
                recipe_name = title_text.getText().toString();
                if(bitmap == null){
                    Toast.makeText(getApplicationContext(), "Select and Image First!", Toast.LENGTH_SHORT).show();
                }
                else if(recipe_name.equals("")){
                    Toast.makeText(getApplicationContext(), "Name your recipe", Toast.LENGTH_SHORT).show();
                }
                else {
                    uploadImage(bitmap);
                    Toast.makeText(getApplicationContext(), "Recipe uploaded!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddRecipe.this, MainActivity.class);
                    AddRecipe.this.startActivity(intent);
                }
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.d("dispatchTakePictureInte", "Photo take successful");
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.colewhitley.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_INTENT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("HERE", "WE ARE HERE");

        if(requestCode == GALLERY_INTENT) {
            Log.d("SET GALLERY", "SETTING A PICTURE FROM GALLERY");
            Uri targetUri = data.getData();
            //text.setText(targetUri.toString());
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                mImageView.setImageBitmap(bitmap);
                uploadImage(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else if(requestCode == CAMERA_INTENT && resultCode == RESULT_OK){
            galleryAddPic();
            bitmap = setPic();
        }
    }

    private void uploadImage(Bitmap bitmap) {
        imageRef = storageRef.child(useremail + "/" + recipe_name + ".png");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] outData = baos.toByteArray();
        UploadTask uploadTask = imageRef.putBytes(outData);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d("UPLOAD", "FAILURE");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d("UPLOAD", "SUCCESS");
            }
        });

        StringRequest postReq = new StringRequest(Request.Method.POST, signPage,
                new Response.Listener<String>() {       // listener, will be called with HTTP response
                    @Override
                    public void onResponse(String response) {
                        Log.d("volley: ", "POST response received.");
                        Log.d("response: ", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {    // volley error callback
                Log.e("volley:", "error! " + error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {  // create data for POST message body
                Map<String, String> params = new HashMap<String, String>();

                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                Date now = new Date();

                Log.d("POST","Posting ");

                params.put("recipeName", recipe_name);
                params.put("tags", tags_text.getText().toString());
                params.put("userName", username);
                params.put("userEmail", useremail);
                params.put("imagePath", useremail + "/" + recipe_name + ".png");
                params.put("date", dateFormat.format(now));
                params.put("public", "1");

                return params;
            }
        };
        queue.add(postReq);


    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private Bitmap setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
//        BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
//        mImageView.setBackground(ob);

        return bitmap;
    }

}
