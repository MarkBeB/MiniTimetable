package org.tud.minitimetable;

import static org.tud.minitimetable.DefaultLocations.getResourceDirectory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.tud.minitimetable.eval.util.CSV;
import org.tud.minitimetable.eval.util.CSV.CSVRecord;
import org.tud.minitimetable.eval.util.StatisticsHelper;
import org.tud.minitimetable.eval.util.Util;

public class LogFinalizer {

	private static final String InvalidNumberPlaceholder = "--";

	public static void main(String[] args) throws IOException, ParseException {

		Path outputDirectory = getResourceDirectory().resolve("workstation").resolve("finalized");
		if (!Files.exists(outputDirectory))
			Files.createDirectories(outputDirectory);

		Path refinedDirectory = getResourceDirectory().resolve("workstation").resolve("refined");

		CSV gips = new CSV(Util.getDecimalFormat());
		gips.read(refinedDirectory.resolve("gips-data.csv"), StandardCharsets.UTF_8, ";", true);

		CSV mzAll = new CSV(Util.getDecimalFormat());
		mzAll.read(refinedDirectory.resolve("minizinc-data-all.csv"), StandardCharsets.UTF_8, ";", true);

		var mzSizeCsv = buildTable_MiniZincModelSize(mzAll, outputDirectory);
		var gipsSizeCsv = buildTable_GipsModelSize(gips, outputDirectory);
		buildTable_MiniZincGipsSizeComparison(mzSizeCsv, gipsSizeCsv, outputDirectory);

		designObjective(mzAll, outputDirectory);
		compareGipsSize(gips, mzAll, outputDirectory);
		compareGipsPresolveSize(gips, mzAll, outputDirectory);
		compareGipsCompileTime(gips, mzAll, outputDirectory);
		compareGipsSolutions(gips, mzAll, outputDirectory);
	}

