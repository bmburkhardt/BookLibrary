package application.util;

import java.time.LocalDateTime;

import javafx.util.StringConverter;

public class DateTimeConverter extends StringConverter<LocalDateTime> {
	public DateTimeConverter() { }

	@Override
	public String toString(LocalDateTime obj) {
		return obj != null ? obj.toString() : LocalDateTime.now().toString();
	}

	@Override
	public LocalDateTime fromString(String string) {
		try {
			LocalDateTime ld = LocalDateTime.parse(string);
			return ld;
		} catch(Exception e ) {
			return null;
		}
	}
}
