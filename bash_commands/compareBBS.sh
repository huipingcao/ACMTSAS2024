#!/bin/bash


java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_5K 
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_15K 
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_30K 
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_60K 

java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m compareBBS -dbname C9_BAY_NONE 
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m compareBBS -dbname L_CAL_NONE 
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m compareBBS -dbname L_NA_NONE 
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m compareBBS -dbname L_SF_NONE 


java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_30K_TSP 
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_60K_TSP
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_15K_TSP 
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m compareBBS -dbname C9_NY_NONE_5K_TSP 
