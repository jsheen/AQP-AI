# show where the q value curve
# april justin

#===================================================================================
#  Part I: Dependencies, sources, directories
#===================================================================================

if(!require(data.table)){install.packages("data.table", repos="http://cran.rstudio.com/", dependencies=TRUE)}
library(data.table)

#===================================================================================
#  Part II: Read in the file and prepare
#===================================================================================

setwd("~/AQP-AI/AI/qValOverTime")
toDisplay <- read.table("qValsFriApr1316_32_02PET2018.txt")

#===================================================================================
#  Part III: Display curve
#===================================================================================

plot(1:nrow(toDisplay), toDisplay$V1, col="black", pch=16, cex=0.5)
