import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class Panel extends JPanel {

    // Robot
    final int robotInchesX = 16;
    final int robotInchesY = 13;
    private double heading = Math.toRadians(0);

    // Field
    final int fieldInchesX = 144;
    final int fieldInchesY = 144;
    final int nodeInches = 4;
    private final int clearanceX = robotInchesX / nodeInches;
    private final int clearanceY = robotInchesY / nodeInches;

    // Screen size
    final int maxCol = fieldInchesX / nodeInches;
    final int maxRow = fieldInchesY / nodeInches;
    final int nodeScreenSize = 25;
    final int screenWidth = (nodeScreenSize * maxCol) + 405;
    final int screenHeight = (nodeScreenSize * maxRow);

    NodeType mouseState = NodeType.OPEN;
    Image backgroundImage;

    int step = 0;

    ArrayList<Node> nodes = new ArrayList<>(maxCol * maxRow);
    Node startNode, goalNode, currentNode;
    ArrayList<Node> openList = new ArrayList<>(), checkedList = new ArrayList<>();
    boolean goalReached = false;

    public Panel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.WHITE);
        this.setFocusable(true);

        // Initialize the nodes and add to the flat list
        for (int row = 0; row < maxRow; row++) {
            for (int col = 0; col < maxCol; col++) {
                Node node = new Node(col, row);
                node.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        handleMouse(e);
                    }
                });
                nodes.add(node);
                this.add(node);
            }
        }

        // Buttons and layout setup
        JButton runButton = new JButton("Run");
        runButton.addActionListener(e -> autoSearch());

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetGrid());

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> setMouseState(NodeType.START));

        JButton solidButton = new JButton("Solid");
        solidButton.addActionListener(e -> setMouseState(NodeType.SOLID));

        JButton openButton = new JButton("Open");
        openButton.addActionListener(e -> setMouseState(NodeType.OPEN));

        JButton goalButton = new JButton("Goal");
        goalButton.addActionListener(e -> setMouseState(NodeType.GOAL));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(runButton);
        buttonPanel.add(startButton);
        buttonPanel.add(goalButton);
        buttonPanel.add(openButton);
        buttonPanel.add(solidButton);
        buttonPanel.add(resetButton);

        JPanel gridPanel = new JPanel(new GridLayout(maxRow, maxCol)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        for (int row = 0; row < maxRow; row++) {
            for (int col = 0; col < maxCol; col++) {
                gridPanel.add(getNodeAt(col, row));
            }
        }

        this.setLayout(new BorderLayout());
        this.add(gridPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.EAST);
    }

    private Node safeGetNodeAt(int col, int row) {
        if (col >= 0 && col < maxCol && row >= 0 && row < maxRow) {
            return getNodeAt(col, row);
        }
        return null;
    }

    private Node getNodeAt(int col, int row) {
        return nodes.get(row * maxCol + col);
    }

    private void setCostOnNodes() {
        for (int row = 0; row < maxRow; row++) {
            for (int col = 0; col < maxCol; col++) {
                Node node = getNodeAt(col, row);
                getCost(node);
            }
        }
    }

    private void getCost(Node node) {
        int xDistance = Math.abs(node.col - startNode.col);
        int yDistance = Math.abs(node.row - startNode.row);
        double angleToNode = Math.atan2(node.row - startNode.row, node.col - startNode.col);
        double angleDifference = Math.abs(angleToNode - heading);
        double headingFactor = 1.0 + (angleDifference / Math.PI);

        node.gCost = (int) ((xDistance + yDistance) * headingFactor);
        xDistance = Math.abs(node.col - goalNode.col);
        yDistance = Math.abs(node.row - goalNode.row);
        node.hCost = (int) ((xDistance + yDistance) * headingFactor);
        node.fCost = node.gCost + node.hCost;
    }

    public void autoSearch() {
        if (startNode == null || goalNode == null) {
            JOptionPane.showMessageDialog(null, "Please set both a start and goal node before running the search.");
            return;
        }

        currentNode = startNode;
        setCostOnNodes();

        while (!goalReached && step < 3000) {
            if (currentNode == null) {
                JOptionPane.showMessageDialog(null, "No path to the goal found. Please adjust the grid and try again.");
                return;
            }

            currentNode.setAsChecked();
            checkedList.add(currentNode);
            openList.remove(currentNode);

            // Check all 8 possible neighbors (including diagonals)
            int[][] directions = {
                    {0, -1}, {0, 1}, {-1, 0}, {1, 0}, // Cardinal directions
                    {-1, -1}, {1, -1}, {-1, 1}, {1, 1}  // Diagonal directions
            };

            for (int[] dir : directions) {
                int neighborCol = currentNode.col + dir[0];
                int neighborRow = currentNode.row + dir[1];

                if (neighborCol >= 0 && neighborCol < maxCol && neighborRow >= 0 && neighborRow < maxRow) {
                    openNode(getNodeAt(neighborCol, neighborRow));
                }
            }

            currentNode = openList.stream()
                    .min((n1, n2) -> n1.fCost != n2.fCost ? Integer.compare(n1.fCost, n2.fCost) : Integer.compare(n1.gCost, n2.gCost))
                    .orElse(null);

            if (currentNode == goalNode) {
                goalReached = true;
                trackPath();
                return;
            }

            step++;
        }

        if (!goalReached) {
            JOptionPane.showMessageDialog(null, "Goal not reached within the step limit.");
        }
    }

    private void openNode(Node node) {
        if (node == null || node.checked || node.type == NodeType.SOLID) {
            return;
        }

        int tentativeGCost = currentNode.gCost + (node.col != currentNode.col && node.row != currentNode.row ? 14 : 10);

        if (!openList.contains(node) || tentativeGCost < node.gCost) {
            node.gCost = tentativeGCost;
            node.hCost = calculateHCost(node);
            node.fCost = node.gCost + node.hCost;
            node.parent = currentNode;

            if (!openList.contains(node)) {
                openList.add(node);
                node.setAsOpen();
            }
        }
    }

    private int calculateHCost(Node node) {
        int xDistance = Math.abs(node.col - goalNode.col);
        int yDistance = Math.abs(node.row - goalNode.row);
        return (xDistance + yDistance) * 10;
    }

    private boolean canRobotOccupy(Node centerNode) {
        int halfWidth = Math.round((float) robotInchesX / (2 * nodeInches));
        int halfHeight = Math.round((float) robotInchesY / (2 * nodeInches));

        int startCol = centerNode.col - halfWidth;
        int endCol = centerNode.col + halfWidth;
        int startRow = centerNode.row - halfHeight;
        int endRow = centerNode.row + halfHeight;

        if (startCol < 0 || endCol >= maxCol || startRow < 0 || endRow >= maxRow) {
            return false;
        }

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                if (getNodeAt(col, row).type == NodeType.SOLID) {
                    return false;
                }
            }
        }

        return true;
    }

    private void trackPath() {
        Node current = goalNode;

        while (current != startNode) {
            current = current.parent;
            if (current != startNode) {
                current.setAsPath();
            }
        }

        goalNode.setAsGoal();
    }

    public void resetGrid() {
        // Reset all attributes related to the search
        currentNode = null;
        startNode = null;
        goalNode = null;
        checkedList.clear();
        openList.clear();
        goalReached = false;
        step = 0;

        // Reset all nodes
        for (Node n : nodes) {
            n.reset();
        }

        // Force the grid to repaint
        repaint();
    }

    private void handleMouse(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            Node clickedNode = (Node) e.getSource();
            switch (mouseState) {
                case START:
                    if (startNode != null && startNode != clickedNode) startNode.setAsOpen();
                    clickedNode.setAsStart();
                    startNode = clickedNode;
                    currentNode = clickedNode;
                    break;

                case GOAL:
                    if (goalNode != null && goalNode != clickedNode) goalNode.setAsOpen();
                    clickedNode.setAsGoal();
                    goalNode = clickedNode;
                    break;

                case SOLID:
                    clickedNode.setAsSolid();
                    break;

                default:
                    clickedNode.setAsOpen();
            }
        }
        repaint();
    }

    private void setMouseState(NodeType state) {
        mouseState = state;
    }
}