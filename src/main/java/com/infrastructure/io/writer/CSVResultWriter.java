package com.infrastructure.io.writer;

import com.domain.model.CampaignStats;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Writes campaign statistics to CSV files.
 * Responsible for formatting and persisting results.
 */
public class CSVResultWriter {
	private static final int BUFFER_SIZE = 65536; // 64 KB buffer
	private static final String HEADER = "campaign_id,total_impressions,total_clicks,total_spend,total_conversions,CTR,CPA";
	
	/**
	 * Writes campaign statistics to a CSV file.
	 *
	 * @param campaigns  list of campaign statistics to write
	 * @param filename   path to the output CSV file
	 * @param resultType description of the result type (e.g., "CTR", "CPA")
	 * @throws IOException if file cannot be written
	 */
	public static void write(List<CampaignStats> campaigns, String filename, String resultType) throws IOException {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename), BUFFER_SIZE))) {
			// Write header
			writer.println(HEADER);
			// Write data rows
			for (CampaignStats stats : campaigns) {
				writer.println(stats.toString());
			}
		}
		System.out.println("✓ " + resultType + " results written to " + filename);
	}
}
