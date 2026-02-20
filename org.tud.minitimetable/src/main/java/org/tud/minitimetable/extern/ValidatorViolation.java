package org.tud.minitimetable.extern;

import java.util.Objects;

public class ValidatorViolation {
	public final String type;
	public final int violations;

	public ValidatorViolation(String type, int violations) {
		this.type = Objects.requireNonNull(type);
		this.violations = violations;
	}

	@Override
	public String toString() {
		return "ValidatorViolation [type=" + type + ", violations=" + violations + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, violations);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValidatorViolation other = (ValidatorViolation) obj;
		return Objects.equals(type, other.type) && violations == other.violations;
	}

}