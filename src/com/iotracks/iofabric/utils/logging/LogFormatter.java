package com.iotracks.iofabric.utils.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
	public String format(LogRecord record) {
		final DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss.SSS");
		StringBuilder builder = new StringBuilder();
		builder.append(df.format(System.currentTimeMillis())).append(" - ");
		builder.append("[").append(record.getLevel()).append("] - ");
		builder.append(formatMessage(record));
		builder.append("\n");
		return builder.toString();
	}

}
