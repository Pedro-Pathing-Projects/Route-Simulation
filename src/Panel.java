import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Panel extends JPanel {
    final int maxCol = 36;
    final int maxRow = 36;
    final int nodeSize = 25;
    final int screenWidth = nodeSize * maxCol;
    final int screenHeight = nodeSize * maxRow;

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
                nodes.add(node);  // Store in flat list
                this.add(node);   // Add to GUI
            }
        }

        setStartNode(2, 4);
        setGoalNode(maxCol - 2, maxRow - 2);

        setSolidNode(2, 9);
        setSolidNode(2, 10);
        setSolidNode(3, 8);
        setSolidNode(3, 9);
        setSolidNode(4, 7);
        setSolidNode(4, 8);
        setSolidNode(5, 6);
        setSolidNode(5, 7);
        setSolidNode(6, 5);
        setSolidNode(6, 6);
        setSolidNode(7, 5);
        setSolidNode(7, 6);
        setSolidNode(8, 4);
        setSolidNode(8, 5);
        setSolidNode(9, 3);
        setSolidNode(9, 4);
        setSolidNode(10, 2);
        setSolidNode(10, 3);
        setSolidNode(11, 1);
        setSolidNode(11, 2);
        setSolidNode(12, 0);
        setSolidNode(12, 1);

        setCostOnNodes();
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
            node.gCost = (int)(1.4 * (xDistance + yDistance));
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
        JOptionPane.showMessageDialog(null, "Search completed in " + duration + " milliseconds.");
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
        if (node == null || node.checked || node.solid) {
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
}
