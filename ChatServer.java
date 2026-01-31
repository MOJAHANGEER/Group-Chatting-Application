package server;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

public class ChatServer {

    private static Set<ClientHandler> clients = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Server started...");
        int port = 2004;

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler client = new ClientHandler(socket);
                clients.add(client);
                client.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Send message to all clients
    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    // Remove client when disconnected
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static synchronized void addClient(ClientHandler client) {
        clients.add(client);
        System.out.println("Client connected. Total: " + clients.size());
    }

}

class ClientHandler extends Thread {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.socket = socket;

        try {
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(socket.getOutputStream(), true);

            ChatServer.addClient(this); // Adding client

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String message;

            while ((message = in.readLine()) != null) {

                // message format: username: actual message
                String sender = message.split(":")[0];
                String msg = message.substring(message.indexOf(":") + 1).trim();

                // SAVE IN DATABASE
                saveMessage(sender, msg);

                // SEND TO ALL CLIENTS
                ChatServer.broadcast(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendMessage(String message) {
        out.println(message);
    }
    
    private void saveMessage(String sender, String message) {

        try {
            Connection con = DBConnection.getConnection();

            String sql =
              "INSERT INTO messages(sender, message) VALUES (?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sender);
            ps.setString(2, message);

            ps.executeUpdate();

            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


