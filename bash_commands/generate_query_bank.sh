#!/bin/bash


java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_NONE_5K -neo4jdb ../Data/C9_NY/db/C9_NY_NONE -GraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_5K -queryPairFolder ../Data/queries -numQuery 1000 
java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_NONE_15K -neo4jdb ../Data/C9_NY/db/C9_NY_NONE -GraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_15K -queryPairFolder ../Data/queries -numQuery 1000 
java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_NONE_30K -neo4jdb ../Data/C9_NY/db/C9_NY_NONE -GraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_30K -queryPairFolder ../Data/queries -numQuery 1000 
java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_NONE_60K -neo4jdb ../Data/C9_NY/db/C9_NY_NONE -GraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_60K -queryPairFolder ../Data/queries -numQuery 1000 

java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateQueryPairs -dbname C9_BAY_NONE -neo4jdb ../Data/C9_BAY/db/C9_BAY_NONE -GraphInfo ../Data/C9_BAY/C9_BAY_NONE -queryPairFolder ../Data/queries -numQuery 1000

java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateQueryPairs -dbname C9_E_NONE -neo4jdb ../Data/C9_E/db/C9_E_NONE -GraphInfo ../Data/C9_E/C9_E_NONE -queryPairFolder ../Data/queries -numQuery 1000 

java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateQueryPairs -dbname C9_CTR_NONE -neo4jdb ../Data/C9_CTR/db/C9_CTR_NONE -GraphInfo ../Data/C9_CTR/C9_CTR_NONE -queryPairFolder ../Data/queries -numQuery 1000 

java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateQueryPairs -dbname L_CAL_NONE -neo4jdb ../Data/L_CAL/db/L_CAL_NONE -GraphInfo ../Data/L_CAL/L_CAL_NONE -queryPairFolder ../Data/queries -numQuery 1000 

java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateQueryPairs -dbname L_NA_NONE -neo4jdb ../Data/L_NA/db/L_NA_NONE -GraphInfo ../Data/L_NA/L_NA_NONE -queryPairFolder ../Data/queries -numQuery 1000

java -Xmx20G -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateQueryPairs -dbname L_SF_NONE -neo4jdb ../Data/L_SF/db/L_SF_NONE -GraphInfo ../Data/L_SF/L_SF_NONE -queryPairFolder ../Data/queries -numQuery 1000 
