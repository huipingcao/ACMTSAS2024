


[//]: # (=================================== Sample by Qixu ==========================================)
```
java -jar BackboneIndex.jar -m IndexBuilding -dbname C9_NY_10K -neo4jdb /home/gqxwolf/mydata/shared_git/BackboneIndex/Data/Neo4jDB -indexFolder /home/gqxwolf/mydata/shared_git/BackboneIndex/Data/Index/C9_NY_10K -min_size 200 -percentage 0.01 -degreeHandle normal
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_10K_Level7 -neo4jdb /home/gqxwolf/mydata/shared_git/BackboneIndex/Data/Neo4jDB -logFolder /home/gqxwolf/mydata/shared_git/BackboneIndex/Data/logs -landmarkIndexFolder /home/gqxwolf/mydata/shared_git/BackboneIndex/Data/Index/landmarks -nLandMark 3 -cLandMark true

java -jar BackboneIndex.jar -m Comparison -dbname C9_NY_10K -logFolder /home/gqxwolf/mydata/shared_git/BackboneIndex/Data/logs -landmarkIndexFolder /home/gqxwolf/mydata/shared_git/BackboneIndex/Data/Index/landmarks -resultFolder /home/gqxwolf/mydata/shared_git/BackboneIndex/Data/result -numQuery 5 -nLandMark 3 -indexFolder /home/gqxwolf/mydata/shared_git/BackboneIndex/Data/Index -neo4jdb /home/gqxwolf/mydata/shared_git/BackboneIndex/Data/Neo4jDB
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_10K -logFolder /home/gqxwolf/mydata/shared_git/BackboneIndex/Data/logs -resultFolder /home/gqxwolf/mydata/shared_git/BackboneIndex/Data/result -timestamp 20221202_163852
```


[//]: # (=================================== Running Commands ==========================================)

[//]: # (=================================== 1. Build Indexes and Landmarks==========================================)
```
[//]: # (C9_NY_NONE_5K)
java -jar BackboneIndex.jar -m IndexBuilding -dbname C9_NY_NONE_5K
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_5K_Level8 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_5K_Level0 -nLandMark 3 -cLandMark true

[//]: # (C9_NY_NONE_15K)
java -jar BackboneIndex.jar -m IndexBuilding -dbname C9_NY_NONE_15K
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_15K_Level3 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_15K_Level4 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_15K_Level5 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_15K_Level6 -nLandMark 3 -cLandMark true

[//]: # (C9_NY_NONE_30K)
java -jar BackboneIndex.jar -m IndexBuilding -dbname C9_NY_NONE_30K
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_30K_Level0 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_30K_Level2 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_30K_Level3 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_30K_Level4 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_30K_Level5 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_30K_Level6 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_30K_Level7 -nLandMark 3 -cLandMark true

[//]: # (C9_NY_NONE_60K)
java -jar BackboneIndex.jar -m IndexBuilding -dbname C9_NY_NONE_60K
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_60K_Level0 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_60K_Level4 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_60K_Level5 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_60K_Level6 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_60K_Level7 -nLandMark 3 -cLandMark true

---
[//]: # (C9_NY_ANTI_5K)
java -jar BackboneIndex.jar -m IndexBuilding -dbname C9_NY_ANTI_5K
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_ANTI_5K_Level0 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_ANTI_5K_Level9 -nLandMark 3 -cLandMark true

---
[//]: # (C9_NY_CORR_5K)
java -jar BackboneIndex.jar -m IndexBuilding -dbname C9_NY_CORR_5K
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_CORR_5K_Level0 -nLandMark 3 -cLandMark true
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_CORR_5K_Level9 -nLandMark 3 -cLandMark true

---
[//]: # (C9_BAY_NONE)
java -jar BackboneIndex.jar -m IndexBuilding -dbname C9_BAY_NONE
java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_BAY_NONE_Level4 -nLandMark 3 -cLandMark true

---
[//]: # (L_NA_NONE)
java -jar BackboneIndex.jar -m IndexBuilding -dbname L_NA_NONE
java -jar BackboneIndex.jar -m BuildLandMark -dbname L_NA_NONE_Level4 -nLandMark 3 -cLandMark true


```


`mvn clean compile assembly:single`