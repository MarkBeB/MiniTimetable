package org.tud.minitimetable.extern.validator;

import java.util.ArrayList;
import java.util.List;

public class ValidatorResult {
	public final List<ValidatorViolation> violations = new ArrayList<>(9);
	public final List<ValidatorCost> costs = new ArrayList<>(8);

	public int getTotalViolations() {
		int sum = 0;
		for (var violation : violations) {
			sum += violation.violations;
		}
		return sum;
	}

	public boolean hasAnyViolations() {
		for (var violation : violations)
			if (violation.violations > 0)
				return true;
		return false;
	}

	public int getTotalCost() {
		int sum = 0;
		for (var cost : costs) {
			sum += cost.total;
		}
		return sum;
	}
}