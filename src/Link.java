import java.awt.*;

public class Link {
    private Shape fromShape;
    private String fromPortPosition;  // port 的位置標識
    private Shape toShape;
    private String toPortPosition;    // port 的位置標識
    private String linkType;  // "Association", "Generalization", "Composition"
    
    public Link(Shape from, Port fromP, Shape to, Port toP, String type) {
        this.fromShape = from;
        this.fromPortPosition = fromP.position;
        this.toShape = to;
        this.toPortPosition = toP.position;
        this.linkType = type;
    }
    
    // 動態取得最新的 from port
    private Port getFromPort() {
        for (Port port : fromShape.getAllPorts()) {
            if (port.position.equals(fromPortPosition)) {
                return port;
            }
        }
        return null;
    }
    
    // 動態取得最新的 to port
    private Port getToPort() {
        for (Port port : toShape.getAllPorts()) {
            if (port.position.equals(toPortPosition)) {
                return port;
            }
        }
        return null;
    }
    
    public boolean involvesShape(Shape shape) {
        return fromShape == shape || toShape == shape;
    }
    
    public void draw(Graphics g) {
        Port fromPort = getFromPort();
        Port toPort = getToPort();
        
        if (fromPort == null || toPort == null) return;
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        
        // 繪製連接線
        g2d.drawLine(fromPort.x, fromPort.y, toPort.x, toPort.y);
        
        // 根據類型繪製箭頭
        drawArrow(g2d, fromPort.x, fromPort.y, toPort.x, toPort.y);
    }
    
    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 15;
        
        // 計算箭頭的三個點
        int arrowX = x2 - (int)(arrowSize * Math.cos(angle));
        int arrowY = y2 - (int)(arrowSize * Math.sin(angle));
        
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        
        xPoints[0] = x2;
        yPoints[0] = y2;
        xPoints[1] = (int)(arrowX - arrowSize/2 * Math.sin(angle));
        yPoints[1] = (int)(arrowY + arrowSize/2 * Math.cos(angle));
        xPoints[2] = (int)(arrowX + arrowSize/2 * Math.sin(angle));
        yPoints[2] = (int)(arrowY - arrowSize/2 * Math.cos(angle));
        
        // 根據類型繪製不同的箭頭樣式
        if (linkType.equals("Association")) {
            // 簡單直線箭頭
            //g2d.drawPolygon(xPoints, yPoints, 3);
            g2d.drawLine(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
            g2d.drawLine(xPoints[0], yPoints[0], xPoints[2], yPoints[2]);
        } else if (linkType.equals("Generalization")) {
            // 空心三角形箭頭
            g2d.setColor(Color.WHITE);
            g2d.fillPolygon(xPoints, yPoints, 3);
            g2d.setColor(Color.BLACK);
            g2d.drawPolygon(xPoints, yPoints, 3);
        } else if (linkType.equals("Composition")) {
            // 菱形
            int[] diamondX = new int[4];
            int[] diamondY = new int[4];
            int diamondSize = 10;
            diamondX[0] = x2;
            diamondY[0] = y2;
            diamondX[1] = (int)(arrowX - diamondSize * Math.sin(angle));
            diamondY[1] = (int)(arrowY + diamondSize * Math.cos(angle));
            diamondX[2] = (int)(arrowX - arrowSize * Math.cos(angle));
            diamondY[2] = (int)(arrowY - arrowSize * Math.sin(angle));
            diamondX[3] = (int)(arrowX + diamondSize * Math.sin(angle));
            diamondY[3] = (int)(arrowY - diamondSize * Math.cos(angle));
            g2d.setColor(Color.WHITE);
            g2d.fillPolygon(diamondX, diamondY, 4);
            g2d.setColor(Color.BLACK);
            g2d.drawPolygon(diamondX, diamondY, 4);
        }
    }
    
    public String getLinkType() {
        return linkType;
    }
}
