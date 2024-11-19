package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import javax.imageio.ImageIO;

public class GridPanel extends JPanel {
    public static final int SCREEN_SIZE = 960; // Fixed screen size
    public static final int GRID_INCHES = 144; // Grid dimensions in inches
    public static final double PIXELS_PER_INCH = (double) SCREEN_SIZE / GRID_INCHES;

    public static double robotInchesX = 16;
    public static double robotInchesY = 13;

    private static final int POINT_RADIUS = 7; // Radius of draggable points
    private Point startPoint; // The start point
    private Point endPoint;   // The end point

    private Rectangle sub = new Rectangle(inchesToPixels(48), inchesToPixels(48), inchesToPixels(48), inchesToPixels(48));

    private BufferedImage backgroundImage;
    private ControlPanel controlPanel;

    private ArrayList<Point> path = new ArrayList<>(); // Holds the calculated path

    public GridPanel(String imagePath, ControlPanel controlPanel) {
        this.controlPanel = controlPanel;

        startPoint = new Point(SCREEN_SIZE / 4, SCREEN_SIZE / 2);
        endPoint = new Point(SCREEN_SIZE * 3 / 4, SCREEN_SIZE / 2);

        try {
            backgroundImage = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            backgroundImage = null;
        }

        MouseAdapter mouseAdapter = new MouseAdapter() {
            private Point draggedPoint = null;

            @Override
            public void mousePressed(MouseEvent e) {
                if (isPointWithinRadius(e.getPoint(), startPoint)) {
                    draggedPoint = startPoint;
                } else if (isPointWithinRadius(e.getPoint(), endPoint)) {
                    draggedPoint = endPoint;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedPoint = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedPoint != null) {
                    draggedPoint.x = e.getX();
                    draggedPoint.y = e.getY();

                    controlPanel.updateCoordinates(
                            pixelsToInches(startPoint.x), pixelsToInches(startPoint.y),
                            pixelsToInches(endPoint.x), pixelsToInches(endPoint.y)
                    );

                    calculatePath(); // Recalculate path whenever a point is moved
                    repaint();
                }
            }

            private boolean isPointWithinRadius(Point p1, Point p2) {
                return p1.distance(p2) <= POINT_RADIUS;
            }

            private double pixelsToInches(int pixels) {
                return pixels / PIXELS_PER_INCH;
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        calculatePath(); // Initial path calculation
    }

    private void calculatePath() {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        boolean[][] visited = new boolean[SCREEN_SIZE][SCREEN_SIZE];
        Node[][] nodes = new Node[SCREEN_SIZE][SCREEN_SIZE];

        Point start = startPoint;
        Point goal = endPoint;

        openSet.add(new Node(start.x, start.y, null, 0, heuristic(start, goal)));

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (visited[current.x][current.y]) continue;
            visited[current.x][current.y] = true;

            if (current.x == goal.x && current.y == goal.y) {
                reconstructPath(current);
                smoothPath(); // Smooth the reconstructed path
                return;
            }

            for (Node neighbor : getNeighbors(current, goal)) {
                if (!visited[neighbor.x][neighbor.y] && isOutsideAllowedRange(neighbor.x, neighbor.y)) {
                    openSet.add(neighbor);
                }
            }
        }

        path.clear(); // Clear the path if no solution is found
    }


    private void smoothPath() {
        if (path.size() < 3) return; // Not enough points to smooth

        ArrayList<Point> smoothedPath = new ArrayList<>();
        smoothedPath.add(path.get(0)); // Add the starting point

        for (int i = 0; i < path.size() - 2; i++) {
            Point p0 = path.get(i);
            Point p1 = path.get(i + 1);
            Point p2 = path.get(i + 2);

            // Generate intermediate points for the Bézier curve
            for (double t = 0; t <= 1; t += 0.02) { // Smaller step for smoother curves
                double x = Math.pow(1 - t, 2) * p0.x + 2 * (1 - t) * t * p1.x + Math.pow(t, 2) * p2.x;
                double y = Math.pow(1 - t, 2) * p0.y + 2 * (1 - t) * t * p1.y + Math.pow(t, 2) * p2.y;
                smoothedPath.add(new Point((int) x, (int) y));
            }
        }

        smoothedPath.add(path.get(path.size() - 1)); // Add the final point
        path = smoothedPath; // Replace the original path with the smoothed one
    }




    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, null);
        }

        g.setColor(new Color(11, 162, 0)); // Start point color
        g.fillOval(startPoint.x - POINT_RADIUS, startPoint.y - POINT_RADIUS, POINT_RADIUS * 2, POINT_RADIUS * 2);

        g.setColor(Color.RED); // End point color
        g.fillOval(endPoint.x - POINT_RADIUS, endPoint.y - POINT_RADIUS, POINT_RADIUS * 2, POINT_RADIUS * 2);

        g.setColor(new Color(147, 147, 147, 72)); // Submersible color
        g.fillRect(sub.x, sub.y, sub.width, sub.height);

        g.setColor(new Color(121, 121, 121, 166)); // Robot dimensions
        g.fillRect(startPoint.x - inchesToPixels(robotInchesX / 2), startPoint.y - inchesToPixels(robotInchesY / 2),
                inchesToPixels(robotInchesX), inchesToPixels(robotInchesY));

        drawPath(g); // Draw the smoothed path
    }

    private void drawPath(Graphics g) {
        g.setColor(Color.BLUE);
        for (int i = 1; i < path.size(); i++) {
            Point p1 = path.get(i - 1);
            Point p2 = path.get(i);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    private void reconstructPath(Node node) {
        path.clear();
        while (node != null) {
            path.add(0, new Point(node.x, node.y));
            node = node.parent;
        }
    }

    private ArrayList<Node> getNeighbors(Node current, Point goal) {
        ArrayList<Node> neighbors = new ArrayList<>();
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] dir : directions) {
            int newX = current.x + dir[0];
            int newY = current.y + dir[1];
            double newCost = current.g + 1;

            neighbors.add(new Node(newX, newY, current, newCost, newCost + heuristic(new Point(newX, newY), goal)));
        }

        return neighbors;
    }

    private boolean isOutsideAllowedRange(int x, int y) {
        Rectangle forbiddenZone = new Rectangle(
                sub.x - inchesToPixels(robotInchesX / 2),
                sub.y - inchesToPixels(robotInchesY / 2),
                sub.width + inchesToPixels(robotInchesX),
                sub.height + inchesToPixels(robotInchesY)
        );

        return !forbiddenZone.contains(x, y);
    }


    private double heuristic(Point p1, Point p2) {
        return p1.distance(p2);
    }

    private static int inchesToPixels(double inches) {
        return (int) (inches * PIXELS_PER_INCH);
    }

    private static class Node implements Comparable<Node> {
        int x, y;
        Node parent;
        double g, f;

        public Node(int x, int y, Node parent, double g, double f) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.g = g;
            this.f = f;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.f, other.f);
        }
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }
}
