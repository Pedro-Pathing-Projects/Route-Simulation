import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;

public class Panel extends JPanel {
    final int maxCol = 36;
    final int maxRow = 36;
    final int nodeSize = 25;
    final int screenWidth = (nodeSize * maxCol) + 333;
    final int screenHeight = (nodeSize * maxRow);

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
                        handleMouse(e);  // Call handleLeftClick when the node is clicked
                    }
                });
                nodes.add(node);  // Store in flat list
                this.add(node);   // Add to GUI

            }
        }

        // Create the run button
        JButton runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                autoSearch();
            }
        });

// Create the reset button

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

        JButton importButton = new JButton("Import");
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    importNodes();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });


// Add the buttons to the buttonPanel

// Set the layout of the Panel to BorderLayout
        this.setLayout(new BorderLayout());

// Add the grid to the center of the Panel
        try {
            backgroundImage = new ImageIcon("intothedeepfield.png").getImage();
        } catch (Exception e) {
            System.err.println("Background image not found.");
            System.out.println("Using default background.");
        }

        JPanel gridPanel = new JPanel(new GridLayout(maxRow, maxCol)){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
                repaint();
            }
        };

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

        buttonPanel.add(startButton);
        buttonPanel.add(goalButton);
        buttonPanel.add(openButton);
        buttonPanel.add(solidButton);
        //  buttonPanel.add(exportButton);
        // buttonPanel.add(importButton);
        System.out.println(buttonPanel.getPreferredSize());
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

    private void importNodes() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("nodes.txt"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] nodeProperties = line.split(",");
            int col = Integer.parseInt(nodeProperties[0]);
            int row = Integer.parseInt(nodeProperties[1]);
            NodeType type = NodeType.valueOf(nodeProperties[2]);
            int gCost = Integer.parseInt(nodeProperties[3]);
            int hCost = Integer.parseInt(nodeProperties[4]);
            int fCost = Integer.parseInt(nodeProperties[5]);
            Node parent = null;
            boolean checked = Boolean.parseBoolean(nodeProperties[6]);
            Node node = new Node(col, row, type, gCost, hCost, fCost, parent, checked);
            nodes.add(node);
        }
        br.close();
        System.out.println(nodes);
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
        setCostOnNodes();
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
            // Diagonal checks with solid adjacency logic (updated for proper diagonal movement)
            if (col - 1 >= 0 && row - 1 >= 0) {
                // Top-left diagonal
                if (getNodeAt(col - 1, row).type != NodeType.SOLID && getNodeAt(col, row - 1).type != NodeType.SOLID) {
                    openNode(getNodeAt(col - 1, row - 1)); // Use cost of 1.4 for diagonal movement
                }
            }
            if (col - 1 >= 0 && row + 1 < maxRow) {
                // Bottom-left diagonal
                if (getNodeAt(col - 1, row).type != NodeType.SOLID && getNodeAt(col, row + 1).type != NodeType.SOLID) {
                    openNode(getNodeAt(col - 1, row + 1)); // Use cost of 1.4 for diagonal movement
                }
            }
            if (col + 1 < maxCol && row - 1 >= 0) {
                // Top-right diagonal
                if (getNodeAt(col + 1, row).type != NodeType.SOLID && getNodeAt(col, row - 1).type != NodeType.SOLID) {
                    openNode(getNodeAt(col + 1, row - 1)); // Use cost of 1.4 for diagonal movement
                }
            }
            if (col + 1 < maxCol && row + 1 < maxRow) {
                // Bottom-right diagonal
                if (getNodeAt(col + 1, row).type != NodeType.SOLID && getNodeAt(col, row + 1).type != NodeType.SOLID) {
                    openNode(getNodeAt(col + 1, row + 1)); // Use cost of 1.4 for diagonal movement
                }
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

        goalNode.setAsGoal();
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
                //startNode.setAsOpen();
                clickedNode.setAsStart();
                startNode = clickedNode;
                currentNode = clickedNode;
                break;

            case GOAL:
                //goalNode.setAsOpen();
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


    private void handleRightClick(MouseEvent e) {
        // Get the clicked node
        Node clickedNode = (Node) e.getSource();

        // Update the node based on the mouse state
        clickedNode.setAsOpen();

        // Repaint the panel to update the visual representation
        repaint();
    }


    private void handleMouse(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1_DOWN_MASK) {
            handleLeftClick(e);
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            handleLeftClick(e);
        } else if (e.getButton() == MouseEvent.BUTTON2) {
            handleRightClick(e);
        }
    }
}