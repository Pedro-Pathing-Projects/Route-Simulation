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
        setOpaque(false); // Make the background transparent
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
        if (!NodeType.START.equals(type) && !NodeType.GOAL.equals(type)) {
            setBackground(Color.orange);
        }
        checked = true;
        repaint();
    }

    public void setAsPath() {
        path = true;
        setBackground(Color.blue);
        repaint();
    }

    public boolean isMouseOver() {
        return getBounds().contains(MouseInfo.getPointerInfo().getLocation());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Enable anti-aliasing for smoother circles
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Determine color based on the node type
        Color fillColor;
        
        fillColor = Color.WHITE;

        if(type == NodeType.SOLID){
            fillColor = Color.BLACK;
        }

        if(checked) {
            fillColor = Color.ORANGE;
        }

        if (path) {
            fillColor = Color.BLUE;
        }

        if(type == NodeType.START) {
            fillColor = Color.GREEN;
        }

        if (type == NodeType.GOAL) {
            fillColor = Color.RED;
        }


        // Get the circle's dimensions
        int diameter = Math.min(getWidth() - 5, getHeight()) - 5; // Leave padding
        int x = (getWidth() - diameter) / 2;
        int y = (getHeight() - diameter) / 2;

        // Draw the circular center
        g2.setColor(fillColor);
        g2.fillOval(x, y, diameter, diameter);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(25, 25); // Default size for nodes
    }
}
