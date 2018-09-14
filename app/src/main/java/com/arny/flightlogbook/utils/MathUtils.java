package com.arny.flightlogbook.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class MathUtils {
    /**
     * дробная часть числа
     *
     * @param x
     * @return
     */
    public static double fracal(double x) {
        return x - (int) x;
    }

    /**
     * целая часть числа
     *
     * @param x
     * @return
     */
    public static int intact(double x) {
        return (int) x;
    }

    /**
     * Отстаток от деления
     *
     * @param x
     * @param y
     * @return
     */
    public static double modulo(double x, double y) {
        return y * (fracal(x / y));
    }

    public static double round(double val, int scale) {
        return new BigDecimal(val).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double getAverage(ArrayList<Double> nums) {
        if (nums.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (Double mark : nums) {
            sum += mark;
        }
        return sum / nums.size();
    }

    public static double getAverage(double num, int size, ArrayList<Double> nums) {
        fillAverage(num, size, nums);
        if (nums.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (Double mark : nums) {
            sum += mark;
        }
        return sum / nums.size();
    }

    public static ArrayList<Double> fillAverage(double num, int size, ArrayList<Double> nums) {
        if (nums.size() < size) {
            nums.add(num);
        } else {
            nums.remove(0);
        }
        return nums;
    }

    public static long randLong(long min, long max) {
        Random rnd = new Random();
        if (min > max) {
            throw new IllegalArgumentException("min>max");
        }
        if (min == max) {
            return min;
        }
        long n = rnd.nextLong();
        n = n == Long.MIN_VALUE ? 0 : n < 0 ? -n : n;
        n = n % (max - min);
        return min + n;
    }

    public static double randDouble(double min, double max) {
        Random rnd = new Random();
        double range = max - min;
        double scaled = rnd.nextDouble() * range;
        return scaled + min; // == (rand.nextDouble() * (max-min)) + min;
    }

    public static double summ(double num1, double num2, int scale) {
        return new BigDecimal(num1).add(new BigDecimal(num2)).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double subtract(double num1, double num2, int scale) {
        return new BigDecimal(num1).subtract(new BigDecimal(num2)).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double multiply(double num1, double num2, int scale) {
        return new BigDecimal(num1).multiply(new BigDecimal(num2)).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double divide(double num1, double num2, int scale) {
        if (num2 == 0.0) {
            return 0.0;
        }
        return new BigDecimal(num1).divide(new BigDecimal(num2), BigDecimal.ROUND_HALF_UP).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static int randInt(int min, int max) {
        Random rnd = new Random();
        int range = max - min + 1;
        return rnd.nextInt(range) + min;
    }

    public static double getPercent(double value, double total) {
        return (value / total) * 100;
    }

    /**
     * @param value   number
     * @param pattern example #.##
     * @return string
     */
    public static String formatNumber(Number value, String pattern) {
        if (value == null) {
            return "";
        }
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(value).replace(",", ".");
    }
}
