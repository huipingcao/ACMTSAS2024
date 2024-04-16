from pathlib import Path

from torch._C import dtype
from readData import LoadData, LoadGraph, LoadSubGraph
import argparse
import os
from torch_geometric.data import ClusterData, ClusterLoader, NeighborSampler, cluster
from os import listdir
from os import path as osp
import torch
import numpy as np
from drawRawGraphs import draw_graph_partition,draw_graph_partition_with_perm_list
from torch_geometric.data import Data
import copy


def dir_path(path):
    Path(path).mkdir(parents=True, exist_ok=True)
    if os.path.isdir(path):
        return path
    else:
        raise argparse.ArgumentTypeError(
            f"readable_dir:{path} is not a valid path")


def load_cluster_data(path, num_parts=10):
    cluster_output_folder = osp.join(path, "clusters")
    Path(cluster_output_folder).mkdir(parents=False, exist_ok=True)
    num_nodes, graph = LoadGraph(path)

    print(graph)
    print(cluster_output_folder)
    # sys.exit(0)

    for f in listdir(cluster_output_folder):
        del_file = osp.join(cluster_output_folder, f)
        try:
            os.remove(del_file)
            print("removed existed cluster file, %s ." % (del_file))
        except OSError as e:
            print("Error: %s : %s" % (f, e.strerror))

    cluster_data = ClusterData(
        graph, num_parts=num_parts, recursive=False, save_dir=cluster_output_folder)
    # print(cluster_data)
    # sys.exit(0)

    cluster_data.num_nodes = num_nodes
    return cluster_data


def get_node_partitions(data):
    row, col, _ = data.data.adj.coo()
    print(data.data.adj)
    # print(row, len(row))
    # print(col, len(row))

    node_partition = np.zeros(data.num_nodes)
    edge_partition = []

    for i in range(0, len(data.partptr)-1):
        print(i, "===========================")
        start = data.partptr[i]
        end = data.partptr[i+1]-1

        # row and col of the edges that in the same partition
        edges_in_part_row_list = []
        edges_in_part_col_list = []
        for idx, d in enumerate(zip(row, col)):
            r = d[0].item()
            c = d[1].item()
            if (start <= r & r <= end) & (start <= c & c <= end):
                edges_in_part_row_list.append(r)
                edges_in_part_col_list.append(c)

            # if idx == 10:
            #     break

        edges_in_part_row = torch.from_numpy(
            np.asarray(edges_in_part_row_list))
        edges_in_part_col = torch.from_numpy(
            np.asarray(edges_in_part_col_list))

        edge_index = torch.stack((edges_in_part_row, edges_in_part_col), dim=0)

        distinct_nodes = set(edges_in_part_row_list)
        distinct_nodes.update(edges_in_part_col_list)
        distinct_nodes = list(distinct_nodes)
        node_partition[distinct_nodes] = i
        edge_partition.append(edge_index)
    print("====================================")

    border_nodes = []
    for idx, d in enumerate(zip(row, col)):
        r = d[0].item()
        c = d[1].item()
        if node_partition[r] != node_partition[c]:
            border_nodes.append(r)
            border_nodes.append(c)

    distinct_nodes = set(border_nodes)
    border_nodes = list(distinct_nodes)

    # node_partition[border_nodes] = 15
    print("number of border nodes:", len(border_nodes))

    return node_partition, edge_partition, border_nodes


def _load_cluster_data(path, sub_name, t_i, num_parts):
    cluster_output_folder = osp.join(path, "clusters")
    Path(cluster_output_folder).mkdir(parents=False, exist_ok=True)
    num_nodes, graph = LoadSubGraph(path, sub_name, t_i)
    print(num_nodes, graph)
    print(cluster_output_folder)

    for f in listdir(cluster_output_folder):
        del_file = osp.join(cluster_output_folder, f)
        try:
            os.remove(del_file)
            print("removed existed cluster file, %s ." % (del_file))
        except OSError as e:
            print("Error: %s : %s" % (f, e.strerror))

    cluster_data = ClusterData(
        graph, num_parts=num_parts, recursive=False, save_dir=cluster_output_folder)
    # print(cluster_data)
    # sys.exit(0)

    cluster_data.num_nodes = num_nodes
    return num_nodes, cluster_data


def _extractSubGraphObj(sub_graph: Data,num_parts):
    sub_partitions = []

    node_partition = np.zeros(sub_graph.num_nodes)

    N = sub_graph.data.num_nodes
    E = sub_graph.data.num_edges

    print(N, E)

    for i in range(num_parts):
        start = sub_graph.partptr[i].tolist()
        end = sub_graph.partptr[i + 1].tolist()
    
        node_idx = torch.arange(start, end)
        data = copy.copy(sub_graph.data)
        if hasattr(data, '__num_nodes__'):
            del data.__num_nodes__
        
        adj, data.adj = sub_graph.data.adj, None
        adj = adj.narrow(0, start, end - start) 
        adj = adj.index_select(1, node_idx)
        row, col, edge_idx = adj.coo()
        
        data.edge_index = torch.stack([row, col], dim=0)
        data.node_idx = torch.tensor(node_idx)
        data.perm = sub_graph.perm[start:end]
        data.part = i
        node_partition[node_idx] = i

        for key, item in data:
            if isinstance(item, torch.Tensor) and item.size(0) == N:
                data[key] = item[node_idx]
            elif isinstance(item, torch.Tensor) and item.size(0) == E:
                data[key] = item[edge_idx]
            else:
                data[key] = item
        print(i, data)
        sub_partitions.append(data)

    row, col, _ = sub_graph.data.adj.coo()
    border_nodes = []
    for idx, d in enumerate(zip(row, col)):
        r = d[0].item()
        c = d[1].item()
        if node_partition[r] != node_partition[c]:
            border_nodes.append(r)
            border_nodes.append(c)

    border_nodes = np.unique(border_nodes)
    print("number of border nodes : ", len(border_nodes))

    for i in range(num_parts):
        sg = sub_partitions[i]
        border_idx = [b_idx for b_idx in border_nodes if b_idx in sg.node_idx]
        border_mask = torch.zeros(len(sg.node_idx), dtype=torch.bool)
        index = [sg.node_idx.tolist().index(b) for b in border_idx]
        border_mask[index] = True
        # print("partition {} has {} nodes with {} border nodes".format(i,len(sub_graph_obj.node_idx), len(border_idx)))
        sg.border_idx = border_idx
        sg.border_mask = border_mask

    for i in range(num_parts):
        print(sub_partitions[i])

    # sys.exit(0)
    return sub_partitions


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Pytorch implementation of Skyline GNN")
    parser.add_argument('--graph_folder', default='/home/gqxwolf/mydata/projectData/skylineGNN/dataset/C9_NY_NONE/C9_NY_NONE_5K/1',
                        type=dir_path, help='the folder that contains the graph information, node and edge')

    params = parser.parse_args()

    path = params.graph_folder
    num_parts = 10
    num_nodes = 5000

    # cluster_data = load_cluster_data(path, num_parts)
    # node_partition, _, border_nodes = get_node_partitions(cluster_data)
    # draw_graph_partition(cluster_data.data, node_partition, border_nodes, num_parts)

    num_node_i, sub_graph_i = _load_cluster_data(path, "5K", 13, num_parts)
    sub_partitions = _extractSubGraphObj(sub_graph_i, num_parts)
    draw_graph_partition_with_perm_list(path, sub_partitions, '5K', 13, num_parts)
