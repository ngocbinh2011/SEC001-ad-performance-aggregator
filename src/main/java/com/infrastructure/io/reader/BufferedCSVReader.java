package com.infrastructure.io.reader;

import com.domain.model.CampaignStats;
import com.domain.repositories.CampaignReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * CSV reader implementation using BufferedReader.
 * Suitable for most cases with good performance and memory efficiency.
 */
public class BufferedCSVReader implements CampaignReader {
	private static final int BUFFER_SIZE = 65536; // 64 KB buffer
	
	@Override
	public Map<String, CampaignStats> readStats(String filename) throws IOException {
		Map<String, CampaignStats> campaignMap = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filename), BUFFER_SIZE)) {
			String line;
			boolean firstLine = true;
			while ((line = br.readLine()) != null) {
				// Skip header row
				if (firstLine) {
					firstLine = false;
					continue;
				}
				processRow(line, campaignMap);
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
}
