package com.infrastructure.io.reader;

import com.domain.model.CampaignStats;

import java.util.Map;

/**
 * Parses a CSV row and extracts campaign data.
 * Responsible for data validation and transformation.
 */
public class CSVRowParser {
	
	/**
	 * Parses a CSV row string and extracts campaign metrics.
	 * Expected format: campaign_id,date,impressions,clicks,spend,conversions
	 * I don't create CampaignStats because reduce memory heap usage
	 *
	 * @param row the CSV row to parse
	 */
	public static void handle(String row, Map<String, CampaignStats> campaignMap) {
		if (row == null || row.isEmpty()) {
			return;
		}
		
		int len = row.length();
		int start = 0;
		int field = 0;
		
		String campaignId = null;
		long impressions = 0;
		long clicks = 0;
		double spend = 0;
		long conversions = 0;
		
		for (int i = 0; i <= len; i++) {
			boolean end = (i == len || row.charAt(i) == ',');
			
			if (!end) {
				continue;
			}
			
			int fieldStart = start;
			int fieldEnd = i;
			
			if (fieldEnd > fieldStart && row.charAt(fieldEnd - 1) == '\r') {
				fieldEnd--;
			}
			
			while (fieldStart < fieldEnd && row.charAt(fieldStart) <= ' ') {
				fieldStart++;
			}
			while (fieldEnd > fieldStart && row.charAt(fieldEnd - 1) <= ' ') {
				fieldEnd--;
			}
			
			try {
				switch (field) {
					case 0:
						if (fieldStart >= fieldEnd) return;
						campaignId = row.substring(fieldStart, fieldEnd).intern();
						break;
					
					case 2:
						impressions = parseLong(row, fieldStart, fieldEnd);
						if (impressions < 0) return;
						break;
					
					case 3:
						clicks = parseLong(row, fieldStart, fieldEnd);
						if (clicks < 0) return;
						break;
					
					case 4:
						spend = parseDouble(row, fieldStart, fieldEnd);
						if (spend < 0 || Double.isNaN(spend) || Double.isInfinite(spend)) return;
						break;
					
					case 5:
						conversions = parseLong(row, fieldStart, fieldEnd);
						if (conversions < 0) return;
						
						CampaignStats stats = campaignMap.get(campaignId);
						if (stats == null) {
							stats = new CampaignStats(campaignId);
							campaignMap.put(campaignId, stats);
						}
						stats.updateStats(impressions, clicks, spend, conversions);
						return;
				}
			} catch (NumberFormatException e) {
				return;
			}
			
			field++;
			start = i + 1;
		}
		
	}
	
	private static long parseLong(String s, int start, int end) {
		if (start >= end) throw new NumberFormatException("Empty number");
		
		long result = 0;
		boolean negative = false;
		
		if (s.charAt(start) == '-') {
			negative = true;
			start++;
			if (start >= end) throw new NumberFormatException("Invalid number");
		}
		
		for (int i = start; i < end; i++) {
			char c = s.charAt(i);
			if (c < '0' || c > '9') throw new NumberFormatException("Invalid long");
			result = result * 10 + (c - '0');
		}
		
		return negative ? -result : result;
	}
	
	private static double parseDouble(String s, int start, int end) {
		if (start >= end) throw new NumberFormatException("Empty double");
		return Double.parseDouble(s.substring(start, end));
	}
}
