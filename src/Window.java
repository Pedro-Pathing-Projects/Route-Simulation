import javax.swing.JFrame;

public class Window {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(new Panel());

        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}
