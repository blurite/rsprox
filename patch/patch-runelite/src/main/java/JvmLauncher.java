import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * This file should probably be replaced with a .jar directly, so that dependencies can be included.
 * Even better would be to just custom roll a RuneLite Launcher fork / bake it into RSProx's.
 * One thing this definitely isn't doing well, is passing along JVM arguments.
 */
public class JvmLauncher
{
	public static void main(String[] args)
	{
		try
		{
			Path jarPath = Paths.get(JvmLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			File tempDir = getOrCreateUniqueTempDir(jarPath);

			List<File> classpath = new ArrayList<>();
			addUnpackedJarsToClasspath(tempDir, classpath);

			launch(classpath, args);
		}
		catch (Exception e)
		{
			System.err.println("Failed to launch application: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static File getOrCreateUniqueTempDir(Path jarPath) throws IOException, NoSuchAlgorithmException
	{
		String jarHash = computeJarHash(jarPath);

		Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "unpackedDeps_" + jarHash);

		if (!Files.exists(tempDir))
		{
			Files.createDirectories(tempDir);
			unpackDependencies(jarPath, tempDir);
		}

		return tempDir.toFile();
	}

	private static String computeJarHash(Path jarPath) throws IOException, NoSuchAlgorithmException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA-256");

		byte[] fileBytes = Files.readAllBytes(jarPath);
		byte[] hashBytes = digest.digest(fileBytes);

		StringBuilder hashHex = new StringBuilder();
		for (byte b : hashBytes)
		{
			hashHex.append(String.format("%02x", b));
		}

		return hashHex.toString();
	}


	private static void unpackDependencies(Path jarPath, Path outputDir) throws IOException
	{
		try (JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarPath.toFile())))
		{
			JarEntry entry;
			while ((entry = jarInputStream.getNextJarEntry()) != null)
			{
				if (entry.isDirectory() || !entry.getName().endsWith(".jar"))
				{
					continue;
				}

				Path extractedFile = outputDir.resolve(entry.getName());
				if (Files.exists(extractedFile))
				{
					continue; // Skip files that already exist
				}

				Files.createDirectories(extractedFile.getParent());
				Files.copy(jarInputStream, extractedFile, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

	private static void addUnpackedJarsToClasspath(File tempDir, List<File> classpath)
	{
		File[] jarFiles = tempDir.listFiles((dir, name) -> name.endsWith(".jar"));
		if (jarFiles != null)
		{
			classpath.addAll(Arrays.asList(jarFiles));
		}
	}

	private static void launch(List<File> classpath, String[] args) throws IOException, InterruptedException
	{
		String javaExePath = getJava();

		List<String> arguments = new ArrayList<>();
		arguments.add(javaExePath);
		arguments.add("-cp");
		arguments.add(buildClasspath(classpath));
		arguments.add("net.runelite.client.RuneLite");
		arguments.addAll(Arrays.asList(args));
		System.out.println("Launching: " + String.join(" ", arguments));

		ProcessBuilder builder = new ProcessBuilder(arguments);
		builder.inheritIO();
		Process process = builder.start();

		int exitCode = process.waitFor();
		System.exit(exitCode);
	}

	private static String buildClasspath(List<File> classpath)
	{
		StringBuilder classPathBuilder = new StringBuilder();
		for (File file : classpath)
		{
			if (classPathBuilder.length() > 0)
			{
				classPathBuilder.append(File.pathSeparatorChar);
			}
			classPathBuilder.append(file.getAbsolutePath());
		}
		return classPathBuilder.toString();
	}

	private static String getJava() throws IOException
	{
		Path javaHome = Paths.get(System.getProperty("java.home"));
		if (!Files.exists(javaHome))
		{
			throw new IOException("JAVA_HOME is not set correctly! directory \"" + javaHome + "\" does not exist.");
		}

		Path javaPath = javaHome.resolve("bin").resolve("java");
		if (!Files.exists(javaPath))
		{
			javaPath = javaHome.resolve("bin").resolve("java.exe");
		}

		if (!Files.exists(javaPath))
		{
			throw new IOException("Java executable not found in directory \"" + javaPath.getParent() + "\"");
		}

		return javaPath.toAbsolutePath().toString();
	}
}
