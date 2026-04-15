import java.awt.event.MouseEvent;

public abstract class Mode {
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
}

class CreateObjectMode extends Mode {
    private String type;
    private int startX, startY;
    private int endX, endY;
    private boolean isDragging = false;
    
    public CreateObjectMode(String t) { this.type = t; }
    
    public String getType() { return this.type; }

    @Override
    public void mousePressed(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();
        endX = startX;
        endY = startY;
        isDragging = true;
        Canvas.getInstance().setPreviewShape(type, startX, startY, endX, endY);
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        endX = e.getX();
        endY = e.getY();
        Canvas.getInstance().setPreviewShape(type, startX, startY, endX, endY);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isDragging) {
            endX = e.getX();
            endY = e.getY();
            
            // 建立最終的物件
            if (type.equals("Rect")) {
                Canvas.getInstance().addShape(new RectObject(startX, startY, endX, endY));
            } else {
                Canvas.getInstance().addShape(new OvalObject(startX, startY, endX, endY));
            }
            
            isDragging = false;
            Canvas.getInstance().clearPreviewShape();
            
            // 恢復到之前的模式
            Canvas.getInstance().restorePreviousMode();
        }
    }
}

class ConnectionMode extends Mode {
    private String linkType;
    private Shape startShape = null;
    private Port startPort = null;
    private int startX, startY;
    private int endX, endY;
    private boolean isDragging = false;
    
    public ConnectionMode(String type) {
        this.linkType = type;
    }
    
    public String getLinkType() { return this.linkType; }

    @Override
    public void mousePressed(MouseEvent e) {
        Canvas canvas = Canvas.getInstance();
        
        // 檢查是否點擊在某個物件的 port 上（Alternative B.1）
        for (Shape shape : canvas.getShapes()) {
            Port port = shape.getPortAt(e.getX(), e.getY());
            if (port != null) {
                startShape = shape;
                startPort = port;
                startX = e.getX();
                startY = e.getY();
                endX = startX;
                endY = startY;
                isDragging = true;
                canvas.setPreviewLink(startPort.getX(), startPort.getY(), endX, endY);
                return;
            }
        }
        
        // 如果沒有點擊在 port 上，不進行任何操作
        isDragging = false;
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (isDragging && startPort != null) {
            endX = e.getX();
            endY = e.getY();
            Canvas.getInstance().setPreviewLink(startPort.getX(), startPort.getY(), endX, endY);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isDragging && startPort != null) {
            endX = e.getX();
            endY = e.getY();
            
            Canvas canvas = Canvas.getInstance();
            Shape endShape = null;
            Port endPort = null;
            
            // 檢查是否在其他物件的 port 上放開（Alternative B.2）
            for (Shape shape : canvas.getShapes()) {
                Port port = shape.getPortAt(endX, endY);
                if (port != null && shape != startShape) {  // 不能連結到同一個物件
                    endShape = shape;
                    endPort = port;
                    break;
                }
            }
            
            // 如果滿足條件，建立 link
            if (endShape != null && endPort != null) {
                Link link = new Link(startShape, startPort, endShape, endPort, linkType);
                canvas.addLink(link);
            }
            
            isDragging = false;
            startShape = null;
            startPort = null;
            canvas.clearPreviewLink();
            
            // 恢復到之前的模式
            canvas.restorePreviousMode();
        }
    }
}

class SelectMode extends Mode {
    private int startX, startY;
    private int endX, endY;
    private boolean isDragging = false;
    private boolean isSelectingArea = false;  // 標記是否在空白區域進行拖曳選擇
    private Shape movingShape = null;  // 正在移動的物件（Use Case D）
    private Shape resizingShape = null;  // 正在調整大小的物件（Use Case F）
    private String resizePortPosition = null;  // 被拖動的 port 位置（Use Case F）
    private int lastX, lastY;  // 上次的座標，用於計算移動距離
    
