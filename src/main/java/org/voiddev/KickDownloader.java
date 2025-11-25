package org.voiddev;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.voiddev.actions.Downloader;
import org.voiddev.api.KickScraper;

public class KickDownloader
{
	static String VODSPOT = "";

	public static void main(String[] args)
	{
		String channelName = "";
		int count = 1;
		if (args.length == 3)
		{
			channelName = args[0];
			VODSPOT = args[1];
			if (!VODSPOT.endsWith(File.separator))
			{
				VODSPOT = VODSPOT + File.separator;
			}
			try
			{
				count = Integer.parseInt(args[2]);
			}
			catch (Exception e)
			{
				System.out.println("The third argument must be a number" + args[2]);
			}
		}
		else
		{
			System.out.println("Need 3 args: channel filelocation count");
			return;
		}

		if (count < 1 || count > 10)
		{
			System.out.println("The third argument must be a number between 1 and 10");
		}

		if (!new File(VODSPOT).exists())
		{
			new File(VODSPOT).mkdirs();
		}

		// Playwright is Microsoft's pupeteer, some type of browser tool
		try (Playwright playwright = Playwright.create())
		{
			// prep browser
			Browser browser = playwright.chromium().launch();
			KickScraper kickScraper = new KickScraper(browser, new BrowserType.LaunchOptions().setHeadless(true));

			// get scraped data
			JsonArray jo = kickScraper.scrapeData(getChannelURL(channelName));
			JsonArray jsonArray = new JsonArray();

			// loop through the vods
			for (int i = 0; i < jo.size() && jsonArray.size() < count; i++)
			{
				JsonObject obj = jo.get(i).getAsJsonObject();

				// skip live vods
				if (obj.has("is_live") && !obj.get("is_live").getAsBoolean())
				{
					String id = obj.get("id").getAsString();

					// if the id of vod has ran before, skip it
					if (!idRan(id))
					{
						jsonArray.add(obj);
						System.out.println("Going to run on: " + id);
					}
				}
			}

			// CompleteableFutures is to async the downloads
			ArrayList<CompletableFuture> completableFutures = new ArrayList<>();

			// loop through the found vods to process
			for (int i = 0; i < jsonArray.size() && i < count; i++)
			{
				JsonObject obj = jsonArray.get(i).getAsJsonObject();

				// async call the download on the vod
				completableFutures.add(
					CompletableFuture.runAsync(() -> {

						String title = obj.get("session_title").getAsString();
						String source = obj.get("source").getAsString();
						String id = obj.get("id").getAsString();
						source = source.replace("hls/master.m3u8", "hls/720p30/playlist.m3u8");
						try
						{
							String videoTitle = title + "_" + obj.get("start_time").getAsString();
							videoTitle = videoTitle.replaceAll("[^a-zA-Z0-9._-]", "_");
							Downloader downloader = new Downloader(source, id, VODSPOT, videoTitle);

							boolean success = downloader.download();
							if (success)
							{
								updateFile(id);
								System.out.println("Download success: " + id);
								if (!downloader.cleanup())
								{
									throw new RuntimeException("Failed to clean up downloader");
								}
							}
							else
							{
								System.out.println("Download failed: " + id);
							}
						}
						catch (Exception e)
						{
							System.out.println("Error downloading video: " + id);
							System.out.println("Error: " + e.getMessage());
							e.printStackTrace();
						}
					})
				);

			}
			if (completableFutures.size() > 0)
			{
				System.out.println("Count: " + completableFutures.size());
				allDone(completableFutures);
				System.out.println("Done");
			}
			else
			{
				System.out.println("No new VODs found");
			}
		}
	}

	// When all vods are downloaded from the async, it will exit.
	private static void allDone(ArrayList<CompletableFuture> completableFutures)
	{
		for (CompletableFuture future : completableFutures)
		{
			future.join();
		}
	}

	// returns the formatted URL from a provided kick username
	private static String getChannelURL(String channelName)
	{
		return "https://kick.com/api/v2/channels/" + channelName + "/videos?cursor=0&sort=date&time=all";
	}

	// read from a file to recall if a vod id has been processed already
	private static boolean idRan(String id)
	{
		try
		{
			String fileContent = Files.readString(Path.of(VODSPOT + "processedVideos.txt"));
			return fileContent.contains(id);
		}
		catch (Exception e)
		{
			return false;
		}
	}

	// write to a file to keep track of what file was already worked on
	private synchronized static void updateFile(String id)
	{
		try
		{
			FileWriter fileWriter = new FileWriter(VODSPOT + "processedVideos.txt", true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write(id);
			bufferedWriter.newLine(); // Writes a platform-specific new line character

			bufferedWriter.close(); // Closes the buffered writer, which also closes the underlying FileWriter
		}
		catch (IOException e)
		{
			System.err.println("An error occurred while writing with BufferedWriter: " + e.getMessage());
		}
	}
}