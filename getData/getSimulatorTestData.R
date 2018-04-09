# Get test data for simulator
# February 2018 Justin Sheen

library(RColorBrewer)
library(leaflet)
library(data.table)
source("~/PETM-shiny/shiny/controller/palettes.R")

# 1. assumed the model has already run

# 2. add block things
output$block <- NA
for (i in 1:nrow(output)) {
  output$block[i] <- saveData[which(saveData$UNICODE == output$UNICODE[i]),]$block[1]
}

# 3. get risk levels of houses
output <- as.data.table(output)
#Creando columna de los colores
output$quant <- YlOrRd.q(output[, probability])
for (i in 1:nrow(output)) {
  if (output$quant[i] == "#FFFFB2") {
    output$quant[i] <- 0
  } else if (output$quant[i] == "#FECC5C") {
    output$quant[i] <- 1
  } else if (output$quant[i] == "#FD8D3C") {
    output$quant[i] <- 2
  } else if (output$quant[i] == "#F03B20") {
    output$quant[i] <- 3
  } else if (output$quant[i] == "#BD0026") {
    output$quant[i] <- 4
  }
}

# 4. prepare and print out
df <- output
varsToKeep <- c("UNICODE", "LATITUDE", "LONGITUDE", "quant","block")
df <- df[,varsToKeep,with=FALSE]

output_direc <- "/Users/Justin/Desktop"
filename <- file.path(output_direc, paste("forSimulator.csv", sep=""))
write.table(df, filename, row.names = F)
