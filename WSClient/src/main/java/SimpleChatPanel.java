import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.io.PrintStream;

public class SimpleChatPanel extends JFrame implements ClientUI {
    private JPanel chatPanel;
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


        // Painel de chat que vai conter as mensagens
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.WHITE);

        // JScrollPane para permitir rolagem
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
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
        appendMessage(from, message, new Color(105, 188, 255), FlowLayout.LEFT); // azul para mensagens recebidas
    }

    private void appendMessage(String from, String message, Color color, int orientation) {
        // Painel para cada mensagem
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new FlowLayout(orientation)); // Mensagens à esquerda ou direita

        // Criar o painel de mensagem com bordas arredondadas
        JTextArea messageArea = new JTextArea(from + ": " + message);
        messageArea.setEditable(false);
        messageArea.setFont(FONT);
        messageArea.setBackground(color);
//        messageArea.setForeground(Color.WHITE);
        messageArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);

        // Definir borda arredondada
        Border border = BorderFactory.createLineBorder(color.darker(), 2);
        messageArea.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        messagePanel.add(messageArea);

        // Adiciona a mensagem ao painel de chat
        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        chatPanel.repaint();

        // Rola automaticamente para a última mensagem
//        JScrollBar vertical = ((JScrollPane) chatPanel.getParent()).getVerticalScrollBar();
//        vertical.setValue(vertical.getMaximum());
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
