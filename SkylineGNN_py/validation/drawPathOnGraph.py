import matplotlib.pyplot as plt
import matplotlib
import pandas as pd
import re
import os
import sys
import gc
import numpy as np
import random as rd
from matplotlib import cm
import time
from os import path as osp
import torch
import torch_geometric
# from torch_geometric.data import DataLoader
from torch_geometric.loader import DataLoader
import time
import math
from torch_geometric.utils.convert import to_networkx
import networkx as nx
import warnings
warnings.simplefilter(action='ignore')

sys.path.append('/Users/hyingchen/PycharmProjects/Graph/ICDE23/GNNquery/SkylineGNN_py/utilities')
sys.path.append('/Users/hyingchen/PycharmProjects/Graph/ICDE23/GNNquery/SkylineGNN_py/validation')
# print(sys.path)
import fnmatch

device = torch.device('cpu')

DEBUG = 0


from torch.nn import CrossEntropyLoss, BCELoss, BCEWithLogitsLoss
from torch_geometric.data import DataLoader
# from torch_geometric.loader import DataLoader
import numpy as np
from model import GraphEncoder
from readData import LoadData, LoadGraph
import os
from MultiCostNetworks import MultiCostNetworks
import torch.nn.functional as F

from torch_geometric.utils import subgraph, k_hop_subgraph
from readCheckPoints import getMaxCheckPointName



@torch.no_grad()
def test(data):
    model.eval()

    total_loss = 0
    total_pre_n = 0
    total_n = 0
    total_graphs = 0
    total_conn_lost = 0
    total_nll_lost = 0

    data = data
    logits = model(data, n_nodes)

    loss = F.nll_loss(logits, data.y)
    total_nll_lost += loss.item() * data.num_graphs

    if conn_loss_enable:
        c_loss = (1/graph_size) * \
            (-math.log(model.connectivity_loss(logits, data)))
        total_conn_lost += c_loss * data.num_graphs
        loss = loss+c_loss

    total_pre_n += (np.argmax(logits.cpu().detach().numpy(), axis=1) == 1).sum() + (
        np.argmax(logits.cpu().detach().numpy(), axis=1) == 2).sum()
    total_n += ((data.y.cpu().detach().numpy() == 1).sum())

    total_graphs += data.num_graphs

    total_loss += loss.item() * data.num_graphs
    # print(f"num_graphs: {data.num_graphs}, total_n: {total_n}, total_pre_n: {total_pre_n}")

    # if n!=0:
    #     print(count, np.argmax(logits.cpu().numpy(),axis=1), n )
    #     sys.exit()

    return total_loss / data.num_graphs, total_nll_lost/data.num_graphs, total_conn_lost/data.num_graphs, total_pre_n / total_graphs, total_n / total_graphs, logits


# helper func to get coordinates info
def getLocationsInfo(nodeID, node_data):
    row = node_data.loc[node_data[0] == nodeID]
    return [row.iloc[0][1], row.iloc[0][2]]


# helper func to draw node with id
def drawSpecialNode(id, ax, node_data, text=None, marker="o", markersize="1.5", showmarker=True, showtext=False, color="b"):
    dest_id = id
    if text is None:
        text = dest_id

    dest_node = node_data.loc[node_data[0] == dest_id]

    if showmarker:
        plt.plot(dest_node[1], dest_node[2], marker,
                 markersize=markersize, color=color)
    if showtext:
        ax.annotate(text, xy=(dest_node[1], dest_node[2]))


# show search space
def draw_instance_coverage(graph_folder, ins_nodes):
    fig, ax = plt.subplots(figsize=(15, 10))
    print(graph_folder)

    ###########################################################################
    node_f_name = graph_folder + "NodeInfo.txt"
    seg_f_name = graph_folder + "SegInfo.txt"
    node_data = pd.read_csv(node_f_name, sep=" ", header=None)

    with open(seg_f_name) as f:
        content = [x.strip("\n") for x in f.readlines()]

    if DEBUG:
        print(f"There are {len(content)} edges in the raw graph. ")
        print(f"There are {len(node_data[0])} nodes in the raw graph. ")

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
        if index % 3000 == 0:
            print(index, "-------------")

    plt.scatter(node_data[1], node_data[2], marker="o", alpha=1)

    for node_id in ins_nodes:
        drawSpecialNode(node_id, ax, node_data, text=None, marker="o", markersize="1.2", showmarker=True, showtext=False,
                        color="g")

    plt.show()


