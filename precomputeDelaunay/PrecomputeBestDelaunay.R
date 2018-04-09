# Precompute best Delaunay Triangulation houses
# March 5, 2018

# dependencies
library(deldir)
library(sp)

# I: Load data
setwd("~/AQP-AI/AI")
rawData <- read.csv("forSimulator.csv", sep=" ")
colsToKeep <- c("UNICODE", "LATITUDE", "LONGITUDE")
rawData <- rawData[,colsToKeep]
rawData$UNICODE <- as.character(rawData$UNICODE)
rawData$LATITUDE <- as.numeric(rawData$LATITUDE)
rawData$LONGITUDE <- as.numeric(rawData$LONGITUDE)

# II: Get max lat and lon for the four corners

# Get triangulation function
getBiggestTriangle <- function(SZ, SZ_inspected) {
  # Triangulation
  vtess <- deldir(SZ_inspected$LATITUDE, SZ_inspected$LONGITUDE)
  triangl <- triang.list(vtess)
  
  # Assign each house to a polygon (list of dataframes)
  df <- list()
  for (j in 1:length(triangl)) {
    polyVertices <- triangl[[j]][2:3]
    
    unicode <- list()
    lat <- list()
    lon <- list()
    i <- 1
    for (k in 1:nrow(SZ)) {
      if (as.character(SZ$UNICODE[k]) %in% as.character(SZ_inspected$UNICODE)) {
        # do nothing
      } else {
        if (point.in.polygon(SZ$LATITUDE[k], SZ$LONGITUDE[k], polyVertices$x, polyVertices$y)) {
          unicode[[i]] <- SZ$UNICODE[k]
          lat[[i]] <- SZ$LATITUDE[k]
          lon[[i]] <- SZ$LONGITUDE[k]
          i <- i + 1
        }
      }
    }
    unicode <- sapply(unicode, rbind)
    lat <- sapply(lat, rbind)
    lon <- sapply(lon, rbind)
    toAdd <- cbind(unicode, lat)
    toAdd <- cbind(toAdd, lon)
    
    df[[j]] <- toAdd
  }
  
  # Return the list of houses that is the largest
  bestDex <- 0
  bestSize <- 0
  for (i in 1:length(df)) {
    if (nrow(df[[i]]) > bestSize) {
      bestDex <- i
      bestSize <- nrow(df[[i]])
    }
  }
  
  toReturn <- df[[bestDex]]
  
  colnames(toReturn) <- c("UNICODE", "LATITUDE", "LONGITUDE")
  
  return(toReturn)
}

# vertex list
vertices <- data.frame(matrix(ncol = 3, nrow = 1))
colnames(vertices) <- c("UNICODE", "LATITUDE", "LONGITUDE")
vertices$UNICODE <- as.character(vertices$UNICODE)

# four corners
maxLat <- max(as.numeric(rawData$LATITUDE))
maxLong <- max(as.numeric(rawData$LONGITUDE))
minLat <- min(as.numeric(rawData$LATITUDE))
minLong <- min(as.numeric(rawData$LONGITUDE))
vertices <- rbind(vertices, c("maxLatmaxLong", maxLat, maxLong))
vertices <- rbind(vertices, c("maxLatminLong", maxLat, minLong))
vertices <- rbind(vertices, c("minLatmaxLong", minLat, maxLong))
vertices <- rbind(vertices, c("minLatminLong", minLat, minLong))
vertices <- vertices[c(2, 3, 4, 5),]
vertices$LATITUDE <- as.numeric(vertices$LATITUDE)
vertices$LONGITUDE <- as.numeric(vertices$LONGITUDE)

for (i in 1:15) {
  # should return a list of the houses of the biggest triangle
  listHouses <- getBiggestTriangle(rawData, vertices)
  
  # then, try each trianglulation for each house of the list of houses to get the house with best triangulation outcome
  bestHouseDex <- 0
  bestHouseNum <- nrow(rawData)
  printCnt <- 1
  for (j in 1:nrow(listHouses)) {
    print(printCnt)
    augOneVertices <- vertices
    augOneVertices <- rbind(augOneVertices, c(listHouses[j,1], listHouses[j,2], listHouses[j,3]))
    augOneVertices$LATITUDE <- as.numeric(augOneVertices$LATITUDE)
    augOneVertices$LONGITUDE <- as.numeric(augOneVertices$LONGITUDE)
    
    result <- getBiggestTriangle(rawData, augOneVertices)
    
    if (nrow(result) < bestHouseNum) {
      bestHouseDex <- j
      bestHouseNum <- nrow(result)
    }
    printCnt <- printCnt + 1
  }
  
  # add this to the vertices
  vertices <- rbind(vertices, c(listHouses[bestHouseDex,1], listHouses[bestHouseDex,2], listHouses[bestHouseDex,3]))
  vertices$LATITUDE <- as.numeric(vertices$LATITUDE)
  vertices$LONGITUDE <- as.numeric(vertices$LONGITUDE)
}

# now, we will have the vertices that we need
View(vertices)
