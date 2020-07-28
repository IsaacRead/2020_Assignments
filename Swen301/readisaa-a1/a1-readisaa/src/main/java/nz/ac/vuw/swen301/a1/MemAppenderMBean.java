package nz.ac.vuw.swen301.a1;

import java.io.FileNotFoundException;

public interface MemAppenderMBean {
	String[] getLogs();
	long getLogCount();
	long getDiscardedLogCount();
	void exportToJSON(String fileName) throws FileNotFoundException;
}
