package org.tud.minitimetable.eval.extract;

import java.util.Objects;

import org.tud.minitimetable.eval.util.NamedCSV;
import org.tud.minitimetable.eval.util.Util;

public class MainCSV extends NamedCSV<MainCSV.Columns> {

	public static enum Columns implements org.tud.minitimetable.eval.util.NamedCSV.CSVHeader {
		Model("model"), //
		Version("version"), //
		Instance("instance"), //
		Run("run"), //
		MemorySize("memoryMB"), //
		PreprocessingTime("preprocessingTimeS"), //
		CompileTimePassOne("compileTimePassOneS"), //
		CompileTimePassTwo("compileTimePassTwoS"), //
		TotalCompileTime("totalCompileTimeS"), //
		CompileOptimized("compileOptimized"), //
		PresolveTime("presolveTimeS"), //
		OriginalConstraints("originalConstraints"), //
		OriginalVariables("originalVariables"), //
		OriginalCoefficients("originalCoefficients"), //
		PresolvedConstraints("presolvedConstraints"), //
		PresolvedVariables("presolvedVariables"), //
		PresolvedCoefficients("presolvedCoefficients"), //
		NumberOfSolutions("numberOfSolutions"), //
		BestObjective("bestObjective"), //
		BestBound("bestBound"), //
		MIPGap("mipGap"), //
		IsSolutionValid("isSolutionValid"), //
		RealObjective("objectiveByValidator"), //
		CompileCrash("isCrashedCompile"), //
		SolveCrash("isCrashedSolve"); //

		private final String name;

		private Columns(String name) {
			this.name = Objects.requireNonNull(name);
		}

		@Override
		public String getColumnName() {
			return this.name;
		}
	}

	public MainCSV() {
		super(Columns.class, Util.getDecimalFormat());
	}

}
