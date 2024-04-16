import matplotlib.pyplot as plt
import matplotlib
import pandas as pd
import os
import gc
import numpy as np
import random as rd
from matplotlib import cm
import time
from os import path as osp
import sys
import torch
import torch_geometric


def getLocationsInfo(nodeID, node_data):
    row = node_data.loc[node_data[0] == nodeID]
    return [row.iloc[0][1], row.iloc[0][2]]


def draw_raw_graph(graph_folder, savetofolder=False, savedname=None, title_name = None):
    # font = {'family': 'Arial', 'weight': 'normal', 'size': 30}
    # matplotlib.rc('font', **font)
    fig, ax = plt.subplots(figsize=(15, 10))
    # fig, ax = plt.subplots(figsize=(15, 10), dpi=200)

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
    if title_name is not None:
        plt.title(title_name, fontsize = 30)

    if savetofolder:
        fig.tight_layout()
        plt.savefig(savedname,bbox_inches='tight')
    else:
        plt.show()


def draw_graph_partition(data, lables, border_nodes, num_part):
    fig, ax = plt.subplots(figsize=(15, 10), dpi=200)
    magma_cmap = cm.get_cmap("magma", num_part)

    print(data, type(data), isinstance(data, torch_geometric.data.data.Data))
    if not hasattr(data, 'adj'):
        row, col = data.edge_index
    else:
        row, col, _ = data.adj.coo()

    index = 0
    for edge in zip(row, col):
        start_id = edge[0]
        end_id = edge[1]
        start_location = data.x[start_id]
        end_location = data.x[end_id]
        x = [start_location[0], end_location[0]]
        y = [start_location[1], end_location[1]]
        plt.plot(x, y, "-c", lw=0.5)
        index += 1
        if index % 1000 == 0:
            print(index, "-------------")

    # print(data.x[:,1])
    # sys.exit()

    plt.scatter(data.x[:, 0], data.x[:, 1], marker="o",
                c=lables, cmap=magma_cmap, alpha=1)

    for b_id in border_nodes:
        dest_node = data.x[b_id]
        ax.annotate("B", xy=(dest_node[0], dest_node[1]))
    plt.show()


def draw_graph(graph_folder, labels, src, dest):
    fig, ax = plt.subplots(figsize=(15, 10), dpi=200)
    # fig, ax = plt.subplots(figsize=(10,8))
    print(graph_folder)
    ###########################################################################
    node_f_name = graph_folder + "NodeInfo.txt"
    seg_f_name = graph_folder + "SegInfo.txt"
    node_data = pd.read_csv(node_f_name, sep=" ", header=None)

    with open(seg_f_name) as f:
        content = [x.strip("\n") for x in f.readlines()]

    print("There are {} edges in the graph. ".format(len(content)))
    print(len(node_data[0]), " nodes", src, dest)

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
        if index % 1000 == 0:
            print(index, "-------------")

    plt.scatter(node_data[1], node_data[2], marker="o", c=c)

    src_node = node_data.loc[node_data[0] == src]
    dest_node = node_data.loc[node_data[0] == dest]

    plt.scatter(src_node[1], src_node[2], marker="*",
                color="k", alpha=0.99, s=400)
    plt.scatter(dest_node[1], dest_node[2], marker="*",
                color="k", alpha=0.99, s=400)

    plt.show()


def draw_graph_with_DataObj(graph, labels, src, dest, size=(10, 8), dpi=100):
    node_data = graph.x.cpu().detach().numpy()
    # fig, ax = plt.subplots(figsize=(10,8))
    fig, ax = plt.subplots(figsize=size, dpi=dpi)

    print(labels.shape)
    print(node_data[:, 0])
    print(node_data[:, 1])

    # fig, ax = plt.subplots(figsize=(10,8))
    ###########################################################################

    col, row = graph.edge_index
    print(len(col))
    print(len(row))

    print("There are {} edges in the graph. ".format(len(col)))
    print(len(node_data[0]), " nodes", src, dest)
    # print(graph.edge_index)

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
    for edge in zip(col, row):
        # print(edge)
        start_id = edge[0].cpu().detach().item()
        end_id = edge[1].cpu().detach().item()
        # print(start_id, end_id, node_data[start_id], node_data[end_id])
        start_location = node_data[start_id]
        end_location = node_data[end_id]
        x = [start_location[0], end_location[0]]
        y = [start_location[1], end_location[1]]
        plt.plot(x, y, "-c", lw=0.5)
        index += 1
        if index % 1000 == 0:
            print(index, "-------------")

    plt.scatter(node_data[:, 0], node_data[:, 1], marker="o", c=c, alpha=1)

    src_node = node_data[src]
    dest_node = node_data[dest]

    plt.scatter(src_node[0], src_node[1], marker="*",
                color="k", alpha=0.99, s=400)
    plt.scatter(dest_node[0], dest_node[1], marker="*",
                color="k", alpha=0.99, s=400)
    plt.show()


def draw_graph_partition_with_perm_list(graph_path: str, graph_list: list, sub_name: str, p_i, num_parts):
    node_f_name = "{}/NodeInfo_{}_{}.txt".format(graph_path, sub_name, p_i)
    seg_f_name = "{}/SegInfo_{}_{}.txt".format(graph_path, sub_name, p_i)
    magma_cmap = cm.get_cmap("magma", num_parts)

    fig, ax = plt.subplots(figsize=(15, 10), dpi=200)

    node_data = pd.read_csv(node_f_name, sep=" ", header=None)
    with open(seg_f_name) as f:
        content = [x.strip("\n") for x in f.readlines()]
    print("There are {} edges in the graph. ".format(len(content)))

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
    c = np.zeros(len(node_data))
    border_nodes = []
    for sg in graph_list:
        c[sg.perm] = sg.part
        # print(sg.border_idx)
        # print(sg.node_idx.tolist())
        # print([sg.node_idx.tolist().index(b) for b in sg.border_idx])
        border_nodes.extend(
            [sg.perm[sg.node_idx.tolist().index(b)].item() for b in sg.border_idx])
        # print(border_nodes)
    plt.scatter(node_data[1], node_data[2], marker="o", c=c, cmap=magma_cmap)

    for b_id in border_nodes:
        dest_node = getLocationsInfo(b_id, node_data)
        ax.annotate("B", xy=(dest_node[0], dest_node[1]))
    plt.show()


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
