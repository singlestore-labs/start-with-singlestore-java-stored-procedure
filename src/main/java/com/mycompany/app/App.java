package com.mycompany.app;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class App 
{
    // TODO: read from config
    private static final String HOSTNAME = "127.0.0.1";
    private static final String PORT = "3306";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String DATABASE = "acme";

    public static void main(String[] args) {
        try {
            verifyDriver();
            
            String connection = String.format("jdbc:mariadb://%s:%s/%s", HOSTNAME, PORT, DATABASE);
            Connection conn = DriverManager.getConnection(connection, USER, PASSWORD);

            long id = create(conn, "Inserted row");
            System.out.println(String.format("Inserted row id %d", id));

            Message message = readOne(conn, id);
            if (message == null) {
                System.out.println("Read one: failed");
            } else {
                System.out.println(String.format("Read one: %d, %s, %tD %tT", message.getId(), message.getContent(), message.getCreateDate(), message.getCreateDate()));
            }
            
            update(conn, id, "Updated row");
            System.out.println(String.format("Updated row id %d", id));
            
            List<Message> messages = readAll(conn);
            System.out.println("Read all rows:");
            for (Message msg: messages)
            {
                System.out.println(String.format("  %d, %s, %tD %tT", msg.getId(), msg.getContent(), msg.getCreateDate(), msg.getCreateDate()));
            }

            delete(conn, id);
            System.out.println(String.format("Deleted row id %d", id));

        } catch (Exception e) {
            System.out.println("Error:");
            System.out.println(e.getLocalizedMessage());                
            e.printStackTrace();
        }
    }

    public static void verifyDriver() throws ClassNotFoundException {
        Class.forName("org.mariadb.jdbc.Driver");
    }
    
    public static Long create(Connection conn, String content) throws SQLException, Exception {
        PreparedStatement stmt = conn.prepareStatement("{ call messages_create(?) }", Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, content);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            throw new Exception("Insert failed");
        }
        Long id = rs.getLong(1);
        return id;
    }
    
    public static Message readOne(Connection conn, Long id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("{ call messages_read_by_id(?) }");
        stmt.setLong(1, id);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            return null;
        }

        Message message = new Message();
        message.setId(rs.getLong("id"));
        message.setContent(rs.getString("content"));
        message.setCreateDate(rs.getDate("createdate"));
        return message;
    }
    
    public static List<Message> readAll(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("{ call messages_read_all() }");
        ResultSet rs = stmt.executeQuery();

        List<Message> messages = new ArrayList<Message>();
        while(rs.next()) {
            Message message = new Message();
            message.setId(rs.getLong("id"));
            message.setContent(rs.getString("content"));
            message.setCreateDate(rs.getDate("createdate"));
            messages.add(message);
        }
        return messages;
    }
    
    public static void update(Connection conn, Long id, String content) throws SQLException, Exception {
        PreparedStatement stmt = conn.prepareStatement("{ call messages_update(?, ?) }");
        stmt.setLong(1, id);
        stmt.setString(2, content);
        stmt.executeQuery();
    }
    
    public static void delete(Connection conn, Long id) throws SQLException, Exception {
        PreparedStatement stmt = conn.prepareStatement("{ call messages_delete(?) }");
        stmt.setLong(1, id);
        stmt.executeQuery();
    }
    
}
