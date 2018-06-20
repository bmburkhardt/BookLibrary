package application.util;

import java.time.LocalDate;

import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class BirthdayConverter extends StringConverter<LocalDate> implements PropertyValidator<LocalDate> {
	private TextField field;
	private LocalDate lastValid = null;
	
	public BirthdayConverter(TextField field) {
		this.field = field;
	}

	@Override
	public String toString(LocalDate obj) {
		lastValid = obj;
		
		return obj != null ? obj.toString() : "";
	}

	@Override
	public LocalDate fromString(String string) {
		try {
			LocalDate ld = LocalDate.parse(string);
			if(validate(ld)) {
				showError(false);
				return ld;// (lastValid = ld);
			} else {
				showError(true);
				return ld;
			}
		} catch(Exception e) {
			showError(true);
			return null; //lastValid;
		}
	}

	@Override
	public boolean validate(LocalDate obj) {
		return obj.isBefore(LocalDate.now());
	}
	
	@Override
	public void revert() {
		this.field.setText(toString(lastValid));
	}

	@Override
	public void showError(boolean shouldShow) {
		if(shouldShow)
			this.field.setStyle("-fx-border-color: #FF0000");
		else
			this.field.setStyle(null);
	}
}
