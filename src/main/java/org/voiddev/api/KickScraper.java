package org.voiddev.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

public class KickScraper
{
	Browser browser;
	BrowserType.LaunchOptions launchOptions;

	public KickScraper(Browser browser, BrowserType.LaunchOptions launchOptions)
	{
		this.browser = browser;
		this.launchOptions = launchOptions;
	}

	public JsonArray scrapeData(String url)
	{
		browser = browser.browserType().launch(launchOptions);
		BrowserContext context = browser.newContext(new Browser.NewContextOptions()
			.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"));
		Page page = context.newPage();
		page.navigate(url);

		page.waitForLoadState(LoadState.NETWORKIDLE);
		String bodyHtml = (String) page.evaluate("document.body.textContent");

		Gson gson = new Gson();
		return gson.fromJson(bodyHtml, JsonArray.class);
	}
}
