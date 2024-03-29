package com.jslib.io;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.jslib.converter.Converter;
import com.jslib.converter.ConverterRegistry;
import com.jslib.util.Files;

/**
 * Files archive output stream. This class works together with {@link FilesInputStream} to transfer files archives. A files
 * archive is a hierarchical files collections bundled with meta data. Archive is compressed using ZIP lossless data compression
 * and meta is carried by a standard Java JAR Manifest. This class gets and output stream and inject meta data and files, on the
 * fly, see {@link #putMeta(String, Object)} and {@link #addFiles(File)} and related.
 * 
 * <p>
 * Common use case is with URL connection and Servlet HTTP request but is not limited to this scenario. Input stream for this
 * class could be a file archive stored on disk, but file content should be generated by files output stream. Both output and
 * input streams are processed on the fly.
 * 
 * <pre>
 * FilesOutputStream outputFiles = new FilesOutputStream(connection.getOutputStream());
 * outputFiles.putMeta(&quot;Base-Directory&quot;, baseDir);
 * outputFiles.addFiles(new File(&quot;source-directory&quot;));
 * outputFiles.close();
 * </pre>
 * 
 * <pre>
 * FilesInputStream inputFiles = new FilesInputStream(httpRequest.getInputStream());
 * File baseDir = inputFiles.getMeta(&quot;Base-Directory&quot;, File.class);
 * for (File file : inputFiles) {
 * 	File targetFile = new File(targetDir, file.getPath());
 * 	files.copy(targetFile);
 * }
 * inputFiles.close();
 * </pre>
 * 
 * <p>
 * Manifest is mandatory even if no meta data is present and is always the first entry in archive. There is a single predefined
 * attribute, the implementation version but application level meta data is supported. Files output stream can put arbitrary
 * meta data using {@link #putMeta(String, Object)}. On receive side, this files archive input stream can retrieve application
 * meta data uing {@link FilesInputStream#getMeta(String)} and related methods.
 * 
 * @author Iulian Rotaru
 */
public class FilesOutputStream extends OutputStream implements Closeable {
	/** Files archive implementation version. */
	private static final String VERSION = "1.0";

	/** The size of the buffer used for internal bytes processing. */
	private static final int BUFFER_SIZE = 4 * 1024;

	/** Output stream where meta data and files are to be injected. */
	private ZipOutputStream filesArchive;

	/**
	 * This archive manifest. It has at least archive version attribute and is updated by {@link #putMeta(String, Object)}. Is
	 * lazily written to this archive just before injecting first file content.
	 */
	private Manifest manifest;

	/**
	 * Construct files archive wrapping an output stream where archive bytes are injected.
	 * 
	 * @param filesArchive output stream to files archive to write to.
	 */
	public FilesOutputStream(OutputStream filesArchive) {
		this.filesArchive = new ZipOutputStream(filesArchive);
		this.manifest = new Manifest();
		Attributes attributes = this.manifest.getMainAttributes();
		attributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), VERSION);
	}

	/** Write byte to files archive. */
	@Override
	public void write(int b) throws IOException {
		filesArchive.write(b);
	}

	/**
	 * Put meta data to this archive manifest. If <code>key</code> already exists is overridden. Meta <code>value</code> is
	 * converted to string and can be any type for which there is a {@link Converter} registered.
	 * 
	 * @param key meta data key,
	 * @param value meta data value.
	 */
	public void putMeta(String key, Object value) {
		manifest.getMainAttributes().putValue(key, ConverterRegistry.getConverter().asString(value));
	}

	/**
	 * Add files hierarchy to this archive. Traverse all <code>baseDir</code> directory files, no matter how deep hierarchy is
	 * and delegate {@link #addFile(File)}.
	 * 
	 * @param baseDir files hierarchy base directory.
	 * @throws IOException if archive writing operation fails.
	 */
	public void addFiles(File baseDir) throws IOException {
		for (String file : FilesIterator.getRelativeNamesIterator(baseDir)) {
			addFileEntry(file, new FileInputStream(new File(baseDir, file)));
		}
	}

	/**
	 * Add files list to this archive. Files from list are relative to given base directory. For every file create a new entry
	 * into this ZIP archive with file name as entry name. As mentioned file/entry name is relative to base directory and does
	 * not start with it. For example, bee it <code>/var/www/</code> a base directory and an item from files list
	 * <code>site/index.htm</code>; resulting entry name is <code>site/index.htm</code> and file content is read from
	 * <code>/var/www/site/index.htm</code>.
	 * 
	 * @param baseDir files base directory,
	 * @param fileNames file names list.
	 * @throws FileNotFoundException if file not found on <code>baseDir</code> descendants.
	 * @throws IOException if writing to archive fails.
	 */
	public void addFiles(File baseDir, List<String> fileNames) throws FileNotFoundException, IOException {
		for (String fileName : fileNames) {
			addFileEntry(fileName, new FileInputStream(new File(baseDir, fileName)));
		}
	}

	/**
	 * Inject file content into archive using file relative path as archive entry name. File to add is relative to a common base
	 * directory. All files from this archive should be part of that base directory hierarchy; it is user code responsibility to
	 * enforce this constrain.
	 * 
	 * @param file file to add to this archive.
	 * @throws IllegalArgumentException if <code>file</code> does not exist or is a directory.
	 * @throws IOException if archive writing operation fails.
	 */
	public void addFile(File file) throws IOException {
		if (!file.exists()) {
			throw new IllegalArgumentException(String.format("File |%s| does not exist.", file));
		}
		if (file.isDirectory()) {
			throw new IllegalArgumentException(String.format("File |%s| is a directory.", file));
		}
		addFileEntry(file.getPath(), new FileInputStream(file));
	}

	/**
	 * Close this files output stream and takes care to write manifest if no files was added. Manifest is written lazily just
	 * before first file entry. For empty files stream we still need to send manifest and this method ensure that.
	 */
	@Override
	public void close() throws IOException {
		// ensure manifest is written even if no files added
		if (manifest != null) {
			addManifestEntry();
		}
		super.close();
	}

	/**
	 * Add manifest entry to this files output stream. This method reset {@link #manifest} to null signaling manifest was
	 * processed.
	 * 
	 * @throws IOException if manifest entry write fails.
	 */
	private void addManifestEntry() throws IOException {
		ZipEntry entry = new ZipEntry(JarFile.MANIFEST_NAME);
		filesArchive.putNextEntry(entry);
		try {
			manifest.write(this);
		} finally {
			filesArchive.closeEntry();
			manifest = null;
		}
	}

	/**
	 * Add file entry to this files archive. This method takes care to lazily write manifest just before first file entry.
	 * 
	 * @param entryName entry name,
	 * @param inputStream file content.
	 * @throws IOException if file entry write fails.
	 */
	private void addFileEntry(String entryName, InputStream inputStream) throws IOException {
		// write lazily the manifest before adding the first file
		if (manifest != null) {
			addManifestEntry();
		}

		ZipEntry entry = new ZipEntry(entryName);
		filesArchive.putNextEntry(entry);

		inputStream = new BufferedInputStream(inputStream);
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			for (;;) {
				int bytesRead = inputStream.read(buffer);
				if (bytesRead <= 0) {
					break;
				}
				write(buffer, 0, bytesRead);
			}
		} finally {
			Files.close(inputStream);
			filesArchive.closeEntry();
		}
	}
}
