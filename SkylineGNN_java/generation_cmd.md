
# Commands for neo4j database, subGraph, TrainingSet generation, and DTW comparison for all three methods

cd to target folder `cd target/`


---
## createDB

`java -jar BackboneIndex.jar -m createDB -dbname <dbname> -neo4jdb <db_path> -GraphInfo <node_edge_file_path>`

============================C9_BAY=============================
```
java -jar BackboneIndex.jar -m createDB -dbname C9_BAY_NONE -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_BAY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_BAY/C9_BAY_NONE
java -jar BackboneIndex.jar -m createDB -dbname C9_BAY_ANTI -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_BAY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_BAY/C9_BAY_ANTI
java -jar BackboneIndex.jar -m createDB -dbname C9_BAY_CORR -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_BAY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_BAY/C9_BAY_CORR
```
============================C9_CTR=============================
```
java -jar BackboneIndex.jar -m createDB -dbname C9_CTR_NONE -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_CTR/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_CTR/C9_CTR_NONE
java -jar BackboneIndex.jar -m createDB -dbname C9_CTR_ANTI -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_CTR/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_CTR/C9_CTR_ANTI
java -jar BackboneIndex.jar -m createDB -dbname C9_CTR_CORR -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_CTR/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_CTR/C9_CTR_CORR
```
============================C9_E=============================
```
java -jar BackboneIndex.jar -m createDB -dbname C9_E_NONE -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_E/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_E/C9_E_NONE
java -jar BackboneIndex.jar -m createDB -dbname C9_E_ANTI -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_E/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_E/C9_E_ANTI
java -jar BackboneIndex.jar -m createDB -dbname C9_E_CORR -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_E/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_E/C9_E_CORR
```
============================C9_NY=============================
```
java -jar BackboneIndex.jar -m createDB -dbname C9_NY_NONE -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE
java -jar BackboneIndex.jar -m createDB -dbname C9_NY_ANTI -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_ANTI
java -jar BackboneIndex.jar -m createDB -dbname C9_NY_CORR -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_CORR
```
============================L_CAL=============================
```
java -jar BackboneIndex.jar -m createDB -dbname L_CAL_NONE -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_CAL/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_CAL/L_CAL_NONE
java -jar BackboneIndex.jar -m createDB -dbname L_CAL_ANTI -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_CAL/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_CAL/L_CAL_ANTI
java -jar BackboneIndex.jar -m createDB -dbname L_CAL_CORR -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_CAL/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_CAL/L_CAL_CORR
```
============================L_NA=============================
```
java -jar BackboneIndex.jar -m createDB -dbname L_NA_NONE -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_NA/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_NA/L_NA_NONE
java -jar BackboneIndex.jar -m createDB -dbname L_NA_ANTI -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_NA/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_NA/L_NA_ANTI
java -jar BackboneIndex.jar -m createDB -dbname L_NA_CORR -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_NA/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_NA/L_NA_CORR
```
============================L_SF=============================
```
java -jar BackboneIndex.jar -m createDB -dbname L_SF_NONE -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_SF/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_SF/L_SF_NONE
java -jar BackboneIndex.jar -m createDB -dbname L_SF_ANTI -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_SF/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_SF/L_SF_ANTI
java -jar BackboneIndex.jar -m createDB -dbname L_SF_CORR -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_SF/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_SF/L_SF_CORR
```

---
## GenerateSubGraph

`java -jar BackboneIndex.jar -m GenerateSubGraph -dbname <src_dbname> -neo4jdb <src_db_path> -GraphInfo <src_node_edge_file_path> -sub_K <numOfKnodes> -outGraphInfo <out_subGraph_path>`

