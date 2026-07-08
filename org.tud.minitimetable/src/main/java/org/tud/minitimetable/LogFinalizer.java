package org.tud.minitimetable;

import static org.tud.minitimetable.DefaultLocations.getResourceDirectory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.tud.minitimetable.eval.util.CSV;
import org.tud.minitimetable.eval.util.CSV.CSVRecord;
import org.tud.minitimetable.eval.util.StatisticsHelper;
import org.tud.minitimetable.eval.util.Util;

public class LogFinalizer {

	private static final String InvalidNumberPlaceholder = "--";

	private static final Function<String, String> zeroInvalid = replaceInvalidNumbers(Util.getDecimalFormat(), 0,
			InvalidNumberPlaceholder);
	private static final Function<String, String> minusOneInvalid = replaceInvalidNumbers(Util.getDecimalFormat(), -1,
			InvalidNumberPlaceholder);

	public static void main(String[] args) throws IOException, ParseException {

		Path outputDirectory = getResourceDirectory().resolve("workstation").resolve("finalized");
		if (!Files.exists(outputDirectory))
			Files.createDirectories(outputDirectory);

		Path refinedDirectory = getResourceDirectory().resolve("workstation").resolve("refined");

		CSV competition = new CSV(Util.getDecimalFormat());
		competition.read(getResourceDirectory().resolve("input").resolve("ihtc-competition.csv"),
				StandardCharsets.UTF_8, ";", true);

		CSV gips = new CSV(Util.getDecimalFormat());
		gips.read(refinedDirectory.resolve("gips-data.csv"), StandardCharsets.UTF_8, ";", true);

		CSV mzAll = new CSV(Util.getDecimalFormat());
		mzAll.read(refinedDirectory.resolve("minizinc-data-all.csv"), StandardCharsets.UTF_8, ";", true);

		CSV mzAllOld = new CSV(Util.getDecimalFormat());
		mzAllOld.read(refinedDirectory.resolve("minizinc-data-all.v1.csv"), StandardCharsets.UTF_8, ";", true);

		// =====================================================================================

		var gipsSizeCsv = buildTable_Gips(gips, outputDirectory, "model-gips-size.csv");

		buildTable_MiniZincCompetitionComparisonSolutions(competition, mzAll, outputDirectory,
				"comparison-mz-competition-solutions.csv");

		var mzSizeCsv = buildTable_MiniZinc(mzAll, outputDirectory, "model-mz-size.csv");

		buildTable_MiniZincTime(mzAll, outputDirectory, "model-mz-time.csv");

		buildTable_MiniZincNaiveComparison(mzAllOld, mzAll, outputDirectory, "comparison-mz-naive.csv");

		buildTable_MiniZincGipsComparisonSolutions(gips, mzAll, outputDirectory, "comparison-mz-gips-solutions.csv");

		buildTable_MiniZincValidatorDiscrepancy(mzAll, outputDirectory, "comparison-mz-validator-solution.csv");

		buildTable_MiniZincGipsSizeComparison(gipsSizeCsv, mzSizeCsv, outputDirectory, "comparison-mz-gips-model.csv");

//		compareGipsSize(gips, mzAll, outputDirectory);
//		compareGipsPresolveSize(gips, mzAll, outputDirectory);
//		compareGipsCompileTime(gips, mzAll, outputDirectory);
//		compareGipsSolutions(gips, mzAll, outputDirectory);

	}

	private static record PairedLookupTable(Set<String> allKeys, Map<String, CSVRecord> left,
			Map<String, CSVRecord> right) {
	}

	private static Map<String, CSVRecord> buildLookup(CSV csv, String byKey) {
		Map<String, CSVRecord> lookup = new HashMap<>();
		csv.stream().forEach(r -> {
			var key = r.getCell(byKey);
			if (lookup.containsKey(key))
				throw new IllegalStateException("Primary Key Duplicate: " + key);
			lookup.put(key, r);
		});
		return lookup;
	}

	private static PairedLookupTable mergePairs(CSV left, CSV right, String byKey) {
		Map<String, CSVRecord> leftLookup = buildLookup(left, byKey);
		Map<String, CSVRecord> rightLookup = buildLookup(right, byKey);

		Set<String> keys = new HashSet<>();
		keys.addAll(leftLookup.keySet());
		keys.addAll(rightLookup.keySet());

		return new PairedLookupTable(keys, leftLookup, rightLookup);
	}

	private static Function<String, String> replaceInvalidNumbers(DecimalFormat format, double mustBeGreaterThan,
			String replacement) {
		return (Function<String, String>) value -> {
			try {
				return isValidNumber(format, mustBeGreaterThan, value) ? value : replacement;
			} catch (ParseException e) {
				e.printStackTrace();
				return replacement;
			}
		};
	}

	private static boolean isValidNumber(DecimalFormat format, double mustBeGreaterThan, Object obj)
			throws ParseException {
		return switch (obj) {
		case null -> false;

		case String s -> {
			if (s.isBlank() || s.trim().equals("--"))
				yield false;

			Number parsedValue = format.parse(s);
			yield Double.compare(parsedValue.doubleValue(), mustBeGreaterThan) > 0;
		}
		case Number n -> {
			yield Double.compare(n.doubleValue(), mustBeGreaterThan) > 0;
		}
		default -> true;
		};
	}

	private static void conditionalSet(CSV csv, int rowIndex, String column, String fromColumn,
			Predicate<String> condition, String onCondition) {
		var target = csv.getCellValue(rowIndex, fromColumn);
		if (condition.test(target))
			csv.setCellValue(rowIndex, column, onCondition);
	}

	private static void setNumberValue(CSV csv, int rowIndex, String column, Map<String, CSVRecord> lookup, String key,
			String fromColumn, Function<String, String> numberHandler) {
		var value = lookup.containsKey(key) ? lookup.get(key).getCell(fromColumn) : null;
		if (numberHandler != null)
			value = numberHandler.apply(value);
		csv.setCellValue(rowIndex, column, value);
	}

	@Deprecated
	private static void setNumberValue(CSV csv, int rowIndex, String column, Map<String, CSVRecord> lookup, String key,
			String fromColumn, String defaultTo) {
		if (lookup.containsKey(key)) {
			var value = lookup.get(key).getCell(fromColumn);
			if (value == null || value.trim().isBlank() || value.trim().equals("-1") || value.trim().equals("-1.00"))
				value = defaultTo;

			csv.setCellValue(rowIndex, column, value);
		} else if (defaultTo != null) {
			csv.setCellValue(rowIndex, column, defaultTo);
		}
	}

	private static void setNumberValue(DecimalFormat format, CSV csv, int rowIndex, String column, Number value,
			String defaultTo) {

		if (value == null || Double.isNaN(value.doubleValue())) {
			if (defaultTo != null)
				csv.setCellValue(rowIndex, column, defaultTo);

			return;
		}

		csv.setCellValue(rowIndex, column, format.format(value));
	}

	private static Number getNumberValue(DecimalFormat format, CSVRecord record, String column,
			Function<String, String> numberHandler) {
		var value = record.getCell(column);
		if (numberHandler != null)
			value = numberHandler.apply(value);

		try {
			return format.parse(value);
		} catch (ParseException e) {
			return null;
		}
	}

	private static Number getNumberValue(DecimalFormat format, CSV csv, int rowIndex, String column,
			Function<String, String> numberHandler) {
		var value = csv.getCellValue(rowIndex, column);
		if (numberHandler != null)
			value = numberHandler.apply(value);

		try {
			return format.parse(value);
		} catch (ParseException e) {
			return null;
		}
	}

