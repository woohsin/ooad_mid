import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Shape {
    protected int x1, y1, x2, y2;
    protected boolean isSelected = false;
    protected static final int MIN_SIZE = 22;  // 最小尺寸限制
    protected String labelName = "";  // 標籤名稱
    protected Color labelColor = new Color(211, 211, 211);

    public abstract void draw(Graphics g);
    public abstract boolean isInside(int x, int y);
    public abstract List<Port> getAllPorts();

    public abstract Shape createInstance(int x1, int y1, int x2, int y2);

    public boolean canResize() {
        return true;  // 基本物件可以調整大小
    }

    public boolean isCompletelyInside(int minX, int maxX, int minY, int maxY) {
        int x1 = Math.min(this.x1, this.x2);
        int x2 = Math.max(this.x1, this.x2);
        int y1 = Math.min(this.y1, this.y2);
        int y2 = Math.max(this.y1, this.y2);
        
        return x1 >= minX && x2 <= maxX && y1 >= minY && y2 <= maxY;
    }

    public Port getPortAt(int px, int py) {
        int tolerance = 8;  // port 的點擊範圍
        for (Port port : getAllPorts()) {
            if (Math.abs(port.getX() - px) <= tolerance && Math.abs(port.getY() - py) <= tolerance) {
                return port;
            }
        }
        return null;
    }
    
    // 邊界
    protected Rectangle getBounds() {
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int width = Math.abs(x1 - x2);
        int height = Math.abs(y1 - y2);
        return new Rectangle(minX, minY, width, height);
    }
    
    // label str置中
    protected void drawLabelText(Graphics g, int minX, int minY, int width, int height) {
        if (!labelName.isEmpty()) {
            g.setColor(Color.BLACK);
            FontMetrics fm = g.getFontMetrics();
            int textX = minX + (width - fm.stringWidth(labelName)) / 2;
            int textY = minY + ((height - fm.getHeight()) / 2) + fm.getAscent();
            g.drawString(labelName, textX, textY);
        }
    }
    
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
            g.fillRect(port.getX() - pw/2, port.getY() - pw/2, pw, pw);
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
    private int x, y;
    private String position;  // "TL", "TR", "BL", "BR", "T", "B", "L", "R" 等
    
    public Port(int x, int y, String position) {
        this.x = x;
        this.y = y;
        this.position = position;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public String getPosition() { return position; }
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
    public boolean isInside(int x, int y) {
        // 預設實作：檢查是否在矩形邊界內
        Rectangle b = getBounds();
        return x >= b.x && x <= b.x + b.width && y >= b.y && y <= b.y + b.height;
    }

    @Override
    public Shape createInstance(int x1, int y1, int x2, int y2) {
        return new RectObject(x1, y1, x2, y2);
    }
    
    @Override
    public List<Port> getAllPorts() {
        List<Port> ports = new ArrayList<>();
        Rectangle bounds = getBounds();
        
        // 四個角
        ports.add(new Port(bounds.x, bounds.y, "TL"));  // Top-Left
        ports.add(new Port(bounds.x + bounds.width, bounds.y, "TR"));  // Top-Right
        ports.add(new Port(bounds.x, bounds.y + bounds.height, "BL"));  // Bottom-Left
        ports.add(new Port(bounds.x + bounds.width, bounds.y + bounds.height, "BR"));  // Bottom-Right
        
        // 四條邊的中點
        ports.add(new Port(bounds.x + bounds.width / 2, bounds.y, "T"));   // Top
        ports.add(new Port(bounds.x + bounds.width / 2, bounds.y + bounds.height, "B"));   // Bottom
        ports.add(new Port(bounds.x, bounds.y + bounds.height / 2, "L"));   // Left
        ports.add(new Port(bounds.x + bounds.width, bounds.y + bounds.height / 2, "R"));   // Right
        
        return ports;
    }
    
    @Override
    public void draw(Graphics g) {
        Rectangle bounds = getBounds();
        
        // 用 labelColor 填充整個矩形
        g.setColor(labelColor);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(Color.BLACK);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        
        // 繪製名稱，置中顯示
        drawLabelText(g, bounds.x, bounds.y, bounds.width, bounds.height);
        
        if (isSelected) drawPorts(g);
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
    public Shape createInstance(int x1, int y1, int x2, int y2) {
        return new OvalObject(x1, y1, x2, y2);
    }

    @Override
    public boolean isInside(int px, int py) {
        Rectangle b = getBounds();
        
        // 1. 取得中心點 (h, k)
        double h = b.getCenterX();
        double k = b.getCenterY();
        
        // 2. 取得半長軸與半短軸 (rx, ry)
        double rx = b.width / 2.0;
        double ry = b.height / 2.0;

        // 防止除以零（寬高為 0 的情況）
        if (rx <= 0 || ry <= 0) return false;

        // 3. 使用橢圓標準方程式判定： (x-h)^2 / rx^2 + (y-k)^2 / ry^2 <= 1
        double dx = px - h;
        double dy = py - k;
        
        return (dx * dx) / (rx * rx) + (dy * dy) / (ry * ry) <= 1.0;
    }
    
    @Override
    public List<Port> getAllPorts() {
        List<Port> ports = new ArrayList<>();
        Rectangle bounds = getBounds();
        int centerX = bounds.x + bounds.width / 2;
        int centerY = bounds.y + bounds.height / 2;
        
        // 四個主要方向的 ports
        ports.add(new Port(centerX, bounds.y, "T"));       // Top
        ports.add(new Port(centerX, bounds.y + bounds.height, "B"));       // Bottom
        ports.add(new Port(bounds.x, centerY, "L"));       // Left
        ports.add(new Port(bounds.x + bounds.width, centerY, "R"));       // Right
        
        return ports;
    }
    
    @Override
    public void draw(Graphics g) {
        Rectangle bounds = getBounds();
        
        // 用 labelColor 填充整個橢圓
        g.setColor(labelColor);
        g.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(Color.BLACK);
        g.drawOval(bounds.x, bounds.y, bounds.width, bounds.height);
        
        // 繪製名稱，置中顯示
        drawLabelText(g, bounds.x, bounds.y, bounds.width, bounds.height);
        
        if (isSelected) drawPorts(g);
    }
}