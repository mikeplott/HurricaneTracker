package com.company;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        HashMap<String, User> users = new HashMap<>();
        ArrayList<Hurricane> hurricanes = new ArrayList<>();

        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("loginName");
                    User user = users.get(name);

                    HashMap m = new HashMap();
                    if (user != null) {
                        m.put("name", user.name);
                    }
                    m.put("hurricanes", hurricanes);
                    return new ModelAndView(m, "home.html");
                },
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/login",
                (request, response) ->  {
                    String username = request.queryParams("loginName");
                    String uPass = request.queryParams("password");

                    User user = users.get(username);
                    if (user == null) {
                        user = new User(username, uPass);
                        users.put(username, user);
                    }
                    else if (!uPass.equals(user.password)) {
                        response.redirect("/");
                        return null;
                    }

                    Session session = request.session();
                    session.attribute("loginName", username);
                    response.redirect("/");
                    return null;
                }
        );

        Spark.post(
                "/logout",
                (request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return null;
                }
        );

        Spark.post(
                "/hurricane",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("loginName");
                    User user = users.get(name);

                    if (user == null) {
                        return null;
                    }

                    String hname = request.queryParams("hname");
                    String hlocation = request.queryParams("hlocation");
                    Hurricane.Category hcategory = Enum.valueOf(Hurricane.Category.class, request.queryParams("hcategory"));
                    String himage = request.queryParams("himage");
                    Hurricane hurricane = new Hurricane(hname, hlocation, hcategory, himage, user);
                    hurricanes.add(hurricane);

                    response.redirect("/");
                    return null;
                }
        );
    }
}
