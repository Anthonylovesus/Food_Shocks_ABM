# Fresh Apple and Commodity Supply Chain ABM

This repository contains Java / Repast Symphony agent-based models for agricultural supply chains. The current main development focus is the fresh apple supply chain model in `src/main/java/com/example/abm/apple`.

The project is configured as an Eclipse / Maven Java 8 project. It also contains data inputs, generated simulation outputs, supporting PDFs, and scripts for post-processing and dashboard generation.

## Top-Level Folder Structure

```text
abm_05082026/
  .classpath                       Eclipse classpath; includes JavaSE-1.8 and Repast Symphony support.
  .project                         Eclipse project metadata.
  .settings/                       Eclipse project settings.
  bin/                             Eclipse build output.
  data/                            CSV and XLSX model inputs.
  output/                          Simulation outputs, dashboards, and presentation text.
  pdf/                             Source PDFs used for calibration and assumptions.
  r/                               R-related workspace folder.
  scripts/                         Utility scripts, including dashboard generation.
  src/main/java/com/example/abm/   Java source code for commodity ABMs.
  target/                          Maven / Java compiled classes.
  tmp/                             Temporary extracted files.
  txt/                             Text workspace folder.
  pom.xml                          Maven project file.
  project_structure.txt            Earlier project structure note.
```

## Important Data and Output Files

### Data Inputs

```text
data/apple_survey_6.13.xlsx
data/apple_survey_6.13_producers.csv
data/model_predictions_2021_2025.csv
data/model_predictions_2021_2025_gradient_boosting_daily_mean.csv
data/apple_02212025_02.csv
```

Key fresh apple inputs:

- `apple_survey_6.13.xlsx`: survey file with farmer IDs and acreage.
- `apple_survey_6.13_producers.csv`: derived producer input file. Each row represents one producer agent with acreage and annual production in pounds.
- `model_predictions_2021_2025.csv`: original prediction file for multiple varieties and models.
- `model_predictions_2021_2025_gradient_boosting_daily_mean.csv`: daily mean Gradient Boosting predicted price across apple varieties. This is used as the fresh apple farmer-to-wholesaler baseline price path.

### Generated Outputs

```text
output/apples_timeseries.csv
output/apples_producer_1.csv ... output/apples_producer_38.csv
output/apple_producers_daily_long.csv
output/apple_producers_interactive_dashboard.html
output/fresh_apple_abm_10min_presentation.txt
```

Key fresh apple outputs:

- `apples_timeseries.csv`: system-level time series.
- `apples_producer_*.csv`: producer-level reports with daily and cumulative sales and revenue.
- `apple_producers_daily_long.csv`: combined long-format producer output for plotting.
- `apple_producers_interactive_dashboard.html`: interactive Plotly dashboard showing daily units sold and daily revenue for all producers.
- `fresh_apple_abm_10min_presentation.txt`: 10-minute project presentation script.

## Commodity Model Structure

All commodity models live under:

```text
src/main/java/com/example/abm/
```

Current commodity packages:

```text
apple/
beef/
dairy/
potatoes/
tomatoes/
wheat/
```

Each commodity package generally follows this structure:

```text
commodity/
  Main.java                    Headless entry point for running the model.
  <Commodity>MarketBuilder.java Repast ContextBuilder; creates agents, schedules steps, writes outputs.
  ModelParams.java             Parameter definitions and defaults.
  MLPriceLoader.java           Loads external price series.
  DataTracker.java             Collects and writes model outputs.
  MathUtil.java                Shared math helpers.
  agents/
    ProducerAgent.java
    WholesaleAgent.java
    RetailAgent.java
    ConsumerAgent.java
```

Some commodities also include a `processed/` subpackage:

```text
apple/processed/
dairy/processed/
tomatoes/processed/
```

The `processed/` folders are separate processed-commodity models and should be treated separately from the fresh commodity models. Recent fresh apple changes were intentionally made in `apple/` only, not in `apple/processed/`.

## Fresh Apple Model Structure

Fresh apple source folder:

```text
src/main/java/com/example/abm/apple/
```

Fresh apple model files:

```text
apple/
  Main.java
  AppleMarketBuilder.java
  ModelParams.java
  ProducerSurveyLoader.java
  MLPriceLoader.java
  TranslogProductionFunction.java
  InputCostScenario.java
  DataTracker.java
  MathUtil.java
  agents/
    ProducerAgent.java
    WholesaleAgent.java
    RetailAgent.java
    ConsumerAgent.java
```

### Main.java

Headless runner for the fresh apple model.

Main responsibilities:

- Creates Repast parameters through `ParametersCreator`.
- Sets default simulation values.
- Sets fresh apple data paths, including producer survey CSV and daily Gradient Boosting price CSV.
- Initializes Repast's `RunEnvironment`.
- Builds the model context using `AppleMarketBuilder`.
- Executes scheduled simulation steps.

The current default main class is:

```text
com.example.abm.apple.Main
```

### AppleMarketBuilder.java

Core Repast model builder for the fresh apple ABM.

Main responsibilities:

