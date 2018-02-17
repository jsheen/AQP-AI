A brief description of each database:

1. gpsdata_COMPLETE.csv: 
- Contains all observations taken in Arequipa during the rociado spray campaign to eliminate chirimacha insects from houses. 
- Because the file is observations, it is possible to have multiple observations (at most two) for one unicode.
- This file also includes ALL GPS points of ALL houses in Arequipa.
- The not_sprayed variable == 1 when it was NOT SPRAYED AND INSPECTED, and == 0 when it was SPRAYED AND INSPECTED.
- If the not_sprayed variable == 1, then the pos_sprayed variable will equal “NA”. If the not_sprayed variable == 0, then the pos_sprayed variable will equal “0” or “1.”
- The pos_sprayed variable == 1 when the house was INFESTED, and == 0 when the house was NOT INFESTED.

2. inspecciones_COMPLETE.csv:
- Contains all observations taken in Arequipa after the rociado spray campaign to eliminate chirimacha insects from houses.
- Because the file is observations, it is possible to have multiple observations for one unicode.
- This file DOES NOT include all GPS points of ALL houses in Arequipa.
- INSP_POSITIVA == 0 when the house was NOT INFESTED, and == 1 when the house was INFESTED
- Just a note: if a house is found to be infested during an inspection, then in principle it is planned to be sprayed.

