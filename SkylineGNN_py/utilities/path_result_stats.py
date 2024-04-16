import sys
import os
from os import listdir
from os.path import isfile, join

from readData import readLogs
from tqdm import tqdm

# results_log_folder = "/home/gqxwolf/shared_git/BackboneIndex/Data/results_bbs/C9_NY_1K/training_GNN"

# onlyfiles = [join(results_log_folder, f) for f in listdir(results_log_folder) if isfile(join(results_log_folder, f))]



# =========================== bbs ================================

# bbs_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_results_bbs/C9_NY_NONE_15K_test_1000/bbs_log_files.txt"
# my_file = open(bbs_path, "r")
# data = my_file.read()
# onlyfiles = data.split("\n")
# # print(onlyfiles)
# my_file.close()
#
# # # sys.exit(0)
#
# nodes = set()
#
# total_path_len = 0
# total_n_path = 0
#
# for f in onlyfiles:
#     if f != '':
#         # print(f)
#         p = readLogs(f)
#         for n in p.nodes:
#             nodes.add(n)
#         print(p)
#         # print(nodes)
#         # print(f'len(nodes): {len(nodes)}')
#
#         total_path_len += p.getAveragePathLength()
#         total_n_path += p.getNumberPaths()
#
#         # sys.exit(0)
#
# print()
# print(f'avg_path_len_in_bbs: {total_path_len/100}')
# print(f'avg_n_path_in_bbs: {total_n_path/100}')



# =================== backbone===================================

backbone_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_results_gnn_backbone/C9_NY_NONE_15K_TSP/epoch100_query100_128_128_32_1_EmbedTrue_Transformer_ConLossTrue/backbone_only/"
dir_list = os.listdir(backbone_path)
# print(dir_list)


path_cnt = 0;
path_len_cnt = 0;
total_avg_n_node_in_path = 0;

for f in dir_list:
    f_full = backbone_path + f

    with open(f_full, 'r') as fp:

        lines = fp.readlines()
        path_cnt += len(lines)
        total_n_node_in_p = 0

        for l in lines:
            print(l)
            print(l.split('--'))
            total_n_node_in_p += len(l.split('--')) // 2

        print(f'total_n_node_in_p: {total_n_node_in_p}')
        print(f'path_cnt: {path_cnt}')

        total_avg_n_node_in_path += total_n_node_in_p / path_cnt

        sys.exit(0)


print(f'avg_n_path_in_backbone: {path_cnt/100}')
print(f'total_avg_n_node_in_path: {total_avg_n_node_in_path/100}')