============================C9_NY=============================
```
============================C9_NY_NONE=============================
[//]: # (from C9_NY_NONE to generate 120K)
java -jar BackboneIndex.jar -m GenerateSubGraph -dbname C9_NY_NONE -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE -sub_K 120 -outGraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_120K
java -jar BackboneIndex.jar -m createDB -dbname C9_NY_NONE_120K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_120K

[//]: # (from 120K to generate 60K)
java -jar BackboneIndex.jar -m GenerateSubGraph -dbname C9_NY_NONE_120K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_120K -sub_K 60 -outGraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_60K
java -jar BackboneIndex.jar -m createDB -dbname C9_NY_NONE_60K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_60K

[//]: # (from 60K to generate 30K)
java -jar BackboneIndex.jar -m GenerateSubGraph -dbname C9_NY_NONE_60K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_60K -sub_K 30 -outGraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_30K
java -jar BackboneIndex.jar -m createDB -dbname C9_NY_NONE_30K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_30K

[//]: # (from 30K to generate 15K)
java -jar BackboneIndex.jar -m GenerateSubGraph -dbname C9_NY_NONE_30K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_30K -sub_K 15 -outGraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_15K
java -jar BackboneIndex.jar -m createDB -dbname C9_NY_NONE_15K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_15K

[//]: # (from 15K to generate 5K)
java -jar BackboneIndex.jar -m GenerateSubGraph -dbname C9_NY_NONE_15K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_15K -sub_K 5 -outGraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_5K
java -jar BackboneIndex.jar -m createDB -dbname C9_NY_NONE_5K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_5K

[//]: # (from 5K to generate 50) //demo
java -jar BackboneIndex.jar -m GenerateSubGraph -dbname C9_NY_NONE_5K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_5K -sub_K 50 -outGraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_50
java -jar BackboneIndex.jar -m createDB -dbname C9_NY_NONE_50 -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_50

---
============================C9_NY_ANTI=============================
[//]: # (from C9_NY_ANTI to generate 5K)
java -jar BackboneIndex.jar -m GenerateSubGraph -dbname C9_NY_ANTI -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_ANTI -sub_K 5 -outGraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_ANTI/C9_NY_ANTI_5K
java -jar BackboneIndex.jar -m createDB -dbname C9_NY_ANTI_5K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_ANTI -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_ANTI/C9_NY_ANTI_5K

---
============================C9_NY_CORR=============================
[//]: # (from C9_NY_CORR to generate 5K)
java -jar BackboneIndex.jar -m GenerateSubGraph -dbname C9_NY_CORR -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_CORR -sub_K 5 -outGraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_CORR/C9_NY_CORR_5K
java -jar BackboneIndex.jar -m createDB -dbname C9_NY_CORR_5K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_CORR -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_CORR/C9_NY_CORR_5K

```

---
## Generate Query Bank

`java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname <src_db_name> -neo4jdb <src_db> -GraphInfo <src_graph> -landmarkIndexFolder -nLandMark 3 -cLandMark true -queryPairFolder -numQuery 100 2>&1 | tee C9_NY_5K.log`

============================C9_NY_NONE=============================
```
[//]: # (Generate and store query pairs bank for C9_NY_NONE_5K)
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_NONE_5K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_5K -queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries -numQuery 1000 
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_NONE_5K_Level4 -numQuery 5000 
 

[//]: # (Generate and store query pairs bank for C9_NY_NONE_15K)
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_NONE_15K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_15K -queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries -numQuery 10000 
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_NONE_15K_Level1 -numQuery 10000 



[//]: # (Generate and store 20000 query pairs bank for C9_NY_NONE_30K)
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_NONE_30K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_30K -queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries -numQuery 10000 

[//]: # (Generate and store 30000 query pairs bank for C9_NY_NONE_60K)
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_NONE_60K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_60K -queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries -numQuery 15000 
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_NONE_60K_Level4 -numQuery 10000 
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_NONE_60K_Level5 -numQuery 10000 
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_NONE_60K_Level6 -numQuery 10000 
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_NONE_60K_Level7 -numQuery 10000 
```

