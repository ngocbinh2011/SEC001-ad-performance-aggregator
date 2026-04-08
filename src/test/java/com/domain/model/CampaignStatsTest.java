package com.domain.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for CampaignStats class.
 * Tests all methods including initialization, statistics updates, and calculations.
 */
public class CampaignStatsTest {
	private CampaignStats campaignStats;
	private static final String CAMPAIGN_ID = "camp_123";
	
	@Before
	public void setUp() {
		campaignStats = new CampaignStats(CAMPAIGN_ID);
	}
	
	/**
	 * Test constructor initializes all fields correctly.
	 */
	@Test
	public void testConstructor() {
		assertEquals("Campaign ID should be initialized", CAMPAIGN_ID, campaignStats.getCampaignId());
		assertEquals("Impressions should be 0", 0, campaignStats.getTotalImpressions());
		assertEquals("Clicks should be 0", 0, campaignStats.getTotalClicks());
		assertEquals("Spend should be 0.0", 0.0, campaignStats.getTotalSpend(), 0.0);
		assertEquals("Conversions should be 0", 0, campaignStats.getTotalConversions());
	}
	
	/**
	 * Test getCTR returns 0.0 when there are no impressions.
	 */
	@Test
	public void testGetCTRWithZeroImpressions() {
		double ctr = campaignStats.getCTR();
		assertEquals("CTR should be 0.0 with zero impressions", 0.0, ctr, 0.0);
	}
	
	/**
	 * Test getCPA returns null when there are no conversions.
	 */
	@Test
	public void testGetCPAWithZeroConversions() {
		campaignStats.updateStats(100, 10, 50.0, 0);
		Double cpa = campaignStats.getCPA();
		assertNull("CPA should be null with zero conversions", cpa);
	}
	
	/**
	 * Test getCTR calculation with valid data.
	 */
	@Test
	public void testGetCTRCalculation() {
		campaignStats.updateStats(1000, 50, 100.0, 5);
		double expectedCTR = 50.0 / 1000.0;
		assertEquals("CTR should be clicks/impressions", expectedCTR, campaignStats.getCTR(), 0.0001);
	}
	
	/**
	 * Test getCPA calculation with valid data.
	 */
	@Test
	public void testGetCPACalculation() {
		campaignStats.updateStats(1000, 50, 100.0, 10);
		Double cpa = campaignStats.getCPA();
		assertNotNull("CPA should not be null with conversions > 0", cpa);
		double expectedCPA = 100.0 / 10.0;
		assertEquals("CPA should be spend/conversions", expectedCPA, cpa, 0.0001);
	}
	
	/**
	 * Test single updateStats call.
	 */
	@Test
	public void testUpdateStatsOnce() {
		campaignStats.updateStats(100, 10, 50.0, 5);
		assertEquals("Impressions should be updated", 100, campaignStats.getTotalImpressions());
		assertEquals("Clicks should be updated", 10, campaignStats.getTotalClicks());
		assertEquals("Spend should be updated", 50.0, campaignStats.getTotalSpend(), 0.0);
		assertEquals("Conversions should be updated", 5, campaignStats.getTotalConversions());
	}
	
	/**
	 * Test multiple updateStats calls accumulate correctly.
	 */
	@Test
	public void testUpdateStatsMultipleTimes() {
		campaignStats.updateStats(100, 10, 50.0, 5);
		campaignStats.updateStats(200, 20, 75.0, 10);
		campaignStats.updateStats(150, 15, 25.0, 5);
		assertEquals("Impressions should be accumulated", 450, campaignStats.getTotalImpressions());
		assertEquals("Clicks should be accumulated", 45, campaignStats.getTotalClicks());
		assertEquals("Spend should be accumulated", 150.0, campaignStats.getTotalSpend(), 0.0001);
		assertEquals("Conversions should be accumulated", 20, campaignStats.getTotalConversions());
	}
	
	/**
	 * Test CTR calculation after multiple updates.
	 */
	@Test
	public void testGetCTRWithMultipleUpdates() {
		campaignStats.updateStats(100, 10, 50.0, 5);
		campaignStats.updateStats(200, 20, 75.0, 10);
		double expectedCTR = 30.0 / 300.0;
		assertEquals("CTR should be correct after multiple updates", expectedCTR, campaignStats.getCTR(), 0.0001);
	}
	
	/**
	 * Test CPA calculation after multiple updates.
	 */
	@Test
	public void testGetCPAWithMultipleUpdates() {
		campaignStats.updateStats(100, 10, 50.0, 5);
		campaignStats.updateStats(200, 20, 75.0, 10);
		Double cpa = campaignStats.getCPA();
		assertNotNull("CPA should not be null", cpa);
		double expectedCPA = 125.0 / 15.0;
		assertEquals("CPA should be correct after multiple updates", expectedCPA, cpa, 0.0001);
	}
	
	/**
	 * Test with zero values in update.
	 */
	@Test
	public void testUpdateStatsWithZeroValues() {
		campaignStats.updateStats(0, 0, 0.0, 0);
		assertEquals("Impressions should be 0", 0, campaignStats.getTotalImpressions());
		assertEquals("Clicks should be 0", 0, campaignStats.getTotalClicks());
		assertEquals("Spend should be 0.0", 0.0, campaignStats.getTotalSpend(), 0.0);
		assertEquals("Conversions should be 0", 0, campaignStats.getTotalConversions());
	}
	
