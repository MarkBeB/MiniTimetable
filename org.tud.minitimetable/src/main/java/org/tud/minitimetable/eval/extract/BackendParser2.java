package org.tud.minitimetable.eval.extract;

import java.io.BufferedReader;

import org.tud.minitimetable.eval.util.StateParser;

@Deprecated
public class BackendParser2 extends StateParser {

	public BackendParser2(BufferedReader reader) {
		super(reader);

		this.addHandler(new ConditionalHandler2(p -> getCurrentLine().equals("A"), p -> System.out.println()));

	}

}
