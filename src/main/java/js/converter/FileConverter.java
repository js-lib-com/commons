package js.converter;

import java.io.File;

import js.util.Files;

/**
 * File converter.
 * 
 * @author Iulian Rotaru
 */
final class FileConverter implements Converter {
	/** Package default constructor. */
	FileConverter() {
	}

	/** Return file instance for the given path string. */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T asObject(String string, Class<T> valueType) {
		// at this point value type is guaranteed to be a File
		return (T) new File(string);
	}

	/** Return file object path. */
	@Override
	public String asString(Object object) {
		// at this point object is guaranteed to be a File
		return Files.path2unix(((File) object).getPath());
	}
}
