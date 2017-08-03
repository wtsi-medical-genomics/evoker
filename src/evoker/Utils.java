package evoker;

import java.nio.file.Paths;

public final class Utils {
	public static String join(String path, String filename) {
		return Paths.get(path, filename).toString();
	}
}