	/**
	 * Test with large values.
	 */
	@Test
	public void testWithLargeValues() {
		long largeImpressions = 1_000_000_000L;
		long largeClicks = 1_000_000L;
		double largeSpend = 1_000_000_000.50;
		long largeConversions = 100_000L;
		campaignStats.updateStats(largeImpressions, largeClicks, largeSpend, largeConversions);
		assertEquals("Large impressions should be stored", largeImpressions, campaignStats.getTotalImpressions());
		assertEquals("Large clicks should be stored", largeClicks, campaignStats.getTotalClicks());
		assertEquals("Large spend should be stored", largeSpend, campaignStats.getTotalSpend(), 0.01);
		assertEquals("Large conversions should be stored", largeConversions, campaignStats.getTotalConversions());
	}
	
	/**
	 * Test with decimal precision in spend.
	 */
	@Test
	public void testWithDecimalPrecision() {
		campaignStats.updateStats(100, 5, 12.99, 3);
		double expectedCPA = 12.99 / 3;
		Double cpa = campaignStats.getCPA();
		assertNotNull("CPA should not be null", cpa);
		assertEquals("CPA should maintain decimal precision", expectedCPA, cpa, 0.0001);
	}
	
	/**
	 * Test toString formatting with all valid data.
	 */
	@Test
	public void testToStringWithAllValidData() {
		campaignStats.updateStats(1000, 50, 100.0, 10);
		String result = campaignStats.toString();
		assertNotNull("toString should not return null", result);
		assertTrue("toString should contain campaign ID", result.contains(CAMPAIGN_ID));
		assertTrue("toString should contain impressions", result.contains("1000"));
		assertTrue("toString should contain clicks", result.contains("50"));
		assertTrue("toString should contain spend", result.contains("100.00"));
		assertTrue("toString should contain conversions", result.contains("10"));
		assertTrue("toString should contain CPA value", result.contains("10.00"));
	}
	
	/**
	 * Test toString formatting when CPA is null.
	 */
	@Test
	public void testToStringWithNullCPA() {
		campaignStats.updateStats(1000, 50, 100.0, 0);
		String result = campaignStats.toString();
		assertNotNull("toString should not return null", result);
		assertTrue("toString should contain 'null' for CPA", result.contains("null"));
	}
	
	/**
	 * Test CTR with equal impressions and clicks (100%).
	 */
	@Test
	public void testGetCTRWithFullClickThrough() {
		campaignStats.updateStats(100, 100, 50.0, 10);
		double ctr = campaignStats.getCTR();
		assertEquals("CTR should be 1.0 when clicks equal impressions", 1.0, ctr, 0.0001);
	}
	
	/**
	 * Test CTR with very small click rate.
	 */
	@Test
	public void testGetCTRWithSmallClickRate() {
		campaignStats.updateStats(1_000_000, 1, 50.0, 1);
		double expectedCTR = 1.0 / 1_000_000.0;
		assertEquals("CTR should handle very small values", expectedCTR, campaignStats.getCTR(), 0.000000001);
	}
	
	/**
	 * Test CPA calculation edge case with spend = 0 and conversions > 0.
	 */
	@Test
	public void testGetCPAWithZeroSpend() {
		campaignStats.updateStats(100, 10, 0.0, 10);
		Double cpa = campaignStats.getCPA();
		assertNotNull("CPA should not be null", cpa);
		assertEquals("CPA should be 0.0 when spend is 0", 0.0, cpa, 0.0001);
	}
	
	/**
	 * Test multiple campaigns do not interfere with each other.
	 */
	@Test
	public void testIndependenceOfMultipleCampaigns() {
		CampaignStats campaign1 = new CampaignStats("camp_001");
		CampaignStats campaign2 = new CampaignStats("camp_002");
		campaign1.updateStats(100, 10, 50.0, 5);
		campaign2.updateStats(200, 30, 100.0, 10);
		assertEquals("Campaign 1 should have correct impressions", 100, campaign1.getTotalImpressions());
		assertEquals("Campaign 2 should have correct impressions", 200, campaign2.getTotalImpressions());
		assertEquals("Campaign 1 CTR should be independent", 10.0 / 100.0, campaign1.getCTR(), 0.0001);
		assertEquals("Campaign 2 CTR should be independent", 30.0 / 200.0, campaign2.getCTR(), 0.0001);
	}
	
	/**
	 * Test getters are consistent with internal state.
	 */
	@Test
	public void testGettersConsistency() {
		campaignStats.updateStats(500, 25, 250.0, 15);
		assertEquals("getCampaignId should match constructor", CAMPAIGN_ID, campaignStats.getCampaignId());
		assertEquals("getTotalImpressions should match updateStats", 500, campaignStats.getTotalImpressions());
		assertEquals("getTotalClicks should match updateStats", 25, campaignStats.getTotalClicks());
		assertEquals("getTotalSpend should match updateStats", 250.0, campaignStats.getTotalSpend(), 0.0001);
		assertEquals("getTotalConversions should match updateStats", 15, campaignStats.getTotalConversions());
	}
	
	/**
	 * Test very high CPA value.
	 */
	@Test
	public void testHighCPAValue() {
		campaignStats.updateStats(100, 10, 999_999.99, 1);
		Double cpa = campaignStats.getCPA();
		assertNotNull("CPA should not be null", cpa);
		assertEquals("CPA should handle large values", 999_999.99, cpa, 0.01);
	}
	
	/**
	 * Test accumulation after reaching conversions = 1 (minimal CPA calculation).
	 */
	@Test
	public void testCPAWithSingleConversion() {
		campaignStats.updateStats(100, 5, 25.50, 1);
		Double cpa = campaignStats.getCPA();
		assertNotNull("CPA should not be null with 1 conversion", cpa);
		assertEquals("CPA should equal spend with 1 conversion", 25.50, cpa, 0.001);
	}
}
