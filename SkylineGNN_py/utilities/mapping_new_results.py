# mapping new skyline paths (nodes order) follows the nodes order of the partititon
from tqdm import tqdm
from os import listdir
from os.path import isfile, join
from readData import readLogs
from pathlib import Path
from readData import LoadGraph
import os
from torch_geometric.data import ClusterData
from clusterTest import *


def writeToDisk(path, log_file):
    folder = osp.split(log_file)[0]
    filename = osp.split(log_file)[1]
    new_log_file = join(folder+"_mapping", filename)
    print(new_log_file)

    for idx, p in enumerate(path.paths):
        output = "{}-->{},[ {} {} {}] ".format(
            path.src, path.dest, path.costs[idx][0], path.costs[idx][1], path.costs[idx][2])
        for i in range(len(p)-1):
            output += '(%s)' % p[i]
        output += '(%s)' % p[len(p)-1]
        print(output)
    return


def mapping_new_paths_after_partition(org_path, node_mapping):
    log_files = [join(org_path, f)
                 for f in listdir(org_path) if isfile(join(org_path, f))]

    for index, f in tqdm(enumerate(log_files)):
        print(index, f)
        p = readLogs(f)

        print(p)

        # print(p.nodes)
        # print(p.paths)

        mapped_nodes = [node_mapping[i] for i in p.nodes]
        mapped_paths = []
        for p_i in p.paths:
            mapped_p = [int(node_mapping[i]) for i in p_i]
            mapped_paths.append(mapped_p)

        p.paths = mapped_paths
        p.nodes = mapped_nodes
        p.src = int(node_mapping[p.src])
        p.dest = int(node_mapping[p.dest])
        writeToDisk(p, f)
        break
    return


if __name__ == '__main__':
    org_path = "/home/gqxwolf/mydata/skylineGNN/data/c9_ny_1k/results_bbs/training_GNN"
    parser = argparse.ArgumentParser(
        description="Pytorch implementation of Skyline GNN")
    parser.add_argument('--graph_folder', default='/home/gqxwolf/mydata/skylineGNN/data/c9_ny_1k',
                        type=dir_path, help='the folder that contains the graph information, node and edge')

    params = parser.parse_args()

    path = params.graph_folder
    num_parts = 5

    cluster_data = load_cluster_data(path, num_parts)
    # print(cluster_data)
    # node_partition, _ = get_node_partitions(cluster_data)
    # print(node_partition)
    # mapping_new_paths_after_partition(org_path, cluster_data)

    perm = cluster_data.perm
    node_partition_0 = np.zeros(cluster_data.num_nodes)
    for i in range(num_parts):
        start = cluster_data.partptr[i]
        end = cluster_data.partptr[i+1]-1
        length = end-start
        parts_idx_list = perm[start:end+1]
        print(length, len(parts_idx_list))
        node_partition_0[parts_idx_list] = i

    node_partition, _ = get_node_partitions(cluster_data)

    # print(node_partition)
    # print(node_partition_0)

    # print(node_partition == node_partition_0)
    org_to_new_id = np.empty(cluster_data.num_nodes)
    for nid, oid in enumerate(perm):
        org_to_new_id[oid] = nid
        # print(nid,oid)
        # if oid.item() in [226,70]:
        #     print(nid, "===>", oid)

    mapping_new_paths_after_partition(org_path, org_to_new_id)

    # num_nodes, graph = LoadGraph(path)
    # draw_graph_partition(graph, node_partition_0, num_parts)
    # draw_graph_partition(cluster_data.data, node_partition, num_parts)