============================C9_NY_ANTI=============================
```
[//]: # (Generate and store 20000 query pairs bank for C9_NY_ANTI_5K)
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_ANTI_5K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_ANTI -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_ANTI/C9_NY_ANTI_5K -queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries -numQuery 10000 
```

============================C9_NY_CORR=============================
```
[//]: # (Generate and store 20000 query pairs bank for C9_NY_CORR_5K)
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_NY_CORR_5K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_CORR -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_CORR/C9_NY_CORR_5K -queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries -numQuery 10000 
```

============================C9_BAY_NONE=============================
```
[//]: # (Generate and store 20000 query pairs bank for C9_BAY_NONE)
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_BAY_NONE_Level4 -numQuery 10000 
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_BAY_NONE_Level5 -numQuery 10000 
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_BAY_NONE_Level6 -numQuery 10000 
java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname C9_BAY_NONE_Level7 -numQuery 10000 
```


---
## Here we need to use BackboneIndex repo to build index for each graph dataset and put results into Data/BackBoneIndex folder

- copy db, graphinfo to BackboneIndex repo
- rename db_name to db_name_Level0
- run java -jar BackboneIndex.jar -m IndexBuilding -dbname *
- run java -jar BackboneIndex.jar -m BuildLandMark -dbname *_Level0 -nLandMark 3 -cLandMark true
- run java -jar BackboneIndex.jar -m BuildLandMark -dbname *_LevelHighest -nLandMark 3 -cLandMark true
- copy index/db_name_Level0 and index/db_name_LevelHighest back to Data/BackBoneIndex folder
- copy landmarks/db_name_Level0 and landmarks/db_name_LevelHighest back to Data/landmarks folder

--- 
## Generate GNNTrainingSet  

`java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname <src_db_name> -neo4jdb <src_db> -GraphInfo <src_graph> -queryPairFolder <src_queryPair_folder> -srcQueryPairFile <src_queryPair_filename> -landmarkIndexFolder -nLandMark 3 -cLandMark true -resultFolder -numQuery 50 -timeout 900000 2>&1 | tee C9_NY_5K.log`