	private static record PairedLookupTable(Set<String> allKeys, Map<String, CSVRecord> gips,
			Map<String, CSVRecord> mz) {
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

	private static PairedLookupTable mergePairs(CSV gips, CSV mz, String byKey) {
		Map<String, CSVRecord> gipsLookup = buildLookup(gips, byKey);
		Map<String, CSVRecord> mzLookup = buildLookup(mz, byKey);

		Set<String> keys = new HashSet<>();
		keys.addAll(gipsLookup.keySet());
		keys.addAll(mzLookup.keySet());

		return new PairedLookupTable(keys, gipsLookup, mzLookup);
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

	private static Number getNumberValue(DecimalFormat format, CSVRecord record, String column) throws ParseException {
		var value = record.getCell(column);
		if (value == null || value.isBlank() || value.equals(InvalidNumberPlaceholder))
			return null;
		Number parsedValue = format.parse(value);
		if (Double.compare(parsedValue.doubleValue(), -1) == 0)
			return null;

		return parsedValue;
	}

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

	private static void designObjective(CSV mz, Path outputDirectory) throws IOException, ParseException {
		CSV csv = new CSV(Util.getDecimalFormat());
		csv.setColumnNames(new String[] { "Instance", //
				"Model Objective Mean", "Model Objective StdDev", //
				"Validator Objective Mean", "Validator Objective StdDev", //
				"Objective R", "Objective Mean", "Objective StdDev", //
		});

		DecimalFormat format = Util.getDecimalFormat();
		var lookupTable = buildLookup(mz, "name");
		Function<String, String> zeroInvalid = replaceInvalidNumbers(format, 0, InvalidNumberPlaceholder);

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
		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Objective Mean", "Objective StdDev", "Objective R", true);

		csv.write(outputDirectory.resolve("design-objective.csv"));
	}

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
					merged.gips, instance, "observer runtime preproc mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "GIPS Preproc StdDev", //
					merged.gips, instance, "observer runtime preproc stddev", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Preproc Mean", //
					merged.mz, instance, "preprocessingTimeS (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Preproc StdDev", //
					merged.mz, instance, "preprocessingTimeS (sd)", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Preproc R", "GIPS Preproc Mean", "MZ Preproc Mean");

			var gipsGipsBuildM = getNumberValue(format, gips, rowIndex, "observer runtime build gips mean");
			var gipsSolverBuildM = getNumberValue(format, gips, rowIndex, "observer runtime build solver mean");
			setNumberValue(format, csv, rowIndex, "GIPS Build Mean",
					calculateCombinedMean(gipsGipsBuildM, gipsSolverBuildM), InvalidNumberPlaceholder);

			var gipsGipsBuildSd = getNumberValue(format, gips, rowIndex, "observer runtime build gips stddev");
			var gipsSolverBuildSd = getNumberValue(format, gips, rowIndex, "observer runtime build solver stddev");
			setNumberValue(format, csv, rowIndex, "GIPS Build StdDev",
					calculateNewStdDev(gipsGipsBuildSd, gipsSolverBuildSd), InvalidNumberPlaceholder);

			setNumberValue(csv, rowIndex, "MZ Build Mean", //
					merged.mz, instance, "totalCompileTimeS (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Build StdDev", //
					merged.mz, instance, "totalCompileTimeS (sd)", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Build R", "GIPS Build Mean", "MZ Build Mean");

		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Preproc Mean", "Preproc StdDev", "Preproc R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Build Mean", "Build StdDev", "Build R", true);

		csv.write(outputDirectory.resolve("compare-gips-mz-compiletime.csv"));
	}

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

			setNumberValue(csv, rowIndex, "GIPS Constraints", merged.gips, instance, "gurobi model rows mean",
					InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Constraints", merged.mz, instance, "originalConstraints (m)",
					InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Constraints R", "GIPS Constraints", "MZ Constraints");

			setNumberValue(csv, rowIndex, "GIPS Variables", merged.gips, instance, "gurobi model cols mean",
					InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Variables", merged.mz, instance, "originalVariables (m)",
					InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Variables R", "GIPS Variables", "MZ Variables");

			setNumberValue(csv, rowIndex, "GIPS Coef", merged.gips, instance, "gurobi model nonzeros mean",
					InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Coef", merged.mz, instance, "originalCoefficients (m)",
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

	private static CSV buildTable_MiniZincModelSize(CSV mz, Path outputDirectory) throws IOException, ParseException {

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

		csv.write(outputDirectory.resolve("mz-model-size.csv"));
		return csv;
	}

	private static CSV buildTable_GipsModelSize(CSV gips, Path outputDirectory) throws IOException, ParseException {

		CSV csv = new CSV(Util.getDecimalFormat());
		csv.setColumnNames(new String[] { "Instance", //
				"GIPS Start Constraints Mean", "GIPS Start Constraints StdDev", //
				"GIPS Preso Constraints Mean", "GIPS Preso Constraints StdDev", //
				"Constraints R", "Constraints Mean", "Constraints StdDev", //
				"GIPS Start Variables Mean", "GIPS Start Variables StdDev", //
				"GIPS Preso Variables Mean", "GIPS Preso Variables StdDev", //
				"Variables R", "Variables Mean", "Variables StdDev", //
				"GIPS Start Coef Mean", "GIPS Start Coef StdDev", //
				"GIPS Preso Coef Mean", "GIPS Preso Coef StdDev", //
				"Coef R", "Coef Mean", "Coef StdDev", //
				"GIPS Presolve Time Mean", "GIPS Presolve Time StdDev", //
		});

		DecimalFormat format = Util.getDecimalFormat();
		var lookupTable = buildLookup(gips, "name");

		for (var instance : lookupTable.keySet().stream().sorted().toList()) {
			var rowIndex = csv.addNewRow();
			csv.setCellValue(rowIndex, "Instance", instance);

			var startConstraintsM = getNumberValue(format, lookupTable.get(instance), "gurobi model rows mean");
			var startConstraintsSd = getNumberValue(format, lookupTable.get(instance), "gurobi model rows stddev");
			var removedConstraintsM = getNumberValue(format, lookupTable.get(instance),
					"gurobi presolve removed rows mean");
			var removedConstraintsSd = getNumberValue(format, lookupTable.get(instance),
					"gurobi presolve removed rows stddev");

			setNumberValue(format, csv, rowIndex, "GIPS Start Constraints Mean", //
					startConstraintsM, InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "GIPS Start Constraints StdDev", //
					startConstraintsSd, InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "GIPS Preso Constraints Mean", //
					calculateSubtracteddMean(startConstraintsM, removedConstraintsM), InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "GIPS Preso Constraints StdDev", //
					calculateNewStdDev(startConstraintsSd, removedConstraintsSd), InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Constraints R", //
					"GIPS Start Constraints Mean", "GIPS Preso Constraints Mean");

			var startVariablesM = getNumberValue(format, lookupTable.get(instance), "gurobi model cols mean");
			var startVariablesSd = getNumberValue(format, lookupTable.get(instance), "gurobi model cols stddev");
			var removedVariablesM = getNumberValue(format, lookupTable.get(instance),
					"gurobi presolve removed cols mean");
			var removedVariablesSd = getNumberValue(format, lookupTable.get(instance),
					"gurobi presolve removed cols stddev");
			setNumberValue(format, csv, rowIndex, "GIPS Start Variables Mean", //
					startVariablesM, InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "GIPS Start Variables StdDev", //
					startVariablesSd, InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "GIPS Preso Variables Mean", //
					calculateSubtracteddMean(startVariablesM, removedVariablesM), InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "GIPS Preso Variables StdDev", //
					calculateNewStdDev(startVariablesSd, removedVariablesSd), InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Variables R", //
					"GIPS Start Variables Mean", "GIPS Preso Variables Mean");

			var startCoefsM = getNumberValue(format, lookupTable.get(instance), "gurobi model nonzeros mean");
			var startCoefsSd = getNumberValue(format, lookupTable.get(instance), "gurobi model nonzeros stddev");
			var removedCoefsM = getNumberValue(format, lookupTable.get(instance),
					"gurobi presolve removed nonzeros mean");
			var removedCoefsSd = getNumberValue(format, lookupTable.get(instance),
					"gurobi presolve removed nonzeros stddev");
			setNumberValue(format, csv, rowIndex, "GIPS Start Coef Mean", //
					startCoefsM, InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "GIPS Start Coef StdDev", //
					startCoefsSd, InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "GIPS Preso Coef Mean", //
					calculateSubtracteddMean(startCoefsM, removedCoefsM), InvalidNumberPlaceholder);
			setNumberValue(format, csv, rowIndex, "GIPS Preso Coef StdDev", //
					calculateNewStdDev(startCoefsSd, removedCoefsSd), InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Coef R", //
					"GIPS Start Coef Mean", "GIPS Preso Coef Mean");

			setNumberValue(csv, rowIndex, "GIPS Presolve Time Mean", //
					lookupTable, instance, "gurobi presolve runtime mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "GIPS Presolve Time StdDev", //
					lookupTable, instance, "gurobi presolve runtime stddev", InvalidNumberPlaceholder);

		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Constraints Mean", "Constraints StdDev", //
				"Constraints R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Variables Mean", "Variables StdDev", //
				"Variables R", true);
//		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Coef Mean", "Coef StdDev", //
//				"Coef R", true);

		csv.write(outputDirectory.resolve("gips-model-size.csv"));
		return csv;
	}

	private static void buildTable_MiniZincGipsSizeComparison(CSV mz, CSV gips, Path outputDirectory)
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
				"GIPS Presolve Time Mean", "GIPS Presolve Time StdDev", //
				"MZ Presolve Time Mean", "MZ Presolve Time StdDev", //
				"Presolve Time R", "Presolve Time Mean", "Presolve Time StdDev" //
		});

		DecimalFormat format = Util.getDecimalFormat();
		PairedLookupTable merged = mergePairs(gips, mz, "Instance");

		for (var instance : merged.allKeys.stream().sorted().filter(e -> !e.equals("final")).toList()) {
			var rowIndex = csv.addNewRow();
			csv.setCellValue(rowIndex, "Instance", instance);

			setNumberValue(csv, rowIndex, "GIPS Constraints Mean", //
					merged.gips, instance, "GIPS Preso Constraints Mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "GIPS Constraints StdDev", //
					merged.gips, instance, "GIPS Preso Constraints StdDev", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Constraints Mean", //
					merged.mz, instance, "MZ Preso Constraints Mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Constraints StdDev", //
					merged.mz, instance, "MZ Preso Constraints StdDev", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Constraints R", //
					"GIPS Constraints Mean", "MZ Constraints Mean");

			setNumberValue(csv, rowIndex, "GIPS Variables Mean", //
					merged.gips, instance, "GIPS Preso Variables Mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "GIPS Variables StdDev", //
					merged.gips, instance, "GIPS Preso Variables StdDev", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Variables Mean", //
					merged.mz, instance, "MZ Preso Variables Mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Variables StdDev", //
					merged.mz, instance, "MZ Preso Variables StdDev", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Variables R", //
					"GIPS Variables Mean", "MZ Variables Mean");

			setNumberValue(csv, rowIndex, "GIPS Coef Mean", //
					merged.gips, instance, "GIPS Preso Coef Mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "GIPS Coef StdDev", //
					merged.gips, instance, "GIPS Preso Coef StdDev", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Coef Mean", //
					merged.mz, instance, "MZ Preso Coef Mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Coef StdDev", //
					merged.mz, instance, "MZ Preso Coef StdDev", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Coef R", //
					"GIPS Coef Mean", "MZ Coef Mean");

			setNumberValue(csv, rowIndex, "GIPS Presolve Time Mean", //
					merged.gips, instance, "GIPS Presolve Time Mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "GIPS Presolve Time StdDev", //
					merged.gips, instance, "GIPS Presolve Time StdDev", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Presolve Time Mean", //
					merged.mz, instance, "MZ Presolve Time Mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Presolve Time StdDev", //
					merged.mz, instance, "MZ Presolve Time StdDev", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Presolve Time R", //
					"GIPS Presolve Time Mean", "MZ Presolve Time Mean");

		}

		var finalRow = csv.addNewRow();
		csv.setCellValue(finalRow, "Instance", "final");
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Constraints Mean", "Constraints StdDev", //
				"Constraints R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Variables Mean", "Variables StdDev", //
				"Variables R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Coef Mean", "Coef StdDev", //
				"Coef R", true);
		calculateMeanAndStdDevOfRatio(format, csv, finalRow, "Presolve Time Mean", "Presolve Time StdDev", //
				"Presolve Time R", true);

		csv.write(outputDirectory.resolve("compare-mz-gips-model-size.csv"));
	}

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
					merged.gips, instance, "gurobi presolve removed rows mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "GIPS Constraints StdDev", //
					merged.gips, instance, "gurobi presolve removed rows stddev", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Constraints Mean", //
					merged.mz, instance, "presolvedConstraints (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Constraints StdDev", //
					merged.mz, instance, "presolvedConstraints (sd)", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Constraints R", //
					"GIPS Constraints Mean", "MZ Constraints Mean");

