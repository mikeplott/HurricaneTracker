package com.company;

import org.junit.Test;

import java.sql.*;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by michaelplott on 10/13/16.
 */
public class MainTest {
    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Main.createTable(conn);
        return conn;
    }

    @Test
    public void testUser() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Mike", "123");
        User user = Main.selectUser(conn, "Mike");
        conn.close();
        assertTrue(user != null);
    }

    @Test
    public void testHurricane() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Mike", "123");
        User user = Main.selectUser(conn, "Mike");
        Main.addHurricane(conn, "lskajdf", "lkajsdlfkj", 3, "alksjdfljaf", "Mike", user.id);
        Main.addHurricane(conn, "alkjsdflkjsal", "kjahsdfljka", 4, "ajldkfjasljf", "Mike", user.id);
        ArrayList<Hurricane> hurricanes = Main.selectHurricanes(conn);
        conn.close();
        assertTrue(hurricanes.size() == 2);
    }

    @Test
    public void testSelector() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Mike", "123");
        User user = Main.selectUser(conn, "Mike");
        Main.addHurricane(conn, "lskajdf", "lkajsdlfkj", 3, "alksjdfljaf", "Mike", user.id);
        Main.addHurricane(conn, "alkjsdflkjsal", "kjahsdfljka", 4, "ajldkfjasljf", "Mike", user.id);
        Hurricane hurricane = Main.hurricaneSelect(conn, 1);
        assertTrue(hurricane != null);
    }

//    @Test
//    public Hurricane getHurricane() throws SQLException {
//        Connection conn = startConnection();
//        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM hurricanes WHERE name = notmathew");
//        ResultSet results = stmt.executeQuery();
//        if (results.next()) {
//            int id = results.getInt("id");
//            String name = results.getString("name");
//            String location = results.getString("location");
//            int cat = results.getInt("category");
//            String image = results.getString("image");
//            String user = results.getString("user");
//            return new Hurricane(id, name, location, cat, image, user);
//        }
//        return null;
//    }

}