	@Deprecated
	private static Number getNumberValue(DecimalFormat format, CSVRecord record, String column) throws ParseException {
		var value = record.getCell(column);
		if (value == null || value.isBlank() || value.equals(InvalidNumberPlaceholder))
			return null;
		Number parsedValue = format.parse(value);
		if (Double.compare(parsedValue.doubleValue(), -1) == 0)
			return null;

		return parsedValue;
	}

	@Deprecated
	private static Number getNumberValue(DecimalFormat format, CSV csv, int rowIndex, String column)
			throws ParseException {
		var value = csv.getCellValue(rowIndex, column);
		if (value == null || value.isBlank() || value.equals(InvalidNumberPlaceholder))
			return null;
		Number parsedValue = format.parse(value);
		if (Double.compare(parsedValue.doubleValue(), -1) == 0)
			return null;

		return parsedValue;
	}

	private static void calculateRatio(DecimalFormat format, CSV csv, int rowIndex, String column, String columnA,
			String columnB) throws ParseException {
		String valueA = csv.getCellValue(rowIndex, columnA);
		String valueB = csv.getCellValue(rowIndex, columnB);
		if (valueA == null || valueA.equals(InvalidNumberPlaceholder) || valueB == null
				|| valueB.equals(InvalidNumberPlaceholder)) {
			csv.setCellValue(rowIndex, column, InvalidNumberPlaceholder);
			return;
		}

		Number nA = format.parse(valueA);
		Number nB = format.parse(valueB);

		var ratio = nA.doubleValue() / nB.doubleValue();
		if (Double.isFinite(ratio)) {
			csv.setCellValue(rowIndex, column, format.format(ratio));
		} else {
			csv.setCellValue(rowIndex, column, InvalidNumberPlaceholder);
		}
	}

	private static void calculateMeanAndStdDevOfRatio(DecimalFormat format, CSV csv, int rowIndex, String columnMean,
			String columnStdDev, String columnRatio, boolean ignoreNull) {

		if (!csv.hasColumnWithName(columnRatio))
			throw new IllegalStateException();

		var ratio = csv.stream() //
				.filter(r -> r.getRowIndex() != rowIndex) //
				.map(r -> r.getCell(columnRatio)) //
				.filter(e -> !(e.equals(InvalidNumberPlaceholder) && ignoreNull)) //
				.map(e -> {
					if (!ignoreNull && e.equals(InvalidNumberPlaceholder))
						return 0d;

					try {
						return format.parse(e);
					} catch (ParseException e1) {
						e1.printStackTrace();
						return null;
					}
				}) //
				.toList();

		var meanAndVariance = StatisticsHelper.calculateMeanAndVariance(ratio, Number::doubleValue, false);
		csv.setCellValue(rowIndex, columnMean, meanAndVariance.mean());
		csv.setCellValue(rowIndex, columnStdDev, meanAndVariance.standardDeviation());
	}

	private static Number calculateCombinedMean(Number valueA, Number valueB) {
		if (valueA == null || valueB == null)
			return null;

		return valueA.doubleValue() + valueB.doubleValue();
	}

	private static Number addNumbers(Number... values) {
		if (values == null || values.length == 0)
			return null;

		double sum = 0d;
		for (var n : values) {
			if (n == null)
				return null;
			sum += n.doubleValue();
		}
		return sum;
	}

	private static Number calculateSubtracteddMean(Number valueA, Number valueB) {
		if (valueA == null || valueB == null)
			return null;

		return valueA.doubleValue() - valueB.doubleValue();
	}

	private static Number calculateNewStdDev(Number valueA, Number valueB) {
		if (valueA == null || valueB == null)
			return null;

		return Math.sqrt(Math.pow(valueA.doubleValue(), 2) + Math.pow(valueB.doubleValue(), 2));
	}

	private static Number calculateNewStdDev(Number valueA, Number... otheValues) {
		if (valueA == null || otheValues == null)
			return null;

		double sum = Math.pow(valueA.doubleValue(), 2);
		for (var n : otheValues)
			sum += Math.pow(n.doubleValue(), 2);
		return Math.sqrt(sum);
	}

	private static void buildTable_MiniZincCompetitionComparisonSolutions(CSV competition, CSV mz, Path outputDirectory,
			String outputFile) throws IOException, ParseException {

		CSV csv = new CSV(Util.getDecimalFormat());
		csv.setColumnNames(new String[] { "Instance", //
				"Model Objective Mean", "Model Objective StdDev", //

				"Model Objective Min", "Model Objective 10m Min", //
				"Official Objective Min", //
				"Objective R", "Objective R Mean", "Objective R StdDev", //

				"Solutions Obtained", "Memory Errors"//
		});

		DecimalFormat format = Util.getDecimalFormat();
		PairedLookupTable merged = mergePairs(competition, mz, "name");

		for (var instance : merged.allKeys.stream().sorted().toList()) {
			var rowIndex = csv.addNewRow();
			csv.setCellValue(rowIndex, "Instance", instance);

			setNumberValue(csv, rowIndex, "Model Objective Min", //
					merged.right, instance, "bestObjective (min)", zeroInvalid);
			setNumberValue(csv, rowIndex, "Model Objective 10m Min", //
					merged.right, instance, "bestObjective10m (min)", zeroInvalid);
			setNumberValue(csv, rowIndex, "Model Objective Mean", //
					merged.right, instance, "bestObjective (m)", zeroInvalid);
			setNumberValue(csv, rowIndex, "Model Objective StdDev", //
					merged.right, instance, "bestObjective (sd)", minusOneInvalid);

			conditionalSet(csv, rowIndex, "Model Objective StdDev", //
					"Model Objective Mean", //
					c -> InvalidNumberPlaceholder.equals(c), InvalidNumberPlaceholder);

			setNumberValue(csv, rowIndex, "Official Objective Min", //
					merged.left, instance, "cost", zeroInvalid);

			setNumberValue(csv, rowIndex, "Solutions Obtained", //
					merged.right, instance, "foundSolution", minusOneInvalid);

			setNumberValue(csv, rowIndex, "Memory Errors", //
					merged.right, instance, "solverCrash", minusOneInvalid);

			calculateRatio(format, csv, rowIndex, "Objective R", "Model Objective Min", "Official Objective Min");
		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Objective R Mean", "Objective R StdDev", "Objective R",
				true);

		csv.write(outputDirectory.resolve(outputFile));
	}

