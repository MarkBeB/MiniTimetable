package org.tud.minitimetable.extern;

import java.util.Objects;

public class ValidatorCost {
	public String name;
	public int total;
	public int weight;
	public int occurences;

	public ValidatorCost(String name, int total, int weight, int occurences) {
		this.name = name;
		this.total = total;
		this.weight = weight;
		this.occurences = occurences;
	}

	@Override
	public String toString() {
		return "ValidatorCost [" + name + ", total=" + total + ", weight=" + weight + ", occurences=" + occurences
				+ "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, occurences, total, weight);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValidatorCost other = (ValidatorCost) obj;
		return Objects.equals(name, other.name) && occurences == other.occurences && total == other.total
				&& weight == other.weight;
	}

}