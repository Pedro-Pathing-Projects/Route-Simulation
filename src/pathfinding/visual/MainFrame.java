package pathfinding.visual;

import javax.swing.*;

public class MainFrame extends JFrame {
    private String iconLocation = "logoITD.png";

    public MainFrame(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(new ImageIcon(getClass().getResource(iconLocation)).getImage());
        setName("Pedro Pathing Route Simulator - Beta");
        setLayout(null);
    }
    
}
