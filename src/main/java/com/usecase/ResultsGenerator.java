package com.usecase;

import com.domain.model.CampaignStats;

import java.util.*;

/**
 * Filters and sorts campaign statistics for output using PriorityQueue optimization.
 * Responsible for generating top 10 lists by CTR and CPA efficiently.
 * Uses min-heap approach to track only top 10 elements in O(n log 10) time.
 */
public class ResultsGenerator {
	private static final int TOP_K = 10;
	
	/**
	 * Generates top 10 campaigns sorted by CTR (highest first).
	 * Optimized using PriorityQueue (min-heap) to avoid sorting entire dataset.
	 *
	 * @param campaignMap the aggregated campaign statistics
	 * @return list of top 10 campaigns by CTR sorted descending
	 */
	public static List<CampaignStats> getTop10ByCTR(Map<String, CampaignStats> campaignMap) {
		// Min-heap: tracks top 10 by CTR with smallest CTR at head
		PriorityQueue<CampaignStats> topK = new PriorityQueue<>(TOP_K,
			(a, b) -> Double.compare(a.getCTR(), b.getCTR()));
		
		for (CampaignStats campaign : campaignMap.values()) {
			topK.offer(campaign);
			if (topK.size() > TOP_K) {
				topK.poll();
			}
		}
		
		// Convert to sorted list (descending by CTR)
		List<CampaignStats> result = new ArrayList<>(topK);
		result.sort((a, b) -> Double.compare(b.getCTR(), a.getCTR()));
		return result;
	}
	
	/**
	 * Generates top 10 campaigns sorted by CPA (lowest first).
	 * Excludes campaigns with zero conversions.
	 * Optimized using PriorityQueue (max-heap) to avoid sorting entire dataset.
	 *
	 * @param campaignMap the aggregated campaign statistics
	 * @return list of top 10 campaigns by CPA sorted ascending
	 */
	public static List<CampaignStats> getTop10ByCPA(Map<String, CampaignStats> campaignMap) {
		// Max-heap: tracks top 10 by CPA (lowest cost) with highest CPA at head
		PriorityQueue<CampaignStats> topK = new PriorityQueue<>(TOP_K,
			(a, b) -> {
				Double cpaA = a.getCPA();
				Double cpaB = b.getCPA();
				if (cpaA == null) return 1;
				if (cpaB == null) return -1;
				return Double.compare(cpaB, cpaA); // Reverse for max-heap
			});
		
		for (CampaignStats campaign : campaignMap.values()) {
			if (campaign.getTotalConversions() > 0) {
				topK.offer(campaign);
				if (topK.size() > TOP_K) {
					topK.poll();
				}
			}
		}
		
		// Convert to sorted list (ascending by CPA)
		List<CampaignStats> result = new ArrayList<>(topK);
		result.sort((a, b) -> {
			Double cpaA = a.getCPA();
			Double cpaB = b.getCPA();
			if (cpaA == null) return 1;
			if (cpaB == null) return -1;
			return Double.compare(cpaA, cpaB);
		});
		return result;
	}
}
