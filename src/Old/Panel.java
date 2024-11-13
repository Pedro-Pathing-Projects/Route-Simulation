import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class Panel extends JPanel {
    final int maxCol = 36;
    final int maxRow = 36;
    final int nodeSize = 25;
    final int screenWidth = nodeSize * maxCol;
    final int screenHeight = nodeSize * maxRow;

    int step = 0;

    Node[][] node = new Node[maxCol][maxRow];
    Node startNode, goalNode, currentNode;
    ArrayList<Node> openList = new ArrayList<>(), checkedList = new ArrayList<>();
    boolean goalReached = false;

    public Panel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setLayout(new GridLayout(maxRow, maxCol));
        this.addKeyListener(new KeyHandler(this));
        this.setFocusable(true);

        int col = 0;
        int row = 0;

        while(col < maxCol && row < maxRow) {
            node[col][row] = new Node(col, row);
            this.add(node[col][row]);
            col++;
            if(col == maxCol) {
                row++;
                col = 0;
            }
        }

        setStartNode(1,1);
        setGoalNode(maxCol-2, maxRow-2);

        setSolidNode(1, 10);
        setSolidNode(2, 9);
        setSolidNode(3, 8);
        setSolidNode(4, 7);
        setSolidNode(5, 6);
        setSolidNode(6, 5);


        setCostOnNodes();
    }

    private void setStartNode(int col, int row) {
        node[col][row].setAsStart();
        startNode = node[col][row];
        currentNode = startNode;
    }

    private void setGoalNode(int col, int row) {
        node[col][row].setAsGoal();
        goalNode = node[col][row];
    }

    public void setSolidNode(int col, int row) {
        node[col][row].setAsSolid();
    }

    private void setCostOnNodes() {
        int col = 0;
        int row = 0;

        while (col < maxCol && row < maxRow) {
            getCost(node[col][row]);
            col++;
            if (col == maxCol) {
                col = 0;
                row++;
            }
        }
    }

    private void getCost(Node node) {
        // G cost
        int xDistance = Math.abs(node.col - currentNode.col);
        int yDistance = Math.abs(node.row - currentNode.row);
        node.gCost = xDistance + yDistance;

        // H cost
        xDistance = Math.abs(node.col - goalNode.col);
        yDistance = Math.abs(node.row - goalNode.row);
        node.hCost = xDistance + yDistance;

        // F cost
        node.fCost = node.gCost + node.hCost;
    }

    public void search() {
        if(!goalReached) {
            int col = currentNode.col;
            int row = currentNode.row;

            currentNode.setAsChecked();
            checkedList.add(currentNode);
            openList.remove(currentNode);

            if(row-1 >= 0) {
                openNode(node[col][row - 1]);
            }
            if (row + 1 < maxRow) {
                openNode(node[col][row + 1]);
            }
            if (col - 1 >= 0) {
                openNode(node[col - 1][row]);
            }
            if (col + 1 < maxCol) {
                openNode(node[col + 1][row]);
            }

            int bestNodeIndex = 0;
            int bestNodeFCost = 999;

            //check for best f cost
            for (int i = 0; i < openList.size(); i++) {
                if (openList.get(i).fCost < bestNodeFCost) {
                    bestNodeIndex = i;
                    bestNodeFCost = openList.get(i).fCost;
                }

                //if f cost is equal check g cost
                else if (openList.get(i).fCost == bestNodeFCost) {
                     if (openList.get(i).gCost < openList.get(bestNodeIndex).gCost) {
                         bestNodeIndex = i;
                     }
                }
            }

            currentNode = openList.get(bestNodeIndex);

            if(currentNode == goalNode) {
                goalReached = true;
                trackPath();
            }
        }
    }

    public void autoSearch() {
        while(!goalReached && step < 300) {
            int col = currentNode.col;
            int row = currentNode.row;

            currentNode.setAsChecked();
            checkedList.add(currentNode);
            openList.remove(currentNode);

            if(row-1 >= 0) {
                openNode(node[col][row - 1]);
            }
            if (row + 1 < maxRow) {
                openNode(node[col][row + 1]);
            }
            if (col - 1 >= 0) {
                openNode(node[col - 1][row]);
            }
            if (col + 1 < maxCol) {
                openNode(node[col + 1][row]);
            }

            int bestNodeIndex = 0;
            int bestNodeFCost = 999;

            //check for best f cost
            for (int i = 0; i < openList.size(); i++) {
                if (openList.get(i).fCost < bestNodeFCost) {
                    bestNodeIndex = i;
                    bestNodeFCost = openList.get(i).fCost;
                }

                //if f cost is equal check g cost
                else if (openList.get(i).fCost == bestNodeFCost) {
                    if (openList.get(i).gCost < openList.get(bestNodeIndex).gCost) {
                        bestNodeIndex = i;
                    }
                }
            }

            currentNode = openList.get(bestNodeIndex);

            if(currentNode == goalNode) {
                goalReached = true;
                trackPath();
            }

        }

        step++;
    }

    public void openNode(Node node) {

        if(!node.open && !node.checked && !node.solid) {
            node.setAsOpen();
            node.parent = currentNode;
            openList.add(node);
        }
    }

    private void trackPath() {
        Node current = goalNode;

        while (current != startNode) {
            current = current.parent;

            if(current != startNode) {
                current.setAsPath();
            }
        }
    }
}
