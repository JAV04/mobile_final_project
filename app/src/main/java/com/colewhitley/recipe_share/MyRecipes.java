package com.colewhitley.recipe_share;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

public class MyRecipes extends AppCompatActivity {

    ListView listView;
    ArrayList<String> items;

    StorageReference storageRef;
    StorageReference imageRef;
    String imagePath;

    String useremail;

    String viewPage;
    String signPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);

        viewPage = "https://recipeshare-9444f.appspot.com";
        signPage = "https://recipeshare-9444f.appspot.com";
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            useremail = extras.getString("useremail");
            Log.d("USEREMAIL", useremail);
        }

        listView = findViewById(R.id.recipe_view);
        items = new ArrayList<>();
        items.add("one");
        items.add("two");
        items.add("three");
        items.add("four");
        items.add("five");
        items.add("six");

        //ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.mylist, R.id.Itemname, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String text = listView.getItemAtPosition(i).toString();
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });

        imagePath = "gs://recipeshare-9444f.appspot.com";
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
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
                ArrayList<String> tempList = new ArrayList<>();
                for(String temp : items){
                    if(temp.toLowerCase().contains(s.toLowerCase())){
                        tempList.add(temp);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MyRecipes.this, android.R.layout.simple_list_item_1, tempList);
                listView.setAdapter(adapter);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
//    public void init(){
//        JsonObjectRequest getReq = new JsonObjectRequest(Request.Method.GET, viewPage, null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        Log.d("volley: ", "GET response received.");
//                        try {
//                            Log.d("length", String.valueOf(response.length()));
//                            Log.d("response: ", response.getJSONObject("0").getString("summary"));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        //list.add(response.toString());
//                        try {
//                            populateListView(response);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    private void populateListView(JSONObject response) throws JSONException {
//                        int i;
//                        int j = 0;
//                        for(i=0; i<response.length(); i++) {
//                            String reqEmail = response.getJSONObject(String.valueOf(i)).getString("userEmail");
//                            if(reqEmail.equalsIgnoreCase(useremail) {
//                                OrderSummary myOrder = new OrderSummary(response.getJSONObject(String.valueOf(i)),currUser);
//                                orders.add(j, myOrder);
//                                j += 1;
//                            }
//                        }
//
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
//
//
//
//                        ArrayAdapter<OrderSummary> adapter = new ArrayAdapter<OrderSummary>(getApplicationContext(),android.R.layout.simple_selectable_list_item, orders);
//                        pastOrders.setAdapter(adapter);
//
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("volley:", "error! " +  error.toString());
//            }
//        } );
//        queue.add(getReq);
//
//    }
}