# show raw graph as background
def draw_raw_graph(graph_folder, savetofolder=False, savedname=None, title_name=None):
    # font = {'family': 'Arial', 'weight': 'normal', 'size': 30}
    # matplotlib.rc('font', **font)
    fig, ax = plt.subplots(figsize=(15, 10))
    # fig, ax = plt.subplots(figsize=(15, 10), dpi=200)
    # print(graph_folder)

    ###########################################################################
    node_f_name = graph_folder + "NodeInfo.txt"
    seg_f_name = graph_folder + "SegInfo.txt"
    node_data = pd.read_csv(node_f_name, sep=" ", header=None)

    with open(seg_f_name) as f:
        content = [x.strip("\n") for x in f.readlines()]
    if DEBUG:
        print(f"There are {len(content)} edges in the graph. ")
        print(f"There are {len(node_data[0])} nodes in the graph. ")

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
        if index % 3000 == 0:
            print(index, "-------------")

    plt.scatter(node_data[1], node_data[2], marker="o", alpha=1)
    if title_name is not None:
        plt.title(title_name, fontsize=30)

    if savetofolder:
        fig.tight_layout()
        plt.savefig(savedname,bbox_inches='tight')
    else:
        plt.show()


def draw_graph(graph_folder, labels, src, dest):
    fig, ax = plt.subplots(figsize=(15, 10), dpi=200)
    # fig, ax = plt.subplots(figsize=(10,8))
    # print(graph_folder)
    ###########################################################################
    node_f_name = graph_folder + "/NodeInfo.txt"
    seg_f_name = graph_folder + "/SegInfo.txt"
    node_data = pd.read_csv(node_f_name, sep=" ", header=None)

    with open(seg_f_name) as f:
        content = [x.strip("\n") for x in f.readlines()]

    print(f"There are {len(content)} edges in the graph. ")
    print(f"There are {len(node_data[0])} nodes in the graph. ")
    print(f"Drawing {src} -> {dest}...")

    c = []
    for l in labels:
        if l == 0:
            c.append("b")
        elif l == 1:
            c.append("r")
        else:
            c.append("y")
    print("============================================")

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
        if index % 3000 == 0:
            print(index, "-------------")

    plt.scatter(node_data[1], node_data[2], marker="o", c=c)

    src_node = node_data.loc[node_data[0] == src]
    dest_node = node_data.loc[node_data[0] == dest]

    plt.scatter(src_node[1], src_node[2], marker="*",
                color="k", alpha=0.99, s=200)
    plt.scatter(dest_node[1], dest_node[2], marker="*",
                color="k", alpha=0.99, s=200)

    plt.show()


