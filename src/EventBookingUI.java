import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class EventBookingUI extends JFrame {
    private JTable eventTable;
    private DefaultTableModel tableModel;
    private JButton bookButton, addButton;
    private Connection connection;

    public EventBookingUI() {
        setTitle("Event Booking System");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Location");
        tableModel.addColumn("Booked");

        eventTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(eventTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        bookButton = new JButton("Book Event");
        bookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bookEvent();
            }
        });
        panel.add(bookButton);

        addButton = new JButton("Add Event");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addEvent();
            }
        });
        panel.add(addButton);

        add(panel, BorderLayout.SOUTH);

        try {
            connection = DatabaseConnection.getConnection();
            loadEvents();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadEvents() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT e.Id, e.Name, e.Location, r.EventId IS NOT NULL AS Booked " +
                    "FROM Events e LEFT JOIN Reservations r ON e.Id = r.EventId");
            while (rs.next()) {
                int id = rs.getInt("Id");
                String name = rs.getString("Name");
                String location = rs.getString("Location");
                boolean booked = rs.getBoolean("Booked");
                tableModel.addRow(new Object[]{id, name, location, booked ? "Yes" : "No"});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void bookEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow != -1) {
            int eventId = (int) tableModel.getValueAt(selectedRow, 0);

            try {
                // Check if the event is already booked
                PreparedStatement checkStmt = connection.prepareStatement("SELECT * FROM Reservations WHERE EventId = ?");
                checkStmt.setInt(1, eventId);
                ResultSet checkRs = checkStmt.executeQuery();

                if (checkRs.next()) {
                    JOptionPane.showMessageDialog(this, "Event already booked.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Book the event
                    PreparedStatement bookStmt = connection.prepareStatement("INSERT INTO Reservations (EventId) VALUES (?)");
                    bookStmt.setInt(1, eventId);
                    bookStmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Event booked successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    tableModel.setValueAt("Yes", selectedRow, 3);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an event to book.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addEvent() {
        JTextField idField = new JTextField(5);
        JTextField nameField = new JTextField(20);
        JTextField locationField = new JTextField(20);

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("ID:"));
        myPanel.add(idField);
        myPanel.add(Box.createHorizontalStrut(15)); // a spacer
        myPanel.add(new JLabel("Name:"));
        myPanel.add(nameField);
        myPanel.add(Box.createHorizontalStrut(15)); // a spacer
        myPanel.add(new JLabel("Location:"));
        myPanel.add(locationField);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
                "Please Enter Event Details", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int id = Integer.parseInt(idField.getText());
            String name = nameField.getText();
            String location = locationField.getText();

            try {
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO Events (Id, Name, Location) VALUES (?, ?, ?)");
                stmt.setInt(1, id);
                stmt.setString(2, name);
                stmt.setString(3, location);
                stmt.executeUpdate();

                tableModel.addRow(new Object[]{id, name, location, "No"});
                JOptionPane.showMessageDialog(this, "Event added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        EventBookingUI ui = new EventBookingUI();
        ui.setVisible(true);
    }
}
