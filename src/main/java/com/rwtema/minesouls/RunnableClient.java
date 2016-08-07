package com.rwtema.minesouls;

public interface RunnableClient extends Runnable {
	// Default implementation that will run on servers when we @SideOnly(Side.CLIENT) the main method
	@Override
	default void run() {

	}
}
