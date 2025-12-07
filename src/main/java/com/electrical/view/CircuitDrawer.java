package com.electrical.view;

import com.electrical.model.DividerResult;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;

/**
 * Класс для отрисовки электрических схем делителя напряжения
 */
public class CircuitDrawer {
    
    private final Canvas canvas;
    private final GraphicsContext gc;
    
    // Цвета
    private static final Color WIRE_COLOR = Color.web("#2C3E50");
    private static final Color RESISTOR_COLOR = Color.web("#E74C3C");
    private static final Color TEXT_COLOR = Color.web("#2C3E50");
    private static final Color NODE_COLOR = Color.web("#3498DB");
    private static final Color LABEL_COLOR = Color.web("#27AE60");
    private static final Color BACKGROUND_COLOR = Color.web("#F8F9FA");
    
    // Размеры
    private static final double RESISTOR_WIDTH = 60;
    private static final double RESISTOR_HEIGHT = 20;
    private static final double WIRE_THICKNESS = 2;
    private static final double NODE_RADIUS = 5;
    
    public CircuitDrawer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }
    
    /**
     * Отрисовать схему делителя напряжения
     */
    public void drawVoltageDivider(DividerResult result) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        
        // Очистка холста
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, width, height);
        
        // Центр схемы
        double centerX = width / 2;
        double startY = 40;
        double endY = height - 40;
        
        // Узлы
        double vinY = startY;
        double voutY = (startY + endY) / 2;
        double gndY = endY;
        
        // Левая линия (основная вертикальная)
        double leftX = centerX - 80;
        // Правая линия для Vout
        double rightX = centerX + 80;
        
        gc.setStroke(WIRE_COLOR);
        gc.setLineWidth(WIRE_THICKNESS);
        
        // Рисуем верхнее плечо
        double upperEndY = drawArmResistors(result.getUpperResistors(), result.isUpperParallel(),
                leftX, vinY + 30, voutY - 30, true, "R1");
        
        // Рисуем нижнее плечо
        double lowerStartY = drawArmResistors(result.getLowerResistors(), result.isLowerParallel(),
                leftX, voutY + 30, gndY - 30, false, "R2");
        
        // Провод от Vin до верхнего плеча
        gc.strokeLine(leftX, vinY, leftX, vinY + 30);
        
        // Провод между плечами
        gc.strokeLine(leftX, upperEndY, leftX, voutY);
        gc.strokeLine(leftX, voutY, leftX, lowerStartY);
        
        // Провод от нижнего плеча до GND
        gc.strokeLine(leftX, gndY - 30, leftX, gndY);
        
        // Провод к Vout
        gc.strokeLine(leftX, voutY, rightX, voutY);
        
        // Узлы
        drawNode(leftX, vinY, "Vin");
        drawNode(leftX, voutY, "");
        drawNode(rightX, voutY, "Vout");
        drawGround(leftX, gndY);
        
        // Подписи напряжений
        gc.setFill(LABEL_COLOR);
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(String.format("Vin = %.2f В", result.getVIn()), leftX + 15, vinY + 5);
        gc.fillText(String.format("Vout = %.4f В", result.getVOutActual()), rightX + 15, voutY + 5);
        gc.fillText("GND", leftX + 15, gndY + 5);
        
        // Информация о схеме
        drawSchemaInfo(result, width, height);
    }
    
    /**
     * Отрисовать резисторы плеча
     * @return Y-координата конца последнего резистора
     */
    private double drawArmResistors(List<Double> resistors, boolean parallel,
                                     double x, double startY, double endY, 
                                     boolean isUpper, String label) {
        if (resistors.isEmpty()) return startY;
        
        double availableHeight = endY - startY;
        
        if (resistors.size() == 1) {
            // Один резистор
            double resY = startY + availableHeight / 2 - RESISTOR_HEIGHT / 2;
            gc.strokeLine(x, startY, x, resY);
            drawResistor(x, resY, false);
            drawResistorLabel(x, resY, resistors.get(0), label);
            gc.strokeLine(x, resY + RESISTOR_HEIGHT, x, endY);
            return endY;
            
        } else if (parallel) {
            // Параллельное соединение
            double spacing = 50;
            double totalWidth = (resistors.size() - 1) * spacing;
            double leftmostX = x - totalWidth / 2;
            
            // Горизонтальные провода сверху и снизу
            double topY = startY + 20;
            double bottomY = endY - 20;
            
            gc.strokeLine(x, startY, x, topY);
            gc.strokeLine(leftmostX, topY, leftmostX + totalWidth, topY);
            gc.strokeLine(leftmostX, bottomY, leftmostX + totalWidth, bottomY);
            gc.strokeLine(x, bottomY, x, endY);
            
            // Резисторы
            for (int i = 0; i < resistors.size(); i++) {
                double resX = leftmostX + i * spacing;
                double resY = (topY + bottomY) / 2 - RESISTOR_HEIGHT / 2;
                
                gc.strokeLine(resX, topY, resX, resY);
                drawResistor(resX, resY, false);
                drawResistorLabel(resX, resY, resistors.get(i), label + (char)('a' + i));
                gc.strokeLine(resX, resY + RESISTOR_HEIGHT, resX, bottomY);
            }
            
            return endY;
            
        } else {
            // Последовательное соединение
            double resHeight = RESISTOR_HEIGHT;
            double gap = 15;
            double totalResHeight = resistors.size() * resHeight + (resistors.size() - 1) * gap;
            double resStartY = startY + (availableHeight - totalResHeight) / 2;
            
            gc.strokeLine(x, startY, x, resStartY);
            
            double currentY = resStartY;
            for (int i = 0; i < resistors.size(); i++) {
                drawResistor(x, currentY, false);
                drawResistorLabel(x, currentY, resistors.get(i), label + (char)('a' + i));
                currentY += resHeight;
                
                if (i < resistors.size() - 1) {
                    gc.strokeLine(x, currentY, x, currentY + gap);
                    currentY += gap;
                }
            }
            
            gc.strokeLine(x, currentY, x, endY);
            return endY;
        }
    }
    
    /**
     * Отрисовать резистор (зигзаг)
     */
    private void drawResistor(double x, double y, boolean horizontal) {
        gc.setStroke(RESISTOR_COLOR);
        gc.setLineWidth(2);
        
        if (horizontal) {
            // Горизонтальный резистор
            double segmentWidth = RESISTOR_WIDTH / 6;
            gc.beginPath();
            gc.moveTo(x, y + RESISTOR_HEIGHT / 2);
            for (int i = 0; i < 6; i++) {
                double nextX = x + (i + 1) * segmentWidth;
                double nextY = (i % 2 == 0) ? y : y + RESISTOR_HEIGHT;
                gc.lineTo(nextX, nextY);
            }
            gc.lineTo(x + RESISTOR_WIDTH, y + RESISTOR_HEIGHT / 2);
            gc.stroke();
        } else {
            // Вертикальный резистор (зигзаг)
            double segmentHeight = RESISTOR_HEIGHT / 4;
            double zigWidth = 10;
            
            gc.beginPath();
            gc.moveTo(x, y);
            gc.lineTo(x + zigWidth, y + segmentHeight);
            gc.lineTo(x - zigWidth, y + 2 * segmentHeight);
            gc.lineTo(x + zigWidth, y + 3 * segmentHeight);
            gc.lineTo(x, y + RESISTOR_HEIGHT);
            gc.stroke();
        }
        
        gc.setStroke(WIRE_COLOR);
        gc.setLineWidth(WIRE_THICKNESS);
    }
    
    /**
     * Подпись резистора
     */
    private void drawResistorLabel(double x, double y, double value, String label) {
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("System", FontWeight.NORMAL, 10));
        gc.setTextAlign(TextAlignment.LEFT);
        
        String valueStr = formatResistance(value);
        gc.fillText(valueStr, x + 15, y + RESISTOR_HEIGHT / 2 + 4);
    }
    
    /**
     * Отрисовать узел
     */
    private void drawNode(double x, double y, String label) {
        gc.setFill(NODE_COLOR);
        gc.fillOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
    }
    
    /**
     * Отрисовать символ земли
     */
    private void drawGround(double x, double y) {
        gc.setStroke(WIRE_COLOR);
        gc.setLineWidth(2);
        
        double lineSpacing = 5;
        double[] widths = {20, 14, 8};
        
        for (int i = 0; i < widths.length; i++) {
            double lineY = y + i * lineSpacing;
            double halfWidth = widths[i] / 2;
            gc.strokeLine(x - halfWidth, lineY, x + halfWidth, lineY);
        }
    }
    
    /**
     * Информация о схеме
     */
    private void drawSchemaInfo(DividerResult result, double width, double height) {
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("System", FontWeight.NORMAL, 11));
        gc.setTextAlign(TextAlignment.LEFT);
        
        double infoX = 20;
        double infoY = height - 80;
        double lineHeight = 15;
        
        gc.fillText("Верхнее плечо: " + result.getUpperResistorsString() + 
                (result.isUpperParallel() && result.getUpperResistors().size() > 1 ? " (параллельно)" : 
                 result.getUpperResistors().size() > 1 ? " (последовательно)" : ""), 
                infoX, infoY);
        gc.fillText("Нижнее плечо: " + result.getLowerResistorsString() + 
                (result.isLowerParallel() && result.getLowerResistors().size() > 1 ? " (параллельно)" : 
                 result.getLowerResistors().size() > 1 ? " (последовательно)" : ""), 
                infoX, infoY + lineHeight);
        gc.fillText(String.format("R_верх = %s, R_низ = %s", 
                formatResistance(result.getUpperResistance()),
                formatResistance(result.getLowerResistance())), 
                infoX, infoY + 2 * lineHeight);
        gc.fillText(String.format("Погрешность: %.4f%%", result.getErrorPercent()), 
                infoX, infoY + 3 * lineHeight);
    }
    
    private String formatResistance(double value) {
        if (value >= 1_000_000) {
            return String.format("%.2f МОм", value / 1_000_000);
        } else if (value >= 1000) {
            return String.format("%.2f кОм", value / 1000);
        } else {
            return String.format("%.2f Ом", value);
        }
    }
}


