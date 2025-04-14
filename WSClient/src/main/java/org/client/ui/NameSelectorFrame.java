package org.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.client.Client;
import org.communication.IOCommunication;
import org.communication.Message;


public class NameSelectorFrame extends JFrame {

    public NameSelectorFrame(PrintStream output) {
        setTitle("Escolha seu nome");
        setSize(350, 160);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JTextField nameField = new JTextField(20);
        JButton chooseButton = new JButton("Selecionar");
        JLabel errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inputPanel.add(new JLabel("Digite seu nome:"));
        inputPanel.add(nameField);
        inputPanel.add(chooseButton);

        JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        errorPanel.add(errorLabel);

        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());
        container.add(inputPanel, BorderLayout.CENTER);
        container.add(errorPanel, BorderLayout.SOUTH);
        add(container);

        Action submitAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    errorLabel.setText("Nome não pode estar vazio.");
                    return;
                }

                IOCommunication ioCommunication = IOCommunication.getInstance(name);

                synchronized (output) {
                    String messageContent = "/choose-name " + name;
                    ioCommunication.sendMessage(output, messageContent);
                }

                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        Message message = ioCommunication.waitSingleMessageReceive(Client.server.getInputStream());

                        if (message.getType().isError()) {
                            errorLabel.setText(message.getContent());
                            return;
                        }

                        SwingUtilities.invokeLater(() -> {
                            dispose();
                            SimpleChatPanel simpleChatPanel = new SimpleChatPanel(name, output);
                            simpleChatPanel.receiveMessage(message);
                        });
                    } catch (IOException ex) {
                        SwingUtilities.invokeLater(() -> errorLabel.setText("Erro na comunicação com o servidor."));
                    }
                });
            }
        };

        chooseButton.addActionListener(submitAction);
        nameField.addActionListener(submitAction);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    Client.closeConnection();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        setVisible(true);
    }
}
