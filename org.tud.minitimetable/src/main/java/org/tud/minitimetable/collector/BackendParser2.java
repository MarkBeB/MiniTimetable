package org.tud.minitimetable.collector;

import java.io.BufferedReader;

import org.tud.minitimetable.util.StateParser;

public class BackendParser2 extends StateParser {

	public BackendParser2(BufferedReader reader) {
		super(reader);

		this.addHandler(new ConditionalHandler2(p -> getCurrentLine().equals("A"), p -> System.out.println()));

	}

}
