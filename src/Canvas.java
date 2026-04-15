import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class Canvas extends JPanel {
    private static Canvas instance;
    private List<Shape> shapes = new ArrayList<>();
    private List<Link> links = new ArrayList<>();
    private Mode currentMode = new SelectMode();
    private Mode previousMode = new SelectMode();
    private String previousModeName = "Select";
    private Shape previewShape = null;
    private int previewLinkX1, previewLinkY1, previewLinkX2, previewLinkY2;
    private boolean showPreviewLink = false;
    private int previewRectX1, previewRectY1, previewRectX2, previewRectY2;
    private boolean showPreviewRect = false;
    private Shape hoveredShape = null;  // 正在被悬停的物件（Use Case F）
    private ToolBar toolBar = null;

    // 確保全程式只會用到這個 instance
    public static Canvas getInstance() {
        if (instance == null) instance = new Canvas();
        return instance;
    }

    public Canvas() {
        setBackground(Color.WHITE);
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { currentMode.mousePressed(e); repaint(); }
            @Override
            public void mouseReleased(MouseEvent e) { currentMode.mouseReleased(e); repaint(); }
            @Override
            public void mouseDragged(MouseEvent e) { currentMode.mouseDragged(e); repaint(); }
            @Override
            public void mouseMoved(MouseEvent e) { 
                // 檢查鼠標是否在某個物件上（Use Case F - Case 1-2）
                Shape prevHovered = hoveredShape;
                hoveredShape = null;
                for (int i = shapes.size() - 1; i >= 0; i--) {
                    Shape s = shapes.get(i);
                    if (s.isInside(e.getX(), e.getY())) {
                        hoveredShape = s;
                        break;
                    }
                }
                if (prevHovered != hoveredShape) {
                    repaint();
                }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    public void setToolBar(ToolBar tb) {
        this.toolBar = tb;
    }

    public void setMode(Mode m) { 
        this.previousMode = this.currentMode;
        // 記錄之前的模式名稱
        if (this.currentMode instanceof SelectMode) {
            this.previousModeName = "Select";
        } else if (this.currentMode instanceof CreateObjectMode) {
            this.previousModeName = ((CreateObjectMode) this.currentMode).getType();
        } else if (this.currentMode instanceof ConnectionMode) {
            this.previousModeName = ((ConnectionMode) this.currentMode).getLinkType();
        }
        this.currentMode = m; 
    }
    
    public void restorePreviousMode() {
        this.currentMode = this.previousMode;
        if (toolBar != null) {
            toolBar.restoreButtonState(previousModeName);
        }
    }
    
    void setPreviewShape(String type, int x1, int y1, int x2, int y2) {
        if (type.equals("Rect")) {
            this.previewShape = new RectObject(x1, y1, x2, y2);
        } else {
            this.previewShape = new OvalObject(x1, y1, x2, y2);
        }
    }
    
    void clearPreviewShape() {
        this.previewShape = null;
    }
    
    void setPreviewLink(int x1, int y1, int x2, int y2) {
        this.previewLinkX1 = x1;
        this.previewLinkY1 = y1;
        this.previewLinkX2 = x2;
        this.previewLinkY2 = y2;
        this.showPreviewLink = true;
    }
    
    void clearPreviewLink() {
        this.showPreviewLink = false;
    }
    
    void setPreviewSelectionRect(int x1, int y1, int x2, int y2) {
        this.previewRectX1 = x1;
        this.previewRectY1 = y1;
        this.previewRectX2 = x2;
        this.previewRectY2 = y2;
        this.showPreviewRect = true;
    }
    
    void clearPreviewSelectionRect() {
        this.showPreviewRect = false;
    }
    
    void addShape(Shape s) { shapes.add(s); }
    void addLink(Link l) { links.add(l); }
    List<Shape> getShapes() { return shapes; }
    
    public Shape getSelectedShape() {
        for (Shape s : shapes) {
            if (s.isSelected) return s;
        }
        return null;
    }

    public List<Shape> getSelectedShapes() {
        List<Shape> selected = new ArrayList<>();
        for (Shape s : shapes) {
            if (s.isSelected) {
                selected.add(s);
            }
        }
        return selected;
    }

    public void groupSelectedShapes() {
        List<Shape> selected = getSelectedShapes();
        if (selected.size() < 2) {
            return; // Alternatives D.1
        }
        CompositeShape composite = new CompositeShape(selected);
        shapes.removeAll(selected);
        shapes.add(composite);
        composite.setSelected(true);
        repaint();
    }

    public void ungroupSelectedShape() {
        List<Shape> selected = getSelectedShapes();
        if (selected.size() != 1 || !(selected.get(0) instanceof CompositeShape)) {
            return; // Alternatives D.2
        }
        CompositeShape composite = (CompositeShape) selected.get(0);
        shapes.remove(composite);
        shapes.addAll(composite.getChildren());
        for (Shape child : composite.getChildren()) {
            child.setSelected(true);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 繪製所有 links
        for (Link link : links) {
            link.draw(g);
        }
        
        // 繪製所有 shapes
        for (Shape s : shapes) {
            s.draw(g);
        }
        
        // 如果鼠標悬停在某個物件上，顯示其 ports（Use Case F - Case 2）
        if (hoveredShape != null) {
            hoveredShape.drawPorts(g);
        }
        
        // 繪製預覽 shape
        if (previewShape != null) {
            previewShape.drawPreview(g);
        }
        
        // 繪製預覽 link
        if (showPreviewLink) {
            g.setColor(Color.GRAY);
            g.drawLine(previewLinkX1, previewLinkY1, previewLinkX2, previewLinkY2);
        }
        
        // 繪製預覽選擇矩形
        if (showPreviewRect) {
            int minX = Math.min(previewRectX1, previewRectX2);
            int maxX = Math.max(previewRectX1, previewRectX2);
            int minY = Math.min(previewRectY1, previewRectY2);
            int maxY = Math.max(previewRectY1, previewRectY2);
            
            g.setColor(new Color(100, 150, 255, 100));  // 半透明藍色
            g.fillRect(minX, minY, maxX - minX, maxY - minY);
            g.setColor(Color.BLUE);
            g.drawRect(minX, minY, maxX - minX, maxY - minY);
        }
    }
}