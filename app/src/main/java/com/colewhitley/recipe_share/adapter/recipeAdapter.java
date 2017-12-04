package com.colewhitley.recipe_share.adapter;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.colewhitley.recipe_share.R;
import com.colewhitley.recipe_share.model.Recipe;
import com.colewhitley.recipe_share.model.Tags;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Created by jvoves on 11/28/17.
 */


public class recipeAdapter extends RecyclerView.Adapter<recipeAdapter.ViewHolder>{
    private ArrayList<Recipe> recipes;
    private Context context;

    public recipeAdapter(Context context,ArrayList<Recipe> recipes) {
        this.recipes = recipes;
        this.context = context;
    }

    @Override
    public recipeAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rowlayout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(recipeAdapter.ViewHolder viewHolder, int i) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        Log.d("LOOK FOR ME",recipes.get(i).imagePath + "recipe.png");
        //StorageReference imageRefRecipe = storageRef.child(recipes.get(i).imagePath + "recipe.png");
        StorageReference imageRefCooked = storageRef.child(recipes.get(i).imagePath + "cooked.png");
        Tags tags = new Tags(recipes.get(i).tags);


        viewHolder.recipeName.setText(recipes.get(i).recipeName);
        viewHolder.tags.setText(tags.toString());

        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(imageRefCooked)
                .into(viewHolder.recipeImage);

    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tags;
        private TextView recipeName;
        private ImageView recipeImage;

        public ViewHolder(View view) {
            super(view);

            tags = (TextView)view.findViewById(R.id.tags);
            recipeName = (TextView)view.findViewById(R.id.recipename);
            recipeImage = (ImageView) view.findViewById(R.id.recipeImage);

        }
    }

}