    @Override
    public void mousePressed(MouseEvent e) {
        Canvas canvas = Canvas.getInstance();
        
        // 檢查是否點擊在任何物件的 port 上（Use Case F - Case 3）
        for (int i = canvas.getShapes().size() - 1; i >= 0; i--) {
            Shape s = canvas.getShapes().get(i);
            Port port = s.getPortAt(e.getX(), e.getY());
            if (port != null && s.canResize()) {  // 檢查物件是否可以調整大小（F.1）
                resizingShape = s;
                resizePortPosition = port.getPosition();
                startX = e.getX();
                startY = e.getY();
                endX = startX;
                endY = startY;
                isDragging = true;
                isSelectingArea = false;
                return;
            }
        }
        
        // 檢查是否點擊在任何物件上
        boolean clickedOnShape = false;
        Shape clickedShape = null;
        
        // 按照相反順序檢查（後添加的物件優先）
        for (int i = canvas.getShapes().size() - 1; i >= 0; i--) {
            Shape s = canvas.getShapes().get(i);
            if (s.isInside(e.getX(), e.getY())) {
                clickedOnShape = true;
                clickedShape = s;
                break;
            }
        }
        
        // 如果點擊在物件上，選中該物件並開始移動準備（Use Case D）
        if (clickedOnShape && clickedShape != null) {
            if (e.isControlDown()) {
                // Ctrl + 點擊：切換選取狀態
                clickedShape.setSelected(!clickedShape.isSelected);
            } else {
                // 普通點擊：取消其他選取，選取該物件
                for (Shape s : canvas.getShapes()) {
                    s.setSelected(false);
                }
                clickedShape.setSelected(true);
            }
            movingShape = clickedShape;
            lastX = e.getX();
            lastY = e.getY();
            isDragging = true;
            isSelectingArea = false;
        } else {
            // 如果沒有點擊在物件上，開始矩形選擇（Use Case C）
            startX = e.getX();
            startY = e.getY();
            endX = startX;
            endY = startY;
            isDragging = true;
            isSelectingArea = true;
            
            // 取消所有物件的 select 狀態（Case 2）
            for (Shape s : canvas.getShapes()) {
                s.setSelected(false);
            }
        }
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (isDragging) {
            if (resizingShape != null) {
                // Use Case F: 調整物件大小
                endX = e.getX();
                endY = e.getY();
                resizingShape.resize(resizePortPosition, endX, endY);
                Canvas.getInstance().repaint();
            } else if (movingShape != null && !isSelectingArea) {
                // Use Case D: 移動物件
                int deltaX = e.getX() - lastX;
                int deltaY = e.getY() - lastY;
                // 移動所有選取的物件
                for (Shape s : Canvas.getInstance().getShapes()) {
                    if (s.isSelected) {
                        s.move(deltaX, deltaY);
                    }
                }
                lastX = e.getX();
                lastY = e.getY();
                Canvas.getInstance().repaint();
            } else if (isSelectingArea) {
                // Use Case C: 矩形選擇
                endX = e.getX();
                endY = e.getY();
                Canvas.getInstance().setPreviewSelectionRect(startX, startY, endX, endY);
            }
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        if (isDragging) {
            if (resizingShape != null) {
                // Use Case F: 完成物件調整
                endX = e.getX();
                endY = e.getY();
                resizingShape.resize(resizePortPosition, endX, endY);
                resizingShape = null;
                resizePortPosition = null;
                Canvas.getInstance().repaint();
            } else if (movingShape != null && !isSelectingArea) {
                // Use Case D: 完成物件移動
                movingShape = null;
                Canvas.getInstance().repaint();
            } else if (isSelectingArea) {
                // Use Case C: 完成矩形選擇
                endX = e.getX();
                endY = e.getY();
                
                Canvas canvas = Canvas.getInstance();
                
                // 計算矩形區域
                int minX = Math.min(startX, endX);
                int maxX = Math.max(startX, endX);
                int minY = Math.min(startY, endY);
                int maxY = Math.max(startY, endY);
                
                // 檢查哪些物件完全落在矩形區域內並選中它們（Case 5）
                boolean foundAny = false;
                for (Shape s : canvas.getShapes()) {
                    if (s.isCompletelyInside(minX, maxX, minY, maxY)) {
                        s.setSelected(true);
                        foundAny = true;
                    } else {
                        s.setSelected(false);
                    }
                }
                
                // 如果沒有物件落在矩形區域內，unselect 所有物件（Alternative C.3）
                if (!foundAny) {
                    for (Shape s : canvas.getShapes()) {
                        s.setSelected(false);
                    }
                }
                
                canvas.clearPreviewSelectionRect();
            }
            
            isDragging = false;
            isSelectingArea = false;
            movingShape = null;
            resizingShape = null;
        }
    }
}