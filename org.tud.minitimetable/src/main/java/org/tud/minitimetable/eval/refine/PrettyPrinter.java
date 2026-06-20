package org.tud.minitimetable.eval.refine;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.function.Supplier;

import org.tud.minitimetable.eval.util.CSV;

public class PrettyPrinter {

	public static interface Pretty<P> {
		P pretty(P value);
	}

	public static interface PrettyCell extends Pretty<String> {

	}

	public static interface PrettyRow extends Pretty<String[]> {

	}

	public static interface PrettyColumn extends Pretty<String[]> {

	}

	public static class PrettyNumberFormat implements PrettyCell {

		private final NumberFormat oldFormat;
		private final NumberFormat newFormat;

		public PrettyNumberFormat(NumberFormat oldFormat, NumberFormat newFormat) {
			this.oldFormat = Objects.requireNonNull(oldFormat);
			this.newFormat = Objects.requireNonNull(newFormat);
		}

		@Override
		public String pretty(String value) {
			try {
				var number = oldFormat.parse(value);
				return newFormat.format(number);
			} catch (NumberFormatException | ParseException e) {
				var s = new NumberFormatException("Failed to parse: " + value);
				s.addSuppressed(e);
				throw s;
			}
		}
	}

	private final Supplier<CSV> csvSupplier;
	private final PrettyRow header;
	private final Pretty<?>[] columns;

	public PrettyPrinter(Supplier<CSV> csvSupplier, PrettyRow header, Pretty<?>[] columns) {
		this.csvSupplier = Objects.requireNonNull(csvSupplier);
		this.header = header;
		this.columns = columns;
	}

	public CSV pretty(CSV csv) {
		if (columns.length != csv.getNumberOfColumns())
			throw new IllegalArgumentException();

		CSV formatted = csvSupplier.get();

		var originalColumns = csv.getColumnNames();
		if (header != null) {
			var newColumns = header.pretty(originalColumns);
			formatted.setColumnNames(newColumns);
		} else {
			formatted.setColumnNames(originalColumns);
		}

		var nCol = csv.getNumberOfColumns();
		var nRow = csv.getNumberOfRows();

		for (var c = 0; c < nCol; c++) {
			if (columns[c] instanceof PrettyColumn pcolumn) {
				String[] allRows = new String[(int) nRow];
				for (var r = 0; r < nRow; ++r)
					allRows[r] = csv.getCellValue(r, c);

				String[] prettyRows = pcolumn.pretty(allRows);
				for (var r = 0; r < nRow; ++r)
					formatted.setCellValue(r, c, prettyRows[r]);

			} else if (columns[c] instanceof PrettyCell pcell) {
				for (var r = 0; r < nRow; ++r)
					formatted.setCellValue(r, c, pcell.pretty(csv.getCellValue(r, c)));

			} else {
				for (var r = 0; r < nRow; ++r)
					formatted.setCellValue(r, c, csv.getCellValue(r, c));

			}
		}

		return formatted;
	}

}
