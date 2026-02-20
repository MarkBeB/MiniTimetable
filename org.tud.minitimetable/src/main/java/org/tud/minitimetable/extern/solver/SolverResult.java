package org.tud.minitimetable.extern.solver;

import java.util.List;
import java.util.Objects;

public class SolverResult {
	public final SolutionStatus status;
	public final List<Solution> solutions;

	public SolverResult(SolutionStatus status, List<Solution> solutions) {
		this.status = status;
		this.solutions = Objects.requireNonNull(solutions, "solutions");
	}

}