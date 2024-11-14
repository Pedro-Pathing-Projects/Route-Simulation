package pathfinding.visual;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pathfinding.element.Grid;
import pathfinding.element.Tile;
import java.awt.Color;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class ControlsPanel extends JPanel {

    private pathfinding.algorithm algorithm;
    private SelectionType selectionType;
    private Grid currentGrid;

    private  JComboBox<String> selector;

    public ControlsPanel(int width, int height, pathfinding.algorithm algorithm) {

        this.algorithm = algorithm;
        this.selectionType = SelectionType.START;
        currentGrid = (Grid) algorithm.getNetwork();

        setBorder(new LineBorder(Color.gray));
        setLayout(null);

        Label selectionLabel = new Label("Selection type:");
        selectionLabel.setBounds(7, 10, width - 20, 25);
        add(selectionLabel);

        selector = new JComboBox<>();
        selector.addItem("Start");
        selector.addItem("End");
        selector.addItem("Obstacle");
        selector.setBounds(10, 35, width - 20, 30);
        selector.addActionListener((ActionEvent e) -> {
            selectionType = selectionType.values()[selector.getSelectedIndex()];
        });
        add(selector);

        JButton reset = new JButton("Reset");
        reset.setBounds(10, height - 40, 80, 30);
        reset.addActionListener((ActionEvent ae) -> {
            algorithm.reset();
            algorithm.updateUI();
            selectionType = SelectionType.START;
        });
        add(reset);

        JButton start = new JButton("Start");
        start.setBounds(110, height - 40, 80, 30);
        start.addActionListener((ActionEvent ae) -> {
            algorithm.solve();
        });
        add(start);

        JButton field = new JButton("Field");
        field.setBounds(60, height, 80, 30);
        field.addActionListener((ActionEvent ae) -> {
            algorithm.reset();
            algorithm.updateUI();
            //PLACEHOLDER
        });
        add(field);

        JButton save = new JButton("Save");
        save.setBounds(10, height+40, 80, 30);
        save.addActionListener((ActionEvent ae) -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showSaveDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                saveGrid(file, currentGrid);
            }
        });
        add(save);

        JButton load = new JButton("Load");
        load.setBounds(100, height+40, 80, 30);
        load.addActionListener((ActionEvent ae) -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                loadGrid(file);
            }
        });
        add(load);

    }

    public void setGrid(Grid grid){
        currentGrid = grid;
        algorithm.setNetwork(currentGrid);
        updateUI();
    }

    public void selectTile(Tile t) {

        switch (selectionType) {
            case START:
                algorithm.setStart(t);
                selectionType = SelectionType.END;
                selector.setSelectedIndex(1);
                break;
            case END:
                algorithm.setEnd(t);
                selectionType = SelectionType.REVERSE;
                selector.setSelectedIndex(2);
                break;
            default:
                t.reverseValidation();
                break;
        }

        algorithm.updateUI();
    }

    private enum SelectionType {
        START, END, REVERSE
    }

    private void saveGrid(File file, Grid grid) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(grid, Grid.class);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadGrid(File file) {
        Gson gson = new Gson();
        try {
            Grid grid = gson.fromJson(new FileReader(file), Grid.class);
            setGrid(grid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
