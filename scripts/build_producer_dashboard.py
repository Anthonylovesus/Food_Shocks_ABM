import csv
import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "output"
PRODUCER_META_PATH = ROOT / "data" / "apple_survey_6.13_producers.csv"
LONG_CSV_PATH = OUT_DIR / "apple_producers_daily_long.csv"
HTML_PATH = OUT_DIR / "apple_producers_interactive_dashboard.html"


def load_metadata():
    meta = {}
    if not PRODUCER_META_PATH.exists():
        return meta
    with PRODUCER_META_PATH.open(newline="", encoding="utf-8-sig") as f:
        for row in csv.DictReader(f):
            pid = int(float(row["farmer_id"]))
            meta[pid] = {
                "acres": float(row.get("acres") or 0),
                "annual_production_lbs": float(row.get("annual_production_lbs") or 0),
                "variety": row.get("variety") or "FRESH APPLES",
            }
    return meta


def load_producer_rows(meta):
    files = []
    for path in OUT_DIR.glob("apples_producer_*.csv"):
        match = re.search(r"apples_producer_(\d+)\.csv$", path.name)
        if match:
            files.append((int(match.group(1)), path))
    files.sort()

    rows = []
    series = {}
    for pid, path in files:
        producer_meta = meta.get(
            pid,
            {"acres": None, "annual_production_lbs": None, "variety": "FRESH APPLES"},
        )
        series[pid] = {
            "iteration": [],
            "price": [],
            "units_sold_day": [],
            "revenue_day": [],
            "units_sold_cum": [],
            "revenue_cum": [],
            "acres": producer_meta["acres"],
            "annual_production_lbs": producer_meta["annual_production_lbs"],
            "variety": producer_meta["variety"],
        }
        with path.open(newline="", encoding="utf-8-sig") as f:
            for source in csv.DictReader(f):
                record = {
                    "producer_id": pid,
                    "iteration": int(source["iteration"]),
                    "price": float(source.get("price") or 0.0),
                    "units_sold_day": int(float(source.get("units_sold_day") or 0)),
                    "revenue_day": float(source.get("revenue_day") or 0.0),
                    "units_sold_cum": int(float(source.get("units_sold_cum") or 0)),
                    "revenue_cum": float(source.get("revenue_cum") or 0.0),
                    "acres": producer_meta["acres"],
                    "annual_production_lbs": producer_meta["annual_production_lbs"],
                    "variety": producer_meta["variety"],
                }
                rows.append(record)
                for key in (
                    "iteration",
                    "price",
                    "units_sold_day",
                    "revenue_day",
                    "units_sold_cum",
                    "revenue_cum",
                ):
                    series[pid][key].append(record[key])

    return files, rows, series