	private static CSV buildTable_MiniZinc(CSV mz, Path outputDirectory, String outputFile)
			throws IOException, ParseException {

		CSV csv = new CSV(Util.getDecimalFormat());
		csv.setColumnNames(new String[] { "Instance", //
				"MemorySize", "Solutions Obtained", "Memory Errors", //
				"Constraints Max", "Variables Max", "NonZeros Max", //

				"Constraints Presolved Mean", "Constraints Presolved StdDev", //
				"Constraints R", "Constraints R Mean", "Constraints R StdDev", //

				"Variables Presolved Mean", "Variables Presolved StdDev", //
				"Variables R", "Variables R Mean", "Variables R StdDev", //

				"NonZeros Presolved Mean", "NonZeros Presolved StdDev", //
				"NonZeros R", "NonZeros R Mean", "NonZeros R StdDev", //

				"Presolve Time Mean", "Presolve Time StdDev", //
				"Preprocessing Time Mean", "Preprocessing Time StdDev", //
				"Build Time Mean", "Build Time StdDev" //
		});

		DecimalFormat format = Util.getDecimalFormat();
		var lookupTable = buildLookup(mz, "name");

		for (var instance : lookupTable.keySet().stream().sorted().toList()) {
			var rowIndex = csv.addNewRow();
			csv.setCellValue(rowIndex, "Instance", instance);

			setNumberValue(csv, rowIndex, "MemorySize", //
					lookupTable, instance, "memoryMB (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Memory Errors", //
					lookupTable, instance, "solverCrash", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Solutions Obtained", //
					lookupTable, instance, "foundSolution", minusOneInvalid);

			setNumberValue(csv, rowIndex, "Constraints Max", //
					lookupTable, instance, "originalConstraints (max)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Variables Max", //
					lookupTable, instance, "originalVariables (max)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "NonZeros Max", //
					lookupTable, instance, "originalCoefficients (max)", minusOneInvalid);

			setNumberValue(csv, rowIndex, "Constraints Presolved Mean", //
					lookupTable, instance, "presolvedConstraints (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Constraints Presolved StdDev", //
					lookupTable, instance, "presolvedConstraints (sd)", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "Constraints R", "Constraints Max", "Constraints Presolved Mean");

			setNumberValue(csv, rowIndex, "Variables Presolved Mean", //
					lookupTable, instance, "presolvedVariables (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Variables Presolved StdDev", //
					lookupTable, instance, "presolvedVariables (sd)", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "Variables R", "Variables Max", "Variables Presolved Mean");

			setNumberValue(csv, rowIndex, "NonZeros Presolved Mean", //
					lookupTable, instance, "presolvedCoefficients (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "NonZeros Presolved StdDev", //
					lookupTable, instance, "presolvedCoefficients (sd)", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "NonZeros R", "NonZeros Max", "NonZeros Presolved Mean");

			setNumberValue(csv, rowIndex, "Presolve Time Mean", //
					lookupTable, instance, "presolveTimeS (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Presolve Time StdDev", //
					lookupTable, instance, "presolveTimeS (sd)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Preprocessing Time Mean", //
					lookupTable, instance, "preprocessingTimeS (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Preprocessing Time StdDev", //
					lookupTable, instance, "preprocessingTimeS (sd)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Build Time Mean", //
					lookupTable, instance, "totalCompileTimeS (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Build Time StdDev", //
					lookupTable, instance, "totalCompileTimeS (sd)", minusOneInvalid);

		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Constraints R Mean", "Constraints R StdDev", //
				"Constraints R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Variables R Mean", "Variables R StdDev", //
				"Variables R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "NonZeros R Mean", "NonZeros R StdDev", //
				"NonZeros R", true);

		csv.write(outputDirectory.resolve(outputFile));
		return csv;
	}

	@Deprecated
	private static void buildTable_MiniZincTime(CSV mz, Path outputDirectory, String outputFile)
			throws IOException, ParseException {

		CSV csv = new CSV(Util.getDecimalFormat());
		csv.setColumnNames(new String[] { "Instance", //
				"Preprocessing Mean", "Preprocessing StdDev", //
				"Compile Mean", "Compile StdDev" //
		});

		DecimalFormat format = Util.getDecimalFormat();
		var lookupTable = buildLookup(mz, "name");

		for (var instance : lookupTable.keySet().stream().sorted().toList()) {
			var rowIndex = csv.addNewRow();
			csv.setCellValue(rowIndex, "Instance", instance);

			setNumberValue(csv, rowIndex, "Preprocessing Mean", //
					lookupTable, instance, "preprocessingTimeS (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Preprocessing StdDev", //
					lookupTable, instance, "preprocessingTimeS (sd)", minusOneInvalid);

			setNumberValue(csv, rowIndex, "Compile Mean", //
					lookupTable, instance, "totalCompileTimeS (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Compile StdDev", //
					lookupTable, instance, "totalCompileTimeS (sd)", minusOneInvalid);
		}

		csv.write(outputDirectory.resolve(outputFile));
	}

	private static void buildTable_MiniZincNaiveComparison(CSV mzAllOld, CSV mzAll, Path outputDirectory,
			String outputFile) throws IOException, ParseException {
		CSV csv = new CSV(Util.getDecimalFormat());
		csv.setColumnNames(new String[] { "Instance", //
				"N Constraints", "C Constraints", //
				"Constraints R", "Constraints R Mean", "Constraints R StdDev", //

				"N Variables", "C Variables", //
				"Variables R", "Variables R Mean", "Variables R StdDev", //

				"N NonZeros", "C NonZeros", //
				"NonZeros R", "NonZeros R Mean", "NonZeros R StdDev", //

				"N Memory", "C Memory", //
				"Memory R", "Memory R Mean", "Memory R StdDev", //

				"N Build Time Mean", "N Build Time StdDev", //
				"C Build Time Mean", "C Build Time StdDev", //
				"Build Time R", "Build Time R Mean", "Build Time R StdDev" //
		});

		DecimalFormat format = Util.getDecimalFormat();
		PairedLookupTable merged = mergePairs(mzAllOld, mzAll, "name");

		Function<String, String> minusOneInvalid = replaceInvalidNumbers(format, -1, InvalidNumberPlaceholder);

		for (var instance : merged.allKeys.stream().sorted().limit(15).toList()) {
			var rowIndex = csv.addNewRow();
			csv.setCellValue(rowIndex, "Instance", instance);

			setNumberValue(csv, rowIndex, "N Constraints", //
					merged.left, instance, "originalConstraints (max)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "C Constraints", //
					merged.right, instance, "originalConstraints (max)", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "Constraints R", //
					"N Constraints", "C Constraints");

			setNumberValue(csv, rowIndex, "N Variables", //
					merged.left, instance, "originalVariables (max)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "C Variables", //
					merged.right, instance, "originalVariables (max)", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "Variables R", //
					"N Variables", "C Variables");

			setNumberValue(csv, rowIndex, "N NonZeros", //
					merged.left, instance, "originalCoefficients (max)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "C NonZeros", //
					merged.right, instance, "originalCoefficients (max)", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "NonZeros R", //
					"N NonZeros", "C NonZeros");

			setNumberValue(csv, rowIndex, "N Memory", //
					merged.left, instance, "memoryMB (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "C Memory", //
					merged.right, instance, "memoryMB (m)", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "Memory R", //
					"N Memory", "C Memory");

			setNumberValue(csv, rowIndex, "N Build Time Mean", //
					merged.left, instance, "totalCompileTimeS (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "C Build Time Mean", //
					merged.right, instance, "totalCompileTimeS (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "N Build Time StdDev", //
					merged.left, instance, "totalCompileTimeS (sd)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "C Build Time StdDev", //
					merged.right, instance, "totalCompileTimeS (sd)", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "Build Time R", //
					"N Build Time Mean", "C Build Time Mean");
		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Constraints R Mean", "Constraints R StdDev", //
				"Constraints R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Variables R Mean", "Variables R StdDev", //
				"Variables R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "NonZeros R Mean", "NonZeros R StdDev", //
				"NonZeros R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Memory R Mean", "Memory R StdDev", //
				"Memory R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Build Time R Mean", "Build Time R StdDev", //
				"Build Time R", true);

		csv.write(outputDirectory.resolve(outputFile));
	}