			setNumberValue(csv, rowIndex, "GIPS Variables Mean", //
					merged.gips, instance, "gurobi presolve removed cols mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "GIPS Variables StdDev", //
					merged.gips, instance, "gurobi presolve removed cols stddev", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Variables Mean", //
					merged.mz, instance, "presolvedVariables (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Variables StdDev", //
					merged.mz, instance, "presolvedVariables (sd)", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Variables R", //
					"GIPS Variables Mean", "MZ Variables Mean");

//			setNumberValue(csv, rowIndex, "GIPS Coef Mean", //
//					merged.gips, instance, "gurobi presolve removed nonzeros mean", InvalidNumberPlaceholder);
			csv.setCellValue(rowIndex, "GIPS Coef Mean", "--");
//			setNumberValue(csv, rowIndex, "GIPS Coef StdDev", //
//			merged.gips, instance, "gurobi presolve removed nonzeros stddev", InvalidNumberPlaceholder);
			csv.setCellValue(rowIndex, "GIPS Coef StdDev", "--");
			setNumberValue(csv, rowIndex, "MZ Coef Mean", //
					merged.mz, instance, "presolvedCoefficients (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Coef StdDev", //
					merged.mz, instance, "presolvedCoefficients (sd)", InvalidNumberPlaceholder);
			calculateRatio(format, csv, rowIndex, "Coef R", //
					"GIPS Coef Mean", "MZ Coef Mean");

