
from math import cos
import os
from typing import ChainMap
import numpy as np
import sys
from NADataProcess.node import nodeObj
from NADataProcess.NADataProcess import non_neg_normal_sample, getSTD, nn_sample_value


def cleanFolderFiles(output_folder, node_file, edge_file):
    if not os.path.exists(output_folder):
        os.mkdir(output_folder)
        print("Created the output folder :  {}".format(output_folder))
    else:
        print("The folder exists, {}".format(output_folder))

    if os.path.exists(node_file):
        os.remove(node_file)
        print("{} was removed".format(node_file))
    else:
        print("The file does not exist and create it later, {}".format(node_file))

    if os.path.exists(edge_file):
        os.remove(edge_file)
        print("{} was removed".format(edge_file))
    else:
        print("The file does not exist and create it later, {}".format(edge_file))


def c9_data_process(dataname, basepath, r_type, num_dim):
    path = basepath + "/C9_" + dataname + "/"
    output_folder = path + r_type + "_output/"
    node_path = path + "USA-road-d." + dataname + ".co"
    edge_path = path + "USA-road-d." + dataname + ".gr"
    new_node_file = output_folder + dataname + "_" + r_type + "_NodeInfo.txt"
    new_edge_file = output_folder + dataname + "_" + r_type + "_SegInfo.txt"

    print(path)
    print(output_folder)
    print(node_path)
    print(edge_path)
    print(new_node_file)
    print(new_edge_file)

    cleanFolderFiles(output_folder, new_node_file, new_edge_file)

    # read the node information
    with open(node_path) as f:
        content = f.readlines()

    content = content[7:]
    content = [x.strip() for x in content]

    print("Sample of the format of the node : {} ".format(content[0]))

    nodeList = {}
    for line in content:
        node_id = (int)(line.split(" ")[1]) - 1
        node_lat = (float)(line.split(" ")[3]) / 1000000
        node_lng = (float)(line.split(" ")[2]) / 1000000
        # print("{} {} {}".format(node_id,node_lat,node_lng))
        nodeList[node_id] = nodeObj(node_id, node_lat, node_lng)

    print("There are {} # of nodes ".format(len(nodeList)))

    file = open(new_node_file, "w")
    for node_id, node in nodeList.items():
        line = "{} {} {}\n".format(node.id, node.lat, node.lng)
        file.write(line)
    file.close()

    content = []
    with open(edge_path) as f:
        content = f.readlines()

    content = content[7:]
    content = [x.strip() for x in content]

    print("Sample of the format of the edges: {} ".format(content[0]))

    sampled_arrays = []
    for i in range(num_dim):
        if r_type == "corr":
            cost1_std, max_v, min_v, mean_v, n_edges = getSTD(content)
            sampled_array = non_neg_normal_sample(
                mean_v, cost1_std, max_v, min_v, 200, 30, 10000)
            sampled_arrays.append(sampled_array)
        elif r_type == "anti":
            cost1_std, max_v, min_v, mean_v, n_edges = getSTD(content)
            sampled_array = non_neg_normal_sample(
                mean_v, cost1_std, max_v, min_v, 200, -30, 10000)
            sampled_arrays.append(sampled_array)

    mapping = set()
    undirect_edges = 0
    line_number = 0
    file = open(new_edge_file, "w")
    for line in content:
        src_id = (int)(line.split(" ")[1]) - 1
        dest_id = (int)(line.split(" ")[2]) - 1

        if line_number % 50000 == 0:
            print("{} ............".format(line_number))

        if (src_id, dest_id) not in mapping and (dest_id, src_id) not in mapping:
            mapping.add((src_id, dest_id))
            undirect_edges += 1
            travel_time = (int)(line.split(" ")[3])
            dim_costs = []
            if r_type == "none":
                for d_i in range(num_dim):
                    cost = np.random.randint(0, 100 + 1)
                    dim_costs.append(cost)
            elif r_type == "corr" or r_type == "anti":
                for d_i in range(num_dim):
                    # print(travel_time)
                    cost = nn_sample_value(travel_time, sampled_arrays[d_i])
                    # print(cost)
                    dim_costs.append(cost)
                    # print("===========================")
            line = "{} {} {}".format(src_id, dest_id, travel_time)
            for c in dim_costs:
                line += " " + str(c)
            line += "\n"
            file.write(line)

        line_number += 1

    file.close()
    print("# of un-directional edges : {}     # of directed edges : {}  ".format(undirect_edges, line_number))