def draw_shortest_path(graph_folder, src, dest, savetofolder=False, savedname=None, title_name=None):

    # print(graph_folder)
    # node_f_name = graph_folder + "NodeInfo.txt"
    # seg_f_name = graph_folder + "SegInfo.txt"
    # node_data = pd.read_csv(node_f_name, sep=" ", header=None)
    #
    # with open(seg_f_name) as f:
    #     edge_data = [x.strip("\n") for x in f.readlines()]
    # if DEBUG:
    #     print(f"There are {len(edge_data)} edges in the graph. ")
    #     print(f"There are {len(node_data[0])} nodes in the graph. ")

    src, dest = src, dest
    num_node, graph = LoadGraph(graph_folder, logger=None, normalization_node=True, normalization_edge=True)
    n_graph = to_networkx(graph, to_undirected=True, remove_self_loops=False)

    ###########################################################################
    shortest_path = nx.dijkstra_path(n_graph, src, dest)
    fig, ax = plt.subplots(figsize=(20, 20))
    if title_name is not None:
        plt.title(title_name, fontsize=50)
    sb = n_graph.subgraph(shortest_path).copy()
    ###########################################################################

    print(nx.number_connected_components(sb))
    max_x = max_y = 0
    min_x = min_y = 1
    n_post = {}
    for idx, n in enumerate(graph.x):
        # print(idx, n[0].item(), n[1].item())
        if idx in shortest_path:
            n_post[idx] = [n[0].item(), n[1].item()]

            if n[0].item() > max_x:
                max_x = n[0].item()
            if n[0].item() < min_x:
                min_x = n[0].item()

            if n[1].item() > max_y:
                max_y = n[1].item()
            if n[1].item() < min_y:
                min_y = n[1].item()

    # print(n_post)
    nx.draw_networkx_edges(sb, n_post, alpha=0.4)
    nx.draw_networkx_nodes(sb, n_post,
                           shortest_path,
                           node_size=20,
                           # node_color=list(p.values()),
                           cmap=plt.cm.Reds_r)
    nx.draw_networkx_nodes(sb, {src: [graph.x[src][0].item(), graph.x[src][1].item()], dest: [graph.x[dest][0].item(),graph.x[dest][1].item()]},
                           [src, dest],
                           node_size=800,
                           node_shape='*',
                           node_color='r')

    plt.xlim(min_x-0.1, max_x+0.1)
    plt.ylim(min_y-0.1, max_y+0.1)
    plt.show()
    plt.show()

    plt.figure(figsize=(20, 20))
    pos = {}
    for idx, n in enumerate(graph.x):
        # print(idx, n[0].item(), n[1].item())
        pos[idx] = [n[0].item(), n[1].item()]
    # print(pos)

    nx.draw_networkx_edges(n_graph, pos, alpha=0.4)
    nx.draw_networkx_nodes(n_graph, pos,
                           shortest_path,
                           node_size=20,
                           # node_color=list(p.values()),
                           cmap=plt.cm.Reds_r)
    nx.draw_networkx_nodes(n_graph, {src: [graph.x[src][0].item(), graph.x[src][1].item()], dest: [graph.x[dest][0].item(),graph.x[dest][1].item()]},
                           [src, dest],
                           node_size=800,
                           node_color='r',
                           node_shape='*')

    plt.xlim(0, 1)
    plt.ylim(0, 1)
    plt.show()

    if savetofolder:
        fig.tight_layout()
        plt.savefig(savedname,bbox_inches='tight')
    else:
        plt.show()


##########################################################################################

# draw a training instance (e.g. bbs log file with multiple skyline paths)
def draw_graph_with_DataObj(graph, labels, src, dest, size=(10, 8), dpi=100, title=None):
    node_data = graph.x.cpu().detach().numpy()
    # fig, ax = plt.subplots(figsize=(10,8))
    fig, ax = plt.subplots(figsize=size, dpi=dpi)

    if DEBUG:
        print(labels.shape)
        print(node_data[:, 0])
        print(node_data[:, 1])

    ###########################################################################

    col, row = graph.edge_index
    if DEBUG:
        print(len(col))
        print(len(row))
        print(f"There are {len(col)} edges in the graph. ")
        print(f"There are {len(node_data[0])} nodes in the graph. ")
        # print(graph.edge_index)


    c = []
    for l in labels:
        if l == 0:
            c.append("white") #(0.8, 0.8, 0.8) #lightgray
        elif l == 1:
            c.append("r")
        else:
            c.append("green")
    print("============================================")

    index = 0
    for edge in zip(col, row):
        # print(edge)
        start_id = edge[0].cpu().detach().item()
        end_id = edge[1].cpu().detach().item()
        # print(start_id, end_id, node_data[start_id], node_data[end_id])
        start_location = node_data[start_id]
        end_location = node_data[end_id]
        x = [start_location[0], end_location[0]]
        y = [start_location[1], end_location[1]]
        plt.plot(x, y, "-c", lw=0.5) #, markersize=10
        index += 1
        if index % 20000 == 0:
            print(index, "----------------------")

    plt.scatter(node_data[:, 0], node_data[:, 1], marker="o", c=c, alpha=1.0) #, s=20

    src_node = node_data[src]
    dest_node = node_data[dest]

    plt.scatter(src_node[0], src_node[1], marker="*",
                color="yellow", alpha=0.99, s=400)
    plt.scatter(dest_node[0], dest_node[1], marker="*",
                color="yellow", alpha=0.99, s=400)

    if SAVE: plt.savefig(f'revision_drawings/{dataset}/{title}.pdf')
    plt.show()




