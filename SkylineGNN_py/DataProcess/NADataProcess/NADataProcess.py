from math import cos
import os
from typing import ChainMap
from .node import nodeObj
import numpy as np
import sys


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


def getSTD(content):
    print("==========getSTD()===========")

    mapping = set()
    cost_list = []
    for line in content:
        src_id = int(line.split(" ")[1])
        dest_id = int(line.split(" ")[2])
        if (src_id, dest_id) not in mapping and (dest_id, src_id) not in mapping:
            travel_time = float(line.split(" ")[3])
            cost_list.append(travel_time)
            mapping.add((src_id, dest_id))
    value_array = np.array(cost_list)
    std_value = value_array.std()
    max_value = value_array.max()
    min_value = value_array.min()
    mean_value = value_array.mean()
    print(f'cost_list: {cost_list}')
    return std_value, max_value, min_value, mean_value, len(cost_list)


def na_data_process(dataname, basepath, r_type, num_dim):
    print("==========na_data_process()===========")

    path = basepath + "/L_" + dataname + "/"
    output_folder = path + r_type + "_output/"

    node_path = path + dataname + ".cnode.txt"
    edge_path = path + dataname + ".cedge.txt"
    # node_path = path + dataname.lower() + ".cnode.txt"
    # edge_path = path + dataname.lower() + ".cedge.txt"
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

    print("There are {} number of nodes ".format(len(content)))

    nodeList = []
    for line in content:
        node_id = (int)(line.split(" ")[0])
        node_lat = (float)(line.split(" ")[1]) / 100
        node_lng = (float)(line.split(" ")[2]) / 100
        n_node = nodeObj(node_id, node_lat, node_lng)
        nodeList.append(n_node)

    file = open(new_node_file, "w")
    for node in nodeList:
        line = "{} {} {}\n".format(node.id, node.lat, node.lng)
        file.write(line)
    file.close()

    content = []
    with open(edge_path) as f:
        content = f.readlines()

    content = [x.strip() for x in content]
    print(content[0])

    sampled_arrays = []
    for i in range(num_dim):
        if r_type == "corr":
            cost1_std, max_v, min_v, mean_v, n_edges = getSTD(content)
            sampled_array = non_neg_normal_sample(
                mean_v, cost1_std, max_v, min_v, 200, 30, 5000)
            sampled_arrays.append(sampled_array)
        elif r_type == "anti":
            cost1_std, max_v, min_v, mean_v, n_edges = getSTD(content)
            sampled_array = non_neg_normal_sample(
                mean_v, cost1_std, max_v, min_v, 200, -30, 5000)
            sampled_arrays.append(sampled_array)

    # print(sampled_array)
    # print(sampled_array.shape)

    mapping = set()
    undirect_edges = 0
    line_number = 0
    file = open(new_edge_file, "w")
    for line in content:
        src_id = int(line.split(" ")[1])
        dest_id = int(line.split(" ")[2])

        if line_number % 50000 == 0:
            print("{} ............".format(line_number))

        # if line_number == 3:
        #     sys.exit(0)

        if (src_id, dest_id) not in mapping and (dest_id, src_id) not in mapping:

            mapping.add((src_id, dest_id))
            undirect_edges += 1
            travel_time = float(line.split(" ")[3])

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

    return


def non_neg_normal_sample(mean_dist, std_dist, max_v, min_v, mean_cost, corr, num_sample, max_iters=1000):
    """ Obtain a non-negative sample from a normal distribution"""

    sampled_array = np.empty([num_sample, 2])

    mu = np.array([mean_dist, mean_cost])
    r = np.array([
        [std_dist**2,  std_dist * 15 * corr],
        [std_dist * 15 * corr, 15**2]
    ])

    iter = 0
    sample_num = 0
    while sample_num < num_sample:
        sample = np.random.multivariate_normal(mu, r)
        iter += 1

        if 0 < sample[1] and 0 < sample[0]:
            if min_v < sample[0] and sample[0] < max_v:
                sampled_array[sample_num][0] = sample[0]
                sampled_array[sample_num][1] = int(sample[1])
                sample_num += 1

    return sampled_array


def nn_sample_value(travel_time, sampled_array):
    # print("-------------nn_sample_value()------------------")
    sorted_array = np.array(sampled_array)
    # print(sorted_array)
    sorted_array[:, 0] -= travel_time
    # print(sorted_array)
    sorted_array = sorted_array[np.argsort(np.abs(sorted_array[:, 0]))]
    # print(f'sorted_array \n{sorted_array}')
    sliced_array = sorted_array[:5, :]
    # print(f'sliced_array \n{sliced_array}')
    cost = int(np.average(sliced_array[:, 1]))
    # print("---------------------------------------------")
    return cost
