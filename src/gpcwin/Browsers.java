package gpcwin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import beans.BrowserHistorySite;
import commons.MyException;
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
	private static final String SQL_FIREFOX_HISTORY  = "select strftime('%d/%m/%Y %H:%M:%S', last_visit_date/1000000, 'unixepoch') as lastVisit, visit_count as visitCount, url, ifnull(title,'') as title FROM moz_places where last_visit_date > strftime('%s', strftime('%Y-%m-%d','now')) * 1000000;";
	
	// --------------------------------------------------------------------------------------------
	// Properties
	// --------------------------------------------------------------------------------------------
	private List<knownBrowsers> browsersList;
	
	// --------------------------------------------------------------------------------------------
	// Constructor
	// --------------------------------------------------------------------------------------------
	public Browsers() throws MyException {
		browsersList = new ArrayList<knownBrowsers>();
		
		// detects the browsers installed into the system
		try {
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec(CMD_LISTBROWSERS);
			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = null;
			while ((line = input.readLine()) != null) {
				switch (line.toLowerCase()) {
				case "firefox":
					browsersList.add(knownBrowsers.FIREFOX);
					break;
				case "google chrome":
					browsersList.add(knownBrowsers.CHROME);
					break;
				case "iexplore":
					browsersList.add(knownBrowsers.EXPLORER);
					break;
				default:
	    			throw new MyException(String.format("Unknown browser: {0}.", line));
				}
			}
            int exitVal = pr.waitFor();
            if (exitVal != 0)
    			throw new MyException(String.format("list_browsers command exited with error code {0}.", exitVal));
		} catch (IOException | InterruptedException ex) {
			throw new MyException("Unable to run list_browsers command.", ex);
		}
	}
	
	// --------------------------------------------------------------------------------------------
	// Public methods
	// --------------------------------------------------------------------------------------------
	public void detectBrowsersHistory() throws DbException {
		for (knownBrowsers browser : browsersList) {
			switch (browser) {
			case FIREFOX:
				detectFirefoxHistory();
				break;
			case CHROME:
				break;
			case EXPLORER:
				break;
			}
		}
	}
	
	// --------------------------------------------------------------------------------------------
	// Private methods
	// --------------------------------------------------------------------------------------------
	private void detectFirefoxHistory() throws DbException {
		// ...\AppData\Roaming\Mozilla\Firefox\Profiles\sxfy4340.default\places.sqlite
		DbSQLite db = new DbSQLite("tools\\places.sqlite");
		List<BrowserHistorySite> history = db.select(SQL_FIREFOX_HISTORY, BrowserHistorySite.class);
		String line;
		for (BrowserHistorySite site : history) {
			line = String.format("%s %5d \"%s\" \"%s\"", site.getLastVisit(), site.getVisitCount(), site.getUrl(), site.getTitle());
			System.out.println(line);
		}
	}
}
