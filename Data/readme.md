# End-toEnd Data Pipeline Descriptions 
Take C9_NY_NONE_5K as an example.

## Raw Graph Datasets
Download C9_NY from http://users.diag.uniroma1.it/challenge9/download.shtml 

<img width="423" alt="image" src="https://github.com/user-attachments/assets/e4a9411b-ac61-448f-96a9-2378b854a9b3">

## Processed graphs with edge weights and subgraphs
C9_NY_NONE: [NodeInfo.txt](Data/C9_NY_NONE/NodeInfo.txt) and [SegInfo.txt](Data/C9_NY_NONE/SegInfo.txt) 

C9_NY_NONE_5K: [NodeInfo.txt](Data/C9_NY_NONE_5K/NodeInfo.txt) and [SegInfo.txt](Data/C9_NY_NONE_5K/SegInfo.txt)

## Processed neo4j Graphs and Abstract Graphs (Levels)
See [C9_NY_NONE_5K db](Data/C9_NY_NONE_5K/db/databases) for example. 

<img width="511" alt="image" src="https://github.com/user-attachments/assets/e3860353-f6f8-4bc4-bd64-c808f7e02ab2"> 

<img width="877" alt="image" src="https://github.com/user-attachments/assets/92e43d34-e462-4181-8ba2-24e5af2b5338">

## BBS Training and Testing Datasets
See [C9_NY_NONE_5K bbs results](Data/bbs_results) for an example of C9_NY_NONE_5K training datasets.

e.g. [bbs_0_2623_C9_NY_NONE_5K.log](Data/bbs_results/bbs_0_2623_C9_NY_NONE_5K.log)

- Contains all the generated backbone skyline paths from node 0 to node 2623 in the graph C9_NY_NONE_5K neo4j 
- A line is a bbs path
  - `<start_node>--><dest_node>,[ <cost1> <cost2> <cost3>]  (<start_node>)--[5013]-->(<intermediate_node>)--[<edge>]-->...--[<edge>]-->(<dest_node>)`
  - `0-->2623,[ 90604.0 1602.0 1971.0]  (0)--[5013]-->(1)--[5014]-->(2)--[5139]-->(3)--[5201]-->(4)--[5170]-->(5)--[4905]-->(469)--[1585]-->(447)--[1528]-->(444)--[1522]-->(434)--[1488]-->(430)--[1474]-->(426)--[1467]-->(423)--[1460]-->(146)--[365]-->(142)--[357]-->(143)--[5382]-->(1717)--[5380]-->(103)--[281]-->(102)--[5374]-->(1714)--[5376]-->(1307)--[5375]-->(1219)--[1901]-->(1248)--[1551]-->(1238)--[1509]-->(1235)--[1502]-->(1236)--[1704]-->(1246)--[1543]-->(1247)--[1225]-->(2734)--[1385]-->(2335)--[217]-->(2334)--[211]-->(2336)--[248]-->(2345)--[1211]-->(2722)--[1202]-->(2723)--[1201]-->(2624)--[1001]-->(2623)`

## Processed Training and Testing .pt files (Labelled) 
<img width="606" alt="image" src="https://github.com/user-attachments/assets/90e5fd13-ca8e-4dbb-bfbb-1a130b0e100e">

- We predict the label for each node in the target graph for a query from start node to destination node.
- A training instance is a set of whole graphs consisting labelled nodes
- Labelling description:
  - <img width="869" alt="image" src="https://github.com/user-attachments/assets/c61d94b6-a87d-49ce-9bdb-35aeca219315">
  - Figure 8 shows an example of a training instance for a query which are represented with the star nodes. Assume there are only two skyline paths between them, (vs , v1, v2, v3, v4, v5, vt ) and (vs , v6, v2, v3, v7, v5, vt ). The nodes on the paths are labeled as type 
1 and marked using dark blue. Note that nodes v2 , v3 , and v5 occur in both paths, which is commonly observed from the answer set. The 1-hop neighbors of these nodes are the type-2 nodes and are marked using light blue in the figure. These nodes form the minimum search space of this 
query. During the query stage, an ideal GNN model predicts all these nodes to be in the search space although an actual GNN model may not accurately predict all these nodes to be in the search space.
- The labelled graphs are processed and splitted into training and testing .pt files

## Mapped Files
See [C9_NY_NONE_5K_epoch100_query100_128_128_32_1_EmbedTrue_Transformer_ConLossTrue.mapping](Data/mapped/C9_NY_NONE_5K_epoch100_query100_128_128_32_1_EmbedTrue_Transformer_ConLossTrue.mapping) for an example.

- e.g. 1683 3652 {1280, 1281, 1282, 1292, 1293, 3472, 1297, 3473, 1299, 1683, 1301, 1298, 1302, 3476, 3481, 3482, 3477, 3484, 3478, 3486, 3485, 1319, 3496, 1321, 3497, 3498, 1068, 1325, 1326, 3500, 3499, 2741, 2742, 3389, 3650, 3652, 200, 201, 202, 208, 209, 210, 3422, 3297, 3298, 3429, 3302, 3431, 3303, 3304, 3305, 3307, 3306, 3308, 3310, 4499, 3313, 3314, 3315, 3317, 3318, 3321, 3323, 1279} 1.009379 [1280, 1282, 1292, 1297, 1298, 1302, 3298, 3302, 3306, 3308, 3310, 3315, 3321, 3323, 3389, 3422, 3429, 3472, 3476, 3477, 3478, 3481, 3486, 3496, 3499, 4499] 0.360360 0.778523 0.984000 0.426178 0.769231 0.906250 0.235294 0.682353 0.617037 0.840566
- ```line = "{} {} {} {:.6f} {} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f}\n".format(int(src), int(
                dest), sub_node_set, sub_graph_finding_time+model_exectue_time, target_node_list, f1_score_1, f1_score_1_s, f1_score_2, f1_score_3,pre_score, pre_score_s, rec_score, rec_score_s, roc, roc_s)```
