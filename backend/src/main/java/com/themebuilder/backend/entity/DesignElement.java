package com.themebuilder.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "design_element")
public class DesignElement extends BaseEntity {

    private String type;

    private double x;
    private double y;

    private double width;
    private double height;

    private double scaleX = 1.0;
    private double scaleY = 1.0;

    private double angle;
    private double opacity = 1.0;

    @Column(columnDefinition = "TEXT")
    private String text;

    private Integer fontSize;
    private String fontFamily;

    private String color;
    private String backgroundColor;

    private String fontWeight;
    private String fontStyle;

    @Column(columnDefinition = "TEXT")
    private String src;

    private Double lineHeight;
    private Double charSpacing;
    private String textAlign;

    private String strokeColor;
    private Double strokeWidth;

    @ElementCollection
    private List<Integer> lineDash;

    private Integer shapeSides;

    @ManyToOne
    @JoinColumn(name = "design_id")
    @JsonIgnore
    private Design design;

    @ManyToOne
    @JoinColumn(name = "template_id")
    @JsonIgnore
    private Template template;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getScaleX() {
        return scaleX;
    }

    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public Double getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(Double lineHeight) {
        this.lineHeight = lineHeight;
    }

    public Double getCharSpacing() {
        return charSpacing;
    }

    public void setCharSpacing(Double charSpacing) {
        this.charSpacing = charSpacing;
    }

    public String getTextAlign() {
        return textAlign;
    }

    public void setTextAlign(String textAlign) {
        this.textAlign = textAlign;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }

    public Double getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(Double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public List<Integer> getLineDash() {
        return lineDash;
    }

    public void setLineDash(List<Integer> lineDash) {
        this.lineDash = lineDash;
    }

    public Integer getShapeSides() {
        return shapeSides;
    }

    public void setShapeSides(Integer shapeSides) {
        this.shapeSides = shapeSides;
    }

    public Design getDesign() {
        return design;
    }

    public void setDesign(Design design) {
        this.design = design;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }
}