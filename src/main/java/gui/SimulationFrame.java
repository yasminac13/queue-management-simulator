package gui;

import businesslogic.SelectionPolicy;
import businesslogic.SimulationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SimulationFrame extends JFrame {

    private JTextField clientsField, queuesField, timeLimitField;
    private JTextField minArrField, maxArrField, minSerField, maxSerField;
    private JButton startButton;
    private JTextArea logArea;
    private JComboBox<SelectionPolicy> strategyBox;

    public SimulationFrame() {

        setTitle("Queues Management Simulation");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel setupPanel = new JPanel(new GridLayout(5, 4, 5, 5));
        setupPanel.setBorder(BorderFactory.createTitledBorder("Simulation Setup"));

        setupPanel.add(new JLabel("Number of Clients (N):"));
        clientsField = new JTextField("4");
        setupPanel.add(clientsField);

        setupPanel.add(new JLabel("Number of Queues (Q):"));
        queuesField = new JTextField("2");
        setupPanel.add(queuesField);

        setupPanel.add(new JLabel("Max Simulation Time:"));
        timeLimitField = new JTextField("60");
        setupPanel.add(timeLimitField);

        setupPanel.add(new JLabel("Min Arrival Time:"));
        minArrField = new JTextField("2");
        setupPanel.add(minArrField);

        setupPanel.add(new JLabel("Max Arrival Time:"));
        maxArrField = new JTextField("30");
        setupPanel.add(maxArrField);

        setupPanel.add(new JLabel("Min Service Time:"));
        minSerField = new JTextField("2");
        setupPanel.add(minSerField);

        setupPanel.add(new JLabel("Max Service Time:"));
        maxSerField = new JTextField("4");
        setupPanel.add(maxSerField);

        setupPanel.add(new JLabel("Routing Strategy:"));
        strategyBox = new JComboBox<>(SelectionPolicy.values());
        setupPanel.add(strategyBox);

        startButton = new JButton("Start Simulation");
        setupPanel.add(new JLabel(""));
        setupPanel.add(startButton);

        add(setupPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Real-Time Log of Events"));

        add(scrollPane, BorderLayout.CENTER);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSimulation();
            }
        });
    }

    private void startSimulation() {
        try {
            logArea.setText("");
            startButton.setEnabled(false);

            SimulationManager manager = new SimulationManager(this);

            manager.setNumberOfClients(Integer.parseInt(clientsField.getText()));
            manager.setNumberOfServers(Integer.parseInt(queuesField.getText()));
            manager.setTimeLimit(Integer.parseInt(timeLimitField.getText()));
            manager.setMinArrivalTime(Integer.parseInt(minArrField.getText()));
            manager.setMaxArrivalTime(Integer.parseInt(maxArrField.getText()));
            manager.setMinProcessingTime(Integer.parseInt(minSerField.getText()));
            manager.setMaxProcessingTime(Integer.parseInt(maxSerField.getText()));
            manager.setSelectionPolicy((SelectionPolicy) strategyBox.getSelectedItem());

            Thread simulationThread = new Thread(manager);
            simulationThread.start();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid integer numbers in all fields!",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            startButton.setEnabled(true);
        }
    }

    public void appendToLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void enableStartButton() {
        SwingUtilities.invokeLater(() -> startButton.setEnabled(true));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulationFrame frame = new SimulationFrame();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}