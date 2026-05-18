package org.tud.minitimetable.eval.util;

import java.util.List;
import java.util.function.ToDoubleFunction;

public class StatisticsHelper {
	public static final double TIME_NS_PER_MS = 1_000_000;
	public static final double TIME_MS_PER_S = 1_000;
	public static final double TIME_NS_PER_S = TIME_MS_PER_S * TIME_NS_PER_MS;

	public static final double BYTES_PER_MiB = 1_048_576;
	public static final double BYTES_PER_MB = 1_000_000;
	public static final double BYTES_PER_KiB = 1_024;
	public static final double BYTES_PER_KB = 1_000;

	public static record MeanAndVariance(double mean, double variance) {
		public double standardDeviation() {
			return StatisticsHelper.calculateStdDeviation(variance());
		}
	}

	public static <T> MeanAndVariance calculateDiffMeanAndDeviation(List<T> dataA, List<T> dataB,
			ToDoubleFunction<? super T> map, boolean dataComplete) {
		// TODO not good
		if (dataA == null || dataA.size() == 0)
			return calculateMeanAndVariance(dataB, map, dataComplete);

		if (dataB == null || dataB.size() == 0)
			return calculateMeanAndVariance(dataA, map, dataComplete);

		var sizeA = dataA.size();
		var sizeB = dataB.size();

		var meanA = calculateMean(dataA, map);
		var meanB = calculateMean(dataB, map);
		var mean = (sizeA * meanA - sizeB * meanB) / (sizeA + sizeB);

		var varianceA = calculateVariance(dataA, map, dataComplete, meanA);
		var varianceB = calculateVariance(dataB, map, dataComplete, meanB);

		var combinedVariance = (sizeA - 1) * varianceA - (sizeB - 1) * varianceB;
		var combinedMean = sizeA * sizeB / (sizeA + sizeB) * (meanA * meanA + meanB * meanB - 2 * meanA * meanB);
		var newSize = sizeA + sizeB - 1;

		var variance = (combinedVariance + combinedMean) / newSize;

		return new MeanAndVariance(mean, variance);
	}

	public static MeanAndVariance calculateDifferenceUnrelated(MeanAndVariance a, MeanAndVariance b) {
		var mean = a.mean - b.mean;
		var variance = a.variance + b.variance;
		return new MeanAndVariance(mean, variance);
	}

	public static <T> double calculateMean(List<T> data, ToDoubleFunction<? super T> map) {
		if (data.size() == 0)
			return 0d;

		double sum = 0;
		for (var point : data) {
			sum += map.applyAsDouble(point);
		}

		int size = data.size();
		return sum / size;
	}

	public static <T> double calculateVariance(List<T> data, ToDoubleFunction<? super T> map, boolean dataComplete) {
		var mean = calculateMean(data, map);
		return calculateVariance(data, map, dataComplete, mean);
	}

	public static <T> double calculateVariance(List<T> data, ToDoubleFunction<? super T> map, boolean isSample,
			double mean) {

		double sum = 0d;
		for (var point : data) {
			var value = map.applyAsDouble(point) - mean;
			sum += value * value;
		}

		if (data.size() <= 1)
			return sum;

		int size = isSample ? data.size() - 1 : data.size();
		return sum / size;
	}

	public static <T> MeanAndVariance calculateMeanAndVariance(List<T> data, ToDoubleFunction<? super T> map,
			boolean dataComplete) {
		var mean = calculateMean(data, map);
		var variance = calculateVariance(data, map, dataComplete, mean);
		return new MeanAndVariance(mean, variance);
	}

	public static <T> double calculateStdDeviation(List<T> data, ToDoubleFunction<? super T> map,
			boolean dataComplete) {
		var mean = calculateMean(data, map);
		var variance = calculateVariance(data, map, dataComplete, mean);
		return calculateStdDeviation(variance);
	}

	public static double calculateStdDeviation(double variance) {
		return Math.sqrt(variance);
	}

	public static <T> List<T> filterOutlier(List<T> data, double limiter, ToDoubleFunction<? super T> map,
			boolean dataComplete) {

		var meanAndVariance = calculateMeanAndVariance(data, map, dataComplete);
		var stdDev = meanAndVariance.standardDeviation();
		var lowerLimit = meanAndVariance.mean() - limiter * stdDev;
		var upperLimit = meanAndVariance.mean() + limiter * stdDev;

		return data.stream().filter(row -> {
			var value = map.applyAsDouble(row);
			return lowerLimit <= value && value <= upperLimit;
		}).toList();
	}
}