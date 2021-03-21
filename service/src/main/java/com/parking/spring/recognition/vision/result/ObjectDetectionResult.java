package com.parking.spring.recognition.vision.result;

import java.awt.*;

public class ObjectDetectionResult extends ClassificationResult {
    // location
    private int x;
    private int y;
    private int width;
    private int height;
    private Rectangle rectangle;

    public ObjectDetectionResult(int classId, String className, float confidence, int x, int y, int width, int height) {
        super(classId, className, confidence);

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rectangle = new Rectangle(x, y, width, height);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void scale(float xScale, float yScale) {
        int dx = Math.round(width * xScale) - width;
        int dy = Math.round(height * yScale) - height;

        this.x -= dx;
        this.y -= dy;

        this.width += dx * 2;
        this.height += dy * 2;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }
}