============================C9_NY_NONE=============================
```
[//]: # (Generate 100 TrainingSet for C9_NY_NONE_5K 
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_5K \
-neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db/C9_NY_NONE \
-GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_5K \
-queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries \
-srcQueryPairFile C9_NY_NONE_5K_query_pairs.txt \
-landmarkIndexFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/landmarks/C9_NY \
-nLandMark 3 \
-cLandMark true \
-resultFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/results_bbs/C9_NY/C9_NY_NONE_5K \
-numQuery 100 2>&1 | tee C9_NY_NONE_5K.log

[//]: # (Generate 1200 TrainingSet for C9_NY_NONE_5K 
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_5K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_5K -queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries -srcQueryPairFile C9_NY_NONE_5K_2000_query_pairs.txt -landmarkIndexFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/landmarks/C9_NY -nLandMark 3 -cLandMark true -resultFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/results_bbs/C9_NY/C9_NY_NONE_5K -numQuery 1200 2>&1 | tee C9_NY_NONE_5K.log

---
[//]: # (Generate 12000 TrainingSet for C9_NY_NONE_15K (half from query pairs used in C9_NY_NONE_5K TrainingSet)
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_15K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_15K -queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries -srcQueryPairFile C9_NY_NONE_5K_24000_query_pairs.txt -landmarkIndexFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/landmarks/C9_NY -nLandMark 3 -resultFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/results_bbs/C9_NY/C9_NY_NONE_15K -numQuery 6000 2>&1 | tee C9_NY_NONE_15K.log
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_15K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_15K -queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries -srcQueryPairFile C9_NY_NONE_15K_20000_query_pairs.txt -landmarkIndexFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/landmarks/C9_NY -nLandMark 3 -resultFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/results_bbs/C9_NY/C9_NY_NONE_15K -numQuery 6000 2>&1 | tee C9_NY_NONE_15K.log

[//]: # (Generate 10000 TrainingSet for C9_NY_NONE_30K (half from query pairs used in C9_NY_NONE_15K TrainingSet)
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_30K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_30K -queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries -srcQueryPairFile C9_NY_NONE_15K_20000_query_pairs.txt -landmarkIndexFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/landmarks/C9_NY -nLandMark 3 -resultFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/results_bbs/C9_NY/C9_NY_NONE_30K -numQuery 5000 2>&1 | tee C9_NY_NONE_30K.log
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_30K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_30K -queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries -srcQueryPairFile C9_NY_NONE_30K_20000_query_pairs.txt -landmarkIndexFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/landmarks/C9_NY -nLandMark 3 -resultFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/results_bbs/C9_NY/C9_NY_NONE_30K -numQuery 5000 2>&1 | tee C9_NY_NONE_30K.log

[//]: # (Generate 10000 TrainingSet for C9_NY_NONE_60K (half from query pairs used in C9_NY_NONE_30K TrainingSet)
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_60K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_60K -queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries -srcQueryPairFile C9_NY_NONE_30K_to_60K_20000_query_pairs.txt -landmarkIndexFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/landmarks/C9_NY -nLandMark 3 -resultFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/results_bbs/C9_NY/C9_NY_NONE_60K -numQuery 6000 2>&1 | tee C9_NY_NONE_60K.log
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_60K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_NONE/C9_NY_NONE_60K -queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries -srcQueryPairFile C9_NY_NONE_60K_to_60K_30000_query_pairs.txt -landmarkIndexFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/landmarks/C9_NY -nLandMark 3 -resultFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/results_bbs/C9_NY/C9_NY_NONE_60K -numQuery 6000 2>&1 | tee C9_NY_NONE_60K.log

---
=============================TSP-GNN=============================
[//]: # (Generate 10000 TrainingSet for C9_NY_NONE_5K
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_5K_Level4 -srcQueryPairFile C9_NY_NONE_5K_Level4_10000_query_pairs.txt -numQuery 3000 >../Data/logs/GenerateBaselineResults/C9_NY_NONE_5K_Level4_3000_05_14.log
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_5K_Level5 -srcQueryPairFile C9_NY_NONE_5K_Level5_10000_query_pairs.txt -numQuery 3000 >../Data/logs/GenerateBaselineResults/C9_NY_NONE_5K_Level5_3000_05_14.log
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_5K_Level6 -srcQueryPairFile C9_NY_NONE_5K_Level6_10000_query_pairs.txt -numQuery 2000 >../Data/logs/GenerateBaselineResults/C9_NY_NONE_5K_Level6_2000_05_14.log
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_5K_Level7 -srcQueryPairFile C9_NY_NONE_5K_Level7_4000_query_pairs.txt -numQuery 2000 >../Data/logs/GenerateBaselineResults/C9_NY_NONE_5K_Level7_2000_05_14.log

java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_5K_Level1 -srcQueryPairFile C9_NY_NONE_5K_Level1_10000_query_pairs.txt -numQuery 1500 >../Data/logs/GenerateBaselineResults/C9_NY_NONE_5K_Level1_1500_05_14.log
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_5K_Level2 -srcQueryPairFile C9_NY_NONE_5K_Level2_10000_query_pairs.txt -numQuery 1500 >../Data/logs/GenerateBaselineResults/C9_NY_NONE_5K_Level2_1500_05_14.log
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_5K_Level3 -srcQueryPairFile C9_NY_NONE_5K_Level3_10000_query_pairs.txt -numQuery 1000 >../Data/logs/GenerateBaselineResults/C9_NY_NONE_5K_Level3_1000_05_14.log


[//]: # (Generate TrainingSet for C9_NY_NONE_15K
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_15K_Level1 -srcQueryPairFile C9_NY_NONE_15K_Level1_20000_query_pairs.txt -numQuery 1500 >../Data/logs/GenerateBaselineResults/C9_NY_NONE_15K_Level1_1500_05_14.log
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_15K_Level2 -srcQueryPairFile C9_NY_NONE_15K_Level2_20000_query_pairs.txt -numQuery 1500 >../Data/logs/GenerateBaselineResults/C9_NY_NONE_15K_Level2_1500_05_14.log



[//]: # (Generate 10000 TrainingSet for C9_NY_NONE_60K
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_60K_Level4 -srcQueryPairFile C9_NY_NONE_60K_Level4_20000_query_pairs.txt -numQuery 3000 2>&1 | tee C9_NY_NONE_60K_Level4.log
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_60K_Level5 -numQuery 2500 2>&1 | tee C9_NY_NONE_60K_Level5.log
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_60K_Level6 -numQuery 2500 2>&1 | tee C9_NY_NONE_60K_Level6.log
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_60K_Level7 -numQuery 2500 2>&1 | tee C9_NY_NONE_60K_Level7.log
```

