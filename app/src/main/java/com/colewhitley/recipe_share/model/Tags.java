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
        tags = str;

        if(tags.contains(","))
            tagsList = Arrays.asList(str.toLowerCase().split(","));
        else
            tagsList = Arrays.asList(str.toLowerCase().split(" "));

    }

    public String toString(){
        return tags;
    }
}
