package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageBackgroundGrid extends JPanel {
    private static final int SCREEN_SIZE = 1000; // Fixed screen size (1000x1000 pixels)
    private static final int GRID_INCHES = 144; // The grid represents 144x144 inches
    private static final double PIXELS_PER_INCH = (double) SCREEN_SIZE / GRID_INCHES; // Pixels per inch

    private static final int POINT_RADIUS = 7; // Radius of the draggable point in pixels
    private Point startPoint; // The start point
    private Point endPoint; // The end point

    private BufferedImage backgroundImage; // The background image
    private ControlPanel controlPanel; // Reference to the ControlPanel to update the fields

    public ImageBackgroundGrid(String imagePath, ControlPanel controlPanel) {
        this.controlPanel = controlPanel; // Initialize the control panel reference

        // Initialize the draggable points (startPoint and endPoint)
        startPoint = new Point(SCREEN_SIZE / 4, SCREEN_SIZE / 2); // Start point at 1/4th of the screen
        endPoint = new Point(SCREEN_SIZE * 3 / 4, SCREEN_SIZE / 2); // End point at 3/4th of the screen

        // Load the background image
        try {
            backgroundImage = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            backgroundImage = null; // Fallback if the image fails to load
        }

        // Add mouse listener for dragging functionality
        MouseAdapter mouseAdapter = new MouseAdapter() {
            private Point draggedPoint = null;

            @Override
            public void mousePressed(MouseEvent e) {
                // Check if the click is inside either point
                if (Math.sqrt(Math.pow(e.getX() - startPoint.x, 2) + Math.pow(e.getY() - startPoint.y, 2)) <= POINT_RADIUS) {
                    draggedPoint = startPoint;
                } else if (Math.sqrt(Math.pow(e.getX() - endPoint.x, 2) + Math.pow(e.getY() - endPoint.y, 2)) <= POINT_RADIUS) {
                    draggedPoint = endPoint;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedPoint = null; // Stop dragging when released
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedPoint != null) {
                    // Update the dragged point's position
                    draggedPoint.x = e.getX();
                    draggedPoint.y = e.getY();

                    // Snap to grid (optional)
                    draggedPoint.x = (int) (Math.round(draggedPoint.x / PIXELS_PER_INCH) * PIXELS_PER_INCH);
                    draggedPoint.y = (int) (Math.round(draggedPoint.y / PIXELS_PER_INCH) * PIXELS_PER_INCH);

                    repaint();

                    // Update control panel's text fields automatically
                    controlPanel.updateCoordinates(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
                }
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the background image
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, null);
        }

        // Draw the start and end points
        g.setColor(new Color(11, 162, 0)); // Start point color
        g.fillOval(startPoint.x - POINT_RADIUS, startPoint.y - POINT_RADIUS, POINT_RADIUS * 2, POINT_RADIUS * 2);

        g.setColor(Color.RED); // End point color
        g.fillOval(endPoint.x - POINT_RADIUS, endPoint.y - POINT_RADIUS, POINT_RADIUS * 2, POINT_RADIUS * 2);
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }
}
