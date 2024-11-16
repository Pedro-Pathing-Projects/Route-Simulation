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
    final int screenWidth = (nodeScreenSize * maxCol) + 380;
    final int screenHeight = (nodeScreenSize * maxRow);

    NodeType mouseState = NodeType.OPEN;
    Image backgroundImage;

    int step = 0;

    ArrayList<Node> nodes = new ArrayList<>(maxCol * maxRow);
    // Additional state to track selected nodes for the new feature
    private ArrayList<Node> selectedNodes = new ArrayList<>();
    Node startNode, goalNode, currentNode;
    ArrayList<Node> openList = new ArrayList<>(), checkedList = new ArrayList<>();
    boolean goalReached = false;

    public Panel() {
        this.setLayout(new BorderLayout());

        // Create grid panel
        JPanel gridPanel = new JPanel(new GridLayout(maxRow, maxCol));
        gridPanel.setBackground(Color.WHITE);

        // Add nodes to grid panel
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
                gridPanel.add(node);
            }
        }

        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

// Action Buttons Group
        JPanel actionPanel = new JPanel(new GridLayout(3, 2, 5, 5)); // 3 rows, 2 columns
        actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        actionPanel.setBackground(new Color(240, 240, 240));

        JButton runButton = createStyledButton("Run");
        runButton.addActionListener(e -> autoSearch());
        actionPanel.add(runButton);

        JButton resetButton = createStyledButton("Reset");
        resetButton.addActionListener(e -> {
            resetGrid();
            JOptionPane.showMessageDialog(null, "Grid has been reset.");
        });
        actionPanel.add(resetButton);

        JButton startButton = createStyledButton("Set Start");
        startButton.addActionListener(e -> {
            setMouseState(NodeType.START);
            JOptionPane.showMessageDialog(null, "Click on a node to set it as the Start node.");
        });
        actionPanel.add(startButton);

        JButton goalButton = createStyledButton("Set Goal");
        goalButton.addActionListener(e -> {
            setMouseState(NodeType.GOAL);
            JOptionPane.showMessageDialog(null, "Click on a node to set it as the Goal node.");
        });
        actionPanel.add(goalButton);

        JButton solidButton = createStyledButton("Set Solid");
        solidButton.addActionListener(e -> {
            setMouseState(NodeType.SOLID);
            JOptionPane.showMessageDialog(null, "Click on nodes to set them as Solid.");
        });
        actionPanel.add(solidButton);

        JButton openButton = createStyledButton("Set Open");
        openButton.addActionListener(e -> {
            setMouseState(NodeType.OPEN);
            JOptionPane.showMessageDialog(null, "Click on nodes to set them as Open.");
        });
        actionPanel.add(openButton);

// Settings Buttons Group
        JPanel settingsPanel = new JPanel(new GridLayout(2, 2, 5, 5)); // 2 rows, 2 columns
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
        settingsPanel.setBackground(new Color(240, 240, 240));

        JButton fillSolidButton = createStyledButton("Fill Solid");
        fillSolidButton.addActionListener(e -> {
            activateFillSolidMode();
            JOptionPane.showMessageDialog(null, "Select 4 corner nodes to fill the area as Solid.");
        });
        settingsPanel.add(fillSolidButton);

        JButton boundaryButton = createStyledButton("Boundary Distance");
        boundaryButton.addActionListener(e -> setBoundaryDistance());
        settingsPanel.add(boundaryButton);

        JButton exportButton = createStyledButton("Export Field");
        exportButton.addActionListener(e -> exportField());
        settingsPanel.add(exportButton);

        JButton importButton = createStyledButton("Import Field");
        importButton.addActionListener(e -> importField());
        settingsPanel.add(importButton);

