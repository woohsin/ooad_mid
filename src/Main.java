import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // 使用 SwingUtilities 確保 UI 在 Event Dispatch Thread 中執行
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}