- Builds the `AppleMarket` context.
- Reads runtime parameters into `ModelParams`.
- Loads the daily Gradient Boosting mean price path.
- Loads 38 producer agents from `apple_survey_6.13_producers.csv`.
- Creates one wholesale agent and one retail agent.
- Creates scaled consumer agents for fresh apple demand.
- Schedules model steps and output writing.
- Applies farm price anchor logic:

```text
farmer price = predicted Gradient Boosting price * farm anchor multiplier
```

- Tracks producer, wholesaler, retailer, and consumer outcomes.

### ModelParams.java

Central parameter file for the fresh apple model.

Main parameter groups:

- Timeframe, including years and steps per year.
- Producer survey path and apple yield assumptions.
- Number of consumer agents and represented consumers per agent.
- Initial budgets and inventories.
- Production cost per pound.
- Cost share assumptions:

```text
Fertilizer cost share   = 0.0710
Fuel cost share         = 0.0320
Other input cost share  = 0.1520
Other cost share        = 0.7450
```

- Translog production function settings:

```text
USE_TRANSLOG_PRODUCTION_FUNCTION = true
TRANSLOG_SELF_INTERACTION = 0.0
TRANSLOG_CROSS_INTERACTION = 0.0
```

- Farm price anchor parameters:

```text
USE_FARM_ANCHORS_FOR_PRICE = true
FARM_ANCHOR_BLEND = 0.90
FARM_DEMAND_LB_PER_CAPITA = 17.0
FARM_SUPPLY_LB_PER_CAPITA = 21.26
```

- Price bounds, price elasticity, seasonality, and AR(1) settings.
- Wholesale and retail markup settings.
- Input data paths.

### ProducerSurveyLoader.java

Loads producer survey data from:

```text
data/apple_survey_6.13_producers.csv
```

Main responsibilities:

- Reads `farmer_id`, `acres`, and `annual_production_lbs`.
- Creates one `ProducerRecord` per farmer.
- Supplies producer metadata to `AppleMarketBuilder`.
- Falls back to 38 empty producer records if the file cannot be loaded.

### MLPriceLoader.java

Loads the fresh apple price path.

Main responsibilities:

- Reads `model_predictions_2021_2025_gradient_boosting_daily_mean.csv`.
- Uses `mean_prediction` where `model = GradientBoosting`.
- Returns the daily baseline price for `FRESH APPLES`.
- Provides fallback prices if a file is unavailable.

### TranslogProductionFunction.java

Implements a normalized translog production function.

Purpose:

- Keeps each farmer's baseline production equal to their survey acreage-derived pounds when there are no shocks.
- Allows future shocks to fertilizer, fuel, other input, or other cost to affect production.

Current logic:

```text
Q = baseline survey production * exp(translog multiplier)
```

At current baseline values, all log ratios are zero, so:

```text
Q = original survey-converted pounds
```

This preserves each farmer's current production level until shocks are introduced.

### InputCostScenario.java

Scenario hook for input cost shocks.

Current behavior:

- Returns baseline fertilizer cost.
- Returns baseline fuel cost.
- Returns baseline other input cost.
- Returns baseline other cost.

Future use:

- Subclass or modify this file to introduce fertilizer, fuel, other input, or other cost shocks over time.

### DataTracker.java

Collects and writes model outputs.

Main outputs:

- `apples_timeseries.csv`
- `apples_producer_1.csv` through `apples_producer_38.csv`

Producer report columns:

```text
iteration,price,units_sold_day,revenue_day,units_sold_cum,revenue_cum
```

### MathUtil.java

Small utility class for shared math operations.

Current use:

- Clamping prices and ratios within specified lower and upper bounds.

## Fresh Apple Agent Files

Fresh apple agents live under:

```text
src/main/java/com/example/abm/apple/agents/
```

### ProducerAgent.java

Represents one fresh apple producer.

Current fresh apple setup:

- 38 producer agents.
- Each producer is based on one survey farmer.
- Each producer has acreage and annual production pounds.
- Production is based on survey-derived annual pounds and the normalized translog production function.
- Producers sell fresh apples to the wholesaler.
- Tracks daily and cumulative units sold and revenue.

Key fields:

- `id`
- `variety`
- `acres`
- `annualProductionLbs`
- `askingPrice`
- `productionCost`
- `producedThisStep`
- `unitsSoldThisStep`
- `revenueThisStep`
- `unitsSoldCum`
- `revenueCum`

### WholesaleAgent.java

Represents the fresh apple wholesaler.

Main responsibilities:

- Holds wholesale inventory by variety.
- Receives retailer orders.
- Procures fresh apples from producers.
- Pays producers at their asking price.
- Sells inventory to the retailer at wholesale ask prices.
- Updates wholesale prices using the producer price and markup.

Current fresh apple setup:

- One wholesale agent.
- One variety: `FRESH APPLES`.

### RetailAgent.java

Represents the fresh apple retailer.

Main responsibilities:

- Holds retail inventory.
- Orders from the wholesaler based on expected demand and inventory cover.
- Updates retail prices based on wholesale ask prices and retail markup.
- Sells to consumer agents when consumers can afford the product.

Current fresh apple setup:

- One retail agent.
- One variety: `FRESH APPLES`.

