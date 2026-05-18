package org.tud.minitimetable.eval.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class Util {

	public static double convertToSeconds(String timeWithUnits) {
		String timeValue = null;
		String unitValue = null;

		timeWithUnits = timeWithUnits.trim();
		if (timeWithUnits.contains(" ")) {
			var split = timeWithUnits.split(" ");
			if (split.length != 2)
				throw new IllegalArgumentException();

			timeValue = split[0];
			unitValue = split[1].toLowerCase();
		} else {
			Pattern pattern = Pattern.compile("(?<time>\\d+(\\.\\d+)?)(\s*(?<unit>\\w+))?");
			var matcher = pattern.matcher(timeWithUnits);
			if (!matcher.find())
				throw new IllegalArgumentException();

			timeValue = matcher.group("time");
			unitValue = matcher.group("unit");
			if (unitValue != null)
				unitValue = unitValue.toLowerCase();
		}

		double rawTimeValue = Double.parseDouble(timeValue);

		double transformed = switch (unitValue) {
		case "ms" -> rawTimeValue / 1000d;
		case "s" -> rawTimeValue;
		case "m" -> rawTimeValue * 60;
		case "h" -> rawTimeValue * 60 * 60;
		default -> throw new IllegalArgumentException("Unexpected value: " + unitValue);
		};

		return transformed;
	}

	public static DecimalFormat getDecimalFormat() {
		DecimalFormatSymbols decimalFormatSymbol = new DecimalFormatSymbols();
		decimalFormatSymbol.setDecimalSeparator('.');
		decimalFormatSymbol.setGroupingSeparator(',');
		DecimalFormat doubleFormatter = new DecimalFormat("#,###.####", decimalFormatSymbol);
		doubleFormatter.setGroupingUsed(true);
		doubleFormatter.setMaximumFractionDigits(4);
		doubleFormatter.setMinimumFractionDigits(4);
		return doubleFormatter;
	}

	public static DecimalFormat getIntegerFormat() {
		DecimalFormatSymbols decimalFormatSymbol = new DecimalFormatSymbols();
		decimalFormatSymbol.setDecimalSeparator('.');
		decimalFormatSymbol.setGroupingSeparator(',');
		DecimalFormat doubleFormatter = new DecimalFormat("#,###", decimalFormatSymbol);
		doubleFormatter.setGroupingUsed(true);
		doubleFormatter.setMaximumFractionDigits(0);
		return doubleFormatter;
	}

	public static String getFileNameTimeStampNow() {
		return getFileNameTimeStamp(LocalDateTime.now());
	}

	public static String getFileNameTimeStamp(LocalDateTime date) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
		return date.format(dateTimeFormatter);
	}

	public static <H> double toDouble(NamedCSV.NamedRecord<H> record, H column) {
		return Double.parseDouble(record.getCell(column));
	}

	public static <H> boolean toBool(NamedCSV.NamedRecord<H> record, H column) {
		return Boolean.parseBoolean(record.getCell(column));
	}

	public static <R extends CSV.CSVRecord> double toDouble(R record, String column) {
		return Double.parseDouble(record.getCell(column));
	}

	public static <R extends CSV.CSVRecord> boolean toBool(R record, String column) {
		return Boolean.parseBoolean(record.getCell(column));
	}

	public static <H> double toDouble(String value) {
		if (value == null)
			return 0;

		if (value.trim().equals("-"))
			return 0;

		if (value.isBlank())
			return 0;

		return Double.parseDouble(value);
	}

}