============================C9_NY_ANTI=============================
```
[//]: # (Generate 10000 TrainingSet for C9_NY_ANTI_5K 
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_ANTI_5K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_ANTI/C9_NY_ANTI_5K -queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries -srcQueryPairFile C9_NY_ANTI_5K_20000_query_pairs.txt -landmarkIndexFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/landmarks/C9_NY -nLandMark 3 -cLandMark true -resultFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/results_bbs/C9_NY/C9_NY_ANTI_5K -numQuery 10000 2>&1 | tee C9_NY_ANTI_5K.log
```

============================C9_NY_CORR=============================
```
[//]: # (Generate 10000 TrainingSet for C9_NY_CORR_5K 
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_CORR_5K -neo4jdb /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/db -GraphInfo /home/hchen/IntelliJProjects/java_SkylineGNN/Data/C9_NY/C9_NY_CORR/C9_NY_CORR_5K -queryPairFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/queries -srcQueryPairFile C9_NY_CORR_5K_20000_query_pairs.txt -landmarkIndexFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/landmarks/C9_NY -nLandMark 3 -cLandMark true -resultFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/results_bbs/C9_NY/C9_NY_CORR_5K -numQuery 10000 2>&1 | tee C9_NY_CORR_5K.log
```


---
============================= TSP-GNN ==============================

============================C9_BAY_NONE=============================
```
[//]: # (Generate 10000 TrainingSet for C9_BAY_NONE 
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_BAY_NONE_Level4 -srcQueryPairFile C9_BAY_NONE_Level4_20000_query_pairs.txt -numQuery 3000 2>&1 | tee C9_BAY_NONE_Level4.log
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_BAY_NONE_Level5 -srcQueryPairFile C9_BAY_NONE_Level5_20000_query_pairs.txt -numQuery 2500 2>&1 | tee C9_BAY_NONE_Level5.log
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_BAY_NONE_Level6 -srcQueryPairFile C9_BAY_NONE_Level6_20000_query_pairs.txt -numQuery 2500 2>&1 | tee C9_BAY_NONE_Level6.log
java -Xmx20G -jar BackboneIndex.jar -m GenerateBaselineResults -dbname C9_BAY_NONE_Level7 -srcQueryPairFile C9_BAY_NONE_Level7_20000_query_pairs.txt -numQuery 2200 2>&1 | tee C9_BAY_NONE_Level7.log
```


--- 
## Compare approximate skyline paths on GNN generated subgraphs with the exact skyline solutions that search on the whole original graph. The subgraph mapping file is generated by the python file, *'SkylineGNN/utilities/validation_dataset.py'*.

`java -jar BackboneIndex.jar -m compareBBS -dbname <src_db_name> -compareResultFolder <> -indexFolder <> -landmarkIndexFolder <> -neo4jdb <src_db>`

