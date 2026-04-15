import java.awt.*;

interface LinkTypeStrategy {
    void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2);
}

class AssociationStrategy implements LinkTypeStrategy {
    public void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 15;
        int arrowX = x2 - (int)(arrowSize * Math.cos(angle));
        int arrowY = y2 - (int)(arrowSize * Math.sin(angle));
        g2d.drawLine(x2, y2, (int)(arrowX - arrowSize/2 * Math.sin(angle)), (int)(arrowY + arrowSize/2 * Math.cos(angle)));
        g2d.drawLine(x2, y2, (int)(arrowX + arrowSize/2 * Math.sin(angle)), (int)(arrowY - arrowSize/2 * Math.cos(angle)));
    }
}

class GeneralizationStrategy implements LinkTypeStrategy {
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

class CompositionStrategy implements LinkTypeStrategy {
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

public class Link {
    private Shape fromShape;
    private String fromPortPosition;
    private Shape toShape;
    private String toPortPosition;
    private LinkTypeStrategy strategy;

    public Link(Shape from, Port fromP, Shape to, Port toP, String type) {
        this.fromShape = from;
        this.fromPortPosition = fromP.getPosition();
        this.toShape = to;
        this.toPortPosition = toP.getPosition();
        
        switch(type) {
            case "Generalization": this.strategy = new GeneralizationStrategy(); break;
            case "Composition": this.strategy = new CompositionStrategy(); break;
            default: this.strategy = new AssociationStrategy(); break;
        }
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

    public void draw(Graphics g) {
        Port fromPort = getFromPort();
        Port toPort = getToPort();
        if (fromPort == null || toPort == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(fromPort.getX(), fromPort.getY(), toPort.getX(), toPort.getY());
        
        strategy.drawArrow(g2d, fromPort.getX(), fromPort.getY(), toPort.getX(), toPort.getY());
    }

    public boolean involvesShape(Shape shape) {
        return fromShape == shape || toShape == shape;
    }
}