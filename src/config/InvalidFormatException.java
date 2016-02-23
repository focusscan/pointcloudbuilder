package config;

public class InvalidFormatException extends ConfigException {
	private static final long serialVersionUID = -46567043223337877L;
	private final String format;
	
	public InvalidFormatException(String f) {
		format = f;
	}
	
	@Override
	public String getMessage() {
		return String.format("Invalid XML node: format %s is invalid", format);
	}
}
