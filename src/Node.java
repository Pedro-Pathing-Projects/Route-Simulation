import javax.swing.*;
import java.awt.*;

public class Node extends JButton {

    Node parent;
    int col, row;
    int gCost, hCost, fCost;
    boolean start, goal, solid, open, checked;

    public Node(int col, int row) {
        this.col = col;
        this.row = row;
        setBackground(Color.white);
        setForeground(Color.black);
        setFocusable(false);
    }

    public void setAsStart() {
        setBackground(Color.green);
        start = true;
    }

    public void setAsGoal() {
        setBackground(Color.red);
        goal = true;
    }

    public void setAsSolid() {
        setBackground(Color.black);
        solid = true;
    }

    public void setAsOpen() {
        setBackground(Color.white);
        open = true;
    }

    public void setAsChecked() {
        if (!start && !goal) {
            setBackground(Color.orange);
        }
        checked = true;
    }

    public void setAsPath() {
        setBackground(Color.blue);
    }
}
