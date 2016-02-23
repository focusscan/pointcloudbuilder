package config;

public class NoFormatSpecifiedException extends ConfigException {
	private static final long serialVersionUID = -19659234789567892L;
	
	public NoFormatSpecifiedException() {
	}
	
	@Override
	public String getMessage() {
		return String.format("Invalid XML node: no format specified for output");
	}
}
