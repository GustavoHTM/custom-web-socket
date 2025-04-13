import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.swing.*;

public class NameSelectorFrame extends JFrame {

    public NameSelectorFrame(Consumer<String> onValidNameSelected, PrintStream output) {
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

                synchronized (output) {
                    output.println("/choose-name " + name + "\n<END>");
                }

                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        Scanner input = new Scanner(Client.server.getInputStream());
                        String response = input.nextLine();
                        String errorMessage = null;

                        if (response.equals("<ERROR>")) {
                            errorMessage = input.nextLine();
                        }

                        String line = input.nextLine();
                        while (!line.equals("<END>")) {
                            line = input.nextLine();
                        }

                        if (errorMessage != null) {
                            errorLabel.setText(errorMessage);
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                dispose();
                                onValidNameSelected.accept(name);
                            });
                        }
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
                Client.closeConnection();
            }
        });

        setVisible(true);
    }
}
