package com.arny.core.utils;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class MathUtils {

	public static int fib(int n) {
		if (n <= 1) return n;
		else return fib(n - 1) + fib(n - 2);
	}

	public static double getPercent(double iter, double total) {
		return round((iter / total) * 100, 2);
	}

    public static double getPercVal(double value, double perc) {
        return (perc / 100) * value;
    }


	public static String simpleDoubleFormat(double d) {
		if (d == 0) {
			return "0";
		}
		DecimalFormat df = new DecimalFormat("0");
		df.setMaximumFractionDigits(340);
		return df.format(d).replace(",", ".");
	}

	/**
	 * Format number by digits units,example-ns,mcs,ms,sec:
	 *
	 * @param number      source
	 * @param units       - string array
	 * @param unitDigDiff - digits diff-1000,1024
	 * @param digits      - precision,after zero
	 * @return formatted string
	 */
	public static String formatNumber(double number, String[] units, int unitDigDiff, int digits) {
		if (number <= 0) return "0";
		int digitGroups = (int) (Math.log10(number) / Math.log10(unitDigDiff));
		StringBuilder digs = new StringBuilder();
		for (int i = 0; i < digits; i++) {
			digs.append("#");
		}
		return new DecimalFormat("#,##0." + digs.toString()).format(number
				/ Math.pow(unitDigDiff, digitGroups))
				+ " " + units[digitGroups];
	}

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
		if (Double.compare(val, Double.NaN) == 0 || Double.compare(val, Double.POSITIVE_INFINITY) == 0 || Double.compare(val, Double.NEGATIVE_INFINITY) == 0) {
			return 0;
		}
		return new BigDecimal(val).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static double summ(double num1, double num2, int scale) {
		return new BigDecimal(num1).add(new BigDecimal(num2)).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static double minus(double num1, double num2, int scale) {
		return new BigDecimal(num1).subtract(new BigDecimal(num2)).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static double multiply(double num1, double num2, int scale) {
		return new BigDecimal(num1).multiply(new BigDecimal(num2)).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static double divide(double num1, double num2, int scale) {
		return new BigDecimal(num1).divide(new BigDecimal(num2), BigDecimal.ROUND_HALF_UP).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
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
		return scaled + min;
	}

	public static int randInt(int min, int max) {
		Random rnd = new Random();
		if (min > max) {
			throw new IllegalArgumentException("min>max");
		}
		if (min == max) {
			return min;
		}
		int n = rnd.nextInt();
		n = n == Integer.MIN_VALUE ? 0 : n < 0 ? -n : n;
		n = n % (max - min);
		return min + n;
	}

	public static double Cos(double angle) {
		return Math.cos(Math.toRadians(angle));
	}

	public static double Acos(double rad) {
		return Math.toDegrees(Math.acos(rad));
	}

	public static double Sin(double angle) {
		return Math.sin(Math.toRadians(angle));
	}

	public static double Asin(double rad) {
		return Math.toDegrees(Math.asin(rad));
	}

	public static double Tan(double angle) {
		return Math.tan(Math.toRadians(angle));
	}

	public static double Atan(double rad) {
		return Math.toDegrees(Math.atan(rad));
	}

	public static double Atan2(double rad1, double rad2) {
		return Math.toDegrees(Math.atan2(rad1, rad2));
	}

	public static double Sqrt(double num) {
		return Math.sqrt(num);
	}

	public static double Exp(double num, double exp) {
		return Math.pow(num, exp);
	}

	public static double Abs(double num) {
		return Math.abs(num);
	}

	public static double getDecGrad(int D, double M, double S) {
		double sign = 1.0;
		if (D < 0 || M < 0 || S < 0) {
			sign = -1.0;
		}
		D = Math.abs(D);
		M = Math.abs(M);
		S = Math.abs(S);
		return sign * (D + (M / 60) + (S / 3600));
	}

	/**
	 * add '0' to number before 10
	 *
	 * @param number
	 * @return
	 */
	public static String pad(int number) {
		if (number >= 10) {
			return String.valueOf(number);
		} else {
			return "0" + String.valueOf(number);
		}
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

	@NotNull
	public static ArrayList<Double> fillAverage(double num, int size, ArrayList<Double> nums) {
		if (nums.size() < size) {
			nums.add(num);
		} else {
			nums.remove(0);
		}
		return nums;
	}

}
