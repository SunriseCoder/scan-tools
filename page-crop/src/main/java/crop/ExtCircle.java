package crop;

import javafx.scene.shape.Circle;

public class ExtCircle extends Circle {
    public ExtCircle previous;
    public ExtCircle next;

    private String name;

    public ExtCircle(String name) {
        this.name = name;
    }

    public ExtCircle(String name, double x, double y, double radius) {
        this.name = name;
        setCenterX(x);
        setCenterY(y);
        setRadius(radius);
    }

    public String getName() {
        return name;
    }

    // TODO Review all calls of this method, maybe possible to replace some with #moveBy
    public void setCenter(double x, double y) {
        setCenterX(x);
        setCenterY(y);
    }

    public void moveByX(int x) {
        setCenterX(getCenterX() + x);
    }

    public void moveByY(int y) {
        setCenterY(getCenterY() + y);
    }
}
