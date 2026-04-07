package com.usecase;

import com.domain.model.CampaignStats;
import com.domain.repositories.CampaignReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Aggregates campaign data from CSV files.
 * Responsible for coordinating CSV reading and using the returned aggregated data.
 */
public class DataAggregator {
	private CampaignReader reader;
	private Map<String, CampaignStats> campaignMap;
	
	public DataAggregator(CampaignReader reader) {
		this.reader = reader;
		this.campaignMap = new HashMap<>();
	}
	
	/**
	 * Reads CSV file and gets aggregated data from the reader.
	 *
	 * @param filename path to the CSV file
	 * @throws IOException if file cannot be read
	 */
	public void aggregate(String filename) throws IOException {
		// Reader directly returns aggregated HashMap
		this.campaignMap = reader.readStats(filename);
	}
	
	/**
	 * Returns the aggregated campaign statistics.
	 */
	public Map<String, CampaignStats> getCampaignMap() {
		return campaignMap;
	}
}
