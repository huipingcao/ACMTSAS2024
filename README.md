# ACMTSAS2024

This repository contains the source code for our ACM TSAS 2024 paper [Backbone Index and GNN Models for Skyline Path Query Evaluation over Multi-cost Road Networks](https://dl.acm.org/doi/10.1145/3660632).


---
# Results
See our results at [results.md](results.md)

---
# Execution

## Installation

- Python 3.6+
- JAVA SDK 11
- neo4j==3.5.14
- Apache Maven==4.0.0
- Python packages
    - `pip install -r requirements.txt`

**To install and run neo4j on Linux via SSH, you can follow below steps:**

`ssh username@your_server_ip`

**Step1: Install Java (OpenJDK 11)**

`sudo apt install openjdk-11-jdk -y`
 
**Step 2: Add the Neo4j Repository**

Add Neo4j’s GPG key:

`wget -O - https://debian.neo4j.com/neotechnology.gpg.key | sudo apt-key add -`

Add the Neo4j APT repository to your sources list:

`echo "deb https://debian.neo4j.com stable 4.4" | sudo tee -a /etc/apt/sources.list.d/neo4j.list`

Update the package list:

`sudo apt update`

**Step 3: Install Neo4j**

`sudo apt install neo4j -y`

**Step 4: Start and Enable Neo4j**

`sudo systemctl start neo4j`

`sudo systemctl enable neo4j`

**Step 5: Confirm Neo4j is Running**

`sudo systemctl status neo4j`


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
or run bash file: [process_raw_datasets.sh](bash_commands/process_raw_datasets.sh)

`bash bash_commands/process_raw_datasets.sh`


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


### Step 5: Create Neo4jDB
Purpose: Create neo4jdb per dataset, per relation.\

```
    >-m createDB -dbname <dbname> -neo4jdb <db_path> -GraphInfo <node_edge_file_path>
    
    >-m createDB -dbname C9_NY_NONE -neo4jdb ../Data/C9_NY_NONE/db -GraphInfo ../Data/C9_NY_NONE
```

`java -jar BackboneIndex.jar -m createDB -dbname <dbname> -neo4jdb <db_path> -GraphInfo <node_edge_file_path>`

or use bash file:

`bash bash_commands/createDB.sh`


### Step 6: Generate Sub Graphs and Neo4jDB (optional)
Purpose: Generate subgraphs for BBS search\
Important notes: hierarchical, e.g. a 5K subgraph needs to be a subgraph of 5K. \

```
    >-m GenerateSubGraph -dbname <src-dbname> -neo4jdb <db_path> -GraphInfo <node_edge_file_path>
    
    >-m GenerateSubGraph -dbname C9_NY_NONE -neo4jdb /Data/C9_NY_NONE/db -GraphInfo /Data/C9_NY_NONE -sub_K 5 -outGraphInfo /Data/C9_NY_NONE_5K 
```
`java -jar BackboneIndex.jar -m GenerateSubGraph -dbname <src_dbname> -neo4jdb <src_db_path> -GraphInfo <src_node_edge_file_path> -sub_K <numOfKnodes> -outGraphInfo <out_subGraph_path>`

or use bash file:

`bash bash_commands/generate_subgraph.sh`



### Step 7: Generate backbone indexes and landmarks

Refer to [EDBT paper readme](BackboneIndex_java/ReadMe.md) for technical implementation details.

Note: Here we need to use BackboneIndex repo to build index for each graph dataset and put results into Data/BackBoneIndex folder

- copy db, graphinfo to BackboneIndex repo
- rename db_name to db_name_Level0
- run java -jar BackboneIndex.jar -m IndexBuilding -dbname *
- run java -jar BackboneIndex.jar -m BuildLandMark -dbname *_Level0 -nLandMark 3 -cLandMark true
- run java -jar BackboneIndex.jar -m BuildLandMark -dbname *_LevelHighest -nLandMark 3 -cLandMark true
- copy index/db_name_Level0 and index/db_name_LevelHighest back to Data/BackBoneIndex folder
- copy landmarks/db_name_Level0 and landmarks/db_name_LevelHighest back to Data/landmarks folder

Use bash file:

`bash bash_commands/build_index_landmark.sh`


### Step 8: Generate Query Bank
Purpose: Generate query pairs from neo4jdb and subgraphs.\

Important notes: 

- The query looks like 1184 3364 >> (start_node) (end_node) 
- Generate *_query_pair_bank.txt randomly contains # of queries more than needed num_queries  

`java -Xmx20G -jar BackboneIndex.jar -m GenerateQueryPairs -dbname <src_db_name> -neo4jdb <src_db> -GraphInfo <src_graph> -landmarkIndexFolder -nLandMark 3 -cLandMark true -queryPairFolder -numQuery 100 2>&1 | tee C9_NY_5K.log`

or use bash file:

`bash bash_commands/generate_query_bank.sh`



### Step 9: Generate training samples (BBS results)

- One training sample is a set of skyline paths between two random pair of nodes.
- One dateset is consisted of given number of training samples, which can be transferred to geometric package compatible dataset while using the python code.
- All the script executable functions can be found in the  `~/SkylineGNN_java/src/main/java/utilities/RunningScripts.java`. Here are some examples of java commands. You can run the executable jar file by compiling it from maven, or can run from you IDE. The provided example commands are only for demonstrated purposes. You can change parameters as you want.  


```
    > java -Xmx20G -jar SkylineGNN_java/target/BackboneIndex.jar -m GenerateBaselineResults -dbname <src_db_name> -neo4jdb <src_db> -GraphInfo <src_graph> -landmarkIndexFolder -nLandMark 3 -cLandMark true -resultFolder -numQuery 100 2>&1 | tee C9_NY_NONE_5K.log
    
    > java -Xmx20G -jar SkylineGNN_java/target/BackboneIndex.jar -m GenerateBaselineResults -dbname C9_NY_NONE_5K -neo4jdb /Data/C9_NY_NONE_5K/db/ -GraphInfo /Data/C9_NY_NONE_5K -landmarkIndexFolder /Data/landmarks/C9_NY_NONE_5K -nLandMark 3 -cLandMark true -resultFolder /Data/results/C9_NY_NONE_5K -numQuery 100 2>&1 | tee C9_NY_NONE_5K.log
```

or use bash file:

`bash bash_commands/generate_trainingset.sh`


**Important notes: **

- Same num_queries are used in all subgraphs

  - Half from query pair bank generated randomly; 
  - Half from src query pairs used in smaller subgraph.
  - e.g. 100 query pairs used in C9_NY_NONE_15K should have 50 query pairs coming from C9_NY_NONE_5K_query_pairs.txt, and 50 from C9_NY_NONE_15K_query_pair_bank.txt (make sure no duplicates).

- There is a timeout threshold (15 min) when generating BBS solutions. The program will continue and store timeout queries into `*_timeout_query_pairs.txt` till generated enough BBS solutions

  

---
## Training

### Geometric Pytroch Code

- Running the code in the file `SkylineGNN_py/003/train_script.py`.
- The file `SkylineGNN_py/003/MultiCostNetworks.py` is responsible for transferring the text training sample files to geometric compatible file. 
- The file `SkylineGNN_py/utilities/validation_dataset.py` is used to validate the model and generate the subgraphs for each query/training sample for late usage.


### Step 10: Train a GNN Model
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

## Testing

### Step 11: Test a Pre-trained GNN Model 
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


## Evaluation

### Step 12: Compare approximate skyline paths on GNN generated subgraphs with the exact skyline solutions that search on the whole original graph. 
The subgraph mapping file is generated by the python file, *'SkylineGNN/utilities/validation_dataset.py'*. Compare there methods by using compareBBS and DTWComparison cases in RunningScripts.java in SkylineGNN_java

1. compare approximate skyline paths on GNN generated subgraphs with the exact skyline solutions that search on the whole original graph. The subgraph mapping file is generated by the python file, `SkylineGNN_py/utilities/validation_dataset.py`.

```
    > java -jar BackboneIndex.jar -m compareBBS -dbname <src_db_name> -compareResultFolder <> -indexFolder <> -landmarkIndexFolder <> -neo4jdb <src_db>

    > -m compareBBS -dbname C9_NY_NONE_5K -resultFolder ../Data/results/C9_NY_NONE_5K -indexFolder ../Data/indexes/C9_NY_NONE_5K -landmarkIndexFolder ../Data/landmarks/C9_NY_NONE_5K
```

or use bash file:

`bash bash_commands/compareBBS.sh`


2. Compare GNN, Backbone, Baseline

```
java -jar BackboneIndex.jar -m DTWComparison -dbname <src_db_name>

============================C9_NY_NONE=============================
java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_NONE_5K 

```

or use bash file:

`bash bash_commands/eval.sh`







