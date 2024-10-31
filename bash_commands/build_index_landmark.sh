#!/bin/bash

java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m IndexBuilding -dbname C9_NY_NONE_5K -neo4jdb ../Data/C9_NY_NONE_5K/db -indexFolder /Data/indexes/C9_NY_NONE_5K -min_size 200 -percentage 0.01 -degreeHandle normal
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_5K_Level1 -neo4jdb ../Data/C9_NY_NONE_5K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_5K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_5K_Level2 -neo4jdb ../Data/C9_NY_NONE_5K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_5K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_5K_Level3 -neo4jdb ../Data/C9_NY_NONE_5K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_5K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_5K_Level4 -neo4jdb ../Data/C9_NY_NONE_5K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_5K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_5K_Level5 -neo4jdb ../Data/C9_NY_NONE_5K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_5K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_5K_Level6 -neo4jdb ../Data/C9_NY_NONE_5K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_5K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_5K_Level7 -neo4jdb ../Data/C9_NY_NONE_5K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_5K -nLandMark 3 -cLandMark true

java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m IndexBuilding -dbname C9_NY_NONE_15K -neo4jdb ../Data/C9_NY_NONE_15K/db -indexFolder ../Data/indexes/C9_NY_NONE_15K -min_size 200 -percentage 0.01 -degreeHandle normal
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_15K_Level1 -neo4jdb ../Data/C9_NY_NONE_15K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_15K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_15K_Level2 -neo4jdb ../Data/C9_NY_NONE_15K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_15K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_15K_Level3 -neo4jdb ../Data/C9_NY_NONE_15K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_15K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_15K_Level4 -neo4jdb ../Data/C9_NY_NONE_15K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_15K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_15K_Level5 -neo4jdb ../Data/C9_NY_NONE_15K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_15K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_15K_Level6 -neo4jdb ../Data/C9_NY_NONE_15K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_15K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_15K_Level7 -neo4jdb ../Data/C9_NY_NONE_15K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_15K -nLandMark 3 -cLandMark true

java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m IndexBuilding -dbname C9_NY_NONE_30K -neo4jdb ../Data/C9_NY_NONE_30K/db -indexFolder ../Data/indexes/C9_NY_NONE_30K -min_size 200 -percentage 0.01 -degreeHandle normal
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_30K_Level1 -neo4jdb ../Data/C9_NY_NONE_30K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_30K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_30K_Level2 -neo4jdb ../Data/C9_NY_NONE_30K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_30K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_30K_Level3 -neo4jdb ../Data/C9_NY_NONE_30K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_30K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_30K_Level4 -neo4jdb ../Data/C9_NY_NONE_30K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_30K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_30K_Level5 -neo4jdb ../Data/C9_NY_NONE_30K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_30K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_30K_Level6 -neo4jdb ../Data/C9_NY_NONE_30K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_30K -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_30K_Level7 -neo4jdb ../Data/C9_NY_NONE_30K/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_30K -nLandMark 3 -cLandMark true


java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m IndexBuilding -dbname C9_BAY_NONE -neo4jdb ../Data/C9_BAY_NONE/db -indexFolder ../Data/indexes/C9_BAY_NONE -min_size 200 -percentage 0.01 -degreeHandle normal
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_BAY_NONE_Level1 -neo4jdb ../Data/C9_BAY_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_BAY_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_BAY_NONE_Level2 -neo4jdb ../Data/C9_BAY_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_BAY_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_BAY_NONE_Level3 -neo4jdb ../Data/C9_BAY_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_BAY_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_BAY_NONE_Level4 -neo4jdb ../Data/C9_BAY_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_BAY_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_BAY_NONE_Level5 -neo4jdb ../Data/C9_BAY_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_BAY_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_BAY_NONE_Level6 -neo4jdb ../Data/C9_BAY_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_BAY_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_BAY_NONE_Level7 -neo4jdb ../Data/C9_BAY_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_BAY_NONE -nLandMark 3 -cLandMark true


