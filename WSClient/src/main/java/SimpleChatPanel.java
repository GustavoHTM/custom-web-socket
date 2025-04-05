import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class SimpleChatPanel extends JFrame implements ClientUI {
    private JTextArea chatArea;
    private JTextArea inputField;
    private JButton sendButton;
    private PrintStream output;

    private static final Font FONT = new Font("Tahoma", Font.PLAIN, 16);

    public SimpleChatPanel(PrintStream output) {
        this.output = output;

        setTitle("Simple Chat");
        setSize(400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(FONT);
        chatArea.setLineWrap(true);

        add(new JScrollPane(chatArea), BorderLayout.CENTER);

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
            chatArea.append("You: " + message + "\n\n");
            this.output.println(message + "\n<END>");
            inputField.setText("");
        }
    }

    @Override
    public void receiveMessage(String from, String message) {
        chatArea.append(from + ": " + message + "\n");
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
