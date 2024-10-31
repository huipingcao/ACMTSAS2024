#!/bin/bash

java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname C9_NY_NONE -neo4jdb ../Data/C9_NY/db -GraphInfo ../Data/C9_NY/C9_NY_NONE
java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname C9_NY_ANTI -neo4jdb ../Data/C9_NY/db -GraphInfo ../Data/C9_NY/C9_NY_ANTI
java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname C9_NY_CORR -neo4jdb ../Data/C9_NY/db -GraphInfo ../Data/C9_NY/C9_NY_CORR

java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname C9_BAY_NONE -neo4jdb ../Data/C9_BAY/db -GraphInfo ../Data/C9_BAY/C9_BAY_NONE
java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname C9_BAY_ANTI -neo4jdb ../Data/C9_BAY/db -GraphInfo ../Data/C9_BAY/C9_BAY_ANTI
java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname C9_BAY_CORR -neo4jdb ../Data/C9_BAY/db -GraphInfo ../Data/C9_BAY/C9_BAY_CORR

java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname C9_CTR_NONE -neo4jdb ../Data/C9_CTR/db -GraphInfo ../Data/C9_CTR/C9_CTR_NONE
java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname C9_CTR_ANTI -neo4jdb ../Data/C9_CTR/db -GraphInfo ../Data/C9_CTR/C9_CTR_ANTI
java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname C9_CTR_CORR -neo4jdb ../Data/C9_CTR/db -GraphInfo ../Data/C9_CTR/C9_CTR_CORR

java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname C9_E_NONE -neo4jdb ../Data/C9_E/db -GraphInfo ../Data/C9_E/C9_E_NONE
java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname C9_E_ANTI -neo4jdb ../Data/C9_E/db -GraphInfo ../Data/C9_E/C9_E_ANTI
java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname C9_E_CORR -neo4jdb ../Data/C9_E/db -GraphInfo ../Data/C9_E/C9_E_CORR

java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname L_CAL_NONE -neo4jdb ../Data/L_CAL/db -GraphInfo ../Data/L_CAL/L_CAL_NONE
java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname L_CAL_ANTI -neo4jdb ../Data/L_CAL/db -GraphInfo ../Data/L_CAL/L_CAL_ANTI
java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname L_CAL_CORR -neo4jdb ../Data/L_CAL/db -GraphInfo ../Data/L_CAL/L_CAL_CORR

java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname L_NA_NONE -neo4jdb ../Data/L_NA/db -GraphInfo ../Data/L_NA/L_NA_NONE
java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname L_NA_ANTI -neo4jdb ../Data/L_NA/db -GraphInfo ../Data/L_NA/L_NA_ANTI
java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname L_NA_CORR -neo4jdb ../Data/L_NA/db -GraphInfo ../Data/L_NA/L_NA_CORR

java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname L_SF_NONE -neo4jdb ../Data/L_SF/db -GraphInfo ../Data/L_SF/L_SF_NONE
java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname L_SF_ANTI -neo4jdb ../Data/L_SF/db -GraphInfo ../Data/L_SF/L_SF_ANTI
java -jar ../java_SkylineGNN/target/BackboneIndex.jar -m createDB -dbname L_SF_CORR -neo4jdb ../Data/L_SF/db -GraphInfo ../Data/L_SF/L_SF_CORR
