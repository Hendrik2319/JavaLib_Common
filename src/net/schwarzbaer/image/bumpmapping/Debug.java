package net.schwarzbaer.image.bumpmapping;

public class Debug {
	static void Assert(boolean condition) {
		if (!condition) throw new IllegalStateException();
	}
}
