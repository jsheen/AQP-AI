# Get average distances traveled per day per inspector
# March Justin

#===================================================================================
#  Part I: Dependencies, sources, directories
#===================================================================================
source(file.path('~/PETM-shiny/shiny/controller/global.R'))

lat_long_direc<- file.path("~/PETM-shiny/unicode_numberMzn/AREQUIPA_GPS_GOOGLE")
latLongFile <- read.csv(file.path(lat_long_direc, "AQP_GPS_GOOGLE_EARTH_PUNTOS_05_jun_2017.csv"))
latLongFile$UNICODE <- as.character(latLongFile$UNICODE)

LoadDataAPP <- function(databaseName = "Chagas_Arequipa", tableName="APP_INSPECTIONS") {
  #Esta funcion retorna un los datos que se ingresaron en el APP
  #
  #ARGS
  # databaseName = Nombre de la base de datos
  # tableName = Nombre de la tabla
  #
  #RETURNS
  # datos_app = datos ingresados por los usuarios en campo al APP
  #
  # Connect to the database
  db <-
    dbConnect(
      MySQL(),
      dbname = databaseName,
      host = dbGlobalConfig$host,
      port = dbGlobalConfig$port,
      user = dbGlobalConfig$user,
      password = dbGlobalConfig$password
    )
  # Construct the fetching query
  query <- sprintf("SELECT * FROM %s", tableName)
  # Submit the fetch query and disconnect
  datos_app <- dbGetQuery(db, query)
  #Desconectarnos de la base de datos
  dbDisconnect(db)
  
  return(datos_app)
}

#===================================================================================
#  Part II: Load and prepare active data (visit data)
#===================================================================================

# parameters
province <- "1"
district <- "13"

# load active data
activedata <- LoadDataAPP()
province="1"
activedata <- data.frame(lapply(activedata, as.character), stringsAsFactors=FALSE)
stopifnot(!is.null(district))
stopifnot(length(province) == 1)
stopifnot(length(district) == 1) 
unicodeFilter <- paste0('^', province, '\\.', district, '\\.') 
unicodeFilter <- paste0(unicodeFilter, collapse = '|') #match any locality pattern

# if there is no active data that matches pattern
matchingRowsInActiveData <- which(grepl(x=activedata$UNI_CODE, pattern = unicodeFilter))
cat(' Records in SQL: ', length(matchingRowsInActiveData), '\n')

# if there is active data, keep just inspected houses and real data (not test
# data) and also delete all houses with no FECHA information
activedata <- activedata[matchingRowsInActiveData,]
realData        <- activedata$TEST_DATA == "0"
activedata <- activedata[which(realData),]
activedata <- activedata[which(!is.na(activedata$FECHA)),]
cat(' True inspections:', nrow(activedata), '\n')

# check that FECHAS are fine for this locality
fecha <- as.Date(x = activedata$FECHA, format = '%Y-%m-%d')
fechaErrorRowNum <- which(is.na(fecha))
if(length(fechaErrorRowNum) > 0) {
  print('Fechas de los inspecciones deben use el formato 2017-12-31')
}

# get INSP_POSITIVA column from activedata
activedata$TOT_INTRA <- gsub(' ', '', activedata$TOT_INTRA)
activedata$TOT_PERI <- gsub(' ', '', activedata$TOT_PERI)
activedata$TOT_INTRA <- ifelse(is.na(activedata$TOT_INTRA) | activedata$TOT_INTRA == "NA",
                               "0", 
                               activedata$TOT_INTRA)
activedata$TOT_PERI <- ifelse(is.na(activedata$TOT_PERI) | activedata$TOT_PERI == "NA", 
                              "0", 
                              activedata$TOT_PERI)
activedata$INSP_POSITIVA <- ifelse(!is.na(activedata$TOT_INTRA) & !is.na(activedata$TOT_PERI) & 
                                     !is.null(activedata$TOT_INTRA) & !is.null(activedata$TOT_PERI) & 
                                     !activedata$TOT_INTRA == "NA" & !activedata$TOT_PERI == "NA" & 
                                     !activedata$TOT_INTRA == "NULL" & !activedata$TOT_PERI == "NULL" & 
                                     as.numeric(activedata$TOT_INTRA) + as.numeric(activedata$TOT_PERI) > 0,
                                   "1",
                                   "0")

