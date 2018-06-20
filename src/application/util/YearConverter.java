package application.util;

import javafx.util.converter.NumberStringConverter;

public class YearConverter extends NumberStringConverter {

	@Override
	public String toString(Number obj) {
		return obj != null ? obj.toString() : "";
	}

	@Override
	public Number fromString(String string) {
		try {
			return Integer.parseInt(string);
		} catch(Exception e) {
			return 0;
		}
	}
}
