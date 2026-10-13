// Main.java - Application entry point
// Launches the Swing admin window

public class Main {
    public static void main(String[] args) {
        // Load MySQL JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Check that lib/mysql-connector-j-9.6.0.jar is on the classpath.");
            return;
        }

        // Launch Swing UI on the Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (RuntimeException e) {
                javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(),
                        "ExamCell could not start", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