# ====================================================================================
# dataset = "C9_NY_5K"
# path = "/Users/hyingchen/PycharmProjects/graph/ICDE23/GNNquery/SkylineGNN_py/data/C9_NY_5K_draw"
# train_paths_folder = "/Users/hyingchen/PycharmProjects/graph/ICDE23/GNNquery/SkylineGNN_py/data/C9_NY_NONE_5K_test_1000"
# data_folder = "/Users/hyingchen/PycharmProjects/graph/ICDE23/GNNquery/SkylineGNN_py/data/C9_NY_5K_draw/"
# checkpoint_folder = '/Users/hyingchen/PycharmProjects/graph/ICDE23/GNNquery/SkylineGNN_py/new_checkpoints/C9_NY_NONE_5K/'
# check_point_file = checkpoint_folder + '128_128_64_100_1_EmbedTrue_5000_train_checkpoint_Transformer_ConLossTrue_2GNN3FFF_train_samples.pt'
# n_nodes = 5000
# ====================================================================================
dataset = "L_CAL"
path = "/Users/hyingchen/PycharmProjects/graph/ICDE23/GNNquery/SkylineGNN_py/data/L_CAL_NONE_TSP"
train_paths_folder = "/Users/hyingchen/PycharmProjects/graph/ICDE23/GNNquery/SkylineGNN_py/data/L_CAL_NONE_l17_test_1000"
data_folder = "/Users/hyingchen/PycharmProjects/graph/ICDE23/GNNquery/SkylineGNN_py/data/L_CAL_NONE_TSP/"
checkpoint_folder = '/Users/hyingchen/PycharmProjects/graph/ICDE23/GNNquery/SkylineGNN_py/new_checkpoints/L_CAL_NONE/'
check_point_file = checkpoint_folder + '128_128_16_100_1_EmbedTrue_21048_train_checkpoint_Transformer_ConLossTrue_2GNN3FFF_train_samples.pt'
n_nodes = 21048

node_dim = 2
edge_dim = 3
embedding_dim = 128
hidden_dim = 128
batch_size = 16
enable_embed = True
output_dim = 3
model_name = "Transformer"
heads = 1
conn_loss_enable = True
enable_edge_attr= True

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
print(device)

num_node, graph = LoadGraph(path, logger=None, normalization_node=True, normalization_edge=True)
graph_size = num_node
print("==========================================")

train_dataset = MultiCostNetworks(graph, train_paths_folder=train_paths_folder, root=data_folder, split='train')
test_dataset = MultiCostNetworks(graph, train_paths_folder=train_paths_folder, root=data_folder, split='test')
train_loader = DataLoader(train_dataset, drop_last=True, batch_size=batch_size)
test_loader = DataLoader(test_dataset, drop_last=True, batch_size=batch_size)

model = GraphEncoder(node_dim, edge_dim, embedding_dim, hidden_dim, output_num_class=output_dim, device=device, enable_embedding=enable_embed,
                     batch=batch_size, model_name=model_name, heads=heads).to(device)
optimizer = torch.optim.Adam(params=model.parameters(), lr=0.0001)

checkpoint = torch.load(check_point_file,map_location=torch.device('cpu'))
model.load_state_dict(checkpoint['model_state_dict'])
resume_epoch = checkpoint['epoch']
loss = checkpoint['loss']


