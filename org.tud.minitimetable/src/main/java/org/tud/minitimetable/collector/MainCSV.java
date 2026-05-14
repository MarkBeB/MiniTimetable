package org.tud.minitimetable.collector;

import java.text.DecimalFormat;
import java.util.Objects;

import org.tud.minitimetable.collector.MainCSV.Columns;
import org.tud.minitimetable.collector.util.NamedCSV;

public class MainCSV extends NamedCSV<Columns> {

//	public static final String COLUMN_MODEL = "model";
//	public static final String COLUMN_MODEL_VERSION = "modelversion";
//	public static final String COLUMN_INSTANCE = "instance";
//	public static final String COLUMN_RUN = "run";
//	public static final String COLUMN_MODEL_SIZE_MB = "modelSizeMB";
//
//	public static final String COLUMN_ORIGINAL_CONSTRAINTS = "constraints";
//	public static final String COLUMN_ORIGINAL_VARIABLES = "variables";
//	public static final String COLUMN_ORIGINAL_COEFFICIENTS = "coefficients";
//
//	public static final String COLUMN_PRESOLVED_CONSTRAINTS = "p-constraints";
//	public static final String COLUMN_PRESOLVED_VARIABLES = "p-variables";
//	public static final String COLUMN_PRESOLVED_COEFFICIENTS = "p-coefficients";
//
//	public static final String[] HEADERS = { COLUMN_MODEL, COLUMN_MODEL_VERSION, COLUMN_INSTANCE, COLUMN_RUN,
//			COLUMN_MODEL_SIZE_MB, COLUMN_ORIGINAL_CONSTRAINTS, COLUMN_ORIGINAL_VARIABLES, COLUMN_ORIGINAL_COEFFICIENTS,
//			COLUMN_PRESOLVED_CONSTRAINTS, COLUMN_PRESOLVED_VARIABLES, COLUMN_PRESOLVED_COEFFICIENTS };

	public static enum Columns implements org.tud.minitimetable.collector.util.NamedCSV.CSVHeader {
		Model("model"), //
		Version("version"), //
		Instance("instance"), //
		Run("run"), //
		MemorySize("memoryMB"), //
		OriginalConstraints("constraints"), //
		OriginalVariables("variables"), //
		OriginalCoefficients("coefficients"), //
		PresolvedConstraints("p-constraints"), //
		PresolvedVariables("p-variables"), //
		PresolvedCoefficients("p-coefficients"); //

		private final String name;

		private Columns(String name) {
			this.name = Objects.requireNonNull(name);
		}

		@Override
		public String getColumnName() {
			return name;
		}
	}

	public MainCSV(DecimalFormat format) {
		super(Columns.class, format);
	}

}