#===================================================================================
#  Part III: Fill in all data / lists for stats later
#===================================================================================

# helper function for distance
dt.haversine <- function(lat_from, lon_from, lat_to, lon_to, r = 6378137){
  radians <- pi/180
  lat_to <- lat_to * radians
  lat_from <- lat_from * radians
  lon_to <- lon_to * radians
  lon_from <- lon_from * radians
  dLat <- (lat_to - lat_from)
  dLon <- (lon_to - lon_from)
  a <- (sin(dLat/2)^2) + (cos(lat_from) * cos(lat_to)) * (sin(dLon/2)^2)
  return(2 * atan2(sqrt(a), sqrt(1 - a)) * r)
}

userActive <- c("21", "8", "9", "14", "20", "24", "2", "18")
visitList <- list()
distList <- list()
inspList <- list()
for (i in 1:length(userActive)) {
  # prepare visit list
  getData <- activedata[which(activedata$USER_NAME == paste0("r", userActive[i]) | activedata$USER_NAME == paste0("R", userActive[i])),]
  getData <- getData[which(getData$UNI_CODE != "1.13.89.89"),] # this is the sole unicode without lat or lon data
  getData <- getData[which(!grepl("2018", getData$FECHA)),]
  
  uniqueDates <- unique(getData$FECHA)
  for (o in 1:length(uniqueDates)) {
    dateData <- getData[which(getData$FECHA == uniqueDates[o]),]
    
    #visitList
    visitList <- c(visitList, nrow(dateData))
    
    #distList
    dateData <- dateData[order(as.POSIXct(dateData$DATETIME, format = "%Y-%m-%d %H:%M:%S")),]
    dateData$UNI_CODE <- gsub(' ', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('A', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('a', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('B', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('b', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('C', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('c', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('D', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('d', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('E', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('e', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('F', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('f', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('O', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('o', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('<', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('>', '', dateData$UNI_CODE)
    dateData$UNI_CODE <- gsub('int', '', dateData$UNI_CODE)
    dateData$LATITUDE <- NA
    dateData$LONGITUDE <- NA
    totDist <- 0
    for (j in 1:nrow(dateData)) {
      print(userActive[i])
      print(dateData$UNI_CODE[j])
      toAddLat <- latLongFile$LATITUDE[which(latLongFile$UNICODE == dateData$UNI_CODE[j])]
      toAddLon <- latLongFile$LONGITUDE[which(latLongFile$UNICODE == dateData$UNI_CODE[j])]
      dateData$LATITUDE[j] <- toAddLat
      dateData$LONGITUDE[j] <- toAddLon
    }
    
    for (k in 2:nrow(dateData)) {
      totDist <- totDist + dt.haversine(dateData$LATITUDE[k], dateData$LONGITUDE[k], 
                                        dateData$LATITUDE[k - 1], dateData$LONGITUDE[k - 1])
    }
    distList <- c(distList, totDist)
    
    #inspList
    realInsps <- dateData$STATUS_INSPECCION %in% c('I', 'inspeccion')
    insps <- dateData[which(realInsps),]
    inspList <- c(inspList, nrow(insps))

  }
}

#===================================================================================
#  Part IV: Get statistics we want
#===================================================================================

visitVec <- unlist(visitList, use.names=FALSE)
distVec <- unlist(distList, use.names=FALSE)
inspVec <- unlist(inspList, use.names=FALSE)
sucVec <- inspVec / visitVec

# visit stats
mean(visitVec) # 23.12
sd(visitVec) # 5.58
max(visitVec) # 33
min(visitVec) # 12

# dist stats
mean(distVec) # 574.93 m
sd(distVec) # 295.66 m
max(distVec) # 1484.53 m
min(distVec) # 118.83 m

# insp stats
mean(inspVec) # 9.38
sd(inspVec) # 4.76
max(inspVec) # 25
min(inspVec) # 5

# percentage successful
mean(sucVec) # 43.52 %
sd(sucVec) # 24.42 %
max(sucVec) # 100 %
min(sucVec) # 16.13 %
