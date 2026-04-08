package com.infrastructure.io.reader;

import com.domain.model.CampaignStats;
import com.domain.repositories.CampaignReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * CSV reader that uses byte streams for file reading.
 * Reads file as raw bytes and parses lines manually.
 * Provides fine-grained control over buffer management.
 */
public class StreamCSVReader implements CampaignReader {
	private static final int BUFFER_SIZE = 65536; // 64 KB buffer
	
	@Override
	public Map<String, CampaignStats> readStats(String filename) throws IOException {
		Map<String, CampaignStats> campaignMap = new HashMap<>();
		try (FileInputStream fis = new FileInputStream(filename)) {
			byte[] buffer = new byte[BUFFER_SIZE];
			StringBuilder line = new StringBuilder(256);
			boolean firstLine = true;
			int bytesRead;
			while ((bytesRead = fis.read(buffer)) != -1) {
				for (int i = 0; i < bytesRead; i++) {
					byte b = buffer[i];
					if (b == '\n') {
						String row = cleanLine(line);
						line.setLength(0);
						// Skip header row
						if (!firstLine && !row.isEmpty()) {
							processRow(row, campaignMap);
						}
						firstLine = false;
					} else {
						line.append((char) b);
					}
				}
			}
			// Handle last line if file doesn't end with newline
			if (line.length() > 0 && !firstLine) {
				String row = cleanLine(line);
				if (!row.isEmpty()) {
					processRow(row, campaignMap);
				}
			}
		}
		return campaignMap;
	}
	
	/**
	 * Processes a single CSV row and updates campaign statistics
	 */
	private void processRow(String row, Map<String, CampaignStats> campaignMap) {
		CSVRowParser.handle(row, campaignMap);
	}
	
	/**
	 * Removes trailing carriage return if present
	 */
	private String cleanLine(StringBuilder line) {
		int len = line.length();
		if (len > 0 && line.charAt(len - 1) == '\r') {
			return line.substring(0, len - 1);
		}
		return line.toString();
	}
}
