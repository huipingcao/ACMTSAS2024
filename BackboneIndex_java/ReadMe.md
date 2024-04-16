# Backbone Index to Support Skyline Path Queries overMulti-cost Road Networks

This repository is the source code for our paper, "Backbone Index to Support Skyline Path Queries over Multi-cost Road Networks", which is accepted by **EDBT 2022**.    
We propose a novel index for skyline path queries (SPQs) on multi-cost road network (MCRN), named Backbone index. The corresponding index construction method to condense an
initial MCRN to multiple smaller summarized graphs with different summarization granularity. Also an efficient approach is introduced to find approximate solutions to SPQs. Our extensive experiments on real-world road networks show that our approach can find meaningful approximate solutions to SPQs by building a very compact Backbone index with reasonable index building time.   
Graph data is stored by using [neo4j](https://neo4j.com/). The code implements 1) creation of neo4j DBs for real-world cities, 2) generation of sub-graphs with the given number of nodes and create the corresponding neo4j DB, 3) Construction of our backbone index, 5) Building of the landmark index that is used for the baseline query and the backbone query on the highest level and 6) the comparison of the baseline query methods with our proposed query method on our Backbone index.  

## Preliminary 
- Neo4j,[https://neo4j.com/](https://neo4j.com/), our code embedded Neo4j ([https://neo4j.com/docs/java-reference/current/java-embedded/](https://neo4j.com/docs/java-reference/current/java-embedded/)). So there is no need to install the neo4j physically. 
- [Apache Maven](https://maven.apache.org/)
- [Apache commons-io](http://commons.apache.org/proper/commons-io/)
- [Apache commons-math3](https://commons.apache.org/proper/commons-math/)
- [Apache commons-cli](https://commons.apache.org/proper/commons-cli/)
- JAVA 14.

## Dataset 
We collects nine real-world road network datasets of from New York city (C9_NY), Bay Area (C9_BAY), Colorado (C9_COL), Florida (C9_FLA), Eastern USA (C9_E), and Central USA (C9_CTR), California (L_CAL), San Francisco (L_SF), and USA (L_NA) from [9th DIMACS Implementation Challenge](http://users.diag.uniroma1.it/challenge9/download.shtml.) and [Real Datasets for Spatial Databases: Road Networks and Points of Interest.](https://www.cs.utah.edu/~lifeifei/SpatialDataset.htm.). The raw road network data contains only the coordinates of nodes
and the spatial length of road segments. We generate two more edge weights by sampling from a uniform distribution in the range of [1,100]. Each road segment has three dimensions.

### Raw Data

[//]: # (ref to [EDBT paper]&#40;https://www.dropbox.com/s/nm2zfdvlytm8aow/backbone_EDBT2022_CR_submitted.pdf?dl=0&#41; for data description.)

- datasets start with C9_ are downloaded from http://users.diag.uniroma1.it/challenge9/download.shtml.
- datasets start with L_ are downloaded from https://www.cs.utah.edu/~lifeifei/SpatialDataset.htm.

| Name        | Description  | # nodes | # arcs | Longitude | Latitude       | 
|-------------|--------------|---------|--------|-----------|----------------|
| C9_NY(NY)   | New York City | 264,346 | 733,846 | [40.3; 41.3] | [73.5; 74.5]   |   
| C9_BAY(BAY) | San Francisco Bay Area | 321,270 | 800,172 | [37.0; 39.0] | [121; 123]     |     
| C9_COL(COL) | Colorado  |  435,666  | 1,057,066 |  [37.0; 41.0] | [102.0; 109.0] |
| C9-FLA(FLA) |  Florida  | 1,070,376  |  2,712,798 | [24.0; 31.0]	 | [79; 87.5]     |
| C9_E(E)     |  Eastern USA  |  3,598,623	 | 8,778,114 | [24.0; 50.0] | [-infty; 79.0] |
| C9_CTR(CTR) |  Central USA  | 14,081,816  | 34,292,496 | [25.0; 50.0] | [79.0; 100.0]	 |

- datasets start with L_ are downloaded from https://www.cs.utah.edu/~lifeifei/SpatialDataset.htm
    - L_CAL is from [California Road Network's Nodes (Node ID, Longitude, Latitude)](https://www.cs.utah.edu/~lifeifei/research/tpq/cal.cnode), [California Road Network's Edges (Edge ID, Start Node ID, End Node ID, L2 Distance)](https://www.cs.utah.edu/~lifeifei/research/tpq/cal.cedge)
    - L_SF is from [San Francisco Road Network's Nodes (Node ID, Normalized X Coordinate, Normalized Y Coordinate)](https://www.cs.utah.edu/~lifeifei/research/tpq/SF.cnode), [San Francisco Road Network's Edges (Edge ID, Start Node ID, End Node ID, L2 Distance)](https://www.cs.utah.edu/~lifeifei/research/tpq/SF.cedge)
    - L_NA is from [North America Road Network's Nodes (Node ID, Normalized X Coordinate, Normalized Y Coordinate)](https://www.cs.utah.edu/~lifeifei/research/tpq/NA.cnode), [North America Road Network's Edges (Edge ID, Start Node ID, End Node ID, L2 Distance)](https://www.cs.utah.edu/~lifeifei/research/tpq/NA.cedge)


- Raw Data can be processed with the code under the [DataProcess](DataProcess) Folder 

## Compile
Execute the maven command, ```mvn clean compile assembly:single```, to compile and package the code. The executable jar file is placed under the *'target'* folder. 

## Usage
```
usage: java -jar BackboneIndex.jar
Run the code of BackboneIndex :
 -cLandMark,--create_new              the flag of creating the new
                                      landmarks or read from the existed
                                      landmarks
 -dbname,--dbname <arg>               name of the neo4j db
 -degreeHandle <arg>                  two degree edges handling,
                                      [none,each,normal], default:normal
 -dtwnormal <arg>                     normalization for cosine similarity
 -GraphInfo,--GraphInfo <arg>         the place where stores the
                                      information of nodes and edges
 -h,--help                            print the help of this command
 -indexFolder,--indexFolder <arg>     the place where to store the index
 -infotype <arg>                      the information type: distribution,
                                      pair, twohop, coef
 -infoverb <arg>                      display the information of the
                                      coefficient in ascending order
 -landmarkIndexFolder <arg>           the place where to store the
                                      landmark index for given level neo4j
                                      db
 -lLandMark,--list_landmarks <arg>    the list of the landmark, split by
                                      comma.
 -logFolder <arg>                     the folder to store the log files,
                                      log files start with the classname
 -m,--method <arg>                    methods to execute, the default
                                      value is 'exact_improved'.
 -min_size,--min_size <arg>           size of the cluster
 -msize <arg>                         number of small nodes to merge
 -neo4jdb,--neo4jPath <arg>           the place where stores the neo4j DB
                                      files
 -nLandMark,--number_andmark <arg>    the number of the landmark
 -numQuery <arg>                      number of queries.
 -outGraphInfo,--outGraphInfo <arg>   the place where stores the output
                                      information of nodes and edges
 -percentage <arg>                    percentage of the edges must be
                                      removed from each level
 -pind <arg>                          p_index for the noise threshold
 -pmethod <arg>                       the method to partition the graphs
 -resultFolder <arg>                  the place stores the paths returned
                                      by BBS and BackBone Query
 -savedFolder,--savedFolder <arg>     the place to save the sub-graph
                                      folders
 -sub_K,--subK <arg>                  number of nodes that the subgraph
                                      generated
 -timeout <arg>                       the timeout of the Baseline query in
                                      ms
 -timestamp <arg>                     data time of the log file and
                                      results returned by bbs and backbone
```

## Steps

### 1. Creation of Neo4j DBs for real-world cities. 
> - java -jar BackboneIndex.jar -m createDB -dbname C9_NY -neo4jdb ../Data/Neo4jDB -GraphInfo ../Data/C9_NY  

### 2. Generate a sub-graph with the given number of nodes and create the neo4j DBs 
> - java -jar BackboneIndex.jar -m GenerateSubGraph -dbname C9_NY -neo4jdb ../Data/Neo4jDB -GraphInfo ../Data/C9_NY -sub_K 10 -outGraphInfo ../Data/C9_NY_10K
> - java -jar BackboneIndex.jar -m createDB -dbname C9_NY_10K -neo4jdb ../Data/Neo4jDB -GraphInfo ../Data/C9_NY_10K
> -------------------------------------------------------------------------------------------------------------------
> - java -jar BackboneIndex.jar -m GenerateSubGraph -dbname C9_NY -neo4jdb ../Data/Neo4jDB -GraphInfo ../Data/C9_NY -sub_K 22 -outGraphInfo ../Data/C9_NY_22K
> - java -jar BackboneIndex.jar -m createDB -dbname C9_NY_22K -neo4jdb ../Data/Neo4jDB -GraphInfo ../Data/C9_NY_22K

### 3. Build the backbone index for given graph 
> - java -jar BackboneIndex.jar -m IndexBuilding -dbname C9_NY_10K -neo4jdb ../Data/Neo4jDB -indexFolder ../Data/Index/C9_NY_10K -min_size 200 -percentage 0.01 -degreeHandle normal
> - java -jar BackboneIndex.jar -m IndexBuilding -dbname C9_NY_22K -neo4jdb ../Data/Neo4jDB -indexFolder ../Data/Index/C9_NY_22K -min_size 200 -percentage 0.01 -degreeHandle normal
> - java -jar BackboneIndex.jar -m IndexBuilding -dbname C9_NY -neo4jdb ../Data/Neo4jDB -indexFolder ../Data/Index/C9_NY -min_size 200 -percentage 0.01 -degreeHandle normal

> - java -jar BackboneIndex.jar -m IndexBuilding -dbname C9_NY_NONE_5K -min_size 200 -percentage 0.01 -degreeHandle normal


### 4. Build the landmark for the highest level of the backbone index and for the original graph. In this example, the highest backbone index is *10*. The landmark index of the original graph is used to speed up the BBS method. 
> - java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_10K_Level10 -neo4jdb ../Data/Neo4jDB -logFolder ../Data/logs -landmarkIndexFolder ../Data/Index/landmarks -nLandMark 3 -cLandMark true
> - java -jar BackboneIndex.jar -m BuildLandMark -dbname C9_NY_10K_Level0 -neo4jdb ../Data/Neo4jDB -logFolder ../Data/logs -landmarkIndexFolder ../Data/Index/landmarks -nLandMark 3 -cLandMark true

### 5. Conduct the comparison of the BBS Baseline and our query on our Backbone index
> - java -jar BackboneIndex.jar -m Comparison -dbname C9_NY_10K -logFolder ../Data/logs -landmarkIndexFolder ../Data/Index/landmarks -resultFolder ../Data/result -numQuery 5 -nLandMark 3 -indexFolder ../Data/Index -neo4jdb ../Data/Neo4jDB

### 6. Show the analysis result, the timestamp is the time when the comparison results are generated in 'step 5'. 
> - java -jar BackboneIndex.jar -m DTWComparison -dbname C9_NY_10K -logFolder ../Data/logs -resultFolder ../Data/result -timestamp 20201027_184337

----------------------------------------------------------------------------------------------------------------------------------------------------
1. The initial graph is stored in the folder that starts with the db name and ends with **_Level0**.
2. The different level of graphs that are generated during the index construction process is ends from **_Level1** to **_LevelL**, where **L** is the highest level.
3. The step 3 builds the landmark index for a specific level graph. In order to make the BBS baseline method works properly, the landmark index of the **Level0** graph needs to be created before the comparison experiments. \*\***NOTICE**: In order to make our query approach works properly, the landmark index of the highest level graph needs to be created. The highest level can be find from the *output* or from the generated index file. For example, the folder of level10 index is empty, but the level9's index is not empty. Then, the highest level graph is **10**.
4. When analyzing the results, the timestamp of the results and the performance log file can be found from the output and must be specified correctly. 
5. The BBS baseline is stopped, if it can not finish in 20 mins. 

## Baseline methods
1. A Baseline Best-first Search method (abbreviated as BBS) which adopts the ideas in [1, 2, 3] to create an initial set of results (the shortest path on each dimension), grow the solutions using best-first search strategy, and apply pruning techniques using landmark indexes.  
2. GTree,[4]
3. Contracting Highways (CH)[5]
---------------------------------------------------------------------
[1] Qixu Gong, Huiping Cao, and Parth Nagarkar. Skyline queries constrained by multi-cost transportation networks. 2019 IEEE 35th International Conference on Data Engineering (ICDE), pages 926–937, 2019.  
[2] Hans-Peter Kriegel, Matthias Renz, and Matthias Schubert. Route skyline queries: A multi-preference path planning approach. 2010 IEEE 26th International Conference on Data Engineering (ICDE 2010), pages 261–272, 2010.  
[3] Bin Yang, Chenjuan Guo, Christian S. Jensen, Manohar Kaul, and Shuo Shang. Multi-cost optimal route planning under time-varying uncertainty. 2013.  
[4] Ruicheng Zhong, Guoliang Li, Kian-Lee Tan, and Lizhu Zhou. G-tree: An efficient index for knn search on road networks. In Proceedings of the 22nd ACM international conference on Information & Knowledge Management, pages 39–48, 2013.  
[5] Robert Geisberger, Peter Sanders, Dominik Schultes, and Daniel Delling. Contraction hierarchies: Faster and simpler hierarchical routing in road networks. In International Workshop on Experimental and Efficient Algorithms, pages 319–333. Springer, 2008  