// Add panels to the main button panel
        buttonPanel.add(actionPanel);
        buttonPanel.add(Box.createVerticalStrut(10)); // Spacer
        buttonPanel.add(settingsPanel);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gridPanel, buttonPanel);
        splitPane.setDividerLocation(800); // Adjust width of the grid panel
        splitPane.setResizeWeight(0.8); // Grid panel takes 80% of the space
        splitPane.setEnabled(false); // Disable resizing by the user

        // Add split pane to main panel
        this.add(splitPane, BorderLayout.CENTER);

        System.out.print(buttonPanel.getPreferredSize());
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180)); // Steel Blue (solid infill)
        button.setForeground(Color.WHITE); // Text color
        button.setOpaque(true); // Ensures solid background
        button.setBorderPainted(false); // Removes default border outline

        // Add hover effect for solid fill
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(100, 149, 237)); // Cornflower Blue (hover infill)
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(70, 130, 180)); // Steel Blue (default infill)
            }
        });

        return button;
    }

    private void setBoundaryDistance() {
        String input = JOptionPane.showInputDialog(
                null,
                "Enter the boundary distance (in nodes):",
                "Boundary Distance",
                JOptionPane.PLAIN_MESSAGE
        );

        // Validate input
        if (input == null || input.isEmpty()) return; // User cancelled input
        int distance;
        try {
            distance = Integer.parseInt(input);
            if (distance <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Invalid input. Please enter a positive integer.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Mark nodes within the boundary distance
        for (Node node : nodes) {
            if (node.type == NodeType.SOLID) {
                for (int row = node.row - distance; row <= node.row + distance; row++) {
                    for (int col = node.col - distance; col <= node.col + distance; col++) {
                        // Ensure the node is within bounds
                        Node boundaryNode = safeGetNodeAt(col, row);
                        if (boundaryNode != null && boundaryNode.type == NodeType.OPEN) {
                            double dist = Math.sqrt(
                                    Math.pow(node.row - row, 2) + Math.pow(node.col - col, 2)
                            );
                            if (dist <= distance) {
                                boundaryNode.setAsBoundary(); // Visual distinction
                            }
                        }
                    }
                }
            }
        }

        JOptionPane.showMessageDialog(
                null,
                "Boundary nodes have been marked as boundary.",
                "Boundary Set",
                JOptionPane.INFORMATION_MESSAGE
        );

        repaint(); // Refresh the grid
    }

    private void activateFillSolidMode() {
        JOptionPane.showMessageDialog(null, "Please select 4 corner nodes by clicking on them.");
        mouseState = NodeType.OPEN; // Temporarily disable other node modes
        selectedNodes.clear(); // Clear any previous selection
    }

    private void exportField() {
        JFileChooser fileChooser = new JFileChooser();
        int choice = fileChooser.showSaveDialog(this);

        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Add .field extension if not present
            if (!file.getName().toLowerCase().endsWith(".field")) {
                file = new File(file.getAbsolutePath() + ".field");
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (Node node : nodes) {
                    writer.write(node.col + "," + node.row + "," + node.type + "," +
                            node.checked + "," + node.path + "\n");
                }
                JOptionPane.showMessageDialog(this, "Field exported successfully!", "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to export field.", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importField() {
        JFileChooser fileChooser = new JFileChooser();

        // Add a file filter for .field files
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Field Files", "field"));

        int choice = fileChooser.showOpenDialog(this);

        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                resetGrid(); // Clear the grid before importing
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    int col = Integer.parseInt(parts[0]);
                    int row = Integer.parseInt(parts[1]);
                    NodeType type = NodeType.valueOf(parts[2]);
                    boolean checked = Boolean.parseBoolean(parts[3]);
                    boolean path = Boolean.parseBoolean(parts[4]);

                    Node node = getNodeAt(col, row);

                    // Set the node's state based on the imported data
                    switch (type) {
                        case START:
                            if (startNode != null) startNode.setAsOpen(); // Reset previous start node
                            node.setAsStart();
                            startNode = node;
                            currentNode = node; // Set current node for pathfinding
                            break;
                        case GOAL:
                            if (goalNode != null) goalNode.setAsOpen(); // Reset previous goal node
                            node.setAsGoal();
                            goalNode = node;
                            break;
                        case SOLID:
                            node.setAsSolid();
                            break;
                        case BOUNDARY:
                            node.setAsBoundary();
                            break;
                        case OPEN:
                            node.setAsOpen();
                            break;
                    }

                    // Restore additional flags
                    node.checked = checked;
                    node.path = path;
                    if (path) {
                        node.setAsPath(); // Visually mark path nodes
                    }

                    // Repaint the node
                    node.repaint();
                }

                // Synchronize the internal state
                synchronizeState();

                JOptionPane.showMessageDialog(this, "Field imported successfully!", "Import Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, "Failed to import field.", "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void synchronizeState() {
        // Clear open and checked lists
        openList.clear();
        checkedList.clear();

        // Reset pathfinding attributes
        goalReached = false;
        step = 0;

        // Recalculate costs for all nodes
        setCostOnNodes();

        // Ensure currentNode points to the startNode
        currentNode = startNode;

        // Add the startNode to the openList
        if (startNode != null) {
            openList.add(startNode);
        }
    }

    private void handleFillSolid(Node clickedNode) {
        if (selectedNodes.contains(clickedNode)) return;

        selectedNodes.add(clickedNode);
        clickedNode.setAsSolid();

        if (selectedNodes.size() == 4) {
            markInteriorAsSolid();
            selectedNodes.clear(); // Clear the selection after filling
        }
    }

    private void markInteriorAsSolid() {
        if (selectedNodes.size() != 4) return;

        // Determine the bounds of the rectangle
        // Determine the bounds of the rectangle
        int minCol = selectedNodes.stream().mapToInt(node -> node.col).min().orElse(0);
        int maxCol = selectedNodes.stream().mapToInt(node -> node.col).max().orElse(this.maxCol - 1); // Use 'this.maxCol'
        int minRow = selectedNodes.stream().mapToInt(node -> node.row).min().orElse(0);
        int maxRow = selectedNodes.stream().mapToInt(node -> node.row).max().orElse(this.maxRow - 1); // Use 'this.maxRow'

        // Mark all interior nodes as solid
        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {
                Node node = getNodeAt(col, row);
                if (!selectedNodes.contains(node)) { // Skip the selected corners
                    node.setAsSolid();
                }
            }
        }

        JOptionPane.showMessageDialog(null, "Interior nodes have been marked as solid.");
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

        if (node.type == NodeType.SOLID || node.type == NodeType.BOUNDARY) {
            node.gCost = Integer.MAX_VALUE;
            node.hCost = Integer.MAX_VALUE;
            node.fCost = Integer.MAX_VALUE;
            return;
        }
        if (!canRobotOccupy(node)) {
            node.gCost = Integer.MAX_VALUE;
            node.hCost = Integer.MAX_VALUE;
            node.fCost = Integer.MAX_VALUE;
            return;
        }
    }

    public void autoSearch() {
        if (startNode == null || goalNode == null) {
            JOptionPane.showMessageDialog(null, "Please set both a start and goal node before running the search.");
            return;
        }

        // Validate the start and goal nodes
        if (!canRobotOccupy(startNode)) {
            JOptionPane.showMessageDialog(null, "Start node is invalid for the robot size.");
            return;
        }

        if (!canRobotOccupy(goalNode)) {
            JOptionPane.showMessageDialog(null, "Goal node is invalid for the robot size.");
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
                    Node neighborNode = getNodeAt(neighborCol, neighborRow);
                    if (neighborNode != null && neighborNode.type != NodeType.SOLID && neighborNode.type != NodeType.BOUNDARY) {
                        openNode(neighborNode);
                    }
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
        // Calculate the half-width and half-height of the robot in terms of nodes
        int halfWidth = (int) Math.ceil((float) robotInchesX / (2 * nodeInches));
        int halfHeight = (int) Math.ceil((float) robotInchesY / (2 * nodeInches));

        // Determine the bounds based on the center node
        int startCol = centerNode.col - halfWidth + 1; // Adjust bounds to center-based calculation
        int endCol = centerNode.col + halfWidth - 1;
        int startRow = centerNode.row - halfHeight + 1;
        int endRow = centerNode.row + halfHeight - 1;

        // Ensure the boundaries are within grid limits
        if (startCol < 0 || endCol >= maxCol || startRow < 0 || endRow >= maxRow) {
            return false; // Robot is out of bounds
        }

        // Check all nodes within the robot's area
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                Node node = getNodeAt(col, row);
                if (node.type == NodeType.SOLID || node.type == NodeType.BOUNDARY) {
                    return false; // Robot overlaps with an obstacle or boundary
                }
            }
        }

        return true; // Robot can occupy this space
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

    private void clearPath() {
        for (Node node : nodes) {
            if (node.path) {
                node.path = false; // Reset path flag
                node.setAsOpen(); // Visually reset the node
            }
        }
    }

    private void handleMouse(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) { // Left mouse click
            Node clickedNode = (Node) e.getSource();

            // Clear previous path before making any modification
            clearPath();

            // Check if filling a solid area
            if (mouseState == NodeType.OPEN && selectedNodes.size() < 4) {
                handleFillSolid(clickedNode);
                return;
            }

            // Prevent modification of boundary nodes
            if (clickedNode.type == NodeType.BOUNDARY) {
                JOptionPane.showMessageDialog(null, "Cannot modify boundary nodes.");
                return;
            }

            // Handle different mouse states
            switch (mouseState) {
                case START:
                    if (startNode != null && startNode != clickedNode) startNode.setAsOpen(); // Reset previous start node
                    clickedNode.setAsStart();
                    startNode = clickedNode;
                    currentNode = clickedNode; // Initialize current node
                    break;

                case GOAL:
                    if (goalNode != null && goalNode != clickedNode) goalNode.setAsOpen(); // Reset previous goal node
                    clickedNode.setAsGoal();
                    goalNode = clickedNode;
                    break;

                case SOLID:
                    clickedNode.setAsSolid();
                    break;

                default:
                    clickedNode.setAsOpen(); // Default case for other types
            }
        }

        // Repaint the grid after handling the mouse event
        repaint();
    }

    private void setMouseState(NodeType state) {
        mouseState = state;
    }
}
