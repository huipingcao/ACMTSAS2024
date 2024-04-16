import sys

import pandas as pd
import numpy as np
import torch
from torch_geometric.data import Data
from os import listdir
from os.path import isfile, join, basename
import re
from tqdm import tqdm
from sklearn.preprocessing import MinMaxScaler
from torch_geometric.utils import to_undirected

reg_exp = "\\(\\d+\\)"


def LoadGraph(path, logger=None, normalization_node=True, normalization_edge=True):
    node_file_path = path+"/NodeInfo.txt"
    edge_file_path = path+"/SegInfo.txt"


    node_df = pd.read_csv(node_file_path, decimal=' ',
                          delim_whitespace=True, header=None)
    n = node_df.shape[0]
    node_df.columns = ['node_id', 'lattitude', 'longitude']
    # print("coordinates with raw values:---------------------------------------")
    # print(node_df.head())
    
    if logger is not None:
        logger.info(
            "coordinates with raw values:---------------------------------------")
        logger.info(node_df.head())

    # normalization latitude and longitude
    if normalization_node:
        scaler = MinMaxScaler()
        node_df['lattitude'] = scaler.fit_transform(
            np.array(node_df['lattitude']).reshape(-1, 1))
        node_df['longitude'] = scaler.fit_transform(
            np.array(node_df['longitude']).reshape(-1, 1))
    # print("normalized coordinates:---------------------------------------")
    # print(node_df.head())
    # print(node_df['lattitude'].max(), node_df['lattitude'].min())
    # print(node_df['longitude'].max(), node_df['longitude'].min())

    if logger is not None:
        logger.info(
            "normalized coordinates:---------------------------------------")
        logger.info(node_df.head())

    edge_df = pd.read_csv(edge_file_path, decimal=' ',
                          delim_whitespace=True, header=None)
    edge_df.columns = ['src_node_id',
                       'dest_node_id', 'cost1', 'cost2', 'cost3']
    # print("edge attributes with raw values:---------------------------------------")
    # print(edge_df.head())

    if logger is not None:
        logger.info(
            "edge attributes with raw values:---------------------------------------")
        logger.info(edge_df.head())

    # Adj information of node [2, E]
    edge_index = torch.tensor(edge_df.iloc[:, 0:2].T.values)

    # edge attr information [node, 3]
    edge_attr = torch.tensor(
        edge_df.iloc[:, 2:].values.astype(np.float), dtype=torch.float)
    row, col = edge_index[0], edge_index[1]

    # edge_index_inv = torch.stack((col, row), dim=0)
    #
    # print(edge_index.shape)
    # print(edge_attr.shape)
    #
    # print(edge_index)
    # print(edge_index_inv)
    #
    # edge_index = torch.cat((edge_index, edge_index_inv), dim=1)
    # edge_attr = torch.cat((edge_attr, edge_attr), dim=0)
    #
    # print(edge_index.shape)
    # print(edge_attr.shape)

    c = node_df.loc[:, node_df.columns[1:3]].values.astype(np.float)
    x = torch.tensor(c)

    # print("Shape of nodes:", x.shape)  # (N,2), coordinations

    graph = Data(x=x, edge_index=edge_index, edge_attr=edge_attr)
    # print("directed graph: ", graph)
    out = to_undirected(graph.edge_index, graph.edge_attr)
    graph.edge_index, graph.edge_attr = out
    # print("undirected graph: ", graph)
    # print('==============================================================')

    # Gather some statistics about the graph.
    # print(f'Number of nodes: {graph.num_nodes}')
    # print(f'Number of edges: {graph.num_edges}')
    # print(f'Average node degree: {graph.num_edges / graph.num_nodes:.2f}')
    # print(f'Contains isolated nodes: {graph.contains_isolated_nodes()}')
    # print(f'Contains self-loops: {graph.contains_self_loops()}')
    # print(f'Is undirected: {graph.is_undirected()}')

    # print('==============================================================')

    if logger is not None:
        logger.info("Shape of nodes: %s", x.shape)  # (N,2), coordinations
        logger.info("directed graph: %s", graph)
        logger.info("undirected graph: %s", graph)
        logger.info(
            '==============================================================')

        # Gather some statistics about the graph.
        logger.info(f'Number of nodes: {graph.num_nodes}')
        logger.info(f'Number of edges: {graph.num_edges}')
        logger.info(
            f'Average node degree: {graph.num_edges / graph.num_nodes:.2f}')
        logger.info(
            f'Contains isolated nodes: {graph.contains_isolated_nodes()}')
        logger.info(f'Contains self-loops: {graph.contains_self_loops()}')
        logger.info(f'Is undirected: {graph.is_undirected()}')
        logger.info(
            '==============================================================')

    return n, graph