# ====================================================================================
# C9_NY_5K: {(14, 6): '1987->1722, n_paths: 76, nodes: 29', (54, 4): '56->2830, n_paths: 3, nodes: 21'}
# L_CAL: {√(13, 5): '19426->13307, n_paths: 4, nodes: 12', {(47, 9): '20569->6806, n_paths: 14, nodes: 194'}
# batch_nums = [b for b in range(54, 55)]
# idxs = [i for i in range(4, 5)]
batch_nums = [47]
idxs = [9]

n = num_node
grahp_size = num_node
test_data = None

working_pairs = {}

# print(len(test_loader))
# sys.exit(0)

for batch_num in batch_nums:
    print(f"trying batch_num {batch_num}............")
    for index, data in enumerate(test_loader):
        # print(f"index: {index}")
        if index == batch_num:
          test_data = data
          break

    # print(f"test_data: {test_data}")

    start_time = time.time()
    val_auc, test_nll_loss, test_conn_loss, test_p_n, test_n, logits = test(test_data)
    end_time = time.time()
    # print("Val:{:.4f}({:.5f}+{:.5f})[{:.5f} - {:.5f}], running time each epoch:{:.2f}".format(
    #             val_auc, test_nll_loss, test_conn_loss, test_p_n, test_n, (end_time - start_time)))

    for i in idxs:
        graph = test_data.to_data_list()[i]
        # print(f"graph: {graph}")

        predictions = np.argmax(logits[i*n:(i+1)*n, :].cpu().detach().numpy(), axis=1)
        data_y = data.y.cpu().detach().numpy()[i*n:(i+1)*n]
        pre_n_i = (predictions == 1).sum() + (predictions == 2).sum()
        total_n_i = (data_y == 1).sum()+(data_y == 2).sum()


        def find_matching_log_file(directory, src, dest):
            pattern = f'*_{src}_{dest}_*.log'
            for file_name in os.listdir(directory):
                if fnmatch.fnmatch(file_name, pattern):
                    # print(file_name)
                    all_nodes = set()
                    with open(directory+file_name, 'r') as file:
                        lines = file.readlines()
                        n_paths = len(lines)
                        # print(n_paths)
                        first_path_nodes = [int(node) for node in re.findall(r'\((\d+)\)', lines[0])] #5K:3
                        for line in lines:
                            nodes_in_path = set(int(node) for node in re.findall(r'\((\d+)\)', line))
                            all_nodes.update(nodes_in_path)
                        if n_paths > 1:
                            return True, n_paths, first_path_nodes, all_nodes
            return False, 0, None, None

        # gnn_logs_path = "/Users/hyingchen/PycharmProjects/graph/ICDE23/GNNquery/SkylineGNN_py/data/C9_NY_NONE_5K/epoch100_query100_128_128_32_1_EmbedTrue_Transformer_ConLossTrue/20230515_172208/"
        # bbs_logs_path = "/Users/hyingchen/PycharmProjects/graph/ICDE23/GNNquery/SkylineGNN_py/data/C9_NY_NONE_5K_test_1000/"
        gnn_logs_path = "/Users/hyingchen/PycharmProjects/graph/ICDE23/GNNquery/SkylineGNN_py/data/L_CAL_NONE_TSP/epoch100_query100_128_128_16_1_EmbedTrue_Transformer_ConLossTrue/20231129_033723/"
        bbs_logs_path = "/Users/hyingchen/PycharmProjects/graph/ICDE23/GNNquery/SkylineGNN_py/data/L_CAL_NONE_l17_test_1000/"

        src = test_data.src_node_idx[i].item()
        dest = test_data.dest_node_idx[i].item()

        matched, n_paths, first_path_nodes, all_nodes = find_matching_log_file(gnn_logs_path, src, dest)
        _, n_paths_bbs, first_path_nodes_bbs, all_nodes_bbs = find_matching_log_file(bbs_logs_path, src, dest)
        # print((n_paths, first_path_nodes))
        # print((n_paths_bbs, first_path_nodes_bbs))

        # if matched and len(all_nodes_bbs)/len(all_nodes)>1.2 and pre_n_i>20:
        if matched:
            print("=======================================")
            print(f"batch_num: {batch_num}, i: {i}")
            print(f"{src}->{dest}, n_paths: {n_paths}, pred nodes: {pre_n_i}, n_paths: {n_paths}, n_paths_bbs: {n_paths_bbs}, "
                  f"gnn len(first_path_nodes): {len(first_path_nodes)}, bbs len(first_path_nodes_bbs): {len(first_path_nodes_bbs)}")
            working_pairs[(batch_num, i)] = f"{src}->{dest}, n_paths: {n_paths}, nodes: {pre_n_i}"
            print("{} / {} ===== overall: {} / {} in {} second".format(pre_n_i, total_n_i, test_p_n, test_n, end_time - start_time))
            # print(f"predictions.shape: {predictions.shape}, data_y.shape: {data_y.shape}")
            # print(f"pre_n_i: {pre_n_i}, total_n_i: {total_n_i}")
            # print(f"src: {src} -> dest: {dest}")  # , len(n_graph.nodes): {len(n_graph.nodes)}, len(target_node_list): {len(target_node_list)}")
            # print(f"first_path_nodes: {first_path_nodes}")
            # print(f"first_path_nodes_bbs: {first_path_nodes_bbs}")

