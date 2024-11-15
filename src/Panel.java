import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Panel extends JPanel {
    final int maxCol = 36;
    final int maxRow = 36;
    final int nodeSize = 25;
    final int screenWidth = (nodeSize * maxCol) + 409;
    final int screenHeight = (nodeSize * maxRow);

    NodeType mouseState = NodeType.OPEN;

    int step = 0;

    ArrayList<Node> nodes = new ArrayList<>(maxCol * maxRow);
    Node startNode, goalNode, currentNode;
    ArrayList<Node> openList = new ArrayList<>(), checkedList = new ArrayList<>();
    boolean goalReached = false;

    public Panel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setLayout(new GridLayout(maxRow, maxCol));
        this.addKeyListener(new KeyHandler(this));
        this.setFocusable(true);

        // Initialize the nodes and add to the flat list
        for (int row = 0; row < maxRow; row++) {
            for (int col = 0; col < maxCol; col++) {
                Node node = new Node(col, row);
                node.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        handleLeftClick(e);  // Call handleLeftClick when the node is clicked
                    }
                });
                nodes.add(node);  // Store in flat list
                this.add(node);   // Add to GUI

            }
        }

        setStartNode(1, 1);
        setGoalNode(maxCol - 2, maxRow - 2);
        setCostOnNodes();

        // Create the run button
        JButton runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                autoSearch();
            }
        });

// Create the reset button
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    exportNodes();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Create the buttons
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Set the mouse state to start
                setMouseState(NodeType.START);
                printMouseState();
            }
        });

        JButton solidButton = new JButton("Solid");
        solidButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Set the mouse state to solid
                setMouseState(NodeType.SOLID);
                printMouseState();
            }
        });


        JButton openButton = new JButton("Open");
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Set the mouse state to open
                setMouseState(NodeType.OPEN);
                printMouseState();
            }
        });

        JButton goalButton = new JButton("Goal");
        goalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Set the mouse state to goal
                setMouseState(NodeType.GOAL);
                printMouseState();
            }
        });

// Add the buttons to the buttonPanel

// Set the layout of the Panel to BorderLayout
        this.setLayout(new BorderLayout());

// Add the grid to the center of the Panel
        JPanel gridPanel = new JPanel(new GridLayout(maxRow, maxCol));
        for (int row = 0; row < maxRow; row++) {
            for (int col = 0; col < maxCol; col++) {
                Node node = getNodeAt(col, row);
                gridPanel.add(node);
            }
        }
        this.add(gridPanel, BorderLayout.CENTER);

