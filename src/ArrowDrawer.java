import java.awt.*;

// 箭头绘制器接口
interface ArrowDrawer {
    void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2);
}

// 关联箭头绘制器
class AssociationArrowDrawer implements ArrowDrawer {
    @Override
    public void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 15;

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

        g2d.drawLine(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
        g2d.drawLine(xPoints[0], yPoints[0], xPoints[2], yPoints[2]);
    }
}

// 泛化箭头绘制器
class GeneralizationArrowDrawer implements ArrowDrawer {
    @Override
    public void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 15;

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

        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(xPoints, yPoints, 3);
        g2d.setColor(Color.BLACK);
        g2d.drawPolygon(xPoints, yPoints, 3);
    }
}

// 组合箭头绘制器
class CompositionArrowDrawer implements ArrowDrawer {
    @Override
    public void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 15;
        int diamondSize = 10;

        int arrowX = x2 - (int)(arrowSize * Math.cos(angle));
        int arrowY = y2 - (int)(arrowSize * Math.sin(angle));

        int[] diamondX = new int[4];
        int[] diamondY = new int[4];
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

// 链接类型枚举
enum LinkType {
    ASSOCIATION("Association", new AssociationArrowDrawer()),
    GENERALIZATION("Generalization", new GeneralizationArrowDrawer()),
    COMPOSITION("Composition", new CompositionArrowDrawer());

    private final String typeName;
    private final ArrowDrawer drawer;

    LinkType(String typeName, ArrowDrawer drawer) {
        this.typeName = typeName;
        this.drawer = drawer;
    }

    public String getTypeName() {
        return typeName;
    }

    public ArrowDrawer getDrawer() {
        return drawer;
    }

    public static LinkType fromString(String typeName) {
        for (LinkType type : values()) {
            if (type.typeName.equals(typeName)) {
                return type;
            }
        }
        return ASSOCIATION; // 默认
    }
}