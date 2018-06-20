package application.util;

import java.time.LocalDate;

import javafx.util.StringConverter;

public class DateConverter extends StringConverter<LocalDate> {
	public DateConverter() { }

	@Override
	public String toString(LocalDate obj) {
		return obj != null ? obj.toString() : LocalDate.now().toString();
	}

	@Override
	public LocalDate fromString(String string) {
		try {
			LocalDate ld = LocalDate.parse(string);
			return ld;
		} catch(Exception e ) {
			return null;
		}
	}
}
