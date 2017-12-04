package com.colewhitley.recipe_share;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.colewhitley.recipe_share.adapter.recipeAdapter;
import com.colewhitley.recipe_share.model.Recipe;
import com.colewhitley.recipe_share.model.Tags;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MyRecipes extends AppCompatActivity {

    ListView listView;
    ArrayList<Recipe> recipes;

    String imagePath;

    String useremail;
    String username;
    String sendEmail;

    String viewPage;
    String signPage;


    StorageReference storageRef;
    StorageReference imageRef;
    StorageReference cookedRef;
    Recipe sendRecipe;

    RequestQueue queue;

    RecyclerView recyclerView;
    Bitmap loadBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);

        viewPage = "https://recipeshare-9444f.appspot.com";
        signPage = "https://recipeshare-9444f.appspot.com";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            useremail = extras.getString("useremail");
            username = extras.getString("username");
        }

//

    }

    @Override
    protected void onResume(){
        super.onResume();
        init();
    }


    public void init() {
        imagePath = "gs://recipeshare-9444f.appspot.com";
        recipes = new ArrayList<Recipe>();
        storageRef = FirebaseStorage.getInstance().getReference();
        queue = Volley.newRequestQueue(getApplicationContext());  // create volley request queue
        JsonObjectRequest getReq = new JsonObjectRequest(Request.Method.GET, viewPage, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("volley: ", "GET response received.");
                        Log.d("length", String.valueOf(response.length()));
                        //list.add(response.toString());
                        try {
                            populateArrayList(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    private void populateArrayList(JSONObject response) throws JSONException {
                        int i;
                        for (i = 0; i < response.length(); i++) {
                            String reqEmail = response.getJSONObject(String.valueOf(i)).getString("userEmail");
                            Log.d("POPULATE", "POPULATING ARRAY LIST");
                            if (reqEmail.equalsIgnoreCase(useremail)) {
                                String recipeName = response.getJSONObject(String.valueOf(i)).getString("recipeName");
                                String imagePath = response.getJSONObject(String.valueOf(i)).getString("imagePath");
                                String tags = response.getJSONObject(String.valueOf(i)).getString("tags");
                                String user = response.getJSONObject(String.valueOf(i)).getString("userName");
                                String userEmail = response.getJSONObject(String.valueOf(i)).getString("userEmail");
                                int owner = Integer.parseInt(response.getJSONObject(String.valueOf(i)).getString("owner"));
                                    recipes.add(new Recipe(recipeName, tags, imagePath, user, userEmail));

//                                Glide.with(getApplicationContext())
//                                        .using(new FirebaseImageLoader())
//                                        .load(imageRef)
//                                        .into(null);
                            }

                        }
                        initViews();

//                        Collections.sort(orders, new Comparator<OrderSummary>() {
//                            DateFormat myFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
//
//                            @Override
//                            public int compare(OrderSummary orderSummary, OrderSummary t1) {
//                                try {
//                                    return myFormat.parse(t1.getOrderDate()).compareTo(myFormat.parse(orderSummary.getOrderDate()));
//                                } catch (ParseException e) {
//                                    throw new IllegalArgumentException(e);
//                                }
//                            }
//                        });
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("volley:", "error! " + error.toString());
            }
        });
        queue.add(getReq);

        Log.d("I AM HERE", "I AM HERE");


    }

    private void initViews() {
        recyclerView = (RecyclerView) findViewById(R.id.card_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        recipeAdapter adapter = new recipeAdapter(getApplicationContext(), recipes);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerViewClickListener(getApplicationContext(), recyclerView, new RecyclerViewClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //Toast.makeText(MyRecipes.this, "click at position " + position, Toast.LENGTH_SHORT).show();

                Recipe loadRecipe = recipes.get(position);
                imageRef = storageRef.child(loadRecipe.userEmail + "/" + loadRecipe.recipeName + "/recipe.png");
                Log.d("PATH", loadRecipe.userEmail + "/" + loadRecipe.recipeName + "/recipe.png");

                final Dialog nagDialog = new Dialog(MyRecipes.this,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                nagDialog.setCancelable(false);
                nagDialog.setContentView(R.layout.preview_image);
                Button btnClose = (Button)nagDialog.findViewById(R.id.btnIvClose);
                ImageView ivPreview = (ImageView)nagDialog.findViewById(R.id.iv_preview_image);


                Thread t = new Thread(){
                    //Bitmap bitmap = null;
                    public void run(){
                        try {
                            loadBitmap = Glide.with(getApplicationContext())
                                    .using(new FirebaseImageLoader())
                                    .load(imageRef).asBitmap().into(-1, -1).get();
                            if (loadBitmap != null)
                                Log.d("SEE RECIPE", "BITMAP NOT NULL");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
//                try {
//                    Log.d("JOINING", "JOINED");
//                    t.join(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                Log.d("STATE", t.getState().toString());
                ivPreview.setImageBitmap(loadBitmap);

                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {

                        nagDialog.dismiss();
                    }
                });
                nagDialog.show();

            }

            @Override
            public void onLongItemClick(View view, int position) {
                sendRecipe = recipes.get(position);

                //alert dialog to input user email
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MyRecipes.this);
                alertDialog.setTitle("Send Recipe");
                alertDialog.setMessage("Enter email of user");

                final EditText input = new EditText(MyRecipes.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                //alertDialog.setIcon(R.drawable.key);

                alertDialog.setPositiveButton("Send",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //send the yeet.
                                Log.d("SENDING", input.getText().toString());
                                sendEmail = input.getText().toString();
                                //download image convert to bitmap and call uploadImage() and it should work
                                imageRef = storageRef.child(sendRecipe.userEmail + "/" + sendRecipe.recipeName + "/recipe.png");
                                cookedRef = storageRef.child(sendRecipe.userEmail + "/" + sendRecipe.recipeName + "/cooked.png");
                                if(imageRef == null|| cookedRef == null)
                                    Log.d("REF NULL", "NULL");


                                Thread t = new Thread(){
                                    Bitmap recipe_bitmap = null;
                                    Bitmap cooked_bitmap = null;
                                    public void run(){
                                        try {
                                            recipe_bitmap = Glide.with(getApplicationContext())
                                                    .using(new FirebaseImageLoader())
                                                    .load(imageRef).asBitmap().into(-1, -1).get();
                                            cooked_bitmap = Glide.with(getApplicationContext())
                                                    .using(new FirebaseImageLoader())
                                                    .load(cookedRef).asBitmap().into(-1, -1).get();
                                            if (recipe_bitmap != null && cooked_bitmap != null) {
                                                uploadImage(recipe_bitmap, "recipe.png");
                                                uploadImage(cooked_bitmap, "cooked.png");
                                            }

                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        } catch (ExecutionException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                t.start();
                                Toast.makeText(MyRecipes.this, "Recipe sent to " + sendEmail, Toast.LENGTH_SHORT).show();

                            }
                        });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
            }
        }));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.item_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                ArrayList<Recipe> tempList = new ArrayList<>();
                Tags tags;
                for (Recipe temp : recipes) {
                    tags = new Tags(temp.tags);
                    if (temp.recipeName.toLowerCase().contains(s.toLowerCase())) {
                        tempList.add(temp);
                    }
                    for(String tag: tags.tagsList){
                        if(tag.toLowerCase().contains(s.toLowerCase())){
                            tempList.add(temp);
                        }
                    }
                }
                recipeAdapter adapter = new recipeAdapter(getApplicationContext(), tempList);
                recyclerView.setAdapter(adapter);
//                ArrayAdapter<String> adapter = new ArrayAdapter<>(MyRecipes.this, android.R.layout.simple_list_item_1, tempList);
//                listView.setAdapter(adapter);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void uploadImage(Bitmap bitmap, String imageName) {
        imageRef = storageRef.child(sendEmail + "/" + sendRecipe.userName + "'s " + sendRecipe.recipeName + "/" + imageName);
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

                params.put("recipeName", sendRecipe.userName + "'s " + sendRecipe.recipeName);
                params.put("tags", sendRecipe.tags);
                params.put("userName", username); //do we want this to be the sender or receivers username?
                params.put("userEmail", sendEmail); //do we want this to be the sender or receivers email?
                params.put("imagePath", sendEmail + "/" + sendRecipe.recipeName + "/");
                params.put("date", dateFormat.format(now));
                params.put("owner", "0");

                return params;
            }
        };
        if(imageName.equalsIgnoreCase("recipe.png"))
            queue.add(postReq);


    }
}