			setNumberValue(csv, rowIndex, "GIPS Runtime Mean", //
					merged.gips, instance, "gurobi presolve runtime mean", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "GIPS Runtime StdDev", //
					merged.gips, instance, "gurobi presolve runtime stddev", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Runtime Mean", //
					merged.mz, instance, "presolveTimeS (m)", InvalidNumberPlaceholder);
			setNumberValue(csv, rowIndex, "MZ Runtime StdDev", //
					merged.mz, instance, "presolveTimeS (sd)", InvalidNumberPlaceholder);
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
					merged.gips, instance, "gurobi best objective mean", zeroInvalid);
			setNumberValue(csv, rowIndex, "GIPS Best Objective StdDev", //
					merged.gips, instance, "gurobi best objective stddev", zeroInvalid);
			setNumberValue(csv, rowIndex, "MZ Best Objective Mean", //
					merged.mz, instance, "bestObjective (m)", zeroInvalid);
			setNumberValue(csv, rowIndex, "MZ Best Objective StdDev", //
					merged.mz, instance, "bestObjective (sd)", zeroInvalid);
			calculateRatio(format, csv, rowIndex, "Best Objective R", //
					"GIPS Best Objective Mean", "MZ Best Objective Mean");

			setNumberValue(csv, rowIndex, "GIPS Best Bound Mean", //
					merged.gips, instance, "gurobi best bound mean", zeroInvalid);
			setNumberValue(csv, rowIndex, "GIPS Best Bound StdDev", //
					merged.gips, instance, "gurobi best bound stddev", zeroInvalid);
			setNumberValue(csv, rowIndex, "MZ Best Bound Mean", //
					merged.mz, instance, "bestBound (m)", zeroInvalid);
			setNumberValue(csv, rowIndex, "MZ Best Bound StdDev", //
					merged.mz, instance, "bestBound (sd)", zeroInvalid);
			calculateRatio(format, csv, rowIndex, "Best Bound R", //
					"GIPS Best Bound Mean", "MZ Best Bound Mean");

			setNumberValue(csv, rowIndex, "GIPS Solutions Mean", //
					merged.gips, instance, "gurobi solution count mean", minusOneInvalid);
			setNumberValue(csv, rowIndex, "GIPS Solutions StdDev", //
					merged.gips, instance, "gurobi solution count stddev", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Solutions Mean", //
					merged.mz, instance, "numberOfSolutions (m)", minusOneInvalid);
			setNumberValue(csv, rowIndex, "MZ Solutions StdDev", //
					merged.mz, instance, "numberOfSolutions (sd)", minusOneInvalid);
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
