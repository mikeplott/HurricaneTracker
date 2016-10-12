package com.company;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./hurricane");
        createTable(conn);
        HashMap<String, User> users = new HashMap<>();
        //ArrayList<Hurricane> hurricanes = new ArrayList<>();


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
                    ArrayList<Hurricane> hurricanes = selectHurricane(conn);
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
                    String thecategory = request.queryParams("hcategory");
                    int category = Integer.valueOf(thecategory);
                    String himage = request.queryParams("himage");
                    String submitBy = user.name;
                    addHurricane(conn, hname, hlocation, category, himage, submitBy);
                    //hurricanes.add(hurricane);

                    response.redirect("/");
                    return null;
                }
        );
    }
    public static void createTable(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS hurricanes (id IDENTITY, name VARCHAR, location VARCHAR, category INT, image VARCHAR, user VARCHAR)");
    }

    public static void addHurricane(Connection conn, String hname, String hlocation, int category, String himage, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO hurricanes VALUES(NULL, ?, ?, ?, ?, ?)");
        stmt.setString(1, hname);
        stmt.setString(2, hlocation);
        stmt.setInt(3, category);
        stmt.setString(4, himage);
        stmt.setString(5, name);
        stmt.execute();
    }

    public static ArrayList selectHurricane(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM hurricanes");
        ResultSet results = stmt.executeQuery();
        ArrayList<Hurricane> hurricanes = new ArrayList<>();
        while (results.next()) {
            int id = results.getInt("id");
            String hname = results.getString("name");
            String hlocation = results.getString("location");
            int hcategory = results.getInt("category");
            String himage = results.getString("image");
            String userName = results.getString("user");
            Hurricane hurricane = new Hurricane(id, hname, hlocation, hcategory, himage, userName);
            hurricanes.add(hurricane);
        }
        return hurricanes;
    }
}
