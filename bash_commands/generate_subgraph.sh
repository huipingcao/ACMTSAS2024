#!/bin/bash

java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateSubGraph -dbname C9_NY_NONE -neo4jdb ../Data/C9_NY/C9_NY_NONE -sub_K 120 -outGraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_120K
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m createDB -dbname C9_NY_NONE_120K -neo4jdb ../Data/C9_NY/db/C9_NY_NONE -GraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE

java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateSubGraph -dbname C9_NY_NONE_120K -neo4jdb ../Data/C9_NY/C9_NY_NONE -sub_K 60 -outGraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_60K
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m createDB -dbname C9_NY_NONE_60K -neo4jdb ../Data/C9_NY/db/C9_NY_NONE -GraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_60K

java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateSubGraph -dbname C9_NY_NONE_60K -neo4jdb ../Data/C9_NY/C9_NY_NONE -sub_K 30 -outGraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_30K
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m createDB -dbname C9_NY_NONE_30K -neo4jdb ../Data/C9_NY/db/C9_NY_NONE -GraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_30K

java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateSubGraph -dbname C9_NY_NONE_30K -neo4jdb ../Data/C9_NY/C9_NY_NONE -sub_K 15 -outGraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_15K
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m createDB -dbname C9_NY_NONE_15K -neo4jdb ../Data/C9_NY/db/C9_NY_NONE -GraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_15K

java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m GenerateSubGraph -dbname C9_NY_NONE_15K -neo4jdb ../Data/C9_NY/C9_NY_NONE -sub_K 5 -outGraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_5K
java -jar ../BackboneIndex_java/target/BackboneIndex.jar -m createDB -dbname C9_NY_NONE_5K -neo4jdb ../Data/C9_NY/db/C9_NY_NONE -GraphInfo ../Data/C9_NY/C9_NY_NONE/C9_NY_NONE_5K
