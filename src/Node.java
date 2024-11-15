import javax.swing.*;
import java.awt.*;

public class Node extends JButton {

    Node parent;
    int col, row;
    int gCost, hCost, fCost;
    boolean checked;
    NodeType type = NodeType.OPEN;

    public Node(int col, int row) {
        this.col = col;
        this.row = row;
        setBackground(Color.white);
        setForeground(Color.black);
        setFocusable(false);
    }


    public Node(int col, int row, NodeType type, int gCost, int hCost, int fCost, Node parent, boolean checked) {
        this.col = col;
        this.row = row;
        this.gCost = gCost;
        this.hCost = hCost;
        this.fCost = fCost;
        this.parent = parent;
        this.checked = checked;
        setBackground(Color.white);
        setForeground(Color.black);
        setFocusable(false);
        this.type = type;
    }

    public void reset() {
        parent = null;
        gCost = 0;
        hCost = 0;
        fCost = 0;
        type = NodeType.OPEN;
        checked = false;
    }

    public void setAsStart() {
        setBackground(Color.green);
        type = NodeType.START;
        repaint();
    }

    public void setAsGoal() {
        setBackground(Color.red);
        type = NodeType.GOAL;
        repaint();
    }

    public void setAsSolid() {
        setBackground(Color.black);
        type = NodeType.SOLID;
        repaint();
    }

    public void setAsOpen() {
        setBackground(Color.white);
        type = NodeType.OPEN;
        repaint();
    }

    public void setAsChecked() {
        if (!NodeType.START.equals(type) && !NodeType.GOAL.equals(type)) {
            setBackground(Color.orange);
        }
        checked = true;
        repaint();
    }

    public void setAsPath() {
        setBackground(Color.blue);
        repaint();
    }

    public boolean isMouseOver() {
        return getBounds().contains(MouseInfo.getPointerInfo().getLocation());
    }

    public Point getLocation() {
        return MouseInfo.getPointerInfo().getLocation();
    }
}
