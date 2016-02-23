package config;

public class InvalidGridException extends ConfigException {
	private static final long serialVersionUID = -71356792653084412L;
	
	public InvalidGridException() {
	}
	
	@Override
	public String getMessage() {
		return String.format("Invalid XML node: subtraction grid does not have length 64");
	}
}
