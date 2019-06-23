package components;

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

    public void setCenter(double x, double y) {
        setCenterX(x);
        setCenterY(y);
    }
}
