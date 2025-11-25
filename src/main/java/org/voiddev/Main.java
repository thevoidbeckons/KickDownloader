package org.voiddev;

import java.io.File;

public class Main
{

	public static void main(String[] args)
	{
		String vodSpot = "";
		String channelName = "";
		String service = "kick";
		int count = 1;
		if (args.length == 3)
		{
//			service = args[0];
			channelName = args[0];
			vodSpot = args[1];
			if (!vodSpot.endsWith(File.separator))
			{
				vodSpot = vodSpot + File.separator;
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

		if (!new File(vodSpot).exists())
		{
			new File(vodSpot).mkdirs();
		}

		if (service == null || service.length() == 0)
		{
			System.out.println("The service name is required");
			return;
		}

		if (service.equalsIgnoreCase("kick") || service.equalsIgnoreCase("k"))
		{
			KickDownloader kickDownloader = new KickDownloader(channelName, count, vodSpot);
			kickDownloader.runDownload();
		}

		// TODO support other services
	}
}
