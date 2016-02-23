package config;

public class NoEdgePathDefinedException extends ConfigException {
	private static final long serialVersionUID = 6123927910547218138L;

	public NoEdgePathDefinedException() {
	}
	
	@Override
	public String getMessage() {
		return "Specification does not specify a edge manifest.";
	}
}
