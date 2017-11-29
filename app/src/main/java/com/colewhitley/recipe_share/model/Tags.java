package com.colewhitley.recipe_share.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by whitguy on 11/28/17.
 */

public class Tags {

    public List<String> tagsList;
    public String tags;

    public Tags(String str){
        //assume that the list is comma seperated
        tagsList = Arrays.asList(str.toLowerCase().split(","));
        tags = str;
    }

    public String toString(){
        return tags;
    }
}
