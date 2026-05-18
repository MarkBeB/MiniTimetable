package org.tud.minitimetable.eval.util;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NamedCSV<H extends Enum<H> & org.tud.minitimetable.eval.util.NamedCSV.CSVHeader> extends CSV {

	public static interface CSVHeader {
		String getColumnName();
	}

	public static interface NamedRecord<H> extends CSVRecord {
		String getCell(H column);

		void setCell(H name, Object value);
	}

	public NamedCSV(Class<H> headerRef) {
		this(headerRef, null);
	}

	public NamedCSV(Class<H> headerRef, DecimalFormat formatter) {
		super(formatter);
		setColumnNames(generateHeaders(headerRef));
	}

	protected String[] generateHeaders(Class<H> enums) {
		var constants = enums.getEnumConstants();
		var header = new String[constants.length];
		for (var i = 0; i < constants.length; ++i) {
			header[i] = constants[i].getColumnName();
		}
		return header;
	}

	public String getCellValue(int row, H column) {
		return getCellValue(row, column.getColumnName());
	}

	public void setCellValue(int row, H column, Object value) {
		setCellValue(row, column.getColumnName(), value);
	}

	@Override
	public Stream<NamedRecord<H>> stream() {
		return StreamSupport.stream(
				Spliterators.spliterator(iteratorNamed(), data.size(), Spliterator.ORDERED & Spliterator.SIZED), false);
	}

	public Iterator<NamedRecord<H>> iteratorNamed() {
		return new Iterator<>() {
			private final Iterator<CSVRecord> superIterator = NamedCSV.super.iterator();

			@Override
			public NamedRecord<H> next() {
				return new NamedRecord<>() {

					private final CSVRecord record = superIterator.next();

					@Override
					public int getRowIndex() {
						return record.getRowIndex();
					}

					@Override
					public String[] getRow() {
						return record.getRow();
					}

					@Override
					public String getCell(String name) {
						return record.getCell(name);
					}

					@Override
					public String getCell(H column) {
						return record.getCell(column.getColumnName());
					}

					@Override
					public String getCell(int colIndex) {
						return record.getCell(colIndex);
					}

					@Override
					public void setCell(String name, Object value) {
						record.setCell(name, value);
					}

					@Override
					public void setCell(H column, Object value) {
						record.setCell(column.getColumnName(), value);
					}

					@Override
					public void setCell(int colIndex, Object value) {
						record.setCell(colIndex, value);
					}

				};
			}

			@Override
			public boolean hasNext() {
				return superIterator.hasNext();
			}
		};
	}

}
