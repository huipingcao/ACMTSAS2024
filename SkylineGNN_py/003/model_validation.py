import matplotlib.pyplot as plt
import pandas as pd 
import torch
from torch.nn import CrossEntropyLoss, BCELoss, BCEWithLogitsLoss
from torch_geometric.data import DataLoader
import numpy as np
from Skyline_GNN_RL.model import GraphEncoder
from Skyline_GNN_RL.readData import LoadData, LoadGraph
import os
from Skyline_GNN_RL.MultiCostNetworks import MultiCostNetworks
import sys
import torch.nn.functional as F

def getLocationsInfo(nodeID, node_data):
    row = node_data.loc[node_data[0] == nodeID]
    return [row.iloc[0][1], row.iloc[0][2]]


def draw_graph(graph_folder):
    fig, ax = plt.subplots(figsize=(10, 8))
    print(graph_folder)
    ###########################################################################
    node_f_name = graph_folder + "NodeInfo.txt"
    seg_f_name = graph_folder + "SegInfo.txt"
    node_data = pd.read_csv(node_f_name, sep=" ", header=None)
    
    with open(seg_f_name) as f:
      content = [x.strip("\n") for x in f.readlines()]
    
    print("There are {} edges in the graph. ".format(len(content)))
    print(len(node_data[0]))

    index = 0
    for seg in content:
        start_id = int(seg.split(" ")[0])
        end_id = int(seg.split(" ")[1])
        start_location = getLocationsInfo(start_id, node_data)
        end_location = getLocationsInfo(end_id, node_data)
        x = [start_location[0], end_location[0]]
        y = [start_location[1], end_location[1]]
        plt.plot(x, y, "-c", lw=0.5)
        index += 1
        if index % 1000 == 0:
            print(index, "-------------")
    
    plt.scatter(node_data[1], node_data[2], marker="o", alpha=1)
    plt.show()


path = "/home/gqxwolf/shared_git/BackboneIndex/Data/C9_NY_5K/"
draw_graph(path)

graph = LoadGraph(path)

print(graph)

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

print(device)


node_dim = 2
edge_dim = 3
embedding_dim = 512
hidden_dim = 512
batch_size = 64
enable_embed = True
output_dim =3 
model_name="SAGE"

n=5000

train_paths_folder = "/home/gqxwolf/shared_git/BackboneIndex/Datac9_ny_5k/results_bbs/training_GNN"
test_dataset = MultiCostNetworks(graph, train_paths_folder = train_paths_folder, root=path, split='test')

test_loader = DataLoader(test_dataset, drop_last=True, batch_size=batch_size)
model = GraphEncoder(node_dim, edge_dim, embedding_dim, hidden_dim, output_num_class=output_dim, device=device, enable_embedding=enable_embed,
                     batch=batch_size, model_name = model_name).to(device)
optimizer = torch.optim.Adam(params=model.parameters(), lr=0.0001), raph(path)