============================C9_NY_NONE=============================
```
[//]: # Compare C9_NY_NONE_5K GNN generated subgraphs with the exact skyline solutions 
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_5K >../Data/logs/compareBBS/C9_NY_NONE_5K_epoch500_query300_compareBBS_05_14.log
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_15K >../Data/logs/compareBBS/C9_NY_NONE_15K_epoch500_query300_compareBBS_05_14.log
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_30K -compareResultFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/results_gnn_backbone/C9_NY
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_60K -compareResultFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/results_gnn_backbone/C9_NY

[//]: # TSP-GNN 
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_30K_TSP 
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_60K_TSP
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_15K_TSP >../Data/logs/compareBBS/C9_NY_NONE_15K_TSP_epoch500_query300_compareBBS_05_14.log
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_5K_TSP >../Data/logs/compareBBS/C9_NY_NONE_5K_TSP_epoch500_query300_compareBBS_05_14.log
```
============================C9_NY_ANTI=============================
```
[//]: # Compare C9_NY_ANTI_5K GNN generated subgraphs with the exact skyline solutions 
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_ANTI_5K -compareResultFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/results_gnn_backbone/C9_NY
```
============================C9_NY_CORR=============================
```
[//]: # Compare C9_NY_CORR_5K GNN generated subgraphs with the exact skyline solutions 
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_CORR_5K -compareResultFolder /home/hchen/IntelliJProjects/java_SkylineGNN/Data/results_gnn_backbone/C9_NY
```

============================L_CAL_NONE=============================
```
[//]: # Compare L_CAL_NONE GNN generated subgraphs with the exact skyline solutions 
java -jar BackboneIndex.jar -m compareBBS -dbname L_CAL_NONE >../Data/logs/compareBBS/L_CAL_NONE_epoch500_compareBBS_05_13.log
java -jar BackboneIndex.jar -m compareBBS -dbname L_CAL_NONE >../Data/logs/compareBBS/L_CAL_NONE_epoch100_compareBBS_05_13.log
java -jar BackboneIndex.jar -m compareBBS -dbname L_CAL_NONE >../Data/logs/compareBBS/L_CAL_NONE_epoch200_compareBBS_05_13.log
java -jar BackboneIndex.jar -m compareBBS -dbname L_CAL_NONE >../Data/logs/compareBBS/L_CAL_NONE_epoch300_compareBBS_05_13.log
java -jar BackboneIndex.jar -m compareBBS -dbname L_CAL_NONE >../Data/logs/compareBBS/L_CAL_NONE_epoch400_compareBBS_05_13.log

java -jar BackboneIndex.jar -m compareBBS -dbname L_CAL_NONE >../Data/logs/compareBBS/L_CAL_NONE_epoch500_query300_compareBBS_05_14.log
```

============================L_CAL_NONE_TSP=============================
```
[//]: # Compare L_CAL_NONE GNN generated subgraphs with the exact skyline solutions 
java -jar BackboneIndex.jar -m compareBBS -dbname L_CAL_NONE_TSP >../Data/logs/compareBBS/L_CAL_NONE_TSP_epoch500_compareBBS_05_13.log
java -jar BackboneIndex.jar -m compareBBS -dbname L_CAL_NONE_TSP >../Data/logs/compareBBS/L_CAL_NONE_TSP_epoch500_query300_compareBBS_05_14.log
```

--- 
## Compare GNN, Backbone, Baseline

`java -jar BackboneIndex.jar -m DTWComparison -dbname <src_db_name>`

============================C9_NY_NONE=============================
```
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_NONE_5K 
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_NONE_15K 
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_NONE_30K 
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_NONE_60K 

java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_NONE_5K_TSP
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_NONE_15K_TSP
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_NONE_30K_TSP
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_NONE_60K_TSP
```
============================C9_NY_ANTI=============================
```
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_ANTI_5K 
```
============================C9_NY_CORR=============================
```
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_CORR_5K 
```

============================L_CAL_NONE=============================
```
java -jar BackboneIndex.jar -m DTWComparison -dbname L_CAL_NONE 
java -jar BackboneIndex.jar -m DTWComparison -dbname L_CAL_NONE_TSP 
```




`mvn clean compile assembly:single`
