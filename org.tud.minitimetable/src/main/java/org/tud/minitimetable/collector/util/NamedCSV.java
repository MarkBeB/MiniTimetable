package org.tud.minitimetable.collector.util;

import java.text.DecimalFormat;
import java.util.Iterator;

public class NamedCSV<H extends Enum<H> & org.tud.minitimetable.collector.util.NamedCSV.CSVHeader> extends CSV {

	public static interface CSVHeader {
		String getColumnName();
	}

	public static interface NamedRecord<H> extends CSVRecord {
		String getField(H column);

		void setField(H name, Object value);
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
					public String getField(String name) {
						return record.getField(name);
					}

					@Override
					public String getField(H column) {
						return record.getField(column.getColumnName());
					}

					@Override
					public String getField(int colIndex) {
						return record.getField(colIndex);
					}

					@Override
					public void setField(String name, Object value) {
						record.setField(name, value);
					}

					@Override
					public void setField(H column, Object value) {
						record.setField(column.getColumnName(), value);
					}

					@Override
					public void setField(int colIndex, Object value) {
						record.setField(colIndex, value);
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
