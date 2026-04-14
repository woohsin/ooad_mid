import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Shape {
    protected int x1, y1, x2, y2;
    protected boolean isSelected = false;
    protected static final int MIN_SIZE = 20;  // 最小尺寸限制
    protected String labelName = "";  // 標籤名稱
    protected Color labelColor = new Color(211, 211, 211);

    public abstract void draw(Graphics g);
    public abstract boolean isInside(int x, int y);
    public abstract Port getPortAt(int x, int y);  // 檢查是否在 port 範圍內
    public abstract List<Port> getAllPorts();       // 取得所有 ports
    public abstract boolean isCompletelyInside(int minX, int maxX, int minY, int maxY);  // 檢查是否完全在矩形區域內
    public abstract boolean canResize();  // 是否可以調整大小（composite 物件返回 false）
    
    public void move(int deltaX, int deltaY) {
        this.x1 += deltaX;
        this.y1 += deltaY;
        this.x2 += deltaX;
        this.y2 += deltaY;
    }
    
    // 調整大小方法（根據 port 位置計算新的邊界）
    public void resize(String portPosition, int newX, int newY) {
        if (!canResize()) return;
        
        int minSize = MIN_SIZE;
        
        // 根據 port 位置更新對應的邊界座標
        // 允許交叉反向拖曳（F.2）
        if (portPosition.contains("T")) {
            y1 = newY;
        }
        if (portPosition.contains("B")) {
            y2 = newY;
        }
        if (portPosition.contains("L")) {
            x1 = newX;
        }
        if (portPosition.contains("R")) {
            x2 = newX;
        }
        
        // 強制最小尺寸限制（F.3）
        if (Math.abs(x2 - x1) < minSize) {
            if (x2 < x1) {
                x2 = x1 - minSize;
            } else {
                x2 = x1 + minSize;
            }
        }
        
        if (Math.abs(y2 - y1) < minSize) {
            if (y2 < y1) {
                y2 = y1 - minSize;
            } else {
                y2 = y1 + minSize;
            }
        }
    }
    
    public void drawPreview(Graphics g) {
        // 預設預覽方式與正常繪製相同但可透明度較低
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        draw(g2d);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    protected void drawPorts(Graphics g) {
        int pw = 8;
        g.setColor(Color.RED);  // 用紅色顯示 ports，便於用戶識別
        // 畫出物件的所有 Port
        for (Port port : getAllPorts()) {
            g.fillRect(port.x - pw/2, port.y - pw/2, pw, pw);
        }
    }
    public void setSelected(boolean b) { this.isSelected = b; }
    
    public String getLabelName() { return labelName; }
    public void setLabelName(String name) { this.labelName = name; }
    public Color getLabelColor() { return labelColor; }
    public void setLabelColor(Color color) { this.labelColor = color; }
}

// Port 類，表示一個連接點
class Port {
    public int x, y;
    public String position;  // "TL", "TR", "BL", "BR", "T", "B", "L", "R" 等
    
    public Port(int x, int y, String position) {
        this.x = x;
        this.y = y;
        this.position = position;
    }
}

class RectObject extends Shape {
    
    public RectObject(int x, int y) {
        this.x1 = x; this.y1 = y;
        this.x2 = x + 80; this.y2 = y + 80;
    }
    
    public RectObject(int x1, int y1, int x2, int y2) {
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
    }
    
    @Override
    public Port getPortAt(int px, int py) {
        int tolerance = 8;  // port 的點擊範圍
        for (Port port : getAllPorts()) {
            if (Math.abs(port.x - px) <= tolerance && Math.abs(port.y - py) <= tolerance) {
                return port;
            }
        }
        return null;
    }
    
    @Override
    public List<Port> getAllPorts() {
        List<Port> ports = new ArrayList<>();
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        
        // 四個角
        ports.add(new Port(minX, minY, "TL"));  // Top-Left
        ports.add(new Port(maxX, minY, "TR"));  // Top-Right
        ports.add(new Port(minX, maxY, "BL"));  // Bottom-Left
        ports.add(new Port(maxX, maxY, "BR"));  // Bottom-Right
        
        // 四條邊的中點
        ports.add(new Port((minX + maxX) / 2, minY, "T"));   // Top
        ports.add(new Port((minX + maxX) / 2, maxY, "B"));   // Bottom
        ports.add(new Port(minX, (minY + maxY) / 2, "L"));   // Left
        ports.add(new Port(maxX, (minY + maxY) / 2, "R"));   // Right
        
        return ports;
    }
    
    @Override
    public boolean isCompletelyInside(int minX, int maxX, int minY, int maxY) {
        int x1 = Math.min(this.x1, this.x2);
        int x2 = Math.max(this.x1, this.x2);
        int y1 = Math.min(this.y1, this.y2);
        int y2 = Math.max(this.y1, this.y2);
        
        // 檢查矩形是否完全落在選擇區域內
        return x1 >= minX && x2 <= maxX && y1 >= minY && y2 <= maxY;
    }
    
    @Override
    public boolean canResize() {
        return true;  // 基本物件可以調整大小
    }
    
    @Override
    public void draw(Graphics g) {
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int width = Math.abs(x1 - x2);
        int height = Math.abs(y1 - y2);
        
        // 用 labelColor 填充整個矩形
        g.setColor(labelColor);
        g.fillRect(minX, minY, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(minX, minY, width, height);
        
        // 繪製名稱，置中顯示
        if (!labelName.isEmpty()) {
            g.setColor(Color.BLACK);
            FontMetrics fm = g.getFontMetrics();
            int textX = minX + (width - fm.stringWidth(labelName)) / 2;
            int textY = minY + ((height - fm.getHeight()) / 2) + fm.getAscent();
            g.drawString(labelName, textX, textY);
        }
        
        if (isSelected) drawPorts(g);
    }
    @Override
    public boolean isInside(int px, int py) {
        return px >= Math.min(x1, x2) && px <= Math.max(x1, x2) && py >= Math.min(y1, y2) && py <= Math.max(y1, y2);
    }
}

class OvalObject extends Shape {
    
    public OvalObject(int x, int y) {
        this.x1 = x; this.y1 = y;
        this.x2 = x + 80; this.y2 = y + 60;
    }
    
    public OvalObject(int x1, int y1, int x2, int y2) {
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
    }
    
    @Override
    public Port getPortAt(int px, int py) {
        int tolerance = 8;  // port 的點擊範圍
        for (Port port : getAllPorts()) {
            if (Math.abs(port.x - px) <= tolerance && Math.abs(port.y - py) <= tolerance) {
                return port;
            }
        }
        return null;
    }
    
    @Override
    public List<Port> getAllPorts() {
        List<Port> ports = new ArrayList<>();
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        int centerX = (minX + maxX) / 2;
        int centerY = (minY + maxY) / 2;
        
        // 四個主要方向的 ports
        ports.add(new Port(centerX, minY, "T"));       // Top
        ports.add(new Port(centerX, maxY, "B"));       // Bottom
        ports.add(new Port(minX, centerY, "L"));       // Left
        ports.add(new Port(maxX, centerY, "R"));       // Right
        
        return ports;
    }
    
    @Override
    public boolean isCompletelyInside(int minX, int maxX, int minY, int maxY) {
        int x1 = Math.min(this.x1, this.x2);
        int x2 = Math.max(this.x1, this.x2);
        int y1 = Math.min(this.y1, this.y2);
        int y2 = Math.max(this.y1, this.y2);
        
        // 檢查椭圆的邊界框是否完全落在選擇區域內
        return x1 >= minX && x2 <= maxX && y1 >= minY && y2 <= maxY;
    }
    
    @Override
    public boolean canResize() {
        return true;  // 基本物件可以調整大小
    }
    
    @Override
    public void draw(Graphics g) {
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int width = Math.abs(x1 - x2);
        int height = Math.abs(y1 - y2);
        
        // 用 labelColor 填充整個橢圓
        g.setColor(labelColor);
        g.fillOval(minX, minY, width, height);
        g.setColor(Color.BLACK);
        g.drawOval(minX, minY, width, height);
        
        // 繪製名稱，置中顯示
        if (!labelName.isEmpty()) {
            g.setColor(Color.BLACK);
            FontMetrics fm = g.getFontMetrics();
            int textX = minX + (width - fm.stringWidth(labelName)) / 2;
            int textY = minY + ((height - fm.getHeight()) / 2) + fm.getAscent();
            g.drawString(labelName, textX, textY);
        }
        
        if (isSelected) drawPorts(g);
    }
    @Override
    public boolean isInside(int px, int py) {
        return px >= Math.min(x1, x2) && px <= Math.max(x1, x2) && py >= Math.min(y1, y2) && py <= Math.max(y1, y2);
    }
}