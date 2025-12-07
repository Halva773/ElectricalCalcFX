package com.electrical.model;

import java.util.List;

/**
 * Результат расчёта делителя напряжения
 */
public class DividerResult implements Comparable<DividerResult> {
    
    private final List<Double> upperResistors;  // Верхнее плечо
    private final List<Double> lowerResistors;  // Нижнее плечо
    private final boolean upperParallel;         // Параллельное соединение верхнего плеча
    private final boolean lowerParallel;         // Параллельное соединение нижнего плеча
    private final double vIn;
    private final double vOutActual;
    private final double vOutRequired;
    private final double errorPercent;
    private final double totalResistance;
    private final double current;               // Ток через делитель
    
    public DividerResult(List<Double> upperResistors, List<Double> lowerResistors,
                         boolean upperParallel, boolean lowerParallel,
                         double vIn, double vOutActual, double vOutRequired) {
        this.upperResistors = upperResistors;
        this.lowerResistors = lowerResistors;
        this.upperParallel = upperParallel;
        this.lowerParallel = lowerParallel;
        this.vIn = vIn;
        this.vOutActual = vOutActual;
        this.vOutRequired = vOutRequired;
        this.errorPercent = Math.abs((vOutActual - vOutRequired) / vOutRequired) * 100;
        
        double rUpper = calculateArmResistance(upperResistors, upperParallel);
        double rLower = calculateArmResistance(lowerResistors, lowerParallel);
        this.totalResistance = rUpper + rLower;
        this.current = vIn / totalResistance;
    }
    
    private double calculateArmResistance(List<Double> resistors, boolean parallel) {
        if (resistors.isEmpty()) return 0;
        if (resistors.size() == 1) return resistors.get(0);
        
        if (parallel) {
            double sum = 0;
            for (double r : resistors) {
                sum += 1.0 / r;
            }
            return 1.0 / sum;
        } else {
            double sum = 0;
            for (double r : resistors) {
                sum += r;
            }
            return sum;
        }
    }
    
    public double getUpperResistance() {
        return calculateArmResistance(upperResistors, upperParallel);
    }
    
    public double getLowerResistance() {
        return calculateArmResistance(lowerResistors, lowerParallel);
    }
    
    public List<Double> getUpperResistors() {
        return upperResistors;
    }
    
    public List<Double> getLowerResistors() {
        return lowerResistors;
    }
    
    public boolean isUpperParallel() {
        return upperParallel;
    }
    
    public boolean isLowerParallel() {
        return lowerParallel;
    }
    
    public double getVIn() {
        return vIn;
    }
    
    public double getVOutActual() {
        return vOutActual;
    }
    
    public double getVOutRequired() {
        return vOutRequired;
    }
    
    public double getErrorPercent() {
        return errorPercent;
    }
    
    public double getTotalResistance() {
        return totalResistance;
    }
    
    public double getCurrent() {
        return current;
    }
    
    public double getPowerDissipation() {
        return vIn * current;
    }
    
    public int getTotalResistorCount() {
        return upperResistors.size() + lowerResistors.size();
    }
    
    public String getSchemaType() {
        StringBuilder sb = new StringBuilder();
        sb.append("R_верх: ");
        if (upperResistors.size() > 1) {
            sb.append(upperParallel ? "параллельно" : "последовательно");
        } else {
            sb.append("одиночный");
        }
        sb.append(", R_низ: ");
        if (lowerResistors.size() > 1) {
            sb.append(lowerParallel ? "параллельно" : "последовательно");
        } else {
            sb.append("одиночный");
        }
        return sb.toString();
    }
    
    @Override
    public int compareTo(DividerResult other) {
        // Сортировка: сначала по точности, потом по количеству элементов, потом по энергопотреблению
        int cmp = Double.compare(this.errorPercent, other.errorPercent);
        if (cmp != 0) return cmp;
        
        cmp = Integer.compare(this.getTotalResistorCount(), other.getTotalResistorCount());
        if (cmp != 0) return cmp;
        
        return Double.compare(this.getPowerDissipation(), other.getPowerDissipation());
    }
    
    public String formatResistance(double value) {
        if (value >= 1_000_000) {
            return String.format("%.2f МОм", value / 1_000_000);
        } else if (value >= 1000) {
            return String.format("%.2f кОм", value / 1000);
        } else {
            return String.format("%.2f Ом", value);
        }
    }
    
    public String getUpperResistorsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < upperResistors.size(); i++) {
            if (i > 0) {
                sb.append(upperParallel ? " || " : " + ");
            }
            sb.append(formatResistance(upperResistors.get(i)));
        }
        return sb.toString();
    }
    
    public String getLowerResistorsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lowerResistors.size(); i++) {
            if (i > 0) {
                sb.append(lowerParallel ? " || " : " + ");
            }
            sb.append(formatResistance(lowerResistors.get(i)));
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("Vout=%.4f В (%.3f%%), R_верх=%s, R_низ=%s, %d резисторов",
                vOutActual, errorPercent, getUpperResistorsString(), getLowerResistorsString(), getTotalResistorCount());
    }
}


