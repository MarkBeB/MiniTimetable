package org.tud.minitimetable.eval.refine;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import org.tud.minitimetable.eval.refine.CSVRefiner.Refinement;
import org.tud.minitimetable.eval.util.CSV;
import org.tud.minitimetable.eval.util.NamedCSV;
import org.tud.minitimetable.eval.util.StatisticsHelper;

public class RefineHelper {

	public static class StdDevRefinement implements Refinement {

		private final String columnName;
		private final String columnNameMean;
		private final String columnNameStdDev;
		private final ToDoubleFunction<String> convertToDouble;

		public StdDevRefinement(String columnName, String meanColumnName, String stddevColumnName,
				ToDoubleFunction<String> convertToDouble) {
			this.columnName = Objects.requireNonNull(columnName);
			this.columnNameMean = Objects.requireNonNull(meanColumnName);
			this.columnNameStdDev = Objects.requireNonNull(stddevColumnName);
			this.convertToDouble = Objects.requireNonNull(convertToDouble);
		}

		@Override
		public List<String> headers() {
			return Arrays.asList(columnNameMean, columnNameStdDev);
		}

		@Override
		public void refine(CSV output, String groupName, int rowIndex, List<CSV.CSVRecord> datapoints) {
			ToDoubleFunction<CSV.CSVRecord> converter = record -> StdDevRefinement.this.convertToDouble
					.applyAsDouble(record.getCell(columnName));
			var point = StatisticsHelper.calculateMeanAndVariance(datapoints, converter, false);
			output.setCellValue(rowIndex, columnNameMean, point.mean());
			output.setCellValue(rowIndex, columnNameStdDev, point.standardDeviation());
		}
	}

	public static class CopyRefinement implements Refinement {
		public static enum CopyElement {
			First, Last
		}

		private final String columnName;
		private final CopyElement target;

		public CopyRefinement(String columnName, CopyElement target) {
			this.columnName = columnName;
			this.target = target;
		}

		@Override
		public Collection<String> headers() {
			return Collections.singleton(columnName);
		}

		@Override
		public void refine(CSV output, String groupName, int rowIndex, List<CSV.CSVRecord> datapoints) {
			var point = switch (target) {
			case First -> datapoints.getFirst();
			case Last -> datapoints.getLast();
			};

			output.setCellValue(rowIndex, columnName, point.getCell(columnName));
		}
	}

	public static class GroupNameRefinement implements Refinement {
		private final String columnName;

		public GroupNameRefinement(String columnName) {
			this.columnName = columnName;
		}

		@Override
		public Collection<String> headers() {
			return Collections.singleton(columnName);
		}

		@Override
		public void refine(CSV output, String groupName, int rowIndex, List<CSV.CSVRecord> datapoints) {
			output.setCellValue(rowIndex, columnName, groupName);
		}
	}

	public static class FunctionRefinement implements Refinement {

		private final String columnName;
		private final Function<List<CSV.CSVRecord>, Object> consumer;

		public FunctionRefinement(String columnName, Function<List<CSV.CSVRecord>, Object> consumer) {
			this.columnName = columnName;
			this.consumer = consumer;
		}

		@Override
		public Collection<String> headers() {
			return Collections.singleton(columnName);
		}

		@Override
		public void refine(CSV output, String groupName, int rowIndex, List<CSV.CSVRecord> datapoints) {
			var result = consumer.apply(datapoints);
			output.setCellValue(rowIndex, columnName, result);
		}
	}

	public static Refinement stdDeviation(String column, String meanColumn, String devColumn,
			ToDoubleFunction<String> convertToDouble) {
		return new StdDevRefinement(column, meanColumn, devColumn, convertToDouble);
	}

	public static Refinement stdDeviation(NamedCSV.CSVHeader column, ToDoubleFunction<String> convertToDouble) {
		var meanColumn = String.format("%s (m)", column.getColumnName());
		var devColumn = String.format("%s (sd)", column.getColumnName());
		return stdDeviation(column.getColumnName(), meanColumn, devColumn, convertToDouble);
	}

	public static Refinement copy(String column, CopyRefinement.CopyElement target) {
		return new CopyRefinement(column, target);
	}

	public static Refinement copy(NamedCSV.CSVHeader column, CopyRefinement.CopyElement target) {
		return copy(column.getColumnName(), target);
	}

	public static Refinement toValue(String column, Function<List<CSV.CSVRecord>, Object> fun) {
		return new FunctionRefinement(column, fun);
	}

	public static Refinement toValue(NamedCSV.CSVHeader column, Function<List<CSV.CSVRecord>, Object> fun) {
		return toValue(column.getColumnName(), fun);
	}

	public static Refinement groupName(String column) {
		return new GroupNameRefinement(column);
	}

}
