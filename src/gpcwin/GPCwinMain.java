package gpcwin;

import commons.MyException;
import database.DbException;

public class GPCwinMain {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: GPCwin { start | stop | test }");
			return;
		}
		
		String mode = args[0].toLowerCase();
		switch (mode) {
		case "start":
			System.out.println("GPCwin start: to be implemented.");
			break;
		case "stop":
			System.out.println("GPCwin stop: to be implemented.");
			break;
		case "test":
			test();
			break;
		default:
			System.out.println("GPCwin: invalid parameter.");
			System.out.println("Usage: GPCwin { start | stop }");
			return;
		}
	}
	
	private static void test() {
		try {
			Browsers browsers = new Browsers();
			browsers.detectBrowsersHistory();
		} catch (MyException | DbException e) {
			e.printStackTrace();
		}
	}
}
