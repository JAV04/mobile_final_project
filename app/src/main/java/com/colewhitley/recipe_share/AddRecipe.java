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

    static final int GALLERY_INTENT_RECIPE = 1;
    static final int GALLERY_INTENT_COOKED = 2;
    static final int CAMERA_INTENT_RECIPE = 3;
    static final int CAMERA_INTENT_COOKED = 4;


    Bitmap bitmap_recipe;
    Bitmap bitmap_cooked;

    Button add_btn;

    EditText title_text;
    EditText tags_text;
    String recipe_name;

    ImageView instructionImgView;
    ImageView cookedImgView;

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

        add_btn = findViewById(R.id.add_btn);

        title_text = findViewById(R.id.title_text);
        tags_text = findViewById(R.id.tags_text);


        instructionImgView = findViewById(R.id.instrimg);
        instructionImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent(CAMERA_INTENT_RECIPE);
            }
        });
        instructionImgView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_INTENT_RECIPE);
                instructionImgView.setPadding(5,5,5,5);
                return false;
            }
        });

        cookedImgView = findViewById(R.id.cookedimg);
        cookedImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent(CAMERA_INTENT_COOKED);
            }
        });
        cookedImgView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_INTENT_COOKED);
                cookedImgView.setPadding(5,5,5,5);
                return false;
            }
        });

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



        add_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //stuff to add the image to filestore
                //need username / email
                recipe_name = title_text.getText().toString();
                if(bitmap_recipe == null || bitmap_cooked == null){
                    Toast.makeText(getApplicationContext(), "Select and Image First!", Toast.LENGTH_SHORT).show();
                }
                else if(recipe_name.equals("")){
                    Toast.makeText(getApplicationContext(), "Name your recipe", Toast.LENGTH_SHORT).show();
                }
                else {
                    uploadImage(bitmap_recipe,"recipe.png");
                    uploadImage(bitmap_cooked,"cooked.png");
                    Toast.makeText(getApplicationContext(), "Recipe uploaded!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddRecipe.this, MainActivity.class);
                    AddRecipe.this.startActivity(intent);
                }
            }
        });
    }

    private void dispatchTakePictureIntent(int image) {
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

                if(image == CAMERA_INTENT_RECIPE) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_INTENT_RECIPE);
                }
                else if(image == CAMERA_INTENT_COOKED){
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_INTENT_COOKED);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("HERE", "WE ARE HERE");

        if(requestCode == GALLERY_INTENT_RECIPE && resultCode == RESULT_OK) {
            Log.d("SET GALLERY", "SETTING A PICTURE FROM GALLERY");
            Uri targetUri = data.getData();
            //text.setText(targetUri.toString());
            try {
                bitmap_recipe = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                instructionImgView.setImageBitmap(bitmap_recipe);
                //uploadImage(bitmap,"recipe.png");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if(requestCode == GALLERY_INTENT_COOKED && resultCode == RESULT_OK) {
            Log.d("SET GALLERY", "SETTING A PICTURE FROM GALLERY");
            Uri targetUri = data.getData();
            //text.setText(targetUri.toString());
            try {
                bitmap_cooked = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                cookedImgView.setImageBitmap(bitmap_cooked);
                //uploadImage(bitmap,"cooked.png");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else if(requestCode == CAMERA_INTENT_RECIPE && resultCode == RESULT_OK){
            galleryAddPic();
            bitmap_recipe = setInstrPic();
        }
        else if(requestCode == CAMERA_INTENT_COOKED && resultCode == RESULT_OK){
            galleryAddPic();
            bitmap_cooked = setCookedPic();
        }
    }

    private void uploadImage(Bitmap bitmap, String imageName) {
        imageRef = storageRef.child(useremail + "/" + recipe_name +"/" + imageName + "/");
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
                params.put("imagePath", useremail + "/" + recipe_name + "/");
                params.put("date", dateFormat.format(now));
                params.put("owner", "1");

                return params;
            }
        };
        if (imageName.equalsIgnoreCase("cooked.png")) {
            queue.add(postReq);
        }


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

    private Bitmap setInstrPic() {

        instructionImgView.setPadding(5,5,5,5);
        // Get the dimensions of the View
        int targetW = instructionImgView.getWidth();
        int targetH = instructionImgView.getHeight();

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
        instructionImgView.setImageBitmap(bitmap);
//        BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
//        instructionImgView.setBackground(ob);

        return bitmap;
    }

    private Bitmap setCookedPic() {
        // Get the dimensions of the View

        cookedImgView.setPadding(5,5,5,5);

        int targetW = cookedImgView.getWidth();
        int targetH = cookedImgView.getHeight();

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
        cookedImgView.setImageBitmap(bitmap);
//        BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
//        instructionImgView.setBackground(ob);

        return bitmap;
    }

}
