#!/bin/bash

java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_5K \
-neo4jdb ../Data/C9_NY/db/C9_NY_NONE \
-GraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_5K \
-queryPairFolder ../Data/queries \
-srcQueryPairFile C9_NY_NONE_5K_query_pairs.txt \
-landmarkIndexFolder ../Data/landmarks/C9_NY \
-nLandMark 3 \
-cLandMark true \
-resultFolder ../Data/results_bbs/C9_NY/C9_NY_NONE_5K \
-numQuery 1000 2>&1 | tee C9_NY_NONE_5K.log

java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_15K \
-neo4jdb ../Data/C9_NY/db/C9_NY_NONE \
-GraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_15K \
-queryPairFolder ../Data/queries \
-srcQueryPairFile C9_NY_NONE_15K_query_pairs.txt \
-landmarkIndexFolder ../Data/landmarks/C9_NY \
-nLandMark 3 \
-cLandMark true \
-resultFolder ../Data/results_bbs/C9_NY/C9_NY_NONE_15K \
-numQuery 1000 2>&1 | tee C9_NY_NONE_15K.log

java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_30K \
-neo4jdb ../Data/C9_NY/db/C9_NY_NONE \
-GraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_30K \
-queryPairFolder ../Data/queries \
-srcQueryPairFile C9_NY_NONE_30K_query_pairs.txt \
-landmarkIndexFolder ../Data/landmarks/C9_NY \
-nLandMark 3 \
-cLandMark true \
-resultFolder ../Data/results_bbs/C9_NY/C9_NY_NONE_30K \
-numQuery 1000 2>&1 | tee C9_NY_NONE_30K.log

java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_60K \
-neo4jdb ../Data/C9_NY/db/C9_NY_NONE \
-GraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_60K \
-queryPairFolder ../Data/queries \
-srcQueryPairFile C9_NY_NONE_60K_query_pairs.txt \
-landmarkIndexFolder ../Data/landmarks/C9_NY \
-nLandMark 3 \
-cLandMark true \
-resultFolder ../Data/results_bbs/C9_NY/C9_NY_NONE_60K \
-numQuery 1000 2>&1 | tee C9_NY_NONE_60K.log

java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateBaselineResults -dbname C9_BAY_NONE \
-neo4jdb ../Data/C9_BAY/db/C9_BAY_NONE \
-GraphInfo ../Data/C9_BAY/C9_BAY_NONE \
-queryPairFolder ../Data/queries \
-srcQueryPairFile C9_BAY_NONE_query_pairs.txt \
-landmarkIndexFolder ../Data/landmarks/C9_BAY \
-nLandMark 3 \
-cLandMark true \
-resultFolder ../Data/results_bbs/C9_BAY/C9_BAY_NONE \
-numQuery 1000 2>&1 | tee C9_BAY_NONE.log

java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateBaselineResults -dbname L_CAL_NONE \
-neo4jdb ../Data/L_CAL/db/L_CAL_NONE \
-GraphInfo ../Data/L_CAL/L_CAL_NONE \
-queryPairFolder ../Data/queries \
-srcQueryPairFile L_CAL_NONE_query_pairs.txt \
-landmarkIndexFolder ../Data/landmarks/L_CAL \
-nLandMark 3 \
-cLandMark true \
-resultFolder ../Data/results_bbs/L_CAL/L_CAL_NONE \
-numQuery 1000 2>&1 | tee L_CAL_NONE.log

java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateBaselineResults -dbname L_NA_NONE \
-neo4jdb ../Data/L_NA/db/L_NA_NONE \
-GraphInfo ../Data/L_NA/L_NA_NONE \
-queryPairFolder ../Data/queries \
-srcQueryPairFile L_NA_NONE_query_pairs.txt \
-landmarkIndexFolder ../Data/landmarks/L_CAL \
-nLandMark 3 \
-cLandMark true \
-resultFolder ../Data/results_bbs/L_NA/L_NA_NONE \
-numQuery 1000 2>&1 | tee L_NA_NONE.log

java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateBaselineResults -dbname L_SF_NONE \
-neo4jdb ../Data/L_SF/db/L_SF_NONE \
-GraphInfo ../Data/L_SF/L_SF_NONE \
-queryPairFolder ../Data/queries \
-srcQueryPairFile L_SF_NONE_query_pairs.txt \
-landmarkIndexFolder ../Data/landmarks/L_SF \
-nLandMark 3 \
-cLandMark true \
-resultFolder ../Data/results_bbs/L_SF/L_SF_NONE \
-numQuery 1000 2>&1 | tee L_SF_NONE.log
