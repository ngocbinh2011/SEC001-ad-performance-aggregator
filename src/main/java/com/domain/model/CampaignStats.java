package com.domain.model;

/**
 * Represents aggregated statistics for a single campaign.
 * Stores metrics and provides calculations for CTR and CPA.
 */
public class CampaignStats {
	private String campaignId;
	private long totalImpressions;
	private long totalClicks;
	private double totalSpend;
	private long totalConversions;
	
	public CampaignStats(String campaignId) {
		this.campaignId = campaignId;
		this.totalImpressions = 0;
		this.totalClicks = 0;
		this.totalSpend = 0.0;
		this.totalConversions = 0;
	}
	
	/**
	 * Updates statistics with new data from a CSV row
	 */
	public void updateStats(long impressions, long clicks, double spend, long conversions) {
		this.totalImpressions += impressions;
		this.totalClicks += clicks;
		this.totalSpend += spend;
		this.totalConversions += conversions;
	}
	
	/**
	 * Calculates Click-Through Rate (CTR) = clicks / impressions
	 */
	public double getCTR() {
		return totalImpressions > 0 ? (double) totalClicks / totalImpressions : 0.0;
	}
	
	/**
	 * Calculates Cost Per Action (CPA) = spend / conversions
	 * Returns null if conversions = 0
	 */
	public Double getCPA() {
		return totalConversions > 0 ? totalSpend / totalConversions : null;
	}
	
	// Getters
	public String getCampaignId() {
		return campaignId;
	}
	
	public long getTotalImpressions() {
		return totalImpressions;
	}
	
	public long getTotalClicks() {
		return totalClicks;
	}
	
	public double getTotalSpend() {
		return totalSpend;
	}
	
	public long getTotalConversions() {
		return totalConversions;
	}
	
	/**
	 * Formats the campaign stats as a CSV row
	 */
	@Override
	public String toString() {
		Double cpa = getCPA();
		String cpaStr = (cpa != null) ? String.format("%.2f", cpa) : "null";
		return String.format("%s,%d,%d,%.2f,%d,%.4f,%s",
			campaignId, totalImpressions, totalClicks, totalSpend, totalConversions, getCTR(), cpaStr);
	}
}
