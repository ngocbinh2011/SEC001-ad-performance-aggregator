package com.domain.repositories;

import com.domain.model.CampaignStats;

import java.io.IOException;
import java.util.Map;

/**
 * Interface for reading CSV files and aggregating campaign statistics.
 * Allows different implementations for benchmarking various file reading methods.
 * Each reader directly returns aggregated results without callbacks.
 */
public interface CampaignReader {
	/**
	 * Reads the CSV file and returns aggregated campaign statistics.
	 *
	 * @param filename the path to the CSV file
	 * @return HashMap with campaign_id as key and CampaignStats as value
	 * @throws IOException if file cannot be read
	 */
	Map<String, CampaignStats> readStats(String filename) throws IOException;
}
