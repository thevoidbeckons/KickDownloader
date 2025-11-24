package org.voiddev.actions;

import java.io.IOException;

public class Downloader
{
	String url;
	String filename;

	public Downloader(String url, String fileName) throws IOException
	{
		this.url = url;
		this.filename = fileName;
	}

	public boolean download() throws Exception
	{
		String ffmpegPath = "ffmpeg"; // Or the full path to your ffmpeg executable
		String[] command = new String[]{
			ffmpegPath,
			"-y",
			"-i",
			url,
			"-threads",
			"0",
			"-c:a",
			"copy",
			filename,
		};

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
}
