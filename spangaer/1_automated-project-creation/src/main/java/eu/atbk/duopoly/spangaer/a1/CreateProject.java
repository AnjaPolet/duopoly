package eu.atbk.duopoly.spangaer.a1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.atbk.duopoly.spangaer.a1.MyIO.copyRecursive;

/**
 * Will create a project given a chosen Java version, a chosen group id 'subdomain' and a artifact ID. The chosen
 * subdomain will also be used as folder to create the project in.<br>
 * <br>
 * It is intentionally implemented using the NIO2 file/path API to allow maximal propagation of operations to the
 * operating system.
 * 
 * @author spangaer
 *
 */
public class CreateProject {

	private static final Logger log = LoggerFactory.getLogger(CreateProject.class);

	/**
	 * Use relatavive path to navigate to parent of parent of current dir. Also turn in to fully path without relative
	 * parts.
	 */
	private static final Path REPO_ROOT = Paths.get("../..").toAbsolutePath().normalize();
	/**
	 * Templates directory
	 */
	private static final Path TEMPLATES = REPO_ROOT.resolve("templates");
	/**
	 * Format string for selecting template
	 */
	private static final String TEMPLATE_NAME_FORMAT = "template-java%d";

	/**
	 * For named selection group in regex
	 */
	private static final String PREFIX = "prefix";
	/**
	 * For named selection group in regex
	 */
	private static final String POSTFIX = "postfix";

	/**
	 * cover all types of spaces, include tabs and new lines
	 */
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\p{Space}");
	/**
	 * match <code><groupId>eu.atbk.duopoly</groupId></code><br>
	 * Consult {@link Pattern} javadocs for syntax details
	 */
	private static final Pattern GROUP_PATTERN = Pattern
			.compile("(?<prefix>\\p{Space}<groupId>eu.atbk.duopoly)(?<postfix></groupId>)");
	/**
	 * match <code><artifactId>template-java8</artifactId></code>
	 *
	 */
	private static final Pattern ARTIFACT_PATTERN = Pattern
			.compile("(?<prefix>\\p{Space}<artifactId>)template-java[7,8](?<postfix></artifactId>)");

	public static void main(String[] args) {
		// base validate input
		if (args.length != 3) {
			log.error("3 input arguments required got {}", args.length);
			System.exit(1);// signal error exit
		}

		// simple parse
		int java = Integer.parseInt(args[0]);
		String domainExtension = args[1].trim(); // remove leading and trailing spaces
		String artifactID = args[2].trim();

		try {
			createProject(java, domainExtension, artifactID);
		} catch (Exception e) {
			// log if something goes wrong, even if unexpected
			log.error("copy failed", e);
			System.exit(1); // don't hide error state
		}

	}

	/**
	 * Create project from template. Can be used from this program or other code
	 * 
	 * @param javaVersion
	 *            Java version 7 or 8
	 * @param domainExtension
	 *            cannot be null, cannot contain spaces
	 * @param artifactId
	 *            cannot be null, cannot contain spaces
	 * @throws IOException
	 */
	public static void createProject(int javaVersion, @Nonnull String domainExtension, @Nonnull String artifactId)
			throws IOException {
		if (javaVersion != 7 && javaVersion != 8)
			throw new IllegalArgumentException("Java version needs to be 7 or 8, got " + javaVersion);

		// make sure there's not spaces inside the stings
		if (SPACE_PATTERN.matcher(domainExtension).find() || SPACE_PATTERN.matcher(artifactId).find()) {
			throw new IllegalArgumentException("domainExtension or artifactId may not contain space characters");
		}

		log.info("Will create a Java {} project {} for {}", javaVersion, artifactId, domainExtension);
		Path projectRoot = copyTemplate(javaVersion, domainExtension, artifactId);
		updatePom(projectRoot, domainExtension, artifactId);
	}

	/**
	 * @param javaVersion
	 * @param domainExtension
	 * @param artifactId
	 * @return the created project root
	 * @throws IOException
	 */
	private static Path copyTemplate(int javaVersion, String domainExtension, String artifactId) throws IOException {
		Path source = TEMPLATES.resolve(String.format(TEMPLATE_NAME_FORMAT, javaVersion));
		Path target = REPO_ROOT.resolve(domainExtension).resolve(artifactId);

		log.trace("Template copy from {} to {}", source, target);
		copyRecursive(source, target);
		return target;

	}

	private static void updatePom(Path projectRoot, String domainExtension, String artifactId) throws IOException {
		/*
		 We could be using XML, Xpath or stream procesing to update the pom, but we know what the input is and
		 we know it's safe to manipulate it on a by line basis. So we're just going to use regexes for this.
		 
		 This trick will fail if there are references elsewhere to this group or artifact id, e.g. in the
		 dependencies.
		  */

		Path pomPath = projectRoot.resolve("pom.xml");

		// read all lines and transform the relevant lines to their new content already
		List<String> newPom = Files.lines(pomPath, StandardCharsets.UTF_8).map(line -> {
			Matcher groupMatch = GROUP_PATTERN.matcher(line);
			if (groupMatch.matches()) {
				// extend group id
				return groupMatch.group(PREFIX) + "." + domainExtension + groupMatch.group(POSTFIX);
			}

			Matcher artMatch = ARTIFACT_PATTERN.matcher(line);
			if (artMatch.matches()) {
				// replace artifact id
				return artMatch.group(PREFIX) + artifactId + artMatch.group(POSTFIX);
			}

			// no match do nothing
			return line;
		}).collect(Collectors.toList());

		if (log.isTraceEnabled()) {
			// this is an expensive operation, so only do it if trace is enabled

			// collect in to single string for logging, add empty string to insert new line
			String body = Stream.concat(Stream.of(""), newPom.stream()).collect(
					Collectors.joining(System.lineSeparator()));
			log.trace(body);
		}

		// write the pom data back to file
		Files.write(pomPath, newPom, StandardCharsets.UTF_8);
	}
}