def write_long_csv(rows):
    fieldnames = [
        "producer_id",
        "iteration",
        "price",
        "units_sold_day",
        "revenue_day",
        "units_sold_cum",
        "revenue_cum",
        "acres",
        "annual_production_lbs",
        "variety",
    ]
    with LONG_CSV_PATH.open("w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)


def html_document(producer_order, top10, summary, series, iterations, total_units, total_revenue):
    data = {
        "producerOrder": producer_order,
        "top10": top10,
        "summary": summary,
        "series": series,
        "totalIterations": iterations,
        "totalUnits": total_units,
        "totalRevenue": total_revenue,
    }
    payload = json.dumps(data, separators=(",", ":"))
    return """<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Fresh Apple Producer Daily Sales Dashboard</title>
  <script src="https://cdn.plot.ly/plotly-2.35.2.min.js"></script>
  <style>
    :root {
      --bg: #f7f7f2;
      --ink: #1e2520;
      --muted: #697268;
      --panel: #ffffff;
      --line: #d9ded6;
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      font-family: Arial, Helvetica, sans-serif;
      background: var(--bg);
      color: var(--ink);
    }
    header {
      padding: 24px 32px 12px;
      border-bottom: 1px solid var(--line);
      background: #ffffff;
    }
    h1 {
      margin: 0 0 8px;
      font-size: 24px;
      font-weight: 700;
      letter-spacing: 0;
    }
    .sub { color: var(--muted); font-size: 14px; margin: 0; }
    main { padding: 20px 32px 32px; }
    .metrics {
      display: grid;
      grid-template-columns: repeat(4, minmax(140px, 1fr));
      gap: 12px;
      margin-bottom: 18px;
    }
    .metric {
      background: var(--panel);
      border: 1px solid var(--line);
      border-radius: 8px;
      padding: 14px 16px;
    }
    .metric .label { color: var(--muted); font-size: 12px; margin-bottom: 6px; }
    .metric .value { font-size: 20px; font-weight: 700; }
    .chart {
      height: 560px;
      background: var(--panel);
      border: 1px solid var(--line);
      border-radius: 8px;
      margin-bottom: 18px;
      padding: 8px;
    }
    @media (max-width: 800px) {
      header, main { padding-left: 16px; padding-right: 16px; }
      .metrics { grid-template-columns: repeat(2, minmax(140px, 1fr)); }
      .chart { height: 460px; }
    }
  </style>
</head>
<body>
  <header>
    <h1>Fresh Apple Producer Daily Sales Dashboard</h1>
    <p class="sub">Interactive daily units sold and daily revenue for 38 producer agents. Click legend items to hide or isolate producers.</p>
  </header>
  <main>
    <section class="metrics">
      <div class="metric"><div class="label">Producer Agents</div><div class="value" id="producerCount"></div></div>
      <div class="metric"><div class="label">Daily Rows</div><div class="value" id="rowCount"></div></div>
      <div class="metric"><div class="label">Survey Acres</div><div class="value" id="totalAcres"></div></div>
      <div class="metric"><div class="label">Annual Production</div><div class="value" id="totalProduction"></div></div>
    </section>
    <div id="unitsChart" class="chart"></div>
    <div id="revenueChart" class="chart"></div>
  </main>
<script>
const DATA = __PAYLOAD__;
const producerOrder = DATA.producerOrder;
const top10 = new Set(DATA.top10);
const summary = DATA.summary;
const series = DATA.series;
const totalIterations = DATA.totalIterations;
const totalUnits = DATA.totalUnits;
const totalRevenue = DATA.totalRevenue;

function fmtInt(v) { return Number(v).toLocaleString(undefined, { maximumFractionDigits: 0 }); }
function fmtMoney(v) { return Number(v).toLocaleString(undefined, { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }); }

document.getElementById('producerCount').textContent = fmtInt(summary.producer_count);
document.getElementById('rowCount').textContent = fmtInt(summary.row_count);
document.getElementById('totalAcres').textContent = fmtInt(summary.total_acres);
document.getElementById('totalProduction').textContent = fmtInt(summary.total_annual_production_lbs) + ' lb';

const colors = ['#2f7d57','#a63d40','#3b6ea8','#c9822b','#6f4d9b','#637441','#b04f8f','#3d8f8f','#8f6b3d','#4d5f7a'];

function producerName(pid) {
  const row = series[pid];
  const acres = row.acres == null ? '' : ', ' + Number(row.acres).toLocaleString() + ' acres';
  return 'Producer ' + pid + acres;
}

function producerHover(pid, metric, label, valueFormatter) {
  const row = series[pid];
  return row.iteration.map((it, idx) =>
    'Producer ' + pid +
    '<br>Iteration: ' + it +
    '<br>' + label + ': ' + valueFormatter(row[metric][idx]) +
    '<br>Acres: ' + row.acres +
    '<br>Annual lb: ' + fmtInt(row.annual_production_lbs) +
    '<extra></extra>'
  );
}

function buildProducerTraces(metric, axisLabel, valueFormatter) {
  return producerOrder.map((pid, idx) => ({
    x: series[pid].iteration,
    y: series[pid][metric],
    type: 'scatter',
    mode: 'lines',
    name: producerName(pid),
    line: { width: top10.has(pid) ? 1.8 : 1, color: colors[idx % colors.length] },
    opacity: top10.has(pid) ? 0.72 : 0.34,
    hoverinfo: 'text',
    text: producerHover(pid, metric, axisLabel, valueFormatter),
    legendgroup: 'producer-' + pid
  }));
}

function chartLayout(title, yTitle) {
  return {
    title: { text: title, x: 0.02, xanchor: 'left' },
    margin: { l: 72, r: 24, t: 56, b: 56 },
    paper_bgcolor: '#ffffff',
    plot_bgcolor: '#ffffff',
    hovermode: 'x unified',
    xaxis: { title: 'Simulation iteration / day', gridcolor: '#edf0eb', zeroline: false },
    yaxis: { title: yTitle, gridcolor: '#edf0eb', zeroline: false },
    legend: { orientation: 'v', x: 1.02, y: 1, font: { size: 10 } },
    updatemenus: [{
      type: 'buttons',
      direction: 'right',
      x: 0,
      y: 1.12,
      buttons: [
        { label: 'All producers', method: 'restyle', args: [{ visible: true }] },
        { label: 'Top 10 + total', method: 'restyle', args: [{ visible: producerOrder.map(pid => top10.has(pid)).concat([true]) }] }
      ]
    }]
  };
}

const unitsTraces = buildProducerTraces('units_sold_day', 'Units sold', fmtInt);
unitsTraces.push({
  x: totalIterations,
  y: totalUnits,
  type: 'scatter',
  mode: 'lines',
  name: 'Total daily units',
  line: { width: 3, color: '#111111' },
  hovertemplate: 'Total<br>Iteration: %{x}<br>Units sold: %{y:,.0f}<extra></extra>'
});

const revenueTraces = buildProducerTraces('revenue_day', 'Revenue', fmtMoney);
revenueTraces.push({
  x: totalIterations,
  y: totalRevenue,
  type: 'scatter',
  mode: 'lines',
  name: 'Total daily revenue',
  line: { width: 3, color: '#111111' },
  hovertemplate: 'Total<br>Iteration: %{x}<br>Revenue: %{y:$,.2f}<extra></extra>'
});

Plotly.newPlot('unitsChart', unitsTraces, chartLayout('Daily Units Sold by Producer', 'Units sold per day'), { responsive: true, displaylogo: false });
Plotly.newPlot('revenueChart', revenueTraces, chartLayout('Daily Revenue by Producer', 'Revenue per day ($)'), { responsive: true, displaylogo: false });
</script>
</body>
</html>
""".replace("__PAYLOAD__", payload)


def write_dashboard(files, rows, series):
    producer_order = [pid for pid, _ in files]
    iterations = sorted({row["iteration"] for row in rows})
    totals_by_iter = {i: {"units_sold_day": 0, "revenue_day": 0.0} for i in iterations}
    for row in rows:
      totals_by_iter[row["iteration"]]["units_sold_day"] += row["units_sold_day"]
      totals_by_iter[row["iteration"]]["revenue_day"] += row["revenue_day"]

    top10 = sorted(
        producer_order,
        key=lambda pid: series[pid]["annual_production_lbs"] or 0,
        reverse=True,
    )[:10]
    summary = {
        "producer_count": len(producer_order),
        "row_count": len(rows),
        "first_iteration": iterations[0] if iterations else None,
        "last_iteration": iterations[-1] if iterations else None,
        "total_acres": sum((series[pid]["acres"] or 0) for pid in producer_order),
        "total_annual_production_lbs": sum(
            (series[pid]["annual_production_lbs"] or 0) for pid in producer_order
        ),
        "top10_by_production": top10,
    }
    html = html_document(
        producer_order,
        top10,
        summary,
        series,
        iterations,
        [totals_by_iter[i]["units_sold_day"] for i in iterations],
        [totals_by_iter[i]["revenue_day"] for i in iterations],
    )
    HTML_PATH.write_text(html, encoding="utf-8")
    return summary


def main():
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    meta = load_metadata()
    files, rows, series = load_producer_rows(meta)
    write_long_csv(rows)
    summary = write_dashboard(files, rows, series)
    print(f"Wrote {LONG_CSV_PATH}")
    print(f"Wrote {HTML_PATH}")
    print(json.dumps(summary, indent=2))


if __name__ == "__main__":
    main()
