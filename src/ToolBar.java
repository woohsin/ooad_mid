import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ToolBar extends JToolBar {
    private List<JButton> buttons = new ArrayList<>();
    private JButton currentSelectedButton = null;
    private String currentMode = "Select";

    public ToolBar() {
        setLayout(new GridLayout(6, 1, 5, 5));
        setFloatable(false);

        String[] names = {"Select", "Association", "Generalization", "Composition", "Rect", "Oval"};
        for (String name : names) {
            JButton btn = new JButton(name);
            buttons.add(btn);
            btn.setBackground(Color.WHITE);
            btn.addActionListener(e -> {
                handleModeSwitch(name, btn);
            });
            add(btn);
        }
        
        // 初始化 Select 模式為選中狀態
        if (!buttons.isEmpty()) {
            JButton selectBtn = buttons.get(0);
            selectBtn.setBackground(Color.BLACK);
            selectBtn.setForeground(Color.WHITE);
            currentSelectedButton = selectBtn;
        }
        
        Canvas.getInstance().setToolBar(this);
    }

    private void resetButtons() {
        for (JButton b : buttons) {
            b.setBackground(Color.WHITE);
            b.setForeground(Color.BLACK);
        }
    }

    private void handleModeSwitch(String name, JButton btn) {
        resetButtons();
        btn.setBackground(Color.BLACK);
        btn.setForeground(Color.WHITE);
        currentSelectedButton = btn;
        currentMode = name;
        
        if (name.equalsIgnoreCase("Select")) {
            Canvas.getInstance().setMode(new SelectMode());
        } else if (name.equalsIgnoreCase("Rect")) {
            Canvas.getInstance().setMode(new CreateObjectMode("Rect"));
        } else if (name.equalsIgnoreCase("Oval")) {
            Canvas.getInstance().setMode(new CreateObjectMode("Oval"));
        } else if (name.equalsIgnoreCase("Association")) {
            Canvas.getInstance().setMode(new ConnectionMode("Association"));
        } else if (name.equalsIgnoreCase("Generalization")) {
            Canvas.getInstance().setMode(new ConnectionMode("Generalization"));
        } else if (name.equalsIgnoreCase("Composition")) {
            Canvas.getInstance().setMode(new ConnectionMode("Composition"));
        }
    }
    
    public void restoreButtonState(String previousMode) {
        resetButtons();
        
        // 找到對應的按鈕並設為選中狀態
        for (int i = 0; i < buttons.size(); i++) {
            String[] names = {"Select", "Association", "Generalization", "Composition", "Rect", "Oval"};
            if (names[i].equalsIgnoreCase(previousMode)) {
                JButton btn = buttons.get(i);
                btn.setBackground(Color.BLACK);
                btn.setForeground(Color.WHITE);
                currentSelectedButton = btn;
                currentMode = previousMode;
                break;
            }
        }
    }
}