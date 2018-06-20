package application.util;

public interface PropertyValidator<T> {
	public boolean validate(T object);
	
	public void revert();
	
	public void showError(boolean shouldShow);
}
