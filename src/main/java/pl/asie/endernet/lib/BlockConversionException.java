package pl.asie.endernet.lib;

public class BlockConversionException extends Exception {
	public BlockConversionException(String modId, String name, String info) {
		super("Error converting to EnderID: " + info + "(mod "+modId+", name "+name+")");
	}
}
