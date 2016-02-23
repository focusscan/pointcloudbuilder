package config;

public class NoDistPathDefinedException extends ConfigException {
	private static final long serialVersionUID = 68698357357354351L;

	public NoDistPathDefinedException() {
	}
	
	@Override
	public String getMessage() {
		return "Specification does not specify a distance manifest.";
	}
}
