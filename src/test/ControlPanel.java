package test;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static test.ImageBackgroundGrid.PIXELS_PER_INCH;
import static test.ImageBackgroundGrid.SCREEN_SIZE;

public class ControlPanel extends JPanel {
    private JTextField startXField, startYField, endXField, endYField;
    private ImageBackgroundGrid gridPanel;

    public ControlPanel() {
        // This will be set after creation
        setLayout(new GridLayout(5, 2));

        // Create and add components to the panel
        add(new JLabel("Start X:"));
        startXField = new JTextField();
        add(startXField);

        add(new JLabel("Start Y:"));
        startYField = new JTextField();
        add(startYField);

        add(new JLabel("End X:"));
        endXField = new JTextField();
        add(endXField);

        add(new JLabel("End Y:"));
        endYField = new JTextField();
        add(endYField);

        // Automatically update the coordinates in the text fields when dragged
        startXField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateStartCoordinates();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateStartCoordinates();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateStartCoordinates();
            }

            private void updateStartCoordinates() {
                try {
                    int startX = Integer.parseInt(startXField.getText());
                    int startY = Integer.parseInt(startYField.getText());
                    gridPanel.getStartPoint().setLocation(startX, inversedY(startY));
                    gridPanel.repaint();
                } catch (NumberFormatException ignored) { }
            }
        });

        endXField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateEndCoordinates();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateEndCoordinates();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateEndCoordinates();
            }

            private void updateEndCoordinates() {
                try {
                    int endX = Integer.parseInt(endXField.getText());
                    int endY = Integer.parseInt(endYField.getText());
                    gridPanel.getEndPoint().setLocation(endX, inversedY(endY));
                    gridPanel.repaint();
                } catch (NumberFormatException ignored) { }
            }
        });
    }

    // Method to update the coordinates in the control panel text fields automatically
    public void updateCoordinates(double startXInches, double startYInches, double endXInches, double endYInches) {
        startXField.setText(String.format("%.2f", startXInches));
        startYField.setText(String.format("%.2f", inversedYInches(startYInches)));
        endXField.setText(String.format("%.2f", endXInches));
        endYField.setText(String.format("%.2f", inversedYInches(endYInches)));
    }


    // Set the grid panel reference
    public void setGridPanel(ImageBackgroundGrid gridPanel) {
        this.gridPanel = gridPanel;
    }

    public static double inversedY(double y) {
        return SCREEN_SIZE- y;
    }


    public static double inversedYInches(double y) {
        return pixelsToInches(SCREEN_SIZE) - y;
    }

    private static double inchesToPixels(int pixels) {
        return pixels / PIXELS_PER_INCH;
    }

    private static double pixelsToInches(int pixels) {
        return pixels / PIXELS_PER_INCH;
    }
}
