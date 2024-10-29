# ACMTSAS2024

This repository contains the source code for our ACM TSAS 2024 paper [Backbone Index and GNN Models for Skyline Path Query Evaluation over Multi-cost Road Networks](https://dl.acm.org/doi/10.1145/3660632).


---
# Results
<img width="876" alt="image" src="https://github.com/user-attachments/assets/7a9a779d-93da-4b7d-b961-4daebde75497">

<img width="631" alt="image" src="https://github.com/user-attachments/assets/681d4ed7-7c64-463b-b5cd-920f48faee7a">

<img width="628" alt="image" src="https://github.com/user-attachments/assets/cc3eccc2-83b9-40ff-a129-279ba7779e25">

<img width="850" alt="image" src="https://github.com/user-attachments/assets/95bf9335-953e-49f4-aa16-bc78c5b57def">


---
# Execution

## Installation

- Python 3.6+
- CUDA
- torch
- torch-geometric
- JAVA SDK
- neo4j
- Apache Maven


## Preprocessing

### Step 1: Download the Raw Data

Refer to [EDBT paper](https://www.dropbox.com/s/nm2zfdvlytm8aow/backbone_EDBT2022_CR_submitted.pdf?dl=0) for data description.

- http://users.diag.uniroma1.it/challenge9/download.shtml
- EDBT paper: https://www.cs.utah.edu/~lifeifei/SpatialDataset.htm
- Datasets start with C9_ are downloaded from http://users.diag.uniroma1.it/challenge9/download.shtml

| Name        | Description  | # nodes | # arcs | Longitude | Latitude       | 
|-------------|--------------|---------|--------|-----------|----------------|
| C9_NY(NY)   | New York City | 264,346 | 733,846 | [40.3; 41.3] | [73.5; 74.5]   |   
| C9_BAY(BAY) | San Francisco Bay Area | 321,270 | 800,172 | [37.0; 39.0] | [121; 123]     |     
| C9_COL(COL) | Colorado  |  435,666  | 1,057,066 |  [37.0; 41.0] | [102.0; 109.0] |
| C9-FLA(FLA) |  Florida  | 1,070,376  |  2,712,798 | [24.0; 31.0]	 | [79; 87.5]     |
| C9_E(E)     |  Eastern USA  |  3,598,623	 | 8,778,114 | [24.0; 50.0] | [-infty; 79.0] |
| C9_CTR(CTR) |  Central USA  | 14,081,816  | 34,292,496 | [25.0; 50.0] | [79.0; 100.0]	 |

- Datasets start with L_ are downloaded from https://www.cs.utah.edu/~lifeifei/SpatialDataset.htm
    - L_CAL is from [California Road Network's Nodes (Node ID, Longitude, Latitude)](https://www.cs.utah.edu/~lifeifei/research/tpq/cal.cnode), [California Road Network's Edges (Edge ID, Start Node ID, End Node ID, L2 Distance)](https://www.cs.utah.edu/~lifeifei/research/tpq/cal.cedge)
    - L_SF is from [San Francisco Road Network's Nodes (Node ID, Normalized X Coordinate, Normalized Y Coordinate)](https://www.cs.utah.edu/~lifeifei/research/tpq/SF.cnode), [San Francisco Road Network's Edges (Edge ID, Start Node ID, End Node ID, L2 Distance)](https://www.cs.utah.edu/~lifeifei/research/tpq/SF.cedge)
    - L_NA is from [North America Road Network's Nodes (Node ID, Normalized X Coordinate, Normalized Y Coordinate)](https://www.cs.utah.edu/~lifeifei/research/tpq/NA.cnode), [North America Road Network's Edges (Edge ID, Start Node ID, End Node ID, L2 Distance)](https://www.cs.utah.edu/~lifeifei/research/tpq/NA.cnode)


### Step 2: Put Data into `/Data` Folder (e.g. `Data/C9_NY_NONE`)



### Step 3: Preprocess - generate edge weights (using Python code) 

Generate weighted edge files with script `SkylineGNN_py/DataProcess/dataprocess.py` (random-NONE, correlated-CORR, anti-correlated-ANTI)

```
python SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/C9_NY_NONE/ --data_name [BAY|CTR|CAL|NY|E|NA|SF] --relation [none|corr|anti] --num_dim 2
```


### Step 4: Set up the environment to run the Java code

The structure of the code is organized accoring to IntelliJ. The project is a Maven project. \
Software: You need to have Java JDK, Maven, IntellJ, and Neo4j installed. \

Experimental env: OS name: "linux", version: "5.4.0-122-generic", arch: "amd64", family: "unix",
CUDA Version: 11.4, Java version: 11.0.15, java-11-openjdk-amd64, Apache Maven 3.6.3, neo4j 4.4.9

  - Install neo4j, modify [neo4j.conf](SkylineGNN_java/conf/neo4j.conf) file. 
  - Check pom.xml
    - \<source>, \<target> >> match your SDK version
    - \<mainClass>utilities.RunningScripts\</mainClass>
    - \<goal>single\</goal> >> maven-assembly-plugin package to wrap all classes

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>qixu.nmsu</groupId>
    <artifactId>Query</artifactId>
    <version>1</version>

    <dependencies>
        <!-- Neo4j Community Dependences-->
        <dependency>
            <groupId>org.neo4j</groupId>
            <artifactId>neo4j-community</artifactId>
            <version>3.5.14</version>
            <type>pom</type>
        </dependency>
        <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>2.6</version>
        </dependency>
        <dependency>
            <groupId>commons-cli</groupId>
            <artifactId>commons-cli</artifactId>
            <version>1.4</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <artifactId>maven-assembly-plugin</artifactId>
                <configuration>
                    <finalName>BackboneIndex</finalName>
                    <appendAssemblyId>false</appendAssemblyId>
                    <archive>
                        <manifest>
                            <mainClass>utilities.RunningScripts</mainClass>
                        </manifest>
                    </archive>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <!-- bind to the packaging phase -->
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

  - Build project to .jar file

    `cd /SkylineGNN_java`
    `sudo apt install maven`
    `mvn clean compile assembly:single`


### Step 5: Generate backbone indexes and landmarks

Refer to [EDBT paper readme](BackboneIndex_java/ReadMe.md) for technical implementation details.

`cd /BackboneIndex_java`

```
[//]: # (C9_NY_NONE_5K)

java -jar target/BackboneIndex.jar -m IndexBuilding -dbname C9_NY_NONE_5K -neo4jdb /Data/C9_NY_NONE_5K/db -indexFolder /Data/indexes/C9_NY_NONE_5K -min_size 200 -percentage 0.01 -degreeHandle normal
java -jar target/BackboneIndex.jar -m BuildLandMark -dbname C9_NY_NONE_5K_Level1 -neo4jdb /Data/C9_NY_NONE_5K/db -logFolder /Data/logs -landmarkIndexFolder /Data/landmarks/C9_NY_NONE_5K -nLandMark 3 -cLandMark true
```


### Step 6: Generate training samples (BBS results)

- One training sample is a set of skyline paths between two random pair of nodes.
- One dateset is consisted of given number of training samples, which can be transferred to geometric package compatible dataset while using the python code.
- All the script executable functions can be found in the  `~/SkylineGNN_java/src/main/java/utilities/RunningScripts.java`. Here are some examples of java commands. You can run the executable jar file by compiling it from maven, or can run from you IDE. The provided example commands are only for demonstrated purposes. You can change parameters as you want.  

1. Create Neo4j Database from raw files.

Purpose: Create neo4jdb per dataset, per relation.\
More commands are in below Section [Full Commands for neo4j database, subGraph, TrainingSet generation, and DTW comparison](https://github.com/huipingcao/ACMTSAS2024/blob/main/README.md#full-commands-for-neo4j-database-subgraph-trainingset-generation-and-dtw-comparison-for-all-three-methods-bbs-gnn-tsp-gnn)

```
    >-m createDB -dbname <dbname> -neo4jdb <db_path> -GraphInfo <node_edge_file_path>
    
    >-m createDB -dbname C9_NY_NONE -neo4jdb /Data/C9_NY_NONE/db -GraphInfo /Data/C9_NY_NONE
```

2. Generate a subgraph from a whole original graph. 

Purpose: Generate subgraphs for BBS search\
Important notes: hierarchical, e.g. a 5K subgraph needs to be a subgraph of 5K. \
More commands are in below Section [Full Commands for neo4j database, subGraph, TrainingSet generation, and DTW comparison](https://github.com/huipingcao/ACMTSAS2024/blob/main/README.md#full-commands-for-neo4j-database-subgraph-trainingset-generation-and-dtw-comparison-for-all-three-methods-bbs-gnn-tsp-gnn)

```
    >-m GenerateSubGraph -dbname <src-dbname> -neo4jdb <db_path> -GraphInfo <node_edge_file_path>
    
    >-m GenerateSubGraph -dbname C9_NY_NONE -neo4jdb /Data/C9_NY_NONE/db -GraphInfo /Data/C9_NY_NONE -sub_K 5 -outGraphInfo /Data/C9_NY_NONE_5K 
```

3. Query Generation

Purpose: Generate query pairs from neo4jdb and subgraphs.\

Important notes: 

- The query looks like 1184 3364 >> (start_node) (end_node) 
- Generate *_query_pair_bank.txt randomly contains # of queries more than needed num_queries  

More commands are in below Section [Full Commands for neo4j database, subGraph, TrainingSet generation, and DTW comparison](https://github.com/huipingcao/ACMTSAS2024/blob/main/README.md#full-commands-for-neo4j-database-subgraph-trainingset-generation-and-dtw-comparison-for-all-three-methods-bbs-gnn-tsp-gnn)


4. Generate given number of training samples:

```
    > java -Xmx20G -jar SkylineGNN_java/target/BackboneIndex.jar -m GenerateBaselineResults -dbname <src_db_name> -neo4jdb <src_db> -GraphInfo <src_graph> -landmarkIndexFolder -nLandMark 3 -cLandMark true -resultFolder -numQuery 100 2>&1 | tee C9_NY_NONE_5K.log
    
    > java -Xmx20G -jar SkylineGNN_java/target/BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_5K -neo4jdb /Data/C9_NY_NONE_5K/db/ -GraphInfo /Data/C9_NY_NONE_5K -landmarkIndexFolder /Data/landmarks/C9_NY_NONE_5K -nLandMark 3 -cLandMark true -resultFolder /Data/results/C9_NY_NONE_5K -numQuery 100 2>&1 | tee C9_NY_NONE_5K.log
```

More commands are in below Section [Full Commands for neo4j database, subGraph, TrainingSet generation, and DTW comparison](https://github.com/huipingcao/ACMTSAS2024/blob/main/README.md#full-commands-for-neo4j-database-subgraph-trainingset-generation-and-dtw-comparison-for-all-three-methods-bbs-gnn-tsp-gnn)

Important notes: 

- Same num_queries are used in all subgraphs

  - Half from query pair bank generated randomly; 
  - Half from src query pairs used in smaller subgraph.
  - e.g. 100 query pairs used in C9_NY_NONE_15K should have 50 query pairs coming from C9_NY_NONE_5K_query_pairs.txt, and 50 from C9_NY_NONE_15K_query_pair_bank.txt (make sure no duplicates).

- There is a timeout threshold (15 min) when generating BBS solutions. The program will continue and store timeout queries into `*_timeout_query_pairs.txt` till generated enough BBS solutions

  


---
## Geometric Pytroch Code

- Running the code in the file `SkylineGNN_py/003/train_script.py`.
- The file `SkylineGNN_py/003/MultiCostNetworks.py` is responsible for transferring the text training sample files to geometric compatible file. 
- The file `SkylineGNN_py/utilities/validation_dataset.py` is used to validate the model and generate the subgraphs for each query/training sample for late usage.


### Step 7: Train a GNN Model
```
cd SkylineGNN_py/003

-------
Functionality: train_script.py trains a GNN model by given parameters 
Input: 1) original graph data; 2) graph data with anotations on the nodes-0,1,2; 3) bbs query results; 
4) a pretrained model from the same if it exists)
Ouput: 1) multiple pretrained gnn models per 100 epoch (saved in checkpoints folder by input parameter) scr code <check_pt_path> variable
2) a log file contains logging info and loss results
python train_script.py --graph_folder <raw_node_edge.txt> --data_folder <processed_graph> --paths_folder <bbs_results> 
--checkpoints_folder <pre-trained gnn model if exists> --log_folder

============================C9_NY_NONE_5K=============================
python train_script.py --gnn_name Transformer
python train_script.py --gnn_name Transformer --heads 2
python train_script.py --gnn_name Transformer --edge_dim 1
python train_script.py --gnn_name Transformer --edge_dim 2
python train_script.py --gnn_name Transformer --edge_dim 3

```


### Step 8: Test a Pre-trained GNN Model 
```
cd SkylineGNN_py/utilities/

```

Modify configuration in `SkylineGNN_py/utilities/validation_dataset.py`:
```
path = "<PATH_TO_YOUR_PROCESSED_GRAPH_DATA>"
train_paths_folder = "<PATH_TO_YOUR_SAVED_CHECKPOINT>"
data_folder = "<PATH_TO_YOUR_PROCESSED_GRAPH_DATA>"
save_folder = "<PATH_TO_YOUR_TARGET_DIR_FOR_MAPPED_FILE>"
checkpoint_folder = '<PATH_TO_YOUR_SAVED_CHECKPOINT_FOLDER>'
check_point_file = checkpoint_folder + '<PATH_TO_YOUR_CHECKPOINT_FILENAME>'

model_name = "Transformer"
n_nodes = <NUM_OF_NODES>
embedding_dim = 128
hidden_dim = 128

node_dim = 2
edge_dim = 3
batch_size = 8
enable_embed = True
output_dim = 3
heads = 1
conn_loss_enable = True
enable_edge_attr = True
```

Then run:
`Python validation_dataset.py`


### Step 9: Compare there methods by using compareBBS and DTWComparison cases in RunningScripts.java in SkylineGNN_java

1. compare approximate skyline paths on GNN generated subgraphs with the exact skyline solutions that search on the whole original graph. The subgraph mapping file is generated by the python file, `SkylineGNN_py/utilities/validation_dataset.py`.

```
    > java -jar BackboneIndex.jar -m compareBBS -dbname <src_db_name> -compareResultFolder <> -indexFolder <> -landmarkIndexFolder <> -neo4jdb <src_db>

    > -m compareBBS -dbname C9_NY_NONE_5K -resultFolder /Data/results/C9_NY_NONE_5K -indexFolder /Data/indexes/C9_NY_NONE_5K -landmarkIndexFolder /Data/landmarks/C9_NY_NONE_5K
```

2. Compare GNN, Backbone, Baseline

```
java -jar BackboneIndex.jar -m DTWComparison -dbname <src_db_name>

============================C9_NY_NONE=============================
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_NONE_5K 

```

## Full Commands for neo4j database, subGraph, TrainingSet generation, and DTW comparison for all three methods (BBS, GNN, TSP-GNN)

cd to target folder `cd target/`


---
### createDB

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
### GenerateSubGraph

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
### Generate Query Bank

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
#### Here we need to use BackboneIndex repo to build index for each graph dataset and put results into Data/BackBoneIndex folder

- copy db, graphinfo to BackboneIndex repo
- rename db_name to db_name_Level0
- run java -jar BackboneIndex.jar -m IndexBuilding -dbname *
- run java -jar BackboneIndex.jar -m BuildLandMark -dbname *_Level0 -nLandMark 3 -cLandMark true
- run java -jar BackboneIndex.jar -m BuildLandMark -dbname *_LevelHighest -nLandMark 3 -cLandMark true
- copy index/db_name_Level0 and index/db_name_LevelHighest back to Data/BackBoneIndex folder
- copy landmarks/db_name_Level0 and landmarks/db_name_LevelHighest back to Data/landmarks folder

--- 
### Generate GNNTrainingSet  

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
### Compare approximate skyline paths on GNN generated subgraphs with the exact skyline solutions that search on the whole original graph. The subgraph mapping file is generated by the python file, *'SkylineGNN/utilities/validation_dataset.py'*.

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
### Compare GNN, Backbone, Baseline

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
