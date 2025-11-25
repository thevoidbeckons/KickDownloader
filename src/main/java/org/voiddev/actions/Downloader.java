package org.voiddev.actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class Downloader
{
	String url;
	String id;
	String fileLocation;
	String fileName;

	public Downloader(String url, String id, String fileLocation, String fileName) throws IOException
	{
		this.url = url;
		this.id = id;
		this.fileLocation = fileLocation;
		this.fileName = fileName;
	}

	public List<String> loadSegments(String m3u8Url) throws Exception
	{
		List<String> segments = new ArrayList<>();
		String url2 = m3u8Url.substring(0, m3u8Url.lastIndexOf("/"));
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(new URL(m3u8Url).openStream())))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (!line.startsWith("#"))
				{  // A .ts segment line
					segments.add(url2 + "/" + line);
				}
			}
		}
		return segments;
	}

	public void downloadAllSegments(List<String> urls, String out) throws Exception
	{
		ExecutorService pool = Executors.newFixedThreadPool(16); // 16 parallel downloads
		Path outputDir = Paths.get(out);
		List<Future<?>> futures = new ArrayList<>();
		if (!new File(out).exists())
		{
			new File(out).mkdirs();
		}

		for (String url : urls)
		{
			futures.add(pool.submit(() -> {
				try (InputStream in = new URL(url).openStream())
				{
					String fileName = url.substring(url.lastIndexOf("/") + 1);
					Files.copy(in, outputDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}));
		}

		for (Future<?> f : futures)
		{
			f.get(); // wait for all to finish
		}
		pool.shutdown();
	}

	public void writeConcatFile(List<String> urls, String segmentLocation, String out) throws IOException
	{

		List<String> lines = urls.stream()
			.map(u -> "file '" + segmentLocation + "/" + u.substring(u.lastIndexOf("/") + 1) + "'")
			.collect(Collectors.toList());

		try (FileWriter fw = new FileWriter(out);
			 BufferedWriter bw = new BufferedWriter(fw)
		)
		{
			bw.write(lines.stream().collect(Collectors.joining("\n")));
		}
	}

	public boolean download() throws Exception
	{
		String vid = fileLocation + fileName + ".mp4";
		String segmentLocation = fileLocation + id;
		String txt = fileLocation + id + File.separator + fileName + ".txt";
		String ffmpegPath = "ffmpeg"; // Or the full path to your ffmpeg executable

		List<String> urls = loadSegments(url);
		downloadAllSegments(urls, fileLocation + id);
		writeConcatFile(urls, segmentLocation, txt);

		String[] command = new String[]{
			ffmpegPath,
			"-y",
			"-f",
			"concat",
			"-safe",
			"0",
			"-i",
			txt,
			"-codec",
			"copy",
			vid,
		};

		System.out.println(Arrays.toString(command));
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.inheritIO(); // Redirect FFmpeg's output to console

		// Start the process
		Process process = processBuilder.start();

		// Wait for the process to complete
		int exitCode = process.waitFor();

		if (exitCode == 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean cleanup()
	{
		String segmentLocation = fileLocation + id;
		File f = new File(segmentLocation);
		boolean exists = f.exists();
		return exists && deleteDirectory(f);
	}

	boolean deleteDirectory(File directoryToBeDeleted)
	{
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null)
		{
			for (File file : allContents)
			{
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}
}
