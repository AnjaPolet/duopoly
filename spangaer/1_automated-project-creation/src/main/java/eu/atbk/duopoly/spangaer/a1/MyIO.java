package eu.atbk.duopoly.spangaer.a1;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around NIO fileops with some safety extras. Yes could have used commons IO utilities here as well (probably).
 * 
 * @author jede
 *
 */
public class MyIO {
	private static final Logger log = LoggerFactory.getLogger(MyIO.class);

	/**
	 * Copy given source to given target recursively (in case it's a directory)<br>
	 * The copy will fail if some of the files exist already.
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	public static void copyRecursive(@Nonnull Path source, @Nonnull Path target) throws IOException {

		if (Files.isRegularFile(source)) {
			copySingle(source, target);

		} else {

			// This is the minimal possible effort copy
			// at least from an operating system perspective
			// each source file will be touched only once

			// a recuresive method calling in to directories would work just the same
			// but this is slightly more modern
			Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path current, BasicFileAttributes attrs) throws IOException {
					super.visitFile(current, attrs);
					Path relative = source.relativize(current);
					Path copyTarget = target.resolve(relative);

					copySingle(current, copyTarget);

					return FileVisitResult.CONTINUE;
				}

			});
		}
	}

	/**
	 * Will copy a single file or directory, ensuring that the target directory exists.<br>
	 * The copy will fail if the file exists already.
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	public static void copySingle(@Nonnull Path source, @Nonnull Path target) throws IOException {
		log.trace("Copying from {} to {}", source, target);
		// ensure parent dir exist before copy
		Files.createDirectories(target.getParent());
		Files.copy(source, target);
	}
}
