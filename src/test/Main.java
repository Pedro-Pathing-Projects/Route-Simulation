package test;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Path Visualizer");

        // Provide the path to your image
        String imagePath = "src/resources/intothedeepfield.png"; // Replace with your image file path

        // Create the control panel first
        ControlPanel controlPanel = new ControlPanel();
        GridPanel panel = new GridPanel(imagePath, controlPanel); // Pass controlPanel to the grid

        panel.setPreferredSize(new Dimension(1000, 960));

        // Set the layout of the frame
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.EAST);

        // Set the control panel reference in the grid panel
        controlPanel.setGridPanel(panel);

        frame.pack();
        frame.setResizable(false); // Prevent resizing to ensure consistent size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
