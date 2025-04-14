package org.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.OptionalInt;

import org.communication.Message;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

public class SimpleChatPanel extends JFrame {
    private final JPanel chatPanel;
    private final JTextArea inputField;
    private final PrintStream output;

    private static final Font FONT = new Font("Consolas", Font.PLAIN, 17);

    public SimpleChatPanel(PrintStream output) {
        this.output = output;
        setTitle("Simple Chat");
        setSize(400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setResizable(false);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));

        JScrollPane scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextArea();
        inputField.setFont(FONT);
        inputField.setLineWrap(true);
        inputField.setWrapStyleWord(true);

        inputPanel.add(inputField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        JButton fileButton = new JButton();
        fileButton.setPreferredSize(new Dimension(32, 32));
        fileButton.setToolTipText("Selecionar arquivo");
        Icon folderIcon = UIManager.getIcon("FileView.directoryIcon");
        fileButton.setIcon(folderIcon);
        fileButton.addActionListener((ActionEvent e) -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(SimpleChatPanel.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String path = selectedFile.getAbsolutePath();
                inputField.setText("/send-file <user-name> " + path);
            }
        });

        buttonPanel.add(fileButton);
        buttonPanel.add(sendButton);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        inputField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "sendMessage");
        inputField.getActionMap().put("sendMessage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputField.getInputMap().put(KeyStroke.getKeyStroke("shift ENTER"), "insert-break");
        inputField.getActionMap().put("insert-break", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputField.append("\n");
            }
        });

        inputField.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    if (!droppedFiles.isEmpty()) {
                        File file = droppedFiles.get(0);
                        inputField.setText("/send-file <user-name> " + file.getAbsolutePath());
                    }
                } catch (Exception ex) {
                    System.out.println("Erro ao processar arquivo: " + ex.getMessage());
                }
            }
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Client.closeConnection();
            }
        });

        setVisible(true);
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;

        appendMessage("You", message, new Color(173, 255, 47), FlowLayout.RIGHT);
        this.output.println(message + "\n<END>");
        inputField.setText("");

        if (message.startsWith("/send-file")) {
            sendFile(message);
        }
    }

    private void sendFile(String message) {
        DataOutputStream dataOutputStream;
        FileInputStream fileInputStream = null;

        try {
            String[] args = message.split(" ");
            File fileToSend = new File(args[2]);

            if (!fileToSend.exists()) {
                fileToSend.createNewFile();
            }

            dataOutputStream = new DataOutputStream(output);
            fileInputStream = new FileInputStream(fileToSend);
            dataOutputStream.writeLong(fileToSend.length());

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesSent = 0;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                totalBytesSent += bytesRead;

                double progress = (double) totalBytesSent / fileToSend.length() * 100;
                System.out.printf("Enviando: %s - Progresso: %.2f%%\n", fileToSend.getName(), progress);
            }
        } catch (Exception exception) {
            System.out.println("ERRO");
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException ignored) { }
        }
    }

    public void receiveMessage(Message message) {
        Color messageColor = message.getType().isError()
            ? new Color(236, 61, 61)
            : new Color(105, 188, 255);

        appendMessage(message.getFrom(), message.getContent().trim(), messageColor, FlowLayout.LEFT);
    }

    private void appendMessage(String from, String message, Color color, int orientation) {
        Border border = BorderFactory.createLineBorder(color.darker(), 2);

        OptionalInt columns = Arrays.stream(message.split("\n"))
                .mapToInt(String::length)
                .max();

        JTextArea messageArea = new JTextArea(message);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setPreferredSize(null);
        messageArea.setEditable(false);
        messageArea.setFont(FONT);
        messageArea.setBackground(color);
        messageArea.setAlignmentX(orientation == FlowLayout.LEFT ? Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT);
        messageArea.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        messageArea.revalidate();
        messageArea.setMaximumSize(new Dimension(
                columns.isPresent()
                        ? Math.min(columns.getAsInt() * 30, 340)
                        : 340,
                getTotalVisibleLines(messageArea) * 20 + 15
        ));

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        JLabel label = new JLabel(sdf.format(new Date()) + " - From: " + from);
        label.setFont(new Font("Tahoma", Font.PLAIN, 10));
        label.setAlignmentX(orientation == FlowLayout.LEFT ? Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messagePanel.add(messageArea);
        messagePanel.add(label);
        messagePanel.setMaximumSize(new Dimension(400, getTotalVisibleLines(messageArea) * 20 + 40));

        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        chatPanel.repaint();
    }

    private static int getTotalVisibleLines(JTextArea textArea) {
        try {
            String content = textArea.getText();
            List<String> lines = Arrays.asList(content.split("\n"));
            return lines.stream()
                    .mapToInt(line -> line.length() / 36)
                    .sum() + lines.size();
        } catch (Exception e) {
            return 1;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PrintStream output = new PrintStream(System.out);
            SimpleChatPanel chat = new SimpleChatPanel(output);
            chat.setVisible(true);
        });
    }
}