# def LoadSubGraph(path, sub_name: str, graph_idx: int, logger=None):
#     node_file_path = "{}/NodeInfo_{}_{}.txt".format(path, sub_name, graph_idx)
#     edge_file_path = "{}/SegInfo_{}_{}.txt".format(path, sub_name, graph_idx)
#
#     print(node_file_path)
#     print(edge_file_path)
#
#     node_df = pd.read_csv(node_file_path, decimal=' ', delim_whitespace=True, header=None)
#     n = node_df.shape[0]
#     node_df.columns = ['node_id', 'lattitude', 'longitude']
#     # print("coordinates with raw values:---------------------------------------")
#     # print(node_df.head())
#     if logger is not None:
#         logger.info(
#             "coordinates with raw values:---------------------------------------")
#         logger.info(node_df.head())
#
#     # normalization latitude and longitude
#     scaler = MinMaxScaler()
#     node_df['lattitude'] = scaler.fit_transform(
#         np.array(node_df['lattitude']).reshape(-1, 1))
#     node_df['longitude'] = scaler.fit_transform(
#         np.array(node_df['longitude']).reshape(-1, 1))
#     # print("normalized coordinates:---------------------------------------")
#     # print(node_df.head())
#     # print(node_df['lattitude'].max(), node_df['lattitude'].min())
#     # print(node_df['longitude'].max(), node_df['longitude'].min())
#
#     if logger is not None:
#         logger.info(
#             "normalized coordinates:---------------------------------------")
#         logger.info(node_df.head())
#
#     edge_df = pd.read_csv(edge_file_path, decimal=' ',
#                           delim_whitespace=True, header=None)
#     edge_df.columns = ['src_node_id',
#                        'dest_node_id', 'cost1', 'cost2', 'cost3']
#     # print("edge attributes with raw values:---------------------------------------")
#     # print(edge_df.head())
#     if logger is not None:
#         logger.info(
#             "edge attributes with raw values:---------------------------------------")
#         logger.info(edge_df.head())
#
#     # Adj information of node [2, E]
#     edge_index = torch.tensor(edge_df.iloc[:, 0:2].T.values)
#
#     # edge attr information [node, 3]
#     edge_attr = torch.tensor(
#         edge_df.iloc[:, 2:].values.astype(np.float), dtype=torch.float)
#     row, col = edge_index[0], edge_index[1]
#
#     c = node_df.loc[:, node_df.columns[1:3]].values.astype(np.float)
#     x = torch.tensor(c)
#
#     # print("Shape of nodes:", x.shape)  # (N,2), coordinations
#
#     graph = Data(x=x, edge_index=edge_index, edge_attr=edge_attr)
#     # print("directed graph: ", graph)
#     out = to_undirected(edge_index, graph.edge_attr)
#     graph.edge_index, graph.edge_attr = out
#     # print("undirected graph: ", graph)
#     # print('==============================================================')
#
#     # Gather some statistics about the graph.
#     # print(f'Number of nodes: {graph.num_nodes}')
#     # print(f'Number of edges: {graph.num_edges}')
#     # print(f'Average node degree: {graph.num_edges / graph.num_nodes:.2f}')
#     # print(f'Contains isolated nodes: {graph.contains_isolated_nodes()}')
#     # print(f'Contains self-loops: {graph.contains_self_loops()}')
#     # print(f'Is undirected: {graph.is_undirected()}')
#
#     # print('==============================================================')
#
#     if logger is not None:
#         logger.info("Shape of nodes: %s", x.shape)  # (N,2), coordinations
#         logger.info("directed graph: %s", graph)
#         logger.info("undirected graph: %s", graph)
#         logger.info(
#             '==============================================================')
#
#         # Gather some statistics about the graph.
#         logger.info(f'Number of nodes: {graph.num_nodes}')
#         logger.info(f'Number of edges: {graph.num_edges}')
#         logger.info(
#             f'Average node degree: {graph.num_edges / graph.num_nodes:.2f}')
#         logger.info(
#             f'Contains isolated nodes: {graph.contains_isolated_nodes()}')
#         logger.info(f'Contains self-loops: {graph.contains_self_loops()}')
#         logger.info(f'Is undirected: {graph.is_undirected()}')
#         logger.info(
#             '==============================================================')
#     return n, graph


