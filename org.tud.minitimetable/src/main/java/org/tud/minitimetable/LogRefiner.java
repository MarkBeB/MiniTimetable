package org.tud.minitimetable;

import static org.tud.minitimetable.DefaultLocations.getResourceDirectory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.tud.minitimetable.eval.extract.MainCSV;
import org.tud.minitimetable.eval.refine.CSVRefiner;
import org.tud.minitimetable.eval.refine.PrettyPrinter;
import org.tud.minitimetable.eval.refine.RefineHelper;
import org.tud.minitimetable.eval.refine.RefineHelper.CopyRefinement.CopyElement;
import org.tud.minitimetable.eval.util.CSV;
import org.tud.minitimetable.eval.util.CSV.CSVRecord;
import org.tud.minitimetable.eval.util.NamedCSV;
import org.tud.minitimetable.eval.util.Util;

public class LogRefiner {

	public static void main(String[] args) throws IOException {

		Path outputDirectory = getResourceDirectory().resolve("workstation").resolve("refined");
		Path gipsData = getResourceDirectory().resolve("workstation").resolve("gips").resolve("gipsl-data-v3.csv");
		Path miniZincData = getResourceDirectory().resolve("workstation").resolve("extracted").resolve("main.csv");

		LogRefiner.refineGipsData(gipsData, outputDirectory);
		LogRefiner.refineMiniZincData(miniZincData, outputDirectory);
	}

	private static void refineGipsData(Path inputFile, Path outputDirectory) throws IOException {
		CSV data = new CSV(null);
		data.read(inputFile, StandardCharsets.UTF_8, ",", true);

		CSV prettyGipsl = makeGipsPretty(data);
		prettyGipsl.write(outputDirectory.resolve("gips-data.csv"), StandardCharsets.UTF_8, ";");
	}

	public static void refineMiniZincData(Path inputFile, Path outputDirectory) throws IOException {
		MainCSV mainCSV = new MainCSV();
		mainCSV.read(inputFile, true);

		var optimized = mainCSV.stream().filter(e -> Util.toBool(e, MainCSV.Columns.CompileOptimized)).toList();
		var nonOptimized = mainCSV.stream().filter(e -> !Util.toBool(e, MainCSV.Columns.CompileOptimized)).toList();

		var refiner = buildMiniZincRefiner();
		var outOptimized = refine(refiner, optimized);
		var outNonOptimized = refine(refiner, nonOptimized);

		outOptimized = makeMiniZincPretty(outOptimized);
		outNonOptimized = makeMiniZincPretty(outNonOptimized);

		outOptimized.write(outputDirectory.resolve("minizinc-o.csv"), StandardCharsets.UTF_8, ";");
		outNonOptimized.write(outputDirectory.resolve("minizinc-no.csv"), StandardCharsets.UTF_8, ";");

	}

	private static CSV makeGipsPretty(CSV csv) {
		PrettyPrinter.PrettyRow header = value -> {
			String[] pretty = new String[value.length];
			for (var i = 0; i < pretty.length; ++i)
				pretty[i] = value[i].replaceAll("_", " ");
			return pretty;
		};

		PrettyPrinter.Pretty<?>[] columns = new PrettyPrinter.Pretty[csv.getNumberOfColumns()];

		DecimalFormat doubleFormatter = Util.getDecimalFormat();
		for (var columnName : csv.getColumnNames()) {
			if (columnName.endsWith("_mean") || columnName.endsWith("_stddev")) {
				columns[csv.getColIndexWithName(columnName)] = new PrettyPrinter.PrettyNumberFormat(doubleFormatter,
						doubleFormatter);
			}
		}

		DecimalFormat integerFormatter = Util.getIntegerFormat();
		columns[csv.getColIndexWithName("gurobi_model_rows_mean")] = new PrettyPrinter.PrettyNumberFormat(
				integerFormatter, integerFormatter);
		columns[csv.getColIndexWithName("gurobi_model_cols_mean")] = new PrettyPrinter.PrettyNumberFormat(
				integerFormatter, integerFormatter);
		columns[csv.getColIndexWithName("gurobi_model_nonzeros_mean")] = new PrettyPrinter.PrettyNumberFormat(
				integerFormatter, integerFormatter);

		columns[csv.getColIndexWithName("name")] = (PrettyPrinter.PrettyCell) value -> {
			int index = value.indexOf(".");
			return index >= 0 ? value.substring(0, index) : value;
		};

		PrettyPrinter pp = new PrettyPrinter(() -> new CSV(Util.getDecimalFormat()), header, columns);
		return pp.pretty(csv);
	}

	private static CSV makeMiniZincPretty(CSV csv) {

//		PrettyPrinter.PrettyRow header = value -> {
//			String[] pretty = new String[value.length];
//			for (var i = 0; i < pretty.length; ++i)
//				pretty[i] = value[i].replaceAll("_", " ");
//			return pretty;
//		};
//
//		PrettyPrinter.Pretty<?>[] columns = new PrettyPrinter.Pretty[csv.getNumberOfColumns()];

//		DecimalFormat doubleFormatter = Util.getDecimalFormat();
//		for (var columnName : csv.getColumnNames()) {
//			if (columnName.endsWith("(m)") || columnName.endsWith("(sd)"))
//				columns[csv.getColIndexWithName(columnName)] = new PrettyPrinter.PrettyNumberFormat(doubleFormatter,
//						doubleFormatter);
//		}
//
//		DecimalFormat integerFormatter = Util.getIntegerFormat();
//		columns[csv.getColIndexWithName(
//				MainCSV.Columns.MemorySize.getColumnName())] = new PrettyPrinter.PrettyNumberFormat(integerFormatter,
//						integerFormatter);

//		PrettyPrinter pp = new PrettyPrinter(() -> new CSV(Util.getDecimalFormat()), header, columns);
//		return pp.pretty(csv);

		return csv;
	}

	public static CSVRefiner buildMiniZincRefiner() {
		CSVRefiner refiner = new CSVRefiner(() -> new CSV(Util.getDecimalFormat()));

		refiner.addRefinement(RefineHelper.groupName("name"));
		refiner.addRefinement(RefineHelper.toValue("sampleSize", List::size));
		refiner.addRefinement(RefineHelper.copy(MainCSV.Columns.MemorySize, CopyElement.First));

		var toDouble = Util.getDoubleParser(Util.getDecimalFormat());

		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.PreprocessingTime, toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.TotalCompileTime, toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.PresolveTime, toDouble));

		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.OriginalConstraints, toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.OriginalVariables, toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.OriginalCoefficients, toDouble));

		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.PresolvedConstraints, toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.PresolvedVariables, toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.PresolvedCoefficients, toDouble));

		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.NumberOfSolutions, toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.BestObjective, toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.BestBound, toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.MIPGap, toDouble));

		refiner.addRefinement(RefineHelper.toValue("solverCrash", data -> data.stream()
				.filter(e -> !Util.toBool(e, MainCSV.Columns.SolveCrash.getColumnName())).count()));

		return refiner;
	}

	private static CSV refine(CSVRefiner refiner, Collection<NamedCSV.NamedRecord<MainCSV.Columns>> datapoints) {

		Map<String, List<CSVRecord>> groupByInstance = datapoints.stream() //
				.filter(e -> !Util.toBool(e, MainCSV.Columns.CompileCrash)) //
				.collect(Collectors.groupingBy( //
						e -> e.getCell(MainCSV.Columns.Instance), //
						Collectors.toList() //
				));

		var order = groupByInstance.keySet().stream().sorted().toList();
		return refiner.refine(groupByInstance, order);
	}

}