# print(working_pairs)


        ################################################################################################################
            SAVE = 1
            import matplotlib.pyplot as plt
            import networkx as nx
            from torch_geometric.utils.convert import to_networkx
            import numpy as np
            import sys

            # plt.figure(figsize=(20, 20))
            # # print(graph.edge_attr.list())
            # print(type(predictions))
            # print(np.where(predictions==1))
            # print(np.where(predictions==1)[0].tolist())

            n_graph = to_networkx(graph, to_undirected=True, remove_self_loops=False)
            # src = test_data.src_node_idx[i].item()
            # dest = test_data.dest_node_idx[i].item()
            target_node_list_1 = np.where(predictions == 1)[0].tolist()
            target_node_list_2 = np.where(predictions == 2)[0].tolist()
            target_node_list = target_node_list_1 + target_node_list_2
            gt_list_1 = np.where(data_y == 1)[0].tolist()
            gt_list_2 = np.where(data_y == 2)[0].tolist()
            gt_list = gt_list_1 + gt_list_2
            # print(f"target_node_list: {len(target_node_list)}, gt_list: {len(gt_list)}")
            # print(f"all_nodes {len(all_nodes)}, all_nodes_bbs {len(all_nodes_bbs)}")
            # print(f"all_nodes_bbs==gt_list: {set(all_nodes_bbs)==set(gt_list)}, all_nodes==target_node_list: {set(all_nodes)==set(target_node_list)}")


            # =========================bbs training instance=========================
            print("=========================bbs training instance=========================")
            sub_node_set = set(gt_list)
            print(f"bbs sub_node_set len: {len(sub_node_set)}, {sub_node_set}")

            plt.figure(figsize=(20, 20))
            sb = n_graph.subgraph(sub_node_set).copy()
            # print(f"nx.number_connected_components(sb): {nx.number_connected_components(sb)}")

            max_x = max_y = 0
            min_x = min_y = 1
            n_post = {}
            for idx, n in enumerate(graph.x):
                # print(idx, n[0].item(), n[1].item())
                if idx in sub_node_set:
                    n_post[idx] = [n[0].item(), n[1].item()]

                    if n[0].item() > max_x:
                        max_x = n[0].item()
                    if n[0].item() < min_x:
                        min_x = n[0].item()

                    if n[1].item() > max_y:
                        max_y = n[1].item()
                    if n[1].item() < min_y:
                        min_y = n[1].item()

            # print(n_post)
            nx.draw_networkx_edges(sb, n_post, alpha=0.4)
            nx.draw_networkx_nodes(sb, n_post,
                                   set(gt_list_1),
                                   node_size=20,
                                   # node_color=list(p.values()),
                                   node_shape='o',
                                   node_color='lightgreen')
            nx.draw_networkx_nodes(sb, n_post,
                                   set(gt_list_2),
                                   node_size=30,
                                   # node_color=list(p.values()),
                                   node_shape='o',
                                   node_color='darkgreen')
            nx.draw_networkx_nodes(sb, {src: [graph.x[src][0].item(), graph.x[src][1].item()],
                                        dest: [graph.x[dest][0].item(), graph.x[dest][1].item()]},
                                   [src, dest],
                                   node_size=1200,
                                   node_shape='*',
                                   node_color='r')

            plt.xlim(min_x - 0.1, max_x + 0.1)
            plt.ylim(min_y - 0.1, max_y + 0.1)
            if SAVE:
                print(f"saving figure: revision_drawings/{dataset}/{dataset}_{src}_{dest}_bbs_training_instance.pdf.........")
                plt.savefig(f'revision_drawings/{dataset}/{dataset}_{src}_{dest}_bbs_training_instance.pdf')
            plt.show()
            plt.show()


            # =========================bbs skyline paths=========================
            print("=========================bbs skyline paths=========================")
            sub_node_set = set(all_nodes_bbs) #.union(set(gt_list_1))
            print(f"bbs sub_node_set len: {len(sub_node_set)}, {sub_node_set}")

            plt.figure(figsize=(20, 20))
            sb = n_graph.subgraph(sub_node_set).copy()
            # print(f"nx.number_connected_components(sb): {nx.number_connected_components(sb)}")

            max_x = max_y = 0
            min_x = min_y = 1
            n_post = {}
            for idx, n in enumerate(graph.x):
                # print(idx, n[0].item(), n[1].item())
                if idx in sub_node_set:
                    n_post[idx] = [n[0].item(), n[1].item()]

                    if n[0].item() > max_x:
                        max_x = n[0].item()
                    if n[0].item() < min_x:
                        min_x = n[0].item()

                    if n[1].item() > max_y:
                        max_y = n[1].item()
                    if n[1].item() < min_y:
                        min_y = n[1].item()

            # print(n_post)
            nx.draw_networkx_edges(sb, n_post, alpha=0.4)
            nx.draw_networkx_nodes(sb, n_post,
                                   sub_node_set,
                                   node_size=20,
                                   # node_color=list(p.values()),
                                   cmap=plt.cm.Reds_r)
            nx.draw_networkx_nodes(sb, n_post,
                                   set(first_path_nodes_bbs), #set(sub_node_set).intersection(set(first_path_nodes_bbs)),
                                   node_size=50,
                                   node_shape='o',
                                   node_color='lightgreen')
            nx.draw_networkx_nodes(sb, {src: [graph.x[src][0].item(), graph.x[src][1].item()],
                                        dest: [graph.x[dest][0].item(), graph.x[dest][1].item()]},
                                   [src, dest],
                                   node_size=1200,
                                   node_shape='*',
                                   node_color='r')

            plt.xlim(min_x - 0.1, max_x + 0.1)
            plt.ylim(min_y - 0.1, max_y + 0.1)
            if SAVE:
                print(f"saving figure: revision_drawings/{dataset}/{dataset}_{src}_{dest}_bbs_paths.pdf.........")
                plt.savefig(f'revision_drawings/{dataset}/{dataset}_{src}_{dest}_bbs_paths_{n_paths_bbs}_nodes_{len(all_nodes_bbs)}.pdf')
            plt.show()
            plt.show()


            # draw_shortest_path(path, src=src, dest=dest,
            #                    title_name=f"gnn shortest path from src {src} -> dest {dest}")
            # =========================gnn skyline paths=========================
            print("=========================gnn skyline paths=========================")
            # sub_node_set = set()
            # for idx, node in enumerate(target_node_list):
            #     s_path = nx.dijkstra_path(n_graph, node, src)
            #     d_path = nx.dijkstra_path(n_graph, node, dest)
            #     # print(idx, ":", node, len(s_path), len(d_path))
            #     sub_node_set.update(s_path)
            #     sub_node_set.update(d_path)
            # sub_node_set.add(src)
            # sub_node_set.add(dest)
            sub_node_set = set(all_nodes)
            print(f"gnn sub_node_set len: {len(sub_node_set)}, {sub_node_set}")

            plt.figure(figsize=(20, 20))
            sb = n_graph.subgraph(sub_node_set).copy()
            # print(f"nx.number_connected_components(sb): {nx.number_connected_components(sb)}")

            max_x = max_y = 0
            min_x = min_y = 1
            n_post = {}
            for idx, n in enumerate(graph.x):
                # print(idx, n[0].item(), n[1].item())
                if idx in sub_node_set:
                    n_post[idx] = [n[0].item(), n[1].item()]

                    if n[0].item() > max_x:
                        max_x = n[0].item()
                    if n[0].item() < min_x:
                        min_x = n[0].item()

                    if n[1].item() > max_y:
                        max_y = n[1].item()
                    if n[1].item() < min_y:
                        min_y = n[1].item()


            # print(n_post)
            nx.draw_networkx_edges(sb, n_post, alpha=0.4)
            nx.draw_networkx_nodes(sb, n_post,
                                   sub_node_set,
                                   node_size=20,
                                   # node_color=list(p.values()),
                                   cmap=plt.cm.Reds_r)
            nx.draw_networkx_nodes(sb, n_post,
                                   set(first_path_nodes),
                                   # set(sub_node_set).intersection(set(first_path_nodes)),
                                   node_size=50,
                                   node_shape='o',
                                   node_color='lightgreen')
            nx.draw_networkx_nodes(sb, {src: [graph.x[src][0].item(), graph.x[src][1].item()], dest: [graph.x[dest][0].item(),graph.x[dest][1].item()]},
                                   [src, dest],
                                   node_size=1200,
                                   node_shape='*',
                                   node_color='r')

            plt.xlim(min_x-0.1, max_x+0.1)
            plt.ylim(min_y-0.1, max_y+0.1)
            if SAVE:
                print(f"saving figure: revision_drawings/{dataset}/{dataset}_{src}_{dest}_gnn_paths.pdf.........")
                plt.savefig(f'revision_drawings/{dataset}/{dataset}_{src}_{dest}_gnn_paths_{n_paths}_nodes_{len(all_nodes)}.pdf')
            plt.show()
            plt.show()