# def LoadData(path):
#     graph = LoadGraph()
#
#     inst = []
#
#     train_paths_folder = path
#     onlyfiles = [f for f in listdir(train_paths_folder) if isfile(
#         join(train_paths_folder, f))]
#
#     count = 0
#
#     for log_file in tqdm(onlyfiles):
#         p = readLogs(join(train_paths_folder, log_file))
#         count += 1
#         inst.append(p)
#
#     return inst, graph


class pathObj():
    def __init__(self, src, dest, nodes, paths, costs):
        self.src = src
        self.dest = dest
        self.nodes = list(nodes)
        self.paths = paths
        self.costs = costs

    def getNumberPaths(self):
        return len(self.paths)

    def getAveragePathLength(self):
        l = 0
        for p in self.paths:
            l += len(p)
        return l / self.getNumberPaths()

    def getTotalPathLength(self):
        l = 0
        for p in self.paths:
            l += len(p)
        return l

    def __str__(self):
        return "{}->{}, " \
               "# of nodes in one bbs result: {}, \n" \
               "# of paths: {}, \n" \
               "total # of paths: {}, \n" \
               "average path length: {:.2f}"\
                .format(self.src, self.dest, len(self.nodes),
                    self.getNumberPaths(),
                    self.getTotalPathLength(),
                    self.getAveragePathLength())


def readLogs(filename:str, t_i=-1):
    if t_i != -1 and not filename.endswith("_{}.log".format(t_i)):
        return None

    count = 0
    paths = []
    nodes = set()
    costs = []

    src = 0
    dest = 0

    with open(filename) as fp:
        Lines = fp.readlines()
        src = int(basename(filename).split("_")[1])
        dest = int(basename(filename).split("_")[2])
        # print(src, dest)
        # print(basename(filename))
        # sys.exit()
        for line in Lines:
            count += 1
            # print("Line{}: {}".format(count, line.strip()))

            info = line.strip().split(" ")
            # print(info)
            # print(info[0].replace("-", "").replace(",", "").replace("[", "").split(">"))

            # src = int(info[0].replace("-", "").replace(",",
            #           "").replace("[", "").split(">")[0])
            # dest = int(info[0].replace("-", "").replace(",",
            #            "").replace("[", "").split(">")[1])
            cost = (float(info[1]), float(info[2]),
                    float(info[3].replace("]", "")))

            # print(src, dest, cost)

            nodes_in_path = [int(s.replace("(", "").replace(")", ""))
                             for s in re.findall(reg_exp, line.strip())]
            # print(nodes_in_path)
            len_path = len(nodes_in_path)

            paths.append(nodes_in_path)
            costs.append(cost)

            for n in nodes_in_path:
                nodes.add(n)

    p_inst = pathObj(src, dest, nodes, paths, costs)
    return p_inst
