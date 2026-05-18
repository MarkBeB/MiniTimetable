package org.tud.minitimetable;

import static org.tud.minitimetable.DefaultLocations.getResourceDirectory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
//		makeGipsPretty();

		Path inpitDir = getCurrentDataDirectory();
		Path outputDir = getResourceDirectory().resolve("workstation").resolve("refined");// .resolve(Util.getFileNameTimeStampNow());

		MainCSV mainCSV = new MainCSV();
		mainCSV.read(inpitDir.resolve("main.csv"), true);

		var optimized = mainCSV.stream().filter(e -> Util.toBool(e, MainCSV.Columns.CompileOptimized)).toList();
		var nonOptimized = mainCSV.stream().filter(e -> !Util.toBool(e, MainCSV.Columns.CompileOptimized)).toList();

//		var outOptimized = refineSize(optimized);
//		var outNonOptimized = refineSize(nonOptimized);

		var refiner = buildRefiner();
		var outOptimized = refine(refiner, optimized);
		var outNonOptimized = refine(refiner, nonOptimized);

		outOptimized = makeMZPretty(outOptimized);
		outNonOptimized = makeMZPretty(outNonOptimized);

		outOptimized.write(outputDir.resolve("minizinc-o.csv"));
		outNonOptimized.write(outputDir.resolve("minizinc-no.csv"));

	}

	private static CSV makeMZPretty(CSV csv) {

		PrettyPrinter.PrettyRow header = value -> {
			String[] pretty = new String[value.length];
			for (var i = 0; i < pretty.length; ++i)
				pretty[i] = value[i].replaceAll("_", " ");
			return pretty;
		};

		PrettyPrinter.Pretty<?>[] columns = new PrettyPrinter.Pretty[csv.getNumberOfColumns()];

		return csv;
	}

	private static void makeGipsPretty() throws IOException {
		var makeGipslPretty = getResourceDirectory().resolve("workstation").resolve("gips")
				.resolve("gipsl-data-v2.csv");
		var outGipslPretty = makeGipslPretty.resolveSibling("gips-data-v2-pretty.csv");

		CSV gipsl = new CSV(null);
		gipsl.read(makeGipslPretty, StandardCharsets.UTF_8, ",", true);

		PrettyPrinter.PrettyRow header = value -> {
			String[] pretty = new String[value.length];
			for (var i = 0; i < pretty.length; ++i)
				pretty[i] = value[i].replaceAll("_", " ");
			return pretty;
		};

		PrettyPrinter.Pretty<?>[] columns = new PrettyPrinter.Pretty[gipsl.getNumberOfColumns()];

		DecimalFormat doubleFormatter = Util.getDecimalFormat();
		for (var columnName : gipsl.getColumnNames()) {
			if (columnName.endsWith("_mean") || columnName.endsWith("_stddev")) {
				columns[gipsl.getColIndexWithName(columnName)] = new PrettyPrinter.PrettyNumber(doubleFormatter);
			}
		}

		DecimalFormat integerFormatter = Util.getIntegerFormat();
		columns[gipsl.getColIndexWithName("gurobi_model_rows_mean")] = new PrettyPrinter.PrettyNumber(integerFormatter);
		columns[gipsl.getColIndexWithName("gurobi_model_cols_mean")] = new PrettyPrinter.PrettyNumber(integerFormatter);
		columns[gipsl.getColIndexWithName("gurobi_model_nonzeros_mean")] = new PrettyPrinter.PrettyNumber(
				integerFormatter);

		PrettyPrinter pp = new PrettyPrinter(() -> new CSV(Util.getDecimalFormat()), header, columns);
		CSV prettyGipsl = pp.pretty(gipsl);
		prettyGipsl.write(outGipslPretty);
	}

	public static CSVRefiner buildRefiner() {
		CSVRefiner refiner = new CSVRefiner(() -> new CSV(Util.getDecimalFormat()));

		refiner.addRefinement(RefineHelper.groupName("name"));
		refiner.addRefinement(RefineHelper.toValue("sampleSize", List::size));
		refiner.addRefinement(RefineHelper.copy(MainCSV.Columns.MemorySize, CopyElement.First));

		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.PreprocessingTime, Util::toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.TotalCompileTime, Util::toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.PresolveTime, Util::toDouble));

		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.OriginalConstraints, Util::toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.OriginalVariables, Util::toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.OriginalCoefficients, Util::toDouble));

		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.PresolvedConstraints, Util::toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.PresolvedVariables, Util::toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.PresolvedCoefficients, Util::toDouble));

		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.NumberOfSolutions, Util::toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.BestObjective, Util::toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.BestBound, Util::toDouble));
		refiner.addRefinement(RefineHelper.stdDeviation(MainCSV.Columns.MIPGap, Util::toDouble));

		refiner.addRefinement(RefineHelper.toValue("solverCrash", data -> data.stream()
				.filter(e -> !Util.toBool(e, MainCSV.Columns.SolveCrash.getColumnName())).count()));

		return refiner;
	}

	private static CSV refine(CSVRefiner refiner, Collection<NamedCSV.NamedRecord<MainCSV.Columns>> datapoints) {

		Map<String, List<CSVRecord>> byInstance = datapoints.stream() //
				.filter(e -> !Util.toBool(e, MainCSV.Columns.CompileCrash)) //
				.collect(Collectors.groupingBy( //
						e -> e.getCell(MainCSV.Columns.Instance), //
						Collectors.toList() //
				));

		var order = byInstance.keySet().stream().sorted().toList();
		return refiner.refine(byInstance, order);
	}

	private static Path getCurrentDataDirectory() throws IOException {
		Path dataDirectory = getResourceDirectory().resolve("workstation").resolve("extracted");
		if (true)
			return dataDirectory;

		try (var stream = Files.newDirectoryStream(dataDirectory)) {
			return StreamSupport.stream(() -> stream.spliterator(), Spliterator.DISTINCT, false)//
					.sorted(Comparator.reverseOrder()) //
					.findFirst() //
					.orElse(null);
		}
	}

}
