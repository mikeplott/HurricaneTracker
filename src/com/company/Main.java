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
        //HashMap<String, User> users = new HashMap<>();
        //ArrayList<Hurricane> hurricanes = new ArrayList<>();


        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("loginName");

                    HashMap m = new HashMap();

                    ArrayList<Hurricane> hurricanes = selectHurricanes(conn);
                    m.put("name", name);
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
                    User user = selectUser(conn, username);
                    if (user == null) {
                        insertUser(conn, username, uPass);
                    }
                    else if (!uPass.equals(user.password)) {
                        Spark.halt(403);
                        return null;
                    }
//                    User user = users.get(username);
//                    if (user == null) {
//                        user = new User(username, uPass);
//                        users.put(username, user);
//                    }
//                    else if (!uPass.equals(user.password)) {
//                        response.redirect("/");
//                        return null;
//                    }

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
                    User user = selectUser(conn, name);
                    if (user == null) {
                        Spark.halt(403);
                        return null;
                    }

                    String hname = request.queryParams("hname");
                    String hlocation = request.queryParams("hlocation");
                    String thecategory = request.queryParams("hcategory");
                    int category = Integer.valueOf(thecategory);
                    String himage = request.queryParams("himage");
                    String submitBy = user.name;
                    int uID = user.id;
                    addHurricane(conn, hname, hlocation, category, himage, submitBy, uID);
                    //hurricanes.add(hurricane);

                    response.redirect("/");
                    return null;
                }
        );

        Spark.post(
                "/delete-hurricane",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("loginName");
                    User user = selectUser(conn, name);
                    if (user == null) {
                        response.redirect("/");
                        return null;
                    }
                    String num = request.queryParams("id");
                    int id = Integer.parseInt(num);
                    System.out.println(user.id);
                    Hurricane h = hurricaneSelect(conn, id);
                    System.out.println(h.name);
                    System.out.println(h.userID);
                    if (user.id == h.userID) {
                        deleteHurricane(conn, id);
                        response.redirect("/");
                        return null;
                    }
                    return null;
                }
        );

        Spark.get(
                "/edit-message",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("loginName");
                    User user = selectUser(conn, name);
                    if (user == null) {
                        response.redirect("/");
                        return null;
                    }
                    String hid = request.queryParams("id");
                    int id = Integer.parseInt(hid);
                    HashMap m = new HashMap();
                    ArrayList<Hurricane> hurricanes = selectHurricanes(conn);
                    for (Hurricane hurricane : hurricanes) {
                        if (hurricane.id == id) {
                            Hurricane h1 = hurricane;
                            m.put("hurricane", h1);
                        }
                        else {
                            response.redirect("/");
                        }
                    }
                    return new ModelAndView(null, "edit.html");
                }
        );

        Spark.post(
                "/edit-message",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("loginName");
                    User user = selectUser(conn, name);
                    if (user == null) {
                        response.redirect("/");
                        return null;
                    }
                    String hid = request.queryParams("hid");
                    int id = Integer.parseInt(hid);
                    String hname = request.queryParams("hname");
                    String hlocation = request.queryParams("hlocation");
                    String hcategory = request.queryParams("hcategory");
                    int category = Integer.parseInt(hcategory);
                    String himage = request.queryParams("himage");
                    editHurricane(conn, id, hname, hlocation, category, himage, name);
                    return null;
                }
        );
    }

    public static void createTable(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS hurricanes (id IDENTITY, name VARCHAR, location VARCHAR, category INT, image VARCHAR, user VARCHAR, user_id INT)");
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR)");
    }

    public static void addHurricane(Connection conn, String hname, String hlocation, int category, String himage, String name, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO hurricanes VALUES(NULL, ?, ?, ?, ?, ?, ?)");
        stmt.setString(1, hname);
        stmt.setString(2, hlocation);
        stmt.setInt(3, category);
        stmt.setString(4, himage);
        stmt.setString(5, name);
        stmt.setInt(6, id);
        stmt.execute();
    }

    public static Hurricane hurricaneSelect(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM hurricanes WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            String name = results.getString("name");
            String location = results.getString("location");
            int cat = results.getInt("category");
            String image = results.getString("image");
            String user = results.getString("user");
            int uID = results.getInt("user_id");
            return new Hurricane(id, name, location, cat, image, user, uID);
        }
        return null;
    }

    public static ArrayList selectHurricanes(Connection conn) throws SQLException {
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

    public static void deleteHurricane(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM hurricanes WHERE hurricanes.id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }

    public static void editHurricane(Connection conn, int id, String hname, String hlocation, int hcategory, String himage, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE VALUES(?, ?, ?, ?, ?, ?)");
        stmt.setInt(1, id);
        stmt.setString(2, hname);
        stmt.setString(3, hlocation);
        stmt.setInt(4, hcategory);
        stmt.setString(5, himage);
        stmt.setString(6, name);
        stmt.execute();
    }

    public static void insertUser(Connection conn, String name, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES(NULL, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, password);
        stmt.execute();
    }

    public static User selectUser(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int id = results.getInt("id");
            String password = results.getString("password");
            return new User(id, name, password);
        }
        return null;
    }
}