	private static void buildTable_MiniZincGipsComparisonSolutions(CSV gips, CSV mzAll, Path outputDirectory,
			String outputFile) throws IOException, ParseException {

		CSV csv = new CSV(Util.getDecimalFormat());
		csv.setColumnNames(new String[] { "Instance", //
				"GIPS Best Objective Mean", "GIPS Best Objective StdDev", //
				"MZ Best Objective Mean", "MZ Best Objective StdDev", //
				"Best Objective R", "Best Objective R Mean", "Best Objective R StdDev", //

				"GIPS Best Bound Mean", "GIPS Best Bound StdDev", //
				"MZ Best Bound Mean", "MZ Best Bound StdDev", //

				"GIPS Gap", //
				"MZ Gap", //
				"Gap R", "Gap R Mean", "Gap R StdDev", //

				"GIPS Solutions Mean", "GIPS Solutions StdDev", //
				"MZ Solutions Mean", "MZ Solutions StdDev", //
				"Solutions R", "Solutions R Mean", "Solutions R StdDev" //
		});

		DecimalFormat percentage = Util.getDecimalFormat();
		percentage.setMaximumFractionDigits(2);

		DecimalFormat format = Util.getDecimalFormat();
		PairedLookupTable merged = mergePairs(gips, mzAll, "name");
		Function<String, String> zeroInvalid = replaceInvalidNumbers(format, 0, InvalidNumberPlaceholder);
		Function<String, String> minusOneInvalid = replaceInvalidNumbers(format, -1, InvalidNumberPlaceholder);

		for (var instance : merged.allKeys.stream().sorted().toList()) {
			var rowIndex = csv.addNewRow();
			csv.setCellValue(rowIndex, "Instance", instance);

			setNumberValue(csv, rowIndex, "GIPS Best Objective Mean", //
					merged.left, instance, "gurobi best objective mean", zeroInvalid);
			setNumberValue(csv, rowIndex, "GIPS Best Objective StdDev", //
					merged.left, instance, "gurobi best objective stddev", zeroInvalid);
			setNumberValue(csv, rowIndex, "MZ Best Objective Mean", //
					merged.right, instance, "bestObjective (m)", zeroInvalid);
			setNumberValue(csv, rowIndex, "MZ Best Objective StdDev", //
					merged.right, instance, "bestObjective (sd)", zeroInvalid);
			calculateRatio(format, csv, rowIndex, "Best Objective R", //
					"GIPS Best Objective Mean", "MZ Best Objective Mean");

			setNumberValue(csv, rowIndex, "GIPS Best Bound Mean", //
					merged.left, instance, "gurobi best bound mean", zeroInvalid);
			setNumberValue(csv, rowIndex, "GIPS Best Bound StdDev", //
					merged.left, instance, "gurobi best bound stddev", zeroInvalid);
			setNumberValue(csv, rowIndex, "MZ Best Bound Mean", //
					merged.right, instance, "bestBound (m)", zeroInvalid);
			setNumberValue(csv, rowIndex, "MZ Best Bound StdDev", //
					merged.right, instance, "bestBound (sd)", zeroInvalid);

			var varGipsGap = getNumberValue(format, merged.left.get(instance), //
					"gurobi mip mean", minusOneInvalid);
			var varMZGap = getNumberValue(format, merged.right.get(instance), //
					"mipGap (m)", minusOneInvalid);

			if (varGipsGap != null && varGipsGap.doubleValue() >= 100d)
				varGipsGap = null;
			if (varMZGap != null && varMZGap.doubleValue() >= 100d)
				varMZGap = null;

			if (varGipsGap != null) {
				csv.setCellValue(rowIndex, "GIPS Gap", percentage.format(varGipsGap) + "\\%");
			} else {
				csv.setCellValue(rowIndex, "GIPS Gap", InvalidNumberPlaceholder);
			}

			if (varMZGap != null) {
				csv.setCellValue(rowIndex, "MZ Gap", percentage.format(varMZGap) + "\\%");
			} else {
				csv.setCellValue(rowIndex, "MZ Gap", InvalidNumberPlaceholder);
			}

			conditionalSet(csv, rowIndex, "GIPS Gap", //
					"GIPS Best Objective Mean", //
					c -> InvalidNumberPlaceholder.equals(c), InvalidNumberPlaceholder);
			conditionalSet(csv, rowIndex, "MZ Gap", //
					"MZ Best Objective Mean", //
					c -> InvalidNumberPlaceholder.equals(c), InvalidNumberPlaceholder);

			if (varGipsGap != null && varMZGap != null) {
				var ratio = varGipsGap.doubleValue() / varMZGap.doubleValue();
				if (Double.isFinite(ratio)) {
					csv.setCellValue(rowIndex, "Gap R", format.format(ratio));
				} else {
					csv.setCellValue(rowIndex, "Gap R", InvalidNumberPlaceholder);
				}
			} else {
				csv.setCellValue(rowIndex, "Gap R", InvalidNumberPlaceholder);
			}

			setNumberValue(csv, rowIndex, "GIPS Solutions Mean", //
					merged.left, instance, "gurobi solution count mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "GIPS Solutions StdDev", //
					merged.left, instance, "gurobi solution count stddev", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Solutions Mean", //
					merged.right, instance, "numberOfSolutions (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Solutions StdDev", //
					merged.right, instance, "numberOfSolutions (sd)", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "Solutions R", //
					"GIPS Solutions Mean", "MZ Solutions Mean");

		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Best Objective R Mean", "Best Objective R StdDev", //
				"Best Objective R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Gap R Mean", "Gap R StdDev", //
				"Gap R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Solutions R Mean", "Solutions R StdDev", //
				"Solutions R", true);

		csv.write(outputDirectory.resolve(outputFile));
	}

	private static void buildTable_MiniZincValidatorDiscrepancy(CSV mz, Path outputDirectory, String outputFile)
			throws IOException, ParseException {
		CSV csv = new CSV(Util.getDecimalFormat());
		csv.setColumnNames(new String[] { "Instance", //
				"Model Objective Mean", "Model Objective StdDev", //
				"Validator Objective Mean", "Validator Objective StdDev", //
				"Objective R", "Objective R Mean", "Objective R StdDev", //
				"Solutions Mean", "Solutions StdDev" //
		});

		DecimalFormat format = Util.getDecimalFormat();
		var lookupTable = buildLookup(mz, "name");
		Function<String, String> zeroInvalid = replaceInvalidNumbers(format, 0, InvalidNumberPlaceholder);
		Function<String, String> minusOneInvalid = replaceInvalidNumbers(format, -1, InvalidNumberPlaceholder);

		for (var instance : lookupTable.keySet().stream().sorted().toList()) {
			var rowIndex = csv.addNewRow();
			csv.setCellValue(rowIndex, "Instance", instance);

			setNumberValue(csv, rowIndex, "Model Objective Mean", //
					lookupTable, instance, "bestObjective (m)", zeroInvalid);
			setNumberValue(csv, rowIndex, "Model Objective StdDev", //
					lookupTable, instance, "bestObjective (sd)", zeroInvalid);
			setNumberValue(csv, rowIndex, "Validator Objective Mean", //
					lookupTable, instance, "objectiveByValidator (m)", zeroInvalid);
			setNumberValue(csv, rowIndex, "Validator Objective StdDev", //
					lookupTable, instance, "objectiveByValidator (sd)", zeroInvalid);
			calculateRatio(format, csv, rowIndex, "Objective R", "Model Objective Mean", "Validator Objective Mean");

			setNumberValue(csv, rowIndex, "Solutions Mean", //
					lookupTable, instance, "numberOfSolutions (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Solutions StdDev", //
					lookupTable, instance, "numberOfSolutions (sd)", minusOneInvalid);
		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Objective R Mean", "Objective R StdDev", "Objective R",
				true);

		csv.write(outputDirectory.resolve(outputFile));
	}

