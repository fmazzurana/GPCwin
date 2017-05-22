package gpcwin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import commons.MyException;
import commons.Utils;
import net.RestClient;

public class GPCwinMain {

	// --------------------------------------------------------------------------------------------
	// Constants
	// --------------------------------------------------------------------------------------------
	final static Logger logger = LogManager.getLogger(GPCwinMain.class);

	//private final static String sendUrl = "http://fmazzurana.noip.me:8080/gws/mail/send?to=%s&subject=%s&text=%s&attach=%s";
	private final static String sendUrl = "http://fmazzurana.noip.me:8080/gws/mail/send?to=%s&subject=%s&text=%s";
	private final static String toAddr = "fabrizio.mazzurana@gmail.com";

	public static void main(String[] args) {
		// TODO
		report("2017-05-19");
	}
	
	/**
	 * Makes the daily report for a given day
	 * 
	 * @param day
	 */
	private static void report(String day) {
		// Lines array to be written into the report file
		List<String> lines = new ArrayList<String>();
		
		// Detects the installed browsers and extracts their history
		try {
			Browsers browsers = new Browsers(lines);
			browsers.detectBrowsersHistory(day);
		} catch (MyException e) {
			// Do nothing, just to handle errors
		}

		// Writes the report file
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");    
		String repFile = "reports\\" + sdf.format(now) + ".log";
		try {
			Utils.writeToFile(lines, repFile);
		} catch (MyException e) {
			e.printStackTrace();
		}
		
		// Sends the report file by email
		
		RestClient rc = new RestClient();
		try {
			//String url = String.format(sendUrl, toAddr, URLEncoder.encode("GPC Report for "+day, "UTF-8"), URLEncoder.encode("See the attached file", "UTF-8"), URLEncoder.encode(repFile, "UTF-8"));
			String url = String.format(sendUrl, toAddr, URLEncoder.encode("GPC Report for "+day, "UTF-8"), URLEncoder.encode("See the attached file", "UTF-8"));
			String result = rc.get(url);
			System.out.println(result);
		} catch (MyException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
