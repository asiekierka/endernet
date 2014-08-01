package pl.asie.endernet.api;

public interface IEnderRedstone {
	/**
	 * Get the entity's redstone value.
	 * @return The public-facing redstone value.
	 */
	public int getRedstoneValue();
	
	/**
	 * Set the redstone value.
	 * @param value Current redstone value.
	 * @return Whether the internal value changed.
	 */
	public boolean setRedstoneValue(int value);
	
	/**
	 * Set the redstone value if received externally.
	 * @param value New redstone value.
	 * @return Whether the internal value changed.
	 */
	public boolean receiveRedstoneValue(int value);
}
