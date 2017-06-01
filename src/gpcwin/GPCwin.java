package gpcwin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import beans.VisitedSite;
import commons.MyException;
import commons.Utils;
import database.DbException;
import gpc.GPCDatabase;
import system.Browsers;

public class GPCwin {

	// --------------------------------------------------------------------------------------------
	// Constants
	// --------------------------------------------------------------------------------------------
	final static Logger logger = LogManager.getLogger(GPCwin.class);

	private GPCDatabase gpcDb;
	
	public static void main(String[] args) {
		logger.info("PROCESS START");
		long mainStartTime = System.currentTimeMillis();
		
		new GPCwin();
		
		logger.info("PROCESS END in " + Utils.elapsedTime(mainStartTime));
	}
	
	private GPCwin() {
		String scanDate, message;
		logger.info("Browsers list");
		Browsers browsers = new Browsers();
		try {
			//gpcDb = new GPCDatabase();
			gpcDb = new GPCDatabase("fmazzurana.noip.me", 3306, "giant", "Eir3annach", null);

			List<String> messages = browsers.detectInstalled();
			gpcDb.messagesInsert(messages);

			List<String> days = gpcDb.calendarDays2Scan();
			for (String day : days) {
				logger.info("Report for: " + day);
				try {
					List<VisitedSite> sites = browsers.detectBrowsersHistory(day);
					// ...store the sites list into the DB
					for (VisitedSite site : sites) {
						site.setDomainFromUrl();
						gpcDb.sitesInsert(site);
					}
					LocalDateTime dt = LocalDateTime.now();
					scanDate = dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
					message = null;
				} catch (MyException e1) {
					scanDate = null;
					message = e1.getMessage();
					logger.fatal(message);
				}
				
				gpcDb.calendarUpdateScan(day, scanDate, message);
//
//				// call WS to generate the report
			}
		} catch (MyException | DbException e) {
			logger.fatal(e.getMessage());
		}
	}
}