	@Deprecated
	private static void compareGipsCompileTime(CSV gips, CSV mz, Path outputDirectory)
			throws IOException, ParseException {

		CSV csv = new CSV(Util.getDecimalFormat());
		csv.setColumnNames(new String[] { "Instance", //
				"GIPS Preproc Mean", "GIPS Preproc StdDev", "MZ Preproc Mean", "MZ Preproc StdDev", //
				"Preproc R", "Preproc Mean", "Preproc StdDev", //
				"GIPS Build Mean", "GIPS Build StdDev", "MZ Build Mean", "MZ Build StdDev", //
				"Build R", "Build Mean", "Build StdDev" //
		});

		DecimalFormat format = Util.getDecimalFormat();
		PairedLookupTable merged = mergePairs(gips, mz, "name");

		for (var instance : merged.allKeys.stream().sorted().toList()) {
			var rowIndex = csv.addNewRow();
			csv.setCellValue(rowIndex, "Instance", instance);

			setNumberValue(csv, rowIndex, "GIPS Preproc Mean", //
					merged.left, instance, "observer runtime preproc mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "GIPS Preproc StdDev", //
					merged.left, instance, "observer runtime preproc stddev", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Preproc Mean", //
					merged.right, instance, "preprocessingTimeS (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Preproc StdDev", //
					merged.right, instance, "preprocessingTimeS (sd)", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Preproc R", "GIPS Preproc Mean", "MZ Preproc Mean");

			var gipsGipsBuildM = getNumberValue(format, gips, rowIndex, "observer runtime build mean");
			setNumberValue(format, csv, rowIndex, "GIPS Build Mean", //
					gipsGipsBuildM, InvalidNumberPlaceholder);

			var gipsGipsBuildSd = getNumberValue(format, gips, rowIndex, "observer runtime build stddev");
			setNumberValue(format, csv, rowIndex, "GIPS Build StdDev", //
					gipsGipsBuildSd, InvalidNumberPlaceholder);

			setNumberValue(csv, rowIndex, "MZ Build Mean", //
					merged.right, instance, "totalCompileTimeS (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Build StdDev", //
					merged.right, instance, "totalCompileTimeS (sd)", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Build R", "GIPS Build Mean", "MZ Build Mean");

		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Preproc Mean", "Preproc StdDev", "Preproc R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Build Mean", "Build StdDev", "Build R", true);

		csv.write(outputDirectory.resolve("compare-gips-mz-compiletime.csv"));
	}

	@Deprecated
	private static void compareGipsSize(CSV gips, CSV mz, Path outputDirectory) throws IOException, ParseException {

		CSV csv = new CSV(Util.getDecimalFormat());
		csv.setColumnNames(new String[] { "Instance", //
				"GIPS Constraints", "MZ Constraints", "Constraints R", "Constraints Mean", "Constraints StdDev", //
				"GIPS Variables", "MZ Variables", "Variables R", "Variables Mean", "Variables StdDev", //
				"GIPS Coef", "MZ Coef", "Coef R", "Coef Mean", "Coef StdDev" //
		});

		DecimalFormat format = Util.getDecimalFormat();
		PairedLookupTable merged = mergePairs(gips, mz, "name");

		for (var instance : merged.allKeys.stream().sorted().toList()) {
			var rowIndex = csv.addNewRow();
			csv.setCellValue(rowIndex, "Instance", instance);

			setNumberValue(csv, rowIndex, "GIPS Constraints", merged.left, instance, "gurobi model rows mean",
					InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Constraints", merged.right, instance, "originalConstraints (m)",
					InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Constraints R", "GIPS Constraints", "MZ Constraints");

			setNumberValue(csv, rowIndex, "GIPS Variables", merged.left, instance, "gurobi model cols mean",
					InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Variables", merged.right, instance, "originalVariables (m)",
					InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Variables R", "GIPS Variables", "MZ Variables");

			setNumberValue(csv, rowIndex, "GIPS Coef", merged.left, instance, "gurobi model nonzeros mean",
					InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Coef", merged.right, instance, "originalCoefficients (m)",
					InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Coef R", "GIPS Coef", "MZ Coef");

		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Constraints Mean", "Constraints StdDev", "Constraints R",
				true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Variables Mean", "Variables StdDev", "Variables R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Coef Mean", "Coef StdDev", "Coef R", true);

		csv.write(outputDirectory.resolve("compare-gips-mz-size.csv"));
	}

	@Deprecated
	private static CSV buildTable_MiniZincModelSize(CSV mz, Path outputDirectory, String outputFile)
			throws IOException, ParseException {

		CSV csv = new CSV(Util.getDecimalFormat());
		csv.setColumnNames(new String[] { "Instance", //
				"Megabyte Mean", "Megabyte StdDev", //

				"MZ Start Constraints Mean", "MZ Start Constraints StdDev", //
				"MZ Preso Constraints Mean", "MZ Preso Constraints StdDev", //
				"Constraints R", "Constraints Mean", "Constraints StdDev", //

				"MZ Start Variables Mean", "MZ Start Variables StdDev", //
				"MZ Preso Variables Mean", "MZ Preso Variables StdDev", //
				"Variables R", "Variables Mean", "Variables StdDev", //

				"MZ Start Coef Mean", "MZ Start Coef StdDev", //
				"MZ Preso Coef Mean", "MZ Preso Coef StdDev", //
				"Coef R", "Coef Mean", "Coef StdDev", //
				"MZ Presolve Time Mean", "MZ Presolve Time StdDev", //
		});

		DecimalFormat format = Util.getDecimalFormat();
		var lookupTable = buildLookup(mz, "name");

		for (var instance : lookupTable.keySet().stream().sorted().toList()) {
			var rowIndex = csv.addNewRow();
			csv.setCellValue(rowIndex, "Instance", instance);

			setNumberValue(csv, rowIndex, "MZ Start Constraints Mean", //
					lookupTable, instance, "originalConstraints (m)", InvalidNumberPlaceholder);
//			setNumberValue(csv, rowIndex, "MZ Start Constraints StdDev", //
//					lookupTable, instance, "originalConstraints (sd)", InvalidNumberPlaceholder);
			csv.setCellValue(rowIndex, "MZ Start Constraints StdDev", "0.00");
			setNumberValue(csv, rowIndex, "MZ Preso Constraints Mean", //
					lookupTable, instance, "presolvedConstraints (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Preso Constraints StdDev", //
					lookupTable, instance, "presolvedConstraints (sd)", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Constraints R", //
					"MZ Start Constraints Mean", "MZ Preso Constraints Mean");

			setNumberValue(csv, rowIndex, "MZ Start Variables Mean", //
					lookupTable, instance, "originalVariables (m)", InvalidNumberPlaceholder);
//			setNumberValue(csv, rowIndex, "MZ Start Variables StdDev", //
//					lookupTable, instance, "originalVariables (sd)", InvalidNumberPlaceholder);
			csv.setCellValue(rowIndex, "MZ Start Variables StdDev", "0.00");
			setNumberValue(csv, rowIndex, "MZ Preso Variables Mean", //
					lookupTable, instance, "presolvedVariables (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Preso Variables StdDev", //
					lookupTable, instance, "presolvedVariables (sd)", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Variables R", //
					"MZ Start Variables Mean", "MZ Preso Variables Mean");

			setNumberValue(csv, rowIndex, "MZ Start Coef Mean", //
					lookupTable, instance, "originalCoefficients (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Start Coef StdDev", //
					lookupTable, instance, "originalCoefficients (sd)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Preso Coef Mean", //
					lookupTable, instance, "presolvedCoefficients (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Preso Coef StdDev", //
					lookupTable, instance, "presolvedCoefficients (sd)", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Coef R", //
					"MZ Start Coef Mean", "MZ Preso Coef Mean");

			setNumberValue(csv, rowIndex, "MZ Presolve Time Mean", //
					lookupTable, instance, "presolveTimeS (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Presolve Time StdDev", //
					lookupTable, instance, "presolveTimeS (sd)", InvalidNumberPlaceholder);

		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Constraints Mean", "Constraints StdDev", //
				"Constraints R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Variables Mean", "Variables StdDev", //
				"Variables R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Coef Mean", "Coef StdDev", //
				"Coef R", true);

		csv.write(outputDirectory.resolve(outputFile));
		return csv;
	}

	private static CSV buildTable_Gips(CSV gips, Path outputDirectory, String outputFile)
			throws IOException, ParseException {

		CSV csv = new CSV(Util.getDecimalFormat());
		csv.setColumnNames(new String[] { "Instance", //
				"Memory Errors", "Memory Error Build", //

				"Constraints Mean", "Constraints StdDev", //
				"Constraints Presolved Mean", "Constraints Presolved StdDev", //
				"Constraints R", "Constraints R Mean", "Constraints R StdDev", //

				"Variables Mean", "Variables StdDev", //
				"Variables Presolved Mean", "Variables Presolved StdDev", //
				"Variables R", "Variables R Mean", "Variables R StdDev", //

				"NonZeros Mean", "NonZeros StdDev", //
				"NonZeros Presolved Mean", "NonZeros Presolved StdDev", //
				"NonZeros R", "NonZeros R Mean", "NonZeros R StdDev", //

				"Presolve Time Mean", "Presolve Time StdDev", //
				"Preprocessing Time Mean", "Preprocessing Time StdDev", //
				"Build Time Mean", "Build Time StdDev" //
		});

		DecimalFormat format = Util.getDecimalFormat();
		var lookupTable = buildLookup(gips, "name");

		for (var instance : lookupTable.keySet().stream().sorted().toList()) {
			var rowIndex = csv.addNewRow();
			csv.setCellValue(rowIndex, "Instance", instance);

			var startConstraintsM = getNumberValue(format, lookupTable.get(instance), //
					"gurobi model rows mean", minusOneInvalid);
			var startConstraintsSd = getNumberValue(format, lookupTable.get(instance), //
					"gurobi model rows stddev", minusOneInvalid);
			var removedConstraintsM = getNumberValue(format, lookupTable.get(instance), //
					"gurobi presolve removed rows mean", minusOneInvalid);
			var removedConstraintsSd = getNumberValue(format, lookupTable.get(instance), //
					"gurobi presolve removed rows stddev", minusOneInvalid);

			setNumberValue(format, csv, rowIndex, "Constraints Mean", //
					startConstraintsM, InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "Constraints StdDev", //
					startConstraintsSd, InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "Constraints Presolved Mean", //
					calculateSubtracteddMean(startConstraintsM, removedConstraintsM), InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "Constraints Presolved StdDev", //
					calculateNewStdDev(startConstraintsSd, removedConstraintsSd), InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Constraints R", //
					"Constraints Mean", "Constraints Presolved Mean");

			var startVariablesM = getNumberValue(format, lookupTable.get(instance), //
					"gurobi model cols mean", minusOneInvalid);
			var startVariablesSd = getNumberValue(format, lookupTable.get(instance), //
					"gurobi model cols stddev", minusOneInvalid);
			var removedVariablesM = getNumberValue(format, lookupTable.get(instance), //
					"gurobi presolve removed cols mean", minusOneInvalid);
			var removedVariablesSd = getNumberValue(format, lookupTable.get(instance), //
					"gurobi presolve removed cols stddev", minusOneInvalid);
			setNumberValue(format, csv, rowIndex, "Variables Mean", //
					startVariablesM, InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "Variables StdDev", //
					startVariablesSd, InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "Variables Presolved Mean", //
					calculateSubtracteddMean(startVariablesM, removedVariablesM), InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "Variables Presolved StdDev", //
					calculateNewStdDev(startVariablesSd, removedVariablesSd), InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Variables R", //
					"Variables Mean", "Variables Presolved Mean");

			var startNonZerosM = getNumberValue(format, lookupTable.get(instance), //
					"gurobi model nonzeros mean", minusOneInvalid);
			var startNonZerosSd = getNumberValue(format, lookupTable.get(instance), //
					"gurobi model nonzeros stddev", minusOneInvalid);
			var removedNonZerosM = getNumberValue(format, lookupTable.get(instance),
					"gurobi presolve removed nonzeros mean", minusOneInvalid);
			var removedNonZerosSd = getNumberValue(format, lookupTable.get(instance), //
					"gurobi presolve removed nonzeros stddev", minusOneInvalid);
			setNumberValue(format, csv, rowIndex, "NonZeros Mean", //
					startNonZerosM, InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "NonZeros StdDev", //
					startNonZerosSd, InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "NonZeros Presolved Mean", //
					calculateSubtracteddMean(startNonZerosM, removedNonZerosM), InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "NonZeros Presolved StdDev", //
					calculateNewStdDev(startNonZerosSd, removedNonZerosSd), InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "NonZeros R", //
					"NonZeros Mean", "NonZeros Presolved Mean");

			setNumberValue(csv, rowIndex, "Presolve Time Mean", //
					lookupTable, instance, "gurobi presolve runtime mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Presolve Time StdDev", //
					lookupTable, instance, "gurobi presolve runtime stddev", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Preprocessing Time Mean", //
					lookupTable, instance, "observer runtime preproc mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Preprocessing Time StdDev", //
					lookupTable, instance, "observer runtime preproc stddev", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Build Time Mean", //
					lookupTable, instance, "observer runtime build mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "Build Time StdDev", //
					lookupTable, instance, "observer runtime build stddev", minusOneInvalid);

			var status = lookupTable.get(instance).getCell("gurobi status");
			{
				var split = status.split(",");
				var countOutOfMemory = Arrays.stream(split).filter("OUT_OF_MEMORY"::equals).count();
				var countTimelimit = Arrays.stream(split).filter("TIMELIMIT"::equals).count();

				setNumberValue(format, csv, rowIndex, "Memory Errors", //
						countOutOfMemory, InvalidNumberPlaceholder);

				var buildRelatedMemoryErrors = 0l;
				if (countOutOfMemory > 0) {
					var maybe = getNumberValue(format, lookupTable.get(instance), //
							"gurobi model rows mean", minusOneInvalid);

					if (maybe == null) {
						// presolve never run, ever
						buildRelatedMemoryErrors = countOutOfMemory;
					}
				}

				setNumberValue(format, csv, rowIndex, "Memory Error Build", //
						buildRelatedMemoryErrors, InvalidNumberPlaceholder);
			}

		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Constraints R Mean", "Constraints R StdDev", //
				"Constraints R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Variables R Mean", "Variables R StdDev", //
				"Variables R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "NonZeros R Mean", "NonZeros R StdDev", //
				"NonZeros R", true);

		csv.write(outputDirectory.resolve(outputFile));
		return csv;
	}

	private static void buildTable_MiniZincGipsSizeComparison(CSV gips, CSV mz, Path outputDirectory, String outputFile)
			throws IOException, ParseException {

		CSV csv = new CSV(Util.getDecimalFormat());
		csv.setColumnNames(new String[] { "Instance", //

				"GIPS Preprocessing Time Mean", "GIPS Preprocessing Time StdDev", //
				"MZ Preprocessing Time Mean", "MZ Preprocessing Time StdDev", //
				"Preprocessing Time R", "Preprocessing Time R Mean", "Preprocessing Time R StdDev", //

				"GIPS Build Time Mean", "GIPS Build Time StdDev", //
				"MZ Build Time Mean", "MZ Build Time StdDev", //
				"Build Time R", "Build Time R Mean", "Build Time R StdDev", //

				"GIPS Constraints", //
				"MZ Constraints", //
				"Constraints R", "Constraints R Mean", "Constraints R StdDev", //

				"GIPS Variables", //
				"MZ Variables", //
				"Variables R", "Variables R Mean", "Variables R StdDev", //

				"GIPS NonZeros", //
				"MZ NonZeros", //
				"NonZeros R", "NonZeros R Mean", "NonZeros R StdDev", //

				"GIPS Presolve Time Mean", "GIPS Presolve Time StdDev", //
				"MZ Presolve Time Mean", "MZ Presolve Time StdDev", //
				"Presolve Time R", "Presolve Time R Mean", "Presolve Time R StdDev", //

				"GIPS Constraints Presolved Mean", "GIPS Constraints Presolved StdDev", //
				"MZ Constraints Presolved Mean", "MZ Constraints Presolved StdDev", //
				"Constraints Presolved R", "Constraints Presolved R Mean", "Constraints Presolved R StdDev", //

				"GIPS Variables Presolved Mean", "GIPS Variables Presolved StdDev", //
				"MZ Variables Presolved Mean", "MZ Variables Presolved StdDev", //
				"Variables Presolved R", "Variables Presolved R Mean", "Variables Presolved R StdDev", //

				"GIPS NonZeros Presolved Mean", "GIPS NonZeros Presolved StdDev", //
				"MZ NonZeros Presolved Mean", "MZ NonZeros Presolved StdDev", //
				"NonZeros Presolved R", "NonZeros Presolved R Mean", "NonZeros Presolved R StdDev", //

		});

		DecimalFormat format = Util.getDecimalFormat();
		PairedLookupTable merged = mergePairs(gips, mz, "Instance");

		for (var instance : merged.allKeys.stream().sorted().filter(e -> !e.equals("final")).toList()) {
			var rowIndex = csv.addNewRow();
			csv.setCellValue(rowIndex, "Instance", instance);

			setNumberValue(csv, rowIndex, "GIPS Preprocessing Time Mean", //
					merged.left, instance, "Preprocessing Time Mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "GIPS Preprocessing Time StdDev", //
					merged.left, instance, "Preprocessing Time StdDev", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Preprocessing Time Mean", //
					merged.right, instance, "Preprocessing Time Mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Preprocessing Time StdDev", //
					merged.right, instance, "Preprocessing Time StdDev", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "Preprocessing Time R", //
					"GIPS Preprocessing Time Mean", "MZ Preprocessing Time Mean");

			setNumberValue(csv, rowIndex, "GIPS Build Time Mean", //
					merged.left, instance, "Build Time Mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "GIPS Build Time StdDev", //
					merged.left, instance, "Build Time StdDev", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Build Time Mean", //
					merged.right, instance, "Build Time Mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Build Time StdDev", //
					merged.right, instance, "Build Time StdDev", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "Build Time R", //
					"GIPS Build Time Mean", "MZ Build Time Mean");

			setNumberValue(csv, rowIndex, "GIPS Constraints", //
					merged.left, instance, "Constraints Mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Constraints", //
					merged.right, instance, "Constraints Max", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "Constraints R", //
					"GIPS Constraints", "MZ Constraints");

			setNumberValue(csv, rowIndex, "GIPS Variables", //
					merged.left, instance, "Variables Mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Variables", //
					merged.right, instance, "Variables Max", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "Variables R", //
					"GIPS Variables", "MZ Variables");

			setNumberValue(csv, rowIndex, "GIPS NonZeros", //
					merged.left, instance, "NonZeros Mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ NonZeros", //
					merged.right, instance, "NonZeros Max", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "NonZeros R", //
					"GIPS NonZeros", "MZ NonZeros");

			setNumberValue(csv, rowIndex, "GIPS Presolve Time Mean", //
					merged.left, instance, "Presolve Time Mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "GIPS Presolve Time StdDev", //
					merged.left, instance, "Presolve Time StdDev", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Presolve Time Mean", //
					merged.right, instance, "Presolve Time Mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Presolve Time StdDev", //
					merged.right, instance, "Presolve Time StdDev", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "Presolve Time R", //
					"GIPS Presolve Time Mean", "MZ Presolve Time Mean");

			setNumberValue(csv, rowIndex, "GIPS Constraints Presolved Mean", //
					merged.left, instance, "Constraints Presolved Mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "GIPS Constraints Presolved StdDev", //
					merged.left, instance, "Constraints Presolved StdDev", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Constraints Presolved Mean", //
					merged.right, instance, "Constraints Presolved Mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Constraints Presolved StdDev", //
					merged.right, instance, "Constraints Presolved StdDev", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "Constraints Presolved R", //
					"GIPS Constraints Presolved Mean", "MZ Constraints Presolved Mean");

			setNumberValue(csv, rowIndex, "GIPS Variables Presolved Mean", //
					merged.left, instance, "Variables Presolved Mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "GIPS Variables Presolved StdDev", //
					merged.left, instance, "Variables Presolved StdDev", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Variables Presolved Mean", //
					merged.right, instance, "Variables Presolved Mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Variables Presolved StdDev", //
					merged.right, instance, "Variables Presolved StdDev", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "Variables Presolved R", //
					"GIPS Variables Presolved Mean", "MZ Variables Presolved Mean");

			setNumberValue(csv, rowIndex, "GIPS NonZeros Presolved Mean", //
					merged.left, instance, "NonZeros Presolved Mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "GIPS NonZeros Presolved StdDev", //
					merged.left, instance, "NonZeros Presolved StdDev", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ NonZeros Presolved Mean", //
					merged.right, instance, "NonZeros Presolved Mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ NonZeros Presolved StdDev", //
					merged.right, instance, "NonZeros Presolved StdDev", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "NonZeros Presolved R", //
					"GIPS NonZeros Presolved Mean", "MZ NonZeros Presolved Mean");

		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, //
				"Preprocessing Time R Mean", "Preprocessing Time R StdDev", //
				"Preprocessing Time R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, //
				"Build Time R Mean", "Build Time R StdDev", //
				"Build Time R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, //
				"Constraints R Mean", "Constraints R StdDev", //
				"Constraints R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, //
				"Variables R Mean", "Variables R StdDev", //
				"Variables R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, //
				"NonZeros R Mean", "NonZeros R StdDev", //
				"NonZeros R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, //
				"Presolve Time R Mean", "Presolve Time R StdDev", //
				"Presolve Time R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, //
				"Constraints Presolved R Mean", "Constraints Presolved R StdDev", //
				"Constraints Presolved R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, //
				"Variables Presolved R Mean", "Variables Presolved R StdDev", //
				"Variables Presolved R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, //
				"NonZeros Presolved R Mean", "NonZeros Presolved R StdDev", //
				"NonZeros Presolved R", true);

		csv.write(outputDirectory.resolve(outputFile));
	}

	@Deprecated
	private static void compareGipsPresolveSize(CSV gips, CSV mz, Path outputDirectory)
			throws IOException, ParseException {

		CSV csv = new CSV(Util.getDecimalFormat());
		csv.setColumnNames(new String[] { "Instance", //
				"GIPS Constraints Mean", "GIPS Constraints StdDev", //
				"MZ Constraints Mean", "MZ Constraints StdDev", //
				"Constraints R", "Constraints Mean", "Constraints StdDev", //
				"GIPS Variables Mean", "GIPS Variables StdDev", //
				"MZ Variables Mean", "MZ Variables StdDev", //
				"Variables R", "Variables Mean", "Variables StdDev", //
				"GIPS Coef Mean", "GIPS Coef StdDev", //
				"MZ Coef Mean", "MZ Coef StdDev", //
				"Coef R", "Coef Mean", "Coef StdDev", //
				"GIPS Runtime Mean", "GIPS Runtime StdDev", //
				"MZ Runtime Mean", "MZ Runtime StdDev", //
				"Runtime R", "Runtime Mean", "Runtime StdDev" //
		});

		DecimalFormat format = Util.getDecimalFormat();
		PairedLookupTable merged = mergePairs(gips, mz, "name");

		for (var instance : merged.allKeys.stream().sorted().toList()) {
			var rowIndex = csv.addNewRow();
			csv.setCellValue(rowIndex, "Instance", instance);

			setNumberValue(csv, rowIndex, "GIPS Constraints Mean", //
					merged.left, instance, "gurobi presolve removed rows mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "GIPS Constraints StdDev", //
					merged.left, instance, "gurobi presolve removed rows stddev", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Constraints Mean", //
					merged.right, instance, "presolvedConstraints (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Constraints StdDev", //
					merged.right, instance, "presolvedConstraints (sd)", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Constraints R", //
					"GIPS Constraints Mean", "MZ Constraints Mean");

			setNumberValue(csv, rowIndex, "GIPS Variables Mean", //
					merged.left, instance, "gurobi presolve removed cols mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "GIPS Variables StdDev", //
					merged.left, instance, "gurobi presolve removed cols stddev", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Variables Mean", //
					merged.right, instance, "presolvedVariables (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Variables StdDev", //
					merged.right, instance, "presolvedVariables (sd)", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Variables R", //
					"GIPS Variables Mean", "MZ Variables Mean");

//			setNumberValue(csv, rowIndex, "GIPS Coef Mean", //
//					merged.gips, instance, "gurobi presolve removed nonzeros mean", InvalidNumberPlaceholder);
			csv.setCellValue(rowIndex, "GIPS Coef Mean", "--");
//			setNumberValue(csv, rowIndex, "GIPS Coef StdDev", //
//			merged.gips, instance, "gurobi presolve removed nonzeros stddev", InvalidNumberPlaceholder);
			csv.setCellValue(rowIndex, "GIPS Coef StdDev", "--");
			setNumberValue(csv, rowIndex, "MZ Coef Mean", //
					merged.right, instance, "presolvedCoefficients (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Coef StdDev", //
					merged.right, instance, "presolvedCoefficients (sd)", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Coef R", //
					"GIPS Coef Mean", "MZ Coef Mean");

			setNumberValue(csv, rowIndex, "GIPS Runtime Mean", //
					merged.left, instance, "gurobi presolve runtime mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "GIPS Runtime StdDev", //
					merged.left, instance, "gurobi presolve runtime stddev", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Runtime Mean", //
					merged.right, instance, "presolveTimeS (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Runtime StdDev", //
					merged.right, instance, "presolveTimeS (sd)", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Runtime R", //
					"GIPS Runtime Mean", "MZ Runtime Mean");

		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Constraints Mean", "Constraints StdDev", //
				"Constraints R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Variables Mean", "Variables StdDev", //
				"Variables R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Coef Mean", "Coef StdDev", //
				"Coef R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Runtime Mean", "Runtime StdDev", //
				"Runtime R", true);

		csv.write(outputDirectory.resolve("compare-gips-mz-presolve-size.csv"));
	}

	@Deprecated
	private static void compareGipsSolutions(CSV gips, CSV mz, Path outputDirectory)
			throws IOException, ParseException {

		CSV csv = new CSV(Util.getDecimalFormat());
		csv.setColumnNames(new String[] { "Instance", //
				"GIPS Best Objective Mean", "GIPS Best Objective StdDev", //
				"MZ Best Objective Mean", "MZ Best Objective StdDev", //
				"Best Objective R", "Best Objective Mean", "Best Objective StdDev", //
				"GIPS Best Bound Mean", "GIPS Best Bound StdDev", //
				"MZ Best Bound Mean", "MZ Best Bound StdDev", //
				"Best Bound R", "Best Bound Mean", "Best Bound StdDev", //
				"GIPS Solutions Mean", "GIPS Solutions StdDev", //
				"MZ Solutions Mean", "MZ Solutions StdDev", //
				"Solutions R", "Solutions Mean", "Solutions StdDev" //
		});

		DecimalFormat format = Util.getDecimalFormat();
		PairedLookupTable merged = mergePairs(gips, mz, "name");
		Function<String, String> zeroInvalid = replaceInvalidNumbers(format, 0, InvalidNumberPlaceholder);
		Function<String, String> minusOneInvalid = replaceInvalidNumbers(format, -1, InvalidNumberPlaceholder);

		for (var instance : merged.allKeys.stream().sorted().toList()) {
			var rowIndex = csv.addNewRow();
			csv.setCellValue(rowIndex, "Instance", instance);

			setNumberValue(csv, rowIndex, "GIPS Best Objective Mean", //
					merged.left, instance, "gurobi best objective mean", zeroInvalid);
			setNumberValue(csv, rowIndex, "GIPS Best Objective StdDev", //
					merged.left, instance, "gurobi best objective stddev", zeroInvalid);
			setNumberValue(csv, rowIndex, "MZ Best Objective Mean", //
					merged.right, instance, "bestObjective (m)", zeroInvalid);
			setNumberValue(csv, rowIndex, "MZ Best Objective StdDev", //
					merged.right, instance, "bestObjective (sd)", zeroInvalid);
			calculateRatio(format, csv, rowIndex, "Best Objective R", //
					"GIPS Best Objective Mean", "MZ Best Objective Mean");

			setNumberValue(csv, rowIndex, "GIPS Best Bound Mean", //
					merged.left, instance, "gurobi best bound mean", zeroInvalid);
			setNumberValue(csv, rowIndex, "GIPS Best Bound StdDev", //
					merged.left, instance, "gurobi best bound stddev", zeroInvalid);
			setNumberValue(csv, rowIndex, "MZ Best Bound Mean", //
					merged.right, instance, "bestBound (m)", zeroInvalid);
			setNumberValue(csv, rowIndex, "MZ Best Bound StdDev", //
					merged.right, instance, "bestBound (sd)", zeroInvalid);
			calculateRatio(format, csv, rowIndex, "Best Bound R", //
					"GIPS Best Bound Mean", "MZ Best Bound Mean");

			setNumberValue(csv, rowIndex, "GIPS Solutions Mean", //
					merged.left, instance, "gurobi solution count mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "GIPS Solutions StdDev", //
					merged.left, instance, "gurobi solution count stddev", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Solutions Mean", //
					merged.right, instance, "numberOfSolutions (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Solutions StdDev", //
					merged.right, instance, "numberOfSolutions (sd)", minusOneInvalid);
			calculateRatio(format, csv, rowIndex, "Solutions R", //
					"GIPS Solutions Mean", "MZ Solutions Mean");

		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Best Objective Mean", "Best Objective StdDev", //
				"Best Objective R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Best Bound Mean", "Best Bound StdDev", //
				"Best Bound R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Solutions Mean", "Solutions StdDev", //
				"Solutions R", true);

		csv.write(outputDirectory.resolve("compare-gips-mz-objective.csv"));
	}

}