### ConsumerAgent.java

Represents scaled fresh apple demand.

Main responsibilities:

- Holds consumer money and willingness to pay.
- Converts annual fresh apple consumption into per-step demand.
- Attempts purchases from the retailer.
- Tracks cumulative spending and items bought.

Current fresh apple setup:

- 1,000 consumer agents.
- Each consumer agent represents many real-world consumers.
- Demand is based on annual fresh apple consumption per person.

## Scripts

### scripts/build_producer_dashboard.py

Creates:

```text
output/apple_producers_daily_long.csv
output/apple_producers_interactive_dashboard.html
```

Use it after running the model if producer CSV outputs have changed.

Command:

```powershell
python scripts/build_producer_dashboard.py
```

If the system Python does not have required packages or is not on `PATH`, use the bundled Codex Python if available.

## Running the Fresh Apple Model

The project was tested using:

```text
Java 8
Repast Symphony 2.11.0
```

The Eclipse classpath includes:

```text
REPAST_SIMPHONY_SUPPORT
GROOVY_SUPPORT
Maven dependency container
```

In Eclipse / Repast Symphony:

1. Open the project in the Repast Symphony Eclipse IDE.
2. Make sure the JRE is JavaSE-1.8.
3. Run:

```text
com.example.abm.apple.Main
```

or use the existing Java launch configuration for `Main.java`.

If using Maven, the `pom.xml` is configured for Java 8 and uses:

```text
com.example.abm.apple.Main
```

as the exec and jar main class.

## Notes on Generated Files

The following folders contain generated or environment-specific files:

```text
bin/
target/
tmp/
output/
```

## Fresh Apple Model Summary

The fresh apple model currently represents:

- One commodity: `FRESH APPLES`
- 38 producer agents from survey acreage
- One wholesale agent
- One retail agent
- Scaled consumer demand
- Daily Gradient Boosting predicted farm price baseline
- Farm supply and demand price anchors
- Production cost per pound split into fertilizer, fuel, other input, and other cost
- Normalized translog production function preserving survey production at baseline
- Producer-level daily and cumulative output reports
- Interactive producer sales and revenue dashboard

Depending on the purpose of a GitHub upload, you may or may not want to commit `output/`.

Recommended:

- Commit source code in `src/`.
- Commit core input files in `data/` if they are needed to run the model.
- Commit `scripts/`.
- Commit selected documentation and calibration PDFs if allowed.
- Avoid committing compiled classes in `bin/` and `target/`.
- Avoid committing temporary files in `tmp/`.

## Suggested .gitignore

Before uploading to GitHub, consider creating a `.gitignore` file with:

```text
bin/
target/
tmp/
.metadata/
*.class

# Optional: ignore generated simulation outputs unless you want to publish them
# output/

# Optional: keep generated dashboards and summaries if useful
# !output/apple_producers_interactive_dashboard.html
# !output/fresh_apple_abm_10min_presentation.txt
```

Do not ignore `data/` if the model needs those files to run, unless you plan to provide the data separately.

## How to Upload This Folder to GitHub

There are three common ways to upload this project.

### Option 1: Command Line Git

1. Create a new empty repository on GitHub.

Example repository name:

```text
fresh-apple-abm
```

Do not initialize it with a README if you want to push this local README directly.

2. Open PowerShell in the project folder:

```powershell
cd C:\eclipse-workspace\abm_05082026
```

3. Initialize Git:

```powershell
git init
```

4. Check what will be added:

```powershell
git status
```

5. Add files:

```powershell
git add README.md pom.xml .project .classpath .settings src data scripts pdf
```

If you also want to upload generated outputs:

```powershell
git add output
```

6. Commit:

```powershell
git commit -m "Initial ABM project upload"
```

7. Connect to GitHub. Replace `YOUR-USERNAME` and `YOUR-REPO`:

```powershell
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/YOUR-REPO.git
git push -u origin main
```

### Option 2: GitHub CLI

If GitHub CLI is installed:

```powershell
cd C:\eclipse-workspace\abm_05082026
git init
git add README.md pom.xml .project .classpath .settings src data scripts pdf
git commit -m "Initial ABM project upload"
gh repo create fresh-apple-abm --private --source . --remote origin --push
```

Use `--public` instead of `--private` if the repository should be public.

### Option 3: GitHub Desktop

1. Open GitHub Desktop.
2. Choose `File > Add local repository`.
3. Select:

```text
C:\eclipse-workspace\abm_05082026
```

4. If GitHub Desktop says it is not a repository, choose to create a repository.
5. Review changed files.
6. Commit the files.
7. Click `Publish repository`.
8. Choose public or private.

## After Uploading

After pushing to GitHub:

1. Open the repository page.
2. Confirm `README.md` renders correctly.
3. Confirm source files are present under:

```text
src/main/java/com/example/abm/
```

4. Confirm required input files are present under:

```text
data/
```

5. If large files are rejected by GitHub, remove them from the commit or use Git Large File Storage.

GitHub has a file size warning around large files and a hard limit for very large files. If you decide to commit large PDFs, model outputs, or raw prediction files, consider using Git LFS.
