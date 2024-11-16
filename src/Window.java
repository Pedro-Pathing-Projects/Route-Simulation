import javax.swing.*;

public class Window {
    public static void main(String[] args) {
        JFrame window = new JFrame("Pathfinding Visualization");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);

        Panel panel = new Panel();
        window.add(panel);

        window.pack();
        window.setSize(1200, 800); // Set a larger size for better layout
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}