########################################################################################################################
            # draw_graph_with_DataObj(graph, data_y, test_data.src_node_idx[i].item(), test_data.dest_node_idx[i].item(), title="training_inst_gt")
            # draw_graph_with_DataObj(graph, predictions, test_data.src_node_idx[i].item(), test_data.dest_node_idx[i].item(), title="gnn_pred")

        # =========================graph background=========================
            # plt.figure(figsize=(20, 20))
            # pos = {}
            # for idx, n in enumerate(graph.x):
            #     # print(idx, n[0].item(), n[1].item())
            #     pos[idx] = [n[0].item(), n[1].item()]
            # # print(pos)
            #
            # nx.draw_networkx_edges(n_graph, pos, alpha=0.4)
            # nx.draw_networkx_nodes(n_graph, pos,
            #                        sub_node_set,
            #                        node_size=20,
            #                        # node_color=list(p.values()),
            #                        cmap=plt.cm.Reds_r)
            # nx.draw_networkx_nodes(n_graph, {src: [graph.x[src][0].item(), graph.x[src][1].item()], dest: [graph.x[dest][0].item(),graph.x[dest][1].item()]},
            #                        [src, dest],
            #                        node_size=800,
            #                        node_color='r',
            #                        node_shape='*')
            #
            # plt.xlim(0, 1)
            # plt.ylim(0, 1)
            # plt.show()


