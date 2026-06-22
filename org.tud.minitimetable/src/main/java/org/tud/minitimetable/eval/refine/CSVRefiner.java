package org.tud.minitimetable.eval.refine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.function.Supplier;

import org.tud.minitimetable.eval.util.CSV;

public class CSVRefiner {

	public static interface Refinement {

		Collection<String> headers();

		public void refine(CSV output, String groupName, int rowIndex, List<CSV.CSVRecord> datapoints);
	}

	private final List<Refinement> refinements = new ArrayList<>();
	private final Supplier<CSV> csvSupplier;

	public CSVRefiner(Supplier<CSV> csvSupplier) {
		this.csvSupplier = Objects.requireNonNull(csvSupplier);
	}

	public void addRefinement(Refinement refinement) {
		this.refinements.add(Objects.requireNonNull(refinement, "refinement"));
	}

	public CSV refine(Map<String, List<CSV.CSVRecord>> groups, Iterable<String> groupOrder) {

//		List<String> headers = new ArrayList<>(1 + refinements.size());
		SequencedCollection<String> headers = new LinkedHashSet<>(1 + refinements.size());

		for (Refinement refinement : refinements)
			headers.addAll(refinement.headers());

		CSV output = csvSupplier.get();
		output.setColumnNames(headers.toArray(n -> new String[n]));

		for (String groupName : groupOrder) {
			int rowIndex = output.addNewRow();
			List<CSV.CSVRecord> datapoints = groups.get(groupName);
			for (Refinement refinement : refinements)
				refinement.refine(output, groupName, rowIndex, datapoints);
		}

		return output;
	}
}