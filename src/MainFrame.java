import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Oops UML Editor");
        setSize(1000, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        Canvas canvas = Canvas.getInstance();
        ToolBar toolBar = new ToolBar();
        
        getContentPane().add(toolBar, BorderLayout.WEST);
        getContentPane().add(canvas, BorderLayout.CENTER);
        
        setJMenuBar(createMenuBar());
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu editMenu = new JMenu("Edit");
        
        JMenuItem group = new JMenuItem("Group"); // Use Case D 
        group.addActionListener(e -> Canvas.getInstance().groupSelectedShapes());
        
        JMenuItem labelItem = new JMenuItem("Label");
        labelItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Shape selected = Canvas.getInstance().getSelectedShape();
                if (selected == null) {
                    JOptionPane.showMessageDialog(MainFrame.this, "請先選取一個物件。", "錯誤", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                new LabelDialog(MainFrame.this, selected).setVisible(true);
            }
        });
        
        JMenuItem ungroup = new JMenuItem("Ungroup");
        ungroup.addActionListener(e -> Canvas.getInstance().ungroupSelectedShape());
        
        editMenu.add(group);
        editMenu.add(ungroup);
        editMenu.add(labelItem);
        menuBar.add(new JMenu("File"));
        menuBar.add(editMenu);
        return menuBar;
    }

    public static void main(String[] args) {
        new MainFrame().setVisible(true);
    }
}

class LabelDialog extends JDialog {
    private JTextField nameField;
    private JButton colorButton;
    private Color selectedColor;
    private Shape shape;
    
    public LabelDialog(Frame parent, Shape shape) {
        super(parent, "Customize Label Style", true);
        this.shape = shape;
        this.selectedColor = shape.getLabelColor();
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Label Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Label Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        nameField = new JTextField(shape.getLabelName(), 20);
        add(nameField, gbc);
        
        // Label Color
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("Label Color:"), gbc);
        gbc.gridx = 1;
        colorButton = new JButton("Choose Color");
        colorButton.setBackground(selectedColor);
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Label Color", selectedColor);
            if (newColor != null) {
                selectedColor = newColor;
                colorButton.setBackground(selectedColor);
            }
        });
        add(colorButton, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        okButton.addActionListener(e -> {
            shape.setLabelName(nameField.getText());
            shape.setLabelColor(selectedColor);
            Canvas.getInstance().repaint();
            dispose();
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);
        
        pack();
        setLocationRelativeTo(parent);
    }
}