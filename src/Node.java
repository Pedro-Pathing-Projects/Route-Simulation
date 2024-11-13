import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Node extends JButton implements ActionListener {

    Node parent;
    int col;
    int row;
    int gCost;
    int hCost;
    int fCost;
    boolean start;
    boolean goal;
    boolean solid;
    boolean open;
    boolean checked;

    public Node(int col, int row) {
        this.col = col;
        this.row = row;

        setBackground(Color.white);
        setForeground(Color.black);
        addActionListener(this);
    }

    public void setAsStart() {
        start = true;
        setBackground(Color.green);
    }

    public void setAsGoal() {
        goal = true;
        setBackground(Color.red);
    }

    public void setAsSolid() {
        solid = true;
        setBackground(Color.black);
    }

    public void setAsOpen() {
        open = true;
    }

    public void setAsChecked() {
        if(!start && !goal) {
            setBackground(Color.orange);
        }
        checked = true;
    }

    public void setAsPath() {
        setBackground(Color.blue);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setBackground(Color.yellow);
    }
}
