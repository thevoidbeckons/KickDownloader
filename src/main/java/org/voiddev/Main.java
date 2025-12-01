package org.voiddev;

import java.io.File;
import java.util.ArrayList;

public class Main
{

	public static void main(String[] args)
	{
		// hard code kick as service for now
		String service = "kick";

		ArrayList<String> channelNameAL = new ArrayList<>();
		ArrayList<String> vodSpotAL = new ArrayList<>();
		ArrayList<String> vodCountAL = new ArrayList<>();
		ArrayList<String> destination = null;

		for (String arg : args)
		{
			if ("-channel".equals(arg))
			{
				destination = channelNameAL;
			}
			else if ("-dir".equals(arg))
			{
				destination = vodSpotAL;
			}
			else if ("-count".equals(arg))
			{
				destination = vodCountAL;
			}
			else if (destination == null)
			{
				System.out.printf("Invalid argument: %s%n", arg);
			}
			else
			{
				destination.add(arg);
			}
		}
		int count = 0;
		String vodSpot;
		if (vodCountAL.size() == 1)
		{
			try
			{
				count = Integer.parseInt(vodCountAL.get(0));
			}
			catch (NumberFormatException e)
			{
				System.out.printf("Invalid argument: '%s' has to be a number for -count", vodCountAL.get(0));
				return;
			}
		}

		if (vodCountAL.size() > 1)
		{
			System.out.print("Only have one value for -count");
			return;
		}

		if (channelNameAL.isEmpty())
		{
			System.out.println("Invalid argument: '-channel' has to be a list of channel names");
			return;
		}

		if (vodSpotAL.size() != 1)
		{
			System.out.println("Invalid argument: '-dir' has to be a directory for vods to read/write to");
			return;
		}

		vodSpot = vodSpotAL.get(0);
		if (!vodSpot.endsWith(File.separator))
		{
			vodSpot += File.separator;
		}
		if (!new File(vodSpot).exists())
		{
			new File(vodSpot).mkdirs();
		}
		if (!new File(vodSpot).exists() || !new File(vodSpot).canWrite() || !new File(vodSpot).canRead() || !new File(vodSpot).isDirectory())
		{
			System.out.println("Invalid argument: '-dir' has to be a directory for vods to read/write to");
			return;
		}

		if (service == null || service.length() == 0)
		{
			System.out.println("The service name is required");
			return;
		}

		if (service.equalsIgnoreCase("kick") || service.equalsIgnoreCase("k"))
		{
			for (String channelName : channelNameAL)
			{
				KickDownloader kickDownloader = new KickDownloader(channelName, count, vodSpot);
				kickDownloader.runDownload();
			}
		}

		// TODO support other services
	}
}
