package com.colewhitley.recipe_share;

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
import android.widget.EditText;
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

/**
 * Created by whitguy on 11/28/17.
 */

public class FindRecipe extends AppCompatActivity {

    ListView listView;
    ArrayList<Recipe> recipes;

    String imagePath;

    String useremail;
    String username;

    String viewPage;
    String signPage;

    StorageReference storageRef;
    StorageReference recipeImageRef;
    StorageReference cookedImageRef;

    RequestQueue queue;

    RecyclerView recyclerView;

    Recipe addRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_recipe);

        viewPage = "https://recipeshare-9444f.appspot.com";
        signPage = "https://recipeshare-9444f.appspot.com";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            useremail = extras.getString("useremail");
            username = extras.getString("username");
        }
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
                            //Log.d("LOOK FOR ME HERHEHEHRE", reqEmail);
                            //Log.d("LOOK FOR ME HERHEHEHRE", useremail);
//                            if (reqEmail.equalsIgnoreCase(useremail)) {
                                String recipeName = response.getJSONObject(String.valueOf(i)).getString("recipeName");
                                String imagePath = response.getJSONObject(String.valueOf(i)).getString("imagePath");
                                String tags = response.getJSONObject(String.valueOf(i)).getString("tags");
                                String user = response.getJSONObject(String.valueOf(i)).getString("userName");
                                String userEmail = response.getJSONObject(String.valueOf(i)).getString("userEmail");
                                int owner = Integer.parseInt(response.getJSONObject(String.valueOf(i)).getString("owner"));
                                //Log.d("LOOK FOR ME HERHEHEHRE", "1 HWRE");
                                if (owner != 0) {
                                    //Log.d("LOOK FOR ME HERHEHEHRE", "2 HWRE");
                                    if(!useremail.equalsIgnoreCase(userEmail))
                                        recipes.add(new Recipe(user + "'s " + recipeName, tags, imagePath, user, userEmail));
                                }

//                                Glide.with(getApplicationContext())
//                                        .using(new FirebaseImageLoader())
//                                        .load(imageRef)
//                                        .into(null);
//                            }

                        }
                        //Log.d("LOOK FOR ME HERHEHEHRE", Integer.toString(recipes.size()));
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

        //Log.d("I AM HERE", "I AM HERE");


    }

    private void initViews() {
        recyclerView = (RecyclerView) findViewById(R.id.card_recycler_view2);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(layoutManager);


        //Log.d("LOOK FOR ME 2", Integer.toString(recipes.size()));
        recipeAdapter adapter = new recipeAdapter(getApplicationContext(), recipes);
        recyclerView.setAdapter(adapter);

        //handle touches
        recyclerView.addOnItemTouchListener(new RecyclerViewClickListener(getApplicationContext(), recyclerView, new RecyclerViewClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //Toast.makeText(MyRecipes.this, "click at position " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongItemClick(View view, int position) {
                addRecipe = recipes.get(position);
                String displayName = addRecipe.userEmail + "/" + addRecipe.recipeName + "/";
                String trueName = displayName.replace(addRecipe.userName + "'s ", "");
                recipeImageRef = storageRef.child(trueName + "recipe.png");
                cookedImageRef = storageRef.child(trueName + "cooked.png");



                Log.d("IMAGEREF", addRecipe.userEmail + "/" + addRecipe.recipeName + ".png");

                Thread t = new Thread(){
                    Bitmap recipe_bitmap = null;
                    Bitmap cooked_bitmap = null;
                    public void run(){
                        try {
                            recipe_bitmap = Glide.with(getApplicationContext())
                                    .using(new FirebaseImageLoader())
                                    .load(recipeImageRef).asBitmap().into(-1, -1).get();
                            cooked_bitmap = Glide.with(getApplicationContext())
                                    .using(new FirebaseImageLoader())
                                    .load(cookedImageRef).asBitmap().into(-1, -1).get();
                            if (recipe_bitmap != null && cooked_bitmap != null) {
                                Log.d("UPLOADING", "upload called");
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
                Toast.makeText(FindRecipe.this, "Added recipe", Toast.LENGTH_SHORT).show();
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
                    else {
                        for (String tag : tags.tagsList) {
                            if (tag.toLowerCase().contains(s.toLowerCase())) {
                                tempList.add(temp);
                            }
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
        StorageReference imageRef = storageRef.child(useremail + "/" + addRecipe.userName + "'s " + addRecipe.recipeName + "/" + imageName);
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

                params.put("recipeName", addRecipe.recipeName);
                params.put("tags", addRecipe.tags);
                params.put("userName", username); //do we want this to be the sender or receivers username?
                params.put("userEmail", useremail); //do we want this to be the sender or receivers email?
                params.put("imagePath", useremail + "/" + addRecipe.userName + "'s " + addRecipe.recipeName + "/");
                params.put("date", dateFormat.format(now));
                params.put("owner", "0");

                return params;
            }
        };

        if(imageName.equalsIgnoreCase("cooked.png")) {
            queue.add(postReq);
        }
    }

}
