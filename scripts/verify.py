import csv
from collections import defaultdict
import os

INPUT_FILE = "ad_data.csv"
OUTPUT_FOLDER = "results"

# Ensure output folder exists
os.makedirs(OUTPUT_FOLDER, exist_ok=True)

# Aggregate data
campaign_data = defaultdict(lambda: {"impressions": 0, "clicks": 0, "spend": 0.0, "conversions": 0})

with open(INPUT_FILE, newline='') as f:
    reader = csv.DictReader(f)
    for row in reader:
        cid = row['campaign_id']
        try:
            impressions = int(row['impressions'])
            clicks = int(row['clicks'])
            spend = float(row['spend'])
            conversions = int(row['conversions'])
        except ValueError:
            continue
        data = campaign_data[cid]
        data['impressions'] += impressions
        data['clicks'] += clicks
        data['spend'] += spend
        data['conversions'] += conversions

# Compute metrics
results = []
for cid, data in campaign_data.items():
    impressions = data['impressions']
    clicks = data['clicks']
    spend = data['spend']
    conversions = data['conversions']
    ctr = clicks / impressions if impressions > 0 else 0
    cpa = spend / conversions if conversions > 0 else None
    results.append({
        "campaign_id": cid,
        "total_impressions": impressions,
        "total_clicks": clicks,
        "total_spend": spend,
        "total_conversions": conversions,
        "CTR": ctr,
        "CPA": cpa
    })

# Helper function to format row for CSV
def format_row(row):
    return {
        "campaign_id": row["campaign_id"],
        "total_impressions": row["total_impressions"],
        "total_clicks": row["total_clicks"],
        "total_spend": f"{row['total_spend']:.2f}",
        "total_conversions": row["total_conversions"],
        "CTR": f"{row['CTR']:.4f}",
        "CPA": f"{row['CPA']:.2f}" if row['CPA'] is not None else ""
    }

# Export top 10 CTR
top10_ctr = sorted(results, key=lambda x: x['CTR'], reverse=True)[:10]
ctr_file = os.path.join(OUTPUT_FOLDER, "top10_ctr.csv")
with open(ctr_file, "w", newline='') as f:
    writer = csv.DictWriter(f, fieldnames=["campaign_id","total_impressions","total_clicks","total_spend","total_conversions","CTR","CPA"])
    writer.writeheader()
    for row in top10_ctr:
        writer.writerow(format_row(row))

# Export top 10 lowest CPA (exclude zero conversions)
top10_cpa = sorted([r for r in results if r['CPA'] is not None], key=lambda x: x['CPA'])[:10]
cpa_file = os.path.join(OUTPUT_FOLDER, "top10_cpa.csv")
with open(cpa_file, "w", newline='') as f:
    writer = csv.DictWriter(f, fieldnames=["campaign_id","total_impressions","total_clicks","total_spend","total_conversions","CTR","CPA"])
    writer.writeheader()
    for row in top10_cpa:
        writer.writerow(format_row(row))

print(f"✅ CSV files exported to: {OUTPUT_FOLDER}")