java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m IndexBuilding -dbname C9_CTR_NONE -neo4jdb ../Data/C9_CTR_NONE/db -indexFolder ../Data/indexes/C9_CTR_NONE -min_size 200 -percentage 0.01 -degreeHandle normal
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_CTR_NONE_Level1 -neo4jdb ../Data/C9_CTR_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_CTR_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_CTR_NONE_Level2 -neo4jdb ../Data/C9_CTR_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_CTR_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_CTR_NONE_Level3 -neo4jdb ../Data/C9_CTR_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_CTR_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_CTR_NONE_Level4 -neo4jdb ../Data/C9_CTR_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_CTR_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_CTR_NONE_Level5 -neo4jdb ../Data/C9_CTR_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_CTR_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_CTR_NONE_Level6 -neo4jdb ../Data/C9_CTR_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_CTR_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname C9_CTR_NONE_Level7 -neo4jdb ../Data/C9_CTR_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/C9_CTR_NONE -nLandMark 3 -cLandMark true


java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m IndexBuilding -dbname L_CAL_NONE -neo4jdb ../Data/L_CAL_NONE/db -indexFolder ../Data/indexes/L_CAL_NONE -min_size 200 -percentage 0.01 -degreeHandle normal
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_CAL_NONE_Level1 -neo4jdb ../Data/L_CAL_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_CAL_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_CAL_NONE_Level2 -neo4jdb ../Data/L_CAL_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_CAL_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_CAL_NONE_Level3 -neo4jdb ../Data/L_CAL_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_CAL_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_CAL_NONE_Level4 -neo4jdb ../Data/L_CAL_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_CAL_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_CAL_NONE_Level5 -neo4jdb ../Data/L_CAL_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_CAL_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_CAL_NONE_Level6 -neo4jdb ../Data/L_CAL_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_CAL_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_CAL_NONE_Level7 -neo4jdb ../Data/L_CAL_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_CAL_NONE -nLandMark 3 -cLandMark true

java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m IndexBuilding -dbname L_NA_NONE -neo4jdb ../Data/L_NA_NONE/db -indexFolder ../Data/indexes/L_NA_NONE -min_size 200 -percentage 0.01 -degreeHandle normal
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_NA_NONE_Level1 -neo4jdb ../Data/L_NA_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_NA_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_NA_NONE_Level2 -neo4jdb ../Data/L_NA_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_NA_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_NA_NONE_Level3 -neo4jdb ../Data/L_NA_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_NA_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_NA_NONE_Level4 -neo4jdb ../Data/L_NA_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_NA_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_NA_NONE_Level5 -neo4jdb ../Data/L_NA_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_NA_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_NA_NONE_Level6 -neo4jdb ../Data/L_NA_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_NA_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_NA_NONE_Level7 -neo4jdb ../Data/L_NA_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_NA_NONE -nLandMark 3 -cLandMark true

java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m IndexBuilding -dbname L_SF_NONE -neo4jdb ../Data/L_SF_NONE/db -indexFolder ../Data/indexes/L_SF_NONE -min_size 200 -percentage 0.01 -degreeHandle normal
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_SF_NONE_Level1 -neo4jdb ../Data/L_SF_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_SF_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_SF_NONE_Level2 -neo4jdb ../Data/L_SF_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_SF_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_SF_NONE_Level3 -neo4jdb ../Data/L_SF_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_SF_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_SF_NONE_Level4 -neo4jdb ../Data/L_SF_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_SF_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_SF_NONE_Level5 -neo4jdb ../Data/L_SF_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_SF_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_SF_NONE_Level6 -neo4jdb ../Data/L_SF_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_SF_NONE -nLandMark 3 -cLandMark true
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m BuildLandMark -dbname L_SF_NONE_Level7 -neo4jdb ../Data/L_SF_NONE/db -logFolder ../Data/logs -landmarkIndexFolder ../Data/landmarks/L_SF_NONE -nLandMark 3 -cLandMark true
