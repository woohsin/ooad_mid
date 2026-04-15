import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ToolBar extends JToolBar {
    private List<JButton> buttons = new ArrayList<>();
    private Map<String, Mode> modeMap = new HashMap<>();
    private JButton currentSelectedButton = null;
    private String currentMode = "Select";

    public ToolBar() {
        setLayout(new GridLayout(0, 1, 5, 5)); //n 列 1 欄（按鈕會垂直排列）元件之間的水平與垂直間距為 5 像素
        setFloatable(false);//固定在視窗左側

        initModeMap();

        for (String name : modeMap.keySet()) {
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

    //任何增加的bottom改這裡就好
    private void initModeMap() {
        // 使用 LinkedHashMap 可以確保按鈕順序按照你 put 的順序排列
        modeMap = new LinkedHashMap<>(); 
        modeMap.put("Select", new SelectMode());
        modeMap.put("Association", new ConnectionMode("Association"));
        modeMap.put("Generalization", new ConnectionMode("Generalization"));
        modeMap.put("Composition", new ConnectionMode("Composition"));
        modeMap.put("Rect", new CreateObjectMode(new RectObject(0,0,0,0)));
        modeMap.put("Oval", new CreateObjectMode(new OvalObject(0,0,0,0)));
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
        
        Mode nextMode = modeMap.get(name);
        if (nextMode != null) {
            Canvas.getInstance().setMode(nextMode);
        }
    }
    
    public void restoreButtonState(String previousMode) {
        resetButtons();
        
        for (JButton btn : buttons) {
            // 直接利用按鈕自帶的文字進行比對
            if (btn.getText().equalsIgnoreCase(previousMode)) {
                btn.setBackground(Color.BLACK);
                btn.setForeground(Color.WHITE);
                this.currentSelectedButton = btn;
                this.currentMode = previousMode;
                break; 
            }
        }
    }
}