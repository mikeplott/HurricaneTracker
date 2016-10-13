package com.company;

/**
 * Created by michaelplott on 10/4/16.
 */
public class Hurricane {
//    enum Category {
//        ONE, TWO, THREE, FOUR, FIVE
//    }
    int id;
    String name;
    String location;
    int category;
    String image;
    String user;
    int userID;

    public Hurricane(int id, String name, String location, int category, String image, String user, int userID) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.category = category;
        this.image = image;
        this.user = user;
        this.userID = userID;
    }

    public Hurricane(int id, String name, String location, int category, String image, String user) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.category = category;
        this.image = image;
        this.user = user;
    }
}
