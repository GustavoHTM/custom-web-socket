import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SimpleChatPanel extends JFrame implements ClientUI {
    private JPanel chatPanel;
    private JTextArea inputField;
    private JButton sendButton;
    private PrintStream output;

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

        sendButton = new JButton("Send");

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputField.getInputMap().put(KeyStroke.getKeyStroke("ctrl ENTER"), "enviar");
        inputField.getActionMap().put("enviar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        setVisible(true);
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            appendMessage("You", message, new Color(173, 255, 47), FlowLayout.RIGHT); // verde claro para mensagens enviadas
            this.output.println(message + "\n<END>");
            inputField.setText("");
        }
    }

    @Override
    public void receiveMessage(String from, String message) {
        appendMessage(from, message.trim(), new Color(105, 188, 255), FlowLayout.LEFT); // azul para mensagens recebidas
    }

    private void appendMessage(String from, String message, Color color, int orientation) {
        Border border = BorderFactory.createLineBorder(color.darker(), 2);

        JTextArea messageArea = new JTextArea(from + ": " + message);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setPreferredSize(null);
        messageArea.setEditable(false);
        messageArea.setFont(FONT);
        messageArea.setBackground(color);
        messageArea.setAlignmentX(orientation == FlowLayout.LEFT ? Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT);
        messageArea.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        messageArea.revalidate();
        messageArea.setMaximumSize(new Dimension(340, getTotalVisibleLines(messageArea) * 20 + 15));

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        JLabel label = new JLabel(sdf.format(new Date()) + " - From: " + from);
        label.setFont(new Font("Tahoma", Font.PLAIN, 10));
        label.setAlignmentX(orientation == FlowLayout.LEFT ? Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messagePanel.add(messageArea);
        messagePanel.add(label);
        messagePanel.add(label);

//        messagePanel.setBackground(Color.yellow);
//        messagePanel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        messagePanel.setMaximumSize(new Dimension(400, getTotalVisibleLines(messageArea) * 20 + 40));

        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        chatPanel.repaint();

//        System.out.println(String.format("Lines: %d | LineCount: %d | Rows: %d | Height: %d | Panel Height: %d",
//            getTotalVisibleLines(messageArea), messageArea.getLineCount(), messageArea.getRows(), messageArea.getHeight(), messagePanel.getHeight())
//        );
    }

    private static int getTotalVisibleLines(JTextArea textArea) {
        try {
            String content = textArea.getText();
            List<String> lines = Arrays.asList(content.split("\n"));

            return lines.stream()
                .mapToInt(linha -> linha.length() / 36)
                .sum() + lines.size();
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public void onClose(Runnable method) {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                method.run();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PrintStream output = new PrintStream(System.out);
            SimpleChatPanel chat = new SimpleChatPanel(output);
            chat.setVisible(true);
        });
    }
}
