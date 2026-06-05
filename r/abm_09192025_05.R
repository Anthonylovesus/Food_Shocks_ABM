#install.packages("tidyr")
#install.packages("ggplot2")
#install.packages("Rtools")

setwd("C:/eclipse-workspace/abm_09192025_05/r")
getwd()

library(tidyr)
library(ggplot2)

# fresh apples

df <- read.csv("apples_producer_full.csv")
df$Iteration <- as.integer(df$Iteration)

# convert df from wide -> long for ggplot
long <- pivot_longer(df, cols = -Iteration,
                     names_to = "Variety", values_to = "price")

p01 <- ggplot(long, aes(x=Iteration, y=price, group=Variety, color=Variety)) +
  geom_line(linewidth=1) +
  labs(x="Iteration", y="Price", title="Fresh Apples Prices") +
  theme_minimal()

dir.create("plots", showWarnings = FALSE)
out_png <- file.path("plots", "fresh_apple_prices.png")

ggsave(filename = out_png, plot = p01, width = 8, height = 5, dpi = 150)

# processed apples

df <- read.csv("apples_processed_producer_full.csv")
df$Iteration <- as.integer(df$Iteration)

# convert df from wide -> long for ggplot
long <- pivot_longer(df, cols = -Iteration,
                     names_to = "Variety", values_to = "price")

p02 <- ggplot(long, aes(x=Iteration, y=price, group=Variety, color=Variety)) +
  geom_line(linewidth=1) +
  labs(x="Iteration", y="Price", title="Processed Apples Prices") +
  theme_minimal()

dir.create("plots", showWarnings = FALSE)
out_png <- file.path("plots", "processed_apple_prices.png")

ggsave(filename = out_png, plot = p02, width = 8, height = 5, dpi = 150)

# tomatoes

df <- read.csv("tomatoes_producer_full.csv")
df$Iteration <- as.integer(df$Iteration)

# convert df from wide -> long for ggplot
long <- pivot_longer(df, cols = -Iteration,
                     names_to = "Variety", values_to = "price")

p03 <- ggplot(long, aes(x=Iteration, y=price, group=Variety, color=Variety)) +
  geom_line(linewidth=1) +
  labs(x="Iteration", y="Price", title="Tomato Prices") +
  theme_minimal()

dir.create("plots", showWarnings = FALSE)
out_png <- file.path("plots", "tomato_prices.png")

ggsave(filename = out_png, plot = p03, width = 8, height = 5, dpi = 150)

# tomatoes

df <- read.csv("tomatoes_processed_producer_full.csv")
df$Iteration <- as.integer(df$Iteration)

# convert df from wide -> long for ggplot
long <- pivot_longer(df, cols = -Iteration,
                     names_to = "Variety", values_to = "price")

p04 <- ggplot(long, aes(x=Iteration, y=price, group=Variety, color=Variety)) +
  geom_line(linewidth=1) +
  labs(x="Iteration", y="Price", title="Processed Tomato Prices") +
  theme_minimal()

dir.create("plots", showWarnings = FALSE)
out_png <- file.path("plots", "processed_tomato_prices.png")

ggsave(filename = out_png, plot = p04, width = 8, height = 5, dpi = 150)


# beef

df <- read.csv("beef_producer_full.csv")
df$Iteration <- as.integer(df$Iteration)

# convert df from wide -> long for ggplot
long <- pivot_longer(df, cols = -Iteration,
                     names_to = "Variety", values_to = "price")

p05 <- ggplot(long, aes(x=Iteration, y=price, group=Variety, color=Variety)) +
  geom_line(linewidth=1) +
  labs(x="Iteration", y="Price", title="Beef Prices") +
  theme_minimal()

dir.create("plots", showWarnings = FALSE)
out_png <- file.path("plots", "beef_prices.png")

ggsave(filename = out_png, plot = p05, width = 8, height = 5, dpi = 150)

# dairy

df <- read.csv("dairy_producer_full.csv")
df$Iteration <- as.integer(df$Iteration)

# convert df from wide -> long for ggplot
long <- pivot_longer(df, cols = -Iteration,
                     names_to = "Variety", values_to = "price")

p06 <- ggplot(long, aes(x=Iteration, y=price, group=Variety, color=Variety)) +
  geom_line(linewidth=1) +
  labs(x="Iteration", y="Price", title="Dairy Prices") +
  theme_minimal()

dir.create("plots", showWarnings = FALSE)
out_png <- file.path("plots", "dairy_prices.png")

ggsave(filename = out_png, plot = p06, width = 8, height = 5, dpi = 150)

# processed dairy

df <- read.csv("dairy_processed_producer_full.csv")
df$Iteration <- as.integer(df$Iteration)

# convert df from wide -> long for ggplot
long <- pivot_longer(df, cols = -Iteration,
                     names_to = "Variety", values_to = "price")

p07 <- ggplot(long, aes(x=Iteration, y=price, group=Variety, color=Variety)) +
  geom_line(linewidth=1) +
  labs(x="Iteration", y="Price", title="Processed Dairy Prices") +
  theme_minimal()

dir.create("plots", showWarnings = FALSE)
out_png <- file.path("plots", "processed_dairy_prices.png")

ggsave(filename = out_png, plot = p07, width = 8, height = 5, dpi = 150)

# wheat

df <- read.csv("wheat_producer_full.csv")
df$Iteration <- as.integer(df$Iteration)

# convert df from wide -> long for ggplot
long <- pivot_longer(df, cols = -Iteration,
                     names_to = "Variety", values_to = "price")

p08 <- ggplot(long, aes(x=Iteration, y=price, group=Variety, color=Variety)) +
  geom_line(linewidth=1) +
  labs(x="Iteration", y="Price", title="Wheat Prices") +
  theme_minimal()

dir.create("plots", showWarnings = FALSE)
out_png <- file.path("plots", "wheat_prices.png")

ggsave(filename = out_png, plot = p08, width = 8, height = 5, dpi = 150)

# sample loop
#csv_files <- list.files(pattern = "\\.csv$", full.names = TRUE)

#for (f in csv_files) {
  
  #df <- read.csv(f)
  
  # skip files that don't come with variable Iteration
  #if (!("Iteration" %in% names(df))) {
    #message("Skipping (no Iteration column): ", f)
    #next
  #}
  
  #df$Iteration <- as.integer(df$Iteration)
  
  # convert df from wide -> long
  #long <- pivot_longer(df, cols = -Iteration,
                       #names_to = "Variety", values_to = "price")
  
  #p <- ggplot(long, aes(x = Iteration, y = price, group = Variety, color = Variety)) +
    #geom_line(linewidth = 1) +
    #labs(x = "Iteration", y = "Price",
         #title = tools::file_path_sans_ext(basename(f))) +
    #theme_minimal()
  
  # save them as pngs
  #out_png <- file.path("plots", paste0(tools::file_path_sans_ext(basename(f)), ".png"))
  #dir.create("plots", showWarnings = FALSE)
  
  #ggsave(out_png, plot = p, width = 8, height = 5, dpi = 150)
  
  #message("Saved: ", out_png)
#}