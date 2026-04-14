import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CompositeShape extends Shape {
    private List<Shape> children = new ArrayList<>();

    public CompositeShape(List<Shape> shapes) {
        this.children.addAll(shapes);
        // 計算邊界
        if (!shapes.isEmpty()) {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            for (Shape s : shapes) {
                minX = Math.min(minX, s.x1);
                minY = Math.min(minY, s.y1);
                maxX = Math.max(maxX, s.x2);
                maxY = Math.max(maxY, s.y2);
            }
            this.x1 = minX;
            this.y1 = minY;
            this.x2 = maxX;
            this.y2 = maxY;
        }
    }

    @Override
    public void draw(Graphics g) {
        for (Shape child : children) {
            child.draw(g);
        }
        // 如果被選取，繪製邊框
        if (isSelected) {
            g.setColor(Color.BLACK);
            g.drawRect(x1, y1, x2 - x1, y2 - y1);
        }
    }

    @Override
    public boolean isInside(int x, int y) {
        for (Shape child : children) {
            if (child.isInside(x, y)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Port getPortAt(int x, int y) {
        for (Shape child : children) {
            Port port = child.getPortAt(x, y);
            if (port != null) {
                return port;
            }
        }
        return null;
    }

    @Override
    public List<Port> getAllPorts() {
        List<Port> allPorts = new ArrayList<>();
        for (Shape child : children) {
            allPorts.addAll(child.getAllPorts());
        }
        return allPorts;
    }

    @Override
    public boolean isCompletelyInside(int minX, int maxX, int minY, int maxY) {
        for (Shape child : children) {
            if (!child.isCompletelyInside(minX, maxX, minY, maxY)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canResize() {
        return false;
    }

    @Override
    public void move(int deltaX, int deltaY) {
        super.move(deltaX, deltaY);
        for (Shape child : children) {
            child.move(deltaX, deltaY);
        }
    }

    public List<Shape> getChildren() {
        return children;
    }

    @Override
    public void setSelected(boolean b) {
        super.setSelected(b); // 設定群組本身的狀態
        // 同步設定所有子物件的狀態
        for (Shape child : children) {
            child.setSelected(b);
        }
    }
}