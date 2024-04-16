`mvn clean compile assembly:single`



============================C9_NY_NONE_5K==========================
```
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_5K >../Data/new_logs/compareBBS/C9_NY_NONE_5K/query100/epoch100_query100_128_128_compareBBS_05_18.log
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_5K >../Data/new_logs/compareBBS/C9_NY_NONE_5K/query300/epoch100_query300_128_32_compareBBS_05_18.log
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_5K >../Data/new_logs/compareBBS/C9_NY_NONE_5K/answered100/epoch100_answered100_128_32_compareBBS_05_18.log
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_5K >../Data/new_logs/compareBBS/C9_NY_NONE_5K/answered100/epoch20_answered100_128_128_compareBBS_05_19.log
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_NONE_5K 

java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_5K_TSP >../Data/new_logs/compareBBS/C9_NY_NONE_5K_TSP/epoch100_query100_compareBBS_05_15.log
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_5K_TSP >../Data/new_logs/compareBBS/C9_NY_NONE_5K_TSP/answered100/epoch20_answered100_128_128_compareBBS_05_19.log
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_NONE_5K_TSP 

java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_5K_TSP_L1-6 >../Data/new_logs/compareBBS/C9_NY_NONE_5K_TSP_L1-6/epoch100_query100_compareBBS_05_18.log
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_NONE_5K_TSP_L1-6 
```

============================C9_NY_NONE_15K==========================
```
java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_15K >../Data/new_logs/compareBBS/C9_NY_NONE_15K/epoch100_query100_compareBBS_05_16.log
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_NONE_15K 

java -jar BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_15K_TSP >../Data/new_logs/compareBBS/C9_NY_NONE_15K_TSP/epoch100_query100_compareBBS_05_17.log
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_NONE_15K_TSP 
```

=============================L_CAL_NONE=============================
```
java -jar BackboneIndex.jar -m compareBBS -dbname L_CAL_NONE >../Data/new_logs/compareBBS/L_CAL_NONE/epoch100_query100_compareBBS_05_16.log
java -jar BackboneIndex.jar -m DTWComparison -dbname L_CAL_NONE

java -jar BackboneIndex.jar -m compareBBS -dbname L_CAL_NONE_TSP >../Data/new_logs/compareBBS/L_CAL_NONE_TSP/epoch100_query100_compareBBS_05_16.log
java -jar BackboneIndex.jar -m DTWComparison -dbname L_CAL_NONE_TSP
```