package com.colewhitley.recipe_share;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.colewhitley.recipe_share.adapter.recipeAdapter;
import com.colewhitley.recipe_share.model.Recipe;
import com.colewhitley.recipe_share.model.Tags;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
    StorageReference imageRef;

    RequestQueue queue;

    RecyclerView recyclerView;

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
//
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
                            Log.d("LOOK FOR ME HERHEHEHRE", reqEmail);
                            Log.d("LOOK FOR ME HERHEHEHRE", useremail);
//                            if (reqEmail.equalsIgnoreCase(useremail)) {
                                String recipeName = response.getJSONObject(String.valueOf(i)).getString("recipeName");
                                String imagePath = response.getJSONObject(String.valueOf(i)).getString("imagePath");
                                String tags = response.getJSONObject(String.valueOf(i)).getString("tags");
                                String user = response.getJSONObject(String.valueOf(i)).getString("userName");
                                int owner = Integer.parseInt(response.getJSONObject(String.valueOf(i)).getString("owner"));
                                Log.d("LOOK FOR ME HERHEHEHRE", "1 HWRE");
                                if (owner != 0) {
                                    Log.d("LOOK FOR ME HERHEHEHRE", "2 HWRE");
                                    recipes.add(new Recipe(user + "'s " + recipeName, tags, imagePath, user));
                                }

//                                Glide.with(getApplicationContext())
//                                        .using(new FirebaseImageLoader())
//                                        .load(imageRef)
//                                        .into(null);
//                            }

                        }
                        Log.d("LOOK FOR ME HERHEHEHRE", Integer.toString(recipes.size()));
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
        recyclerView = (RecyclerView) findViewById(R.id.card_recycler_view2);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(layoutManager);


        Log.d("LOOK FOR ME 2", Integer.toString(recipes.size()));
        recipeAdapter adapter = new recipeAdapter(getApplicationContext(), recipes);
        recyclerView.setAdapter(adapter);

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

}
