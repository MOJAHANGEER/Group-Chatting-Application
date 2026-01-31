package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClient extends JFrame {

    private JPanel chatPanel;
    private JTextField inputField;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ChatClient(String username) {
        this.username = username;

        setTitle("WhatsApp Style Chat");
        setSize(420, 620);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(37, 211, 102));
        topBar.setPreferredSize(new Dimension(100, 55));

        JLabel title = new JLabel("  Group Chat Application");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 17));

        JPanel icons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        icons.setOpaque(false);

        icons.add(new JLabel("📞"));
        icons.add(new JLabel("📹"));
        icons.add(new JLabel("⋮"));

        topBar.add(title, BorderLayout.WEST);
        topBar.add(icons, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(229, 221, 213)); // WhatsApp bg

        JScrollPane scroll = new JScrollPane(chatPanel);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        inputPanel.setBackground(Color.WHITE);

        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 20)); 
        inputField.setPreferredSize(new Dimension(100, 45));  

        inputField.setBorder(new EmptyBorder(8, 10, 8, 10));

        JButton sendBtn = new JButton("➤");
        sendBtn.setBackground(new Color(37, 211, 102));
        sendBtn.setForeground(Color.WHITE);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        sendBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        connectToServer();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 2004);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        String msg;
                        while ((msg = in.readLine()) != null) {
                            boolean isMe = msg.startsWith(username + ":");
                            addMessage(msg, isMe);
                        }
                    } catch (IOException e) {
                        addMessage("Disconnected", false);
                    }
                }
            });
            t.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Server not running!");
            System.exit(0);
        }
    }

    private void sendMessage() {
        String msg = inputField.getText();
        if (!msg.trim().isEmpty()) {
            out.println(username + ": " + msg);
            inputField.setText("");
        }
    }

    
    private void addMessage(String fullMessage, boolean isMe) {

        // Split name and message
        String sender;
        String message;

        int idx = fullMessage.indexOf(":");
        if (idx != -1) {
            sender = fullMessage.substring(0, idx);
            message = fullMessage.substring(idx + 1).trim();
        } else {
            sender = "Unknown";
            message = fullMessage;
        }

        // Full row (alignment control)
        JPanel row = new JPanel(
                new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 6, 2));
        row.setOpaque(false);

        // Bubble (content-size only)
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new EmptyBorder(4, 6, 4, 6));
        bubble.setOpaque(true);

        if (isMe) {
            bubble.setBackground(new Color(5, 97, 98)); // dark green
        } else {
            bubble.setBackground(Color.WHITE);
        }

        // Sender name (TOP)
        JLabel nameLabel = new JLabel(sender);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 11));
        nameLabel.setForeground(isMe ? Color.WHITE : new Color(0, 102, 0));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Message text (SECOND LINE)
        JLabel messageLabel = new JLabel(
                "<html><body style='width:200px'>" + message + "</body></html>");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(isMe ? Color.WHITE : Color.BLACK);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Time (BOTTOM RIGHT)
        JLabel timeLabel = new JLabel(getTime());
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 8));
        timeLabel.setForeground(isMe ? Color.LIGHT_GRAY : Color.GRAY);
        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        // Add components
        bubble.add(nameLabel);
        bubble.add(Box.createVerticalStrut(2)); // tiny gap
        bubble.add(messageLabel);
        bubble.add(Box.createVerticalStrut(2));
        bubble.add(timeLabel);

        row.add(bubble);
        chatPanel.add(row);

        chatPanel.revalidate();
        chatPanel.repaint();
    }


    private String getTime() {
        return new SimpleDateFormat("HH:mm").format(new Date());
    }

    public static void main(String[] args) {
        String name = JOptionPane.showInputDialog("Enter your name:");
        if (name != null && !name.trim().isEmpty()) {
            new ChatClient(name).setVisible(true);
        }
    }
}

