package gpcwin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import beans.BrowserHistorySite;
import commons.MyException;
import commons.Utils;
import database.DbException;
import database.DbSQLite;

public class Browsers {

	// --------------------------------------------------------------------------------------------
	// Types
	// --------------------------------------------------------------------------------------------
	private static enum knownBrowsers  { FIREFOX /* 0 */, CHROME /* 1 */, EXPLORER /* 2 */ };
	
	// --------------------------------------------------------------------------------------------
	// Constants
	// --------------------------------------------------------------------------------------------
	private static final String CMD_LISTBROWSERS  = "tools\\list_browsers.bat";
	private static final String dashes = "----------------------------------------------------------------------------------------------------";
	
	// --------------------------------------------------------------------------------------------
	// Properties
	// --------------------------------------------------------------------------------------------
	private List<knownBrowsers> browsersList;
	private List<String> messages;
	
	// --------------------------------------------------------------------------------------------
	// Public methods
	// --------------------------------------------------------------------------------------------
	/**
	 * Constructor: detects the browsers installed into the system
	 * 
	 * @param lines List where to put messages
	 * @throws MyException
	 */
	public Browsers(List<String> lines) throws MyException {
		messages = lines;
		messages.add("BROWSERS LIST...");
		browsersList = new ArrayList<knownBrowsers>();
		
		try {
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec(CMD_LISTBROWSERS);
			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = null;
			while ((line = input.readLine()) != null) {
				switch (line.toLowerCase()) {
				case "firefox":
					browsersList.add(knownBrowsers.FIREFOX);
					messages.add("- Firefox");
					break;
				case "google chrome":
					browsersList.add(knownBrowsers.CHROME);
					messages.add("- Chrome");
					break;
				case "iexplore":
					browsersList.add(knownBrowsers.EXPLORER);
					messages.add("- IExplorer");
					break;
				default:
					messages.add("- unhandled " + line);
				}
			}
            int exitVal = pr.waitFor();
            if (exitVal != 0) {
            	messages.add(String.format("- command exited with error code %d", exitVal));
				throw new MyException();
            }
		} catch (IOException | InterruptedException ex) {
			messages.add("- error running list_browsers command: " + ex.getMessage());
			throw new MyException();
		}
		messages.add("");
	}
	
	/**
	 * Detects the browser history for a given day for all handled browsers
	 * 
	 * @param day
	 * @throws DbException
	 */
	public void detectBrowsersHistory(String day) throws MyException {
		List<String> urls = new ArrayList<String>();
		for (knownBrowsers browser : browsersList) {
			switch (browser) {
			case FIREFOX:
				detectFirefoxHistory(day, urls);
				break;
			case CHROME:
				detectChromeHistory(day, urls);
				break;
			case EXPLORER:
				break;
			}
		}
		
		// creates a list of unique domains
		List<String> domains = new ArrayList<String>();
		int p;
		for (String url : urls) {
			// http://<domain>/<extra-path>
			p = url.indexOf("//");
			if (p != -1)
				url = url.substring(p+2);
			p = url.indexOf("/");
			if (p != -1)
				url = url.substring(0, p);
			if (!domains.contains(url))
				domains.add(url);
		}

		messages.add("DOMAINS SUMMARY...");
		domains.sort(null);
		for (String domain : domains) {
			messages.add("  "+domain);
		}
	}
	
	// --------------------------------------------------------------------------------------------
	// Private methods
	// --------------------------------------------------------------------------------------------
	private void detectFirefoxHistory(String day, List<String> urls) throws MyException {
		messages.add("FIREFOX HISTORY...");
		String appdata = System.getenv("APPDATA") + "\\Mozilla\\Firefox\\Profiles";
		
		// Gets the profiles list
		File[] profiles = Utils.listDirectory(new File(appdata));
		if (profiles == null) {
			messages.add("- no profiles found");
			throw new MyException();
		}
		
		// for each profile...
		String sqlCmd = "select strftime('%d/%m/%Y %H:%M:%S', last_visit_date/1000000, 'unixepoch') as lastVisit, typed, url, ifnull(title,'') as title " +
				"from moz_places where strftime('%Y-%m-%d', last_visit_date/1000000, 'unixepoch') = '" + day + "' order by last_visit_date;";
		for (File p : profiles) {
			messages.add("Profile: " + p.getName());
			String dbFile = p.getAbsolutePath() + "\\places.sqlite";
			
			fm(dbFile, sqlCmd, urls);
		}
		messages.add("");
	}

	private void detectChromeHistory(String day, List<String> urls) throws MyException {
		messages.add("CHROME HISTORY...");
		String dbFile = System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\User Data\\Default\\History";
		String sqlCmd = "select strftime('%d/%m/%Y %H:%M:%S', last_visit_time/1000000-11644473600, 'unixepoch') as lastVisit, typed_count as typed, url, ifnull(title,'') as title " +
				"from urls where strftime('%Y-%m-%d', last_visit_time/1000000-11644473600, 'unixepoch') = '" + day + "' order by last_visit_time;";
		
		fm(dbFile, sqlCmd, urls);
		messages.add("");
	}
	
	private void fm(String dbFile, String sqlCmd, List<String> urls) {
		// opens and queries database
		try {
			DbSQLite db = new DbSQLite(dbFile);
			List<BrowserHistorySite> history = db.select(sqlCmd, BrowserHistorySite.class);
			for (BrowserHistorySite site : history) {
				messages.add(dashes);
				messages.add(String.format("  last visit: %s", site.getLastVisit()));
				messages.add(String.format("  typed:      %d", site.getTyped()));
				messages.add(String.format("  url:        %s", site.getUrl()));
				messages.add(String.format("  title:      %s", site.getTitle()));
				urls.add(site.getUrl());
			}
			messages.add(dashes);
		} catch (DbException e) {
			messages.add("- error reading history file");
		}
	}
}
