package com.colewhitley.recipe_share.model;

/**
 * Created by jvoves on 11/28/17.
 */

public class Recipe {

        public String recipeName;
        public String tags;
        public String imagePath;
        public String userName;
        public String userEmail;

        public Recipe(String recipeName, String tags, String imagePath, String userName, String userEmail){
            this.recipeName = recipeName;
            this.tags = tags;
            this.imagePath = imagePath;
            this.userName = userName;
            this.userEmail = userEmail;

        }
}