// Add the buttons to the east of the Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(runButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(startButton);
        buttonPanel.add(goalButton);
        buttonPanel.add(openButton);
        buttonPanel.add(solidButton);
        this.add(buttonPanel, BorderLayout.EAST);
    }

    public void printMouseState() {
        System.out.println("Mouse State: " + mouseState);
    }

    private void exportNodes() throws IOException {
        BufferedWriter br = new BufferedWriter(new FileWriter("nodes.txt"));
        br.write(nodes.toString());
        br.flush();
        br.close();
    }

    // Helper method to get the node at a given column and row
    private Node getNodeAt(int col, int row) {
        return nodes.get(row * maxCol + col);  // Map 2D to 1D
    }

    private void setStartNode(int col, int row) {
        Node node = getNodeAt(col, row);
        node.setAsStart();
        startNode = node;
        currentNode = startNode;
    }

    private void setGoalNode(int col, int row) {
        Node node = getNodeAt(col, row);
        node.setAsGoal();
        goalNode = node;
    }

    public void setSolidNode(int col, int row) {
        getNodeAt(col, row).setAsSolid();
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
        // G cost
        int xDistance = Math.abs(node.col - startNode.col);
        int yDistance = Math.abs(node.row - startNode.row);
        if (xDistance == 1 && yDistance == 1) {
            // Diagonal move
            node.gCost = (int) (1.4 * (xDistance + yDistance));
        } else {
            // Horizontal/Vertical move
            node.gCost = xDistance + yDistance;
        }

        // H cost
        xDistance = Math.abs(node.col - goalNode.col);
        yDistance = Math.abs(node.row - goalNode.row);
        node.hCost = xDistance + yDistance;

        // F cost
        node.fCost = node.gCost + node.hCost;
    }


    public void autoSearch() {
        long startTime = System.currentTimeMillis();  // Start timing

        while (!goalReached && step < 3000) {
            int col = currentNode.col;
            int row = currentNode.row;

            currentNode.setAsChecked();
            checkedList.add(currentNode);
            openList.remove(currentNode);

            // Check adjacent nodes (adding diagonal checks)
            if (row - 1 >= 0) {
                openNode(getNodeAt(col, row - 1));
            }
            if (row + 1 < maxRow) {
                openNode(getNodeAt(col, row + 1));
            }
            if (col - 1 >= 0) {
                openNode(getNodeAt(col - 1, row));
            }
            if (col + 1 < maxCol) {
                openNode(getNodeAt(col + 1, row));
            }
            // Diagonal checks
            if (col - 1 >= 0 && row - 1 >= 0) {
                openNode(getNodeAt(col - 1, row - 1));
            }
            if (col - 1 >= 0 && row + 1 < maxRow) {
                openNode(getNodeAt(col - 1, row + 1));
            }
            if (col + 1 < maxCol && row - 1 >= 0) {
                openNode(getNodeAt(col + 1, row - 1));
            }
            if (col + 1 < maxCol && row + 1 < maxRow) {
                openNode(getNodeAt(col + 1, row + 1));
            }

            // Choose the best node with lowest F cost
            int bestNodeIndex = 0;
            int bestNodeFCost = 999;

            for (int i = 0; i < openList.size(); i++) {
                if (openList.get(i).fCost < bestNodeFCost) {
                    bestNodeIndex = i;
                    bestNodeFCost = openList.get(i).fCost;
                } else if (openList.get(i).fCost == bestNodeFCost) {
                    if (openList.get(i).gCost < openList.get(bestNodeIndex).gCost) {
                        bestNodeIndex = i;
                    }
                }
            }

            currentNode = openList.get(bestNodeIndex);

            if (currentNode == goalNode) {
                goalReached = true;
                trackPath();
                break;  // Exit the loop once the goal is reached
            }

            step++;
        }

        long endTime = System.currentTimeMillis();  // End timing
        long duration = endTime - startTime;  // Calculate duration

        // Display duration in a popup
        //JOptionPane.showMessageDialog(null, "Search completed in " + duration + " milliseconds.");
    }

    private Node getBestNode() {
        Node bestNode = null;
        int bestFCost = Integer.MAX_VALUE;
        int bestGCost = Integer.MAX_VALUE;

        // Iterate over openList to find node with lowest fCost, and if tied, lowest gCost
        for (Node node : openList) {
            if (node.fCost < bestFCost || (node.fCost == bestFCost && node.gCost < bestGCost)) {
                bestFCost = node.fCost;
                bestGCost = node.gCost;
                bestNode = node;
            }
        }
        return bestNode;
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
        return (xDistance + yDistance) * 10; // H cost with straight movement costs
    }


    private void trackPath() {
        Node current = goalNode;

        while (current != startNode) {
            current = current.parent;
            if (current != startNode) {
                current.setAsPath();
            }
        }
    }

    private void setMouseState(NodeType state) {
        // Set the mouse state
        mouseState = state;

        // Update the nodes based on the mouse state
        for (Node node : nodes) {
            if (node.isMouseOver()) {
                // Update the node based on the mouse state
                switch (state) {
                    case START:
                        node.setAsStart();
                        break;
                    case SOLID:
                        node.setAsSolid();
                        break;
                    default:
                        node.setAsOpen();
                        break;
                }
            }
        }
    }

    private void handleLeftClick(MouseEvent e) {
        // Get the clicked node
        Node clickedNode = (Node) e.getSource();

        // Update the node based on the mouse state
        switch (mouseState) {
            case START:
                // Ensure only one start node
                if (startNode != null) {
                    startNode.setAsOpen();  // Reset the previous start node
                }
                clickedNode.setAsStart();
                startNode = clickedNode;
                break;

            case GOAL:
                // Ensure only one goal node
                if (goalNode != null) {
                    goalNode.setAsOpen();  // Reset the previous goal node
                }
                clickedNode.setAsGoal();
                goalNode = clickedNode;
                break;

            case SOLID:
                clickedNode.setAsSolid();
                break;

            default:
                clickedNode.setAsOpen();
                break;
        }

        // Repaint the panel to update the visual representation
        repaint();
    }
}
