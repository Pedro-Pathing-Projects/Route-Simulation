import javax.swing.*;
import java.awt.*;

public class Node extends JButton {
    Node parent;
    int col, row;
    int gCost, hCost, fCost;
    boolean checked, path;
    NodeType type = NodeType.OPEN;

    public Node(int col, int row) {
        this.col = col;
        this.row = row;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusable(false);
    }

    public Node(int col, int row, NodeType type, int gCost, int hCost, int fCost, Node parent, boolean checked) {
        this(col, row);
        this.gCost = gCost;
        this.hCost = hCost;
        this.fCost = fCost;
        this.parent = parent;
        this.checked = checked;
        this.type = type;
    }

    public void reset() {
        parent = null;
        gCost = 0;
        hCost = 0;
        fCost = 0;
        type = NodeType.OPEN;
        checked = false;
        repaint();
    }

    public void setAsStart() {
        type = NodeType.START;
        repaint();
    }

    public void setAsGoal() {
        type = NodeType.GOAL;
        repaint();
    }

    public void setAsSolid() {
        type = NodeType.SOLID;
        repaint();
    }

    public void setAsOpen() {
        type = NodeType.OPEN;
        repaint();
    }

    public void setAsChecked() {
        checked = true;
        repaint();
    }

    public void setAsPath() {
        path = true;
        repaint();
    }

    public boolean isMouseOver() {
        return getBounds().contains(MouseInfo.getPointerInfo().getLocation());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        super.setContentAreaFilled(false);
        super.setBorderPainted(false);

        // Enable anti-aliasing for smoother circles
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Determine color based on the node type
        Color fillColor;
        
        fillColor = new Color(213, 213, 213);

        if(type == NodeType.SOLID){
            fillColor = new Color(77, 77, 77);
        }

        if(checked) {
        //    fillColor = new Color(255, 203, 132);
        }

        if (path) {
            fillColor = new Color(96, 170, 199);
        }

        if(type == NodeType.START) {
            fillColor = new Color(132, 255, 132);
        }

        if (type == NodeType.GOAL) {
            fillColor = new Color(240, 78, 78);
        }



        // Get the circle's dimensions
        int diameter = Math.min(getWidth(), getHeight()); // Leave padding
        int x = (getWidth() - diameter) / 2;
        int y = (getHeight() - diameter) / 2;

        //Draw the circular center
        g2.setColor(fillColor);
        g2.fillOval(x, y, diameter, diameter);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(25, 25); // Default size for nodes
    }
}
