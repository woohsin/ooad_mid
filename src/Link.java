import java.awt.*;

// 1. 策略介面：定義「畫箭頭」的合約
interface ArrowDrawer {
    void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2);
}

// 2. 具體策略實作：各自負責不同類型的幾何運算
class AssociationDrawer implements ArrowDrawer {
    public void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 15;
        int arrowX = x2 - (int)(arrowSize * Math.cos(angle));
        int arrowY = y2 - (int)(arrowSize * Math.sin(angle));
        g2d.drawLine(x2, y2, (int)(arrowX - arrowSize/2 * Math.sin(angle)), (int)(arrowY + arrowSize/2 * Math.cos(angle)));
        g2d.drawLine(x2, y2, (int)(arrowX + arrowSize/2 * Math.sin(angle)), (int)(arrowY - arrowSize/2 * Math.cos(angle)));
    }
}

class GeneralizationDrawer implements ArrowDrawer {
    public void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 15;
        int arrowX = x2 - (int)(arrowSize * Math.cos(angle));
        int arrowY = y2 - (int)(arrowSize * Math.sin(angle));
        int[] xPoints = {x2, (int)(arrowX - arrowSize/2 * Math.sin(angle)), (int)(arrowX + arrowSize/2 * Math.sin(angle))};
        int[] yPoints = {y2, (int)(arrowY + arrowSize/2 * Math.cos(angle)), (int)(arrowY - arrowSize/2 * Math.cos(angle))};
        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(xPoints, yPoints, 3);
        g2d.setColor(Color.BLACK);
        g2d.drawPolygon(xPoints, yPoints, 3);
    }
}

class CompositionDrawer implements ArrowDrawer {
    public void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 15;
        int diamondSize = 10;
        int arrowX = x2 - (int)(arrowSize * Math.cos(angle));
        int arrowY = y2 - (int)(arrowSize * Math.sin(angle));
        int[] dx = {x2, (int)(arrowX - diamondSize * Math.sin(angle)), (int)(arrowX - arrowSize * Math.cos(angle)), (int)(arrowX + diamondSize * Math.sin(angle))};
        int[] dy = {y2, (int)(arrowY + diamondSize * Math.cos(angle)), (int)(arrowY - arrowSize * Math.sin(angle)), (int)(arrowY - diamondSize * Math.cos(angle))};
        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(dx, dy, 4);
        g2d.setColor(Color.BLACK);
        g2d.drawPolygon(dx, dy, 4);
    }
}

// 3. 類型列舉：將字串名稱與對應的繪製器綁定
enum LinkType {
    ASSOCIATION("Association", new AssociationDrawer()),
    GENERALIZATION("Generalization", new GeneralizationDrawer()),
    COMPOSITION("Composition", new CompositionDrawer());

    private final String typeName;
    private final ArrowDrawer drawer;

    LinkType(String typeName, ArrowDrawer drawer) {
        this.typeName = typeName;
        this.drawer = drawer;
    }

    public ArrowDrawer getDrawer() { return drawer; }

    public static LinkType fromString(String typeName) {
        for (LinkType type : values()) {
            if (type.typeName.equalsIgnoreCase(typeName)) return type;
        }
        return ASSOCIATION; // 預設值
    }
}

// 4. 主類別
public class Link {
    private Shape fromShape;
    private String fromPortPosition;
    private Shape toShape;
    private String toPortPosition;
    private LinkType type; // 直接儲存 Enum

    public Link(Shape from, Port fromP, Shape to, Port toP, String typeName) {
        this.fromShape = from;
        this.fromPortPosition = fromP.getPosition();
        this.toShape = to;
        this.toPortPosition = toP.getPosition();
        this.type = LinkType.fromString(typeName); // 透過 Enum 工廠初始化
    }

    public void draw(Graphics g) {
        Port fromPort = getFromPort();
        Port toPort = getToPort();
        if (fromPort == null || toPort == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        
        // 畫主線
        g2d.drawLine(fromPort.getX(), fromPort.getY(), toPort.getX(), toPort.getY());
        
        // 委託 Enum 裡面的 Drawer 畫箭頭
        type.getDrawer().drawArrow(g2d, fromPort.getX(), fromPort.getY(), toPort.getX(), toPort.getY());
    }

    private Port getFromPort() {
        for (Port port : fromShape.getAllPorts()) {
            if (port.getPosition().equals(fromPortPosition)) return port;
        }
        return null;
    }

    private Port getToPort() {
        for (Port port : toShape.getAllPorts()) {
            if (port.getPosition().equals(toPortPosition)) return port;
        }
        return null;
    }

    public boolean involvesShape(Shape shape) {
        return fromShape == shape || toShape == shape;
    }
}