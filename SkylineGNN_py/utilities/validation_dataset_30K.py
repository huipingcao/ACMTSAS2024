from sklearn.metrics import f1_score,precision_score, recall_score
from sklearn.utils import shuffle
from tqdm import tqdm
import sys
import os
import torch
from torch.nn import CrossEntropyLoss, BCELoss, BCEWithLogitsLoss
from torch_geometric.data import DataLoader
# from torch_geometric.loader import DataLoader
import numpy as np
import os
import torch.nn.functional as F
import time
from torch_geometric.utils import subgraph, k_hop_subgraph
import math
from torch_geometric.utils.convert import to_networkx
import networkx as nx
import datetime

# import type of the models
# sys.path.append('/home/gqxwolf/mydata/GNN_Combinatroal/skylineGNN/Base001')
sys.path.append('/home/gqxwolf/mydata/GNN_Combinatroal/skylineGNN/validation')
sys.path.append('/home/gqxwolf/mydata/GNN_Combinatroal/skylineGNN/utilities')

from drawRawGraphs import draw_graph_with_DataObj
from model import GraphEncoder
from readData import LoadData, LoadGraph
from MultiCostNetworksORG import MultiCostNetworksORG
from readCheckPoints import getMaxCheckPointName

# level_str = 'level9'

# Paths stores the information of graphs, the paths, the process dataset files, and the models
# path = "/home/gqxwolf/mydata/projectData/skylineGNN/dataset/C9_NY_CORR/C9_NY_CORR_30K/processed/"+level_str
# train_paths_folder = "/home/gqxwolf/mydata/projectData/skylineGNN/results_back/C9_NY_CORR_30K/processed/"+level_str+"/results_bbs"
# check_point_file = '/home/gqxwolf/mydata/projectData/skylineGNN/models/C9_NY_CORR_30K/processed/level9/2/256_256_32_1000_1_EmbedTrue_5473_train_checkpoint_GCN_ConLossFalse_2GNN3FF_10k_train_samples.pt'
# data_folder = "/home/gqxwolf/mydata/projectData/skylineGNN/processed/C9_NY_CORR_30K/processed/"+level_str+"/2"

# print(path)
# print(train_paths_folder)

# check_point_file = '/home/gqxwolf/mydata/projectData/skylineGNN/models/C9_BAY_100K_Level8_Processed/256_256_32_1000_1_EmbedTrue_15588_train_checkpoint_SAGE_ConLossFalse_2GNN3FF_10k_train_samples.pt'
# model_name = "SAGE"
# n_nodes = 100000

# print(data_folder)

check_point_file = '/home/gqxwolf/mydata/projectData/skylineGNN/models/C9_BAY_80K_Level7_Processed/256_256_32_1000_1_EmbedTrue_13624_train_checkpoint_SAGE_ConLossFalse_2GNN3FF_10k_train_samples.pt'
model_name = "SAGE"
n_nodes = 80000
print(check_point_file)
# org_path = "/home/gqxwolf/mydata/projectData/skylineGNN/dataset/C9_BAY/C9_BAY_100K"
# org_train_paths_folder = "/home/gqxwolf/mydata/projectData/skylineGNN/results_back/C9_BAY/C9_BAY_100K/results_unfinished"
# org_data_folder = "/home/gqxwolf/mydata/projectData/skylineGNN/processed/C9_BAY_100K/2"
# save_folder = "/home/gqxwolf/mydata/projectData/skylineGNN/Mapped/C9_BAY/C9_BAY_100K/unfinished"


org_path = "/home/gqxwolf/mydata/projectData/skylineGNN/dataset/C9_BAY/C9_BAY_80K"
org_train_paths_folder = "/home/gqxwolf/mydata/projectData/skylineGNN/results_back/C9_BAY/C9_BAY_80K/results_bbs"
org_data_folder = "/home/gqxwolf/mydata/projectData/skylineGNN/processed/C9_BAY_80K/1"
save_folder = "/home/gqxwolf/mydata/projectData/skylineGNN/Mapped/C9_BAT_80K"


os.makedirs(save_folder, exist_ok=True)

# Parameters
node_dim = 2
edge_dim = 3
embedding_dim = 256
hidden_dim = 256
batch_size = 32
enable_embed = True
output_dim = 3
heads = 1
conn_loss_enable = False
enable_edge_attr = False
save_mapping_file_path = "{}/{}_{}_{}_{}_Embed{}_{}_ConLoss{}.mapping".format(save_folder, embedding_dim, hidden_dim, batch_size, heads, enable_embed, model_name, conn_loss_enable)

device = torch.device('cpu')
print(device)

# #load dataloader
# num_node, graph = LoadGraph(path, logger=None, normalization_node=False, normalization_edge=True)
# graph_size = num_node
# print("==========================================")
# # train_dataset = MultiCostNetworks(graph, train_paths_folder=train_paths_folder, root=data_folder, split='train')
# test_dataset = MultiCostNetworks(graph, train_paths_folder=train_paths_folder, root=data_folder, split='test')
# # train_loader = DataLoader(train_dataset, drop_last=True, batch_size=batch_size)
# test_loader = DataLoader(test_dataset, drop_last=True, batch_size=batch_size)

# initialize model
model = GraphEncoder(node_dim, edge_dim, embedding_dim, hidden_dim, output_num_class=output_dim, device=device, enable_embedding=enable_embed,
                     batch=batch_size, model_name=model_name, heads=heads)
optimizer = torch.optim.Adam(params=model.parameters(), lr=0.0001)

checkpoint = torch.load(check_point_file, map_location=torch.device('cpu'))
model.load_state_dict(checkpoint['model_state_dict'])
resume_epoch = checkpoint['epoch']
loss = checkpoint['loss']

# test function
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
    # print(data)
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
    print(data.num_graphs, total_n, total_pre_n)

    # if n!=0:
    #     print(count, np.argmax(logits.cpu().numpy(),axis=1), n )
    #     sys.exit()

    return total_loss / data.num_graphs, total_nll_lost/data.num_graphs, total_conn_lost/data.num_graphs, total_pre_n / total_graphs, total_n / total_graphs, logits


# n = graph_size
# print(graph)


if os.path.exists(save_mapping_file_path):
    os.remove(save_mapping_file_path)
    print("removed existing file:", save_mapping_file_path)
else:
    print(save_mapping_file_path, "The file does not exist")

# check_point_file = '/home/gqxwolf/mydata/projectData/skylineGNN/models/C9_NY_CORR_30K/GCN_models/1/256_256_32_1000_1_EmbedTrue_30000_train_checkpoint_SAGE_ConLossTrue_2GNN3FF_10k_train_samples.pt'

print(org_path)
print(org_train_paths_folder)
print(org_data_folder)

org_num_node, org_graph = LoadGraph(org_path, logger=None, normalization_node=True, normalization_edge=True)
# print(org_num_node)
# print(org_graph)
print("==========================================")

# org_train_dataset = MultiCostNetworks(org_graph, train_paths_folder=org_train_paths_folder, root=org_data_folder, split='train')
org_test_dataset  = MultiCostNetworksORG(org_graph, train_paths_folder=org_train_paths_folder, root=org_data_folder)
# org_train_loader  = DataLoader(org_train_dataset, drop_last=True, batch_size=batch_size, shuffle=True)
org_test_loader   = DataLoader(org_test_dataset, drop_last=False, batch_size=batch_size, shuffle=True)

n_graph = to_networkx(org_graph, to_undirected=True, remove_self_loops=False)
n = org_num_node
graph_size = org_num_node

count=0
exit_loop = False

with open(save_mapping_file_path, 'a') as f:
    # for index, test_data in enumerate(org_train_loader):
    #     if exit_loop:
    #         break
    #     start_time = time.time()
    #     val_auc, test_nll_loss, test_conn_loss, test_p_n, test_n, logits = test(test_data)
    #     end_time = time.time()
    #     print("{}, Val:{:.4f}({:.5f}+{:.5f})[{:.5f} - {:.5f}], running time each epoch:{:.2f}".format(
    #         index, val_auc, test_nll_loss, test_conn_loss, test_p_n, test_n, (end_time - start_time)))
    #     model_exectue_time = end_time - start_time

    #     for i in range(batch_size):
    #         count+=1
    #         # if count == 500:
    #         #     exit_loop=True
    #         #     break
    #         start_time = time.time()
    #         predictions = np.argmax(
    #             logits[i*n:(i+1)*n, :].cpu().detach().numpy(), axis=1)
    #         data_y = test_data.y.cpu().detach().numpy()[i*n:(i+1)*n]
    #         pre_n_i = (predictions == 1).sum() + (predictions == 2).sum()
    #         total_n_i = (data_y == 1).sum()+(data_y == 2).sum()
    #         src = test_data.src_node_idx[i].item()
    #         dest = test_data.dest_node_idx[i].item()

    #         # Find connected sub_grap
    #         target_node_list = np.where(predictions == 1)[0].tolist()
    #         sub_node_set = set()

    #         for idx, node in enumerate(target_node_list):
    #             s_path = nx.dijkstra_path(n_graph, node, src)
    #             d_path = nx.dijkstra_path(n_graph, node, dest)
    #             # print(idx, ":", node, len(s_path), len(d_path))
    #             sub_node_set.update(s_path)
    #             sub_node_set.update(d_path)
    #         sub_node_set.add(src)
    #         sub_node_set.add(dest)
    #         end_time = time.time()
    #         sub_graph_finding_time = end_time-start_time

    #         m_data_y = np.zeros(n, dtype=bool)
    #         m_data_y[np.nonzero(data_y)] = True
    #         m_predict_y = np.zeros(n, dtype=bool)
    #         m_predict_y[np.nonzero(predictions)] = True
    #         m_sub_predict_y = np.zeros(n, dtype=bool)
    #         m_sub_predict_y[list(sub_node_set)] = True

    #         f1_score_1 = f1_score(m_data_y, m_predict_y)
    #         f1_score_2 = f1_score(data_y, predictions, average='micro')
    #         f1_score_3 = f1_score(data_y, predictions, average='macro')
    #         # f1_score_4 = f1_score(data_y, predictions, average='none')
    #         f1_score_1_s = f1_score(m_data_y, m_sub_predict_y)

    #         # print(f1_score_1, f1_score_1_s, f1_score_2, f1_score_3 , np.count_nonzero(m_data_y),  np.count_nonzero(m_predict_y), len(sub_node_set))
    #         # sys.exit(0)

    #         pre_score = precision_score(m_data_y, m_predict_y)
    #         pre_score_s = precision_score(m_data_y, m_sub_predict_y)

    #         rec_score = recall_score(m_data_y, m_predict_y)
    #         rec_score_s = recall_score(m_data_y, m_sub_predict_y)

    #         print("{}  {}-{}: {}->{} {} / {} ===== {}, sub_graph size : {}, time spend {:.6f} f1:{:.6f},f1_sub:{:.6f}, f1_micro:{:.6f}, f1_macro:{:.6f}, pre:{:.4f}, pre_sub:{:.4f}, recall:{:.4f}, recall_sub:{:.4f}"
    #         .format(count, index, i,src, dest, pre_n_i, total_n_i, float(pre_n_i/total_n_i), len(sub_node_set), sub_graph_finding_time+model_exectue_time, 
    #         f1_score_1, f1_score_1_s, f1_score_2, f1_score_3,pre_score,pre_score_s,rec_score,rec_score_s))
    #         line = "{} {} {} {:.6f} {} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f}\n".format(int(src), int(
    #             dest), sub_node_set, sub_graph_finding_time+model_exectue_time, target_node_list, f1_score_1, f1_score_1_s, f1_score_2, f1_score_3,pre_score,pre_score_s,rec_score,rec_score_s)
    #         f.write(line)

    for index, test_data in enumerate(org_test_loader):
        # if exit_loop:
        #     break
        start_time = time.time()
        val_auc, test_nll_loss, test_conn_loss, test_p_n, test_n, logits = test(test_data)
        end_time = time.time()
        print("{}, Val:{:.4f}({:.5f}+{:.5f})[{:.5f} - {:.5f}], running time each epoch:{:.2f}".format(
            index, val_auc, test_nll_loss, test_conn_loss, test_p_n, test_n, (end_time - start_time)))
        model_exectue_time = end_time - start_time

        for i in range(batch_size):
            count+=1
            print(datetime.datetime.now())

            # if count == 500:
            #     exit_loop=True
            #     break
            start_time = time.time()
            predictions = np.argmax(
                logits[i*n:(i+1)*n, :].cpu().detach().numpy(), axis=1)
            data_y = test_data.y.cpu().detach().numpy()[i*n:(i+1)*n]
            pre_n_i = (predictions == 1).sum() + (predictions == 2).sum()
            total_n_i = (data_y == 1).sum()+(data_y == 2).sum()
            src = test_data.src_node_idx[i].item()
            dest = test_data.dest_node_idx[i].item()

            # Find connected sub_grap
            target_node_list = np.where(predictions == 1)[0].tolist()
            sub_node_set = set()

            for idx, node in enumerate(target_node_list):
                s_path = nx.dijkstra_path(n_graph, node, src)
                d_path = nx.dijkstra_path(n_graph, node, dest)
                # print(idx, ":", node, len(s_path), len(d_path))
                sub_node_set.update(s_path)
                sub_node_set.update(d_path)
            sub_node_set.add(src)
            sub_node_set.add(dest)
            end_time = time.time()
            sub_graph_finding_time = end_time-start_time

            m_data_y = np.zeros(n, dtype=bool)
            m_data_y[np.nonzero(data_y)] = True
            m_predict_y = np.zeros(n, dtype=bool)
            m_predict_y[np.nonzero(predictions)] = True
            m_sub_predict_y = np.zeros(n, dtype=bool)
            m_sub_predict_y[list(sub_node_set)] = True

            f1_score_1 = f1_score(m_data_y, m_predict_y)
            f1_score_2 = f1_score(data_y, predictions, average='micro')
            f1_score_3 = f1_score(data_y, predictions, average='macro')
            # f1_score_4 = f1_score(data_y, predictions, average='none')
            f1_score_1_s = f1_score(m_data_y, m_sub_predict_y)

            pre_score = precision_score(m_data_y, m_predict_y)
            pre_score_s = precision_score(m_data_y, m_sub_predict_y)

            rec_score = recall_score(m_data_y, m_predict_y)
            rec_score_s = recall_score(m_data_y, m_sub_predict_y)
            print("{}  {}-{}: {}->{} {} / {} ===== {}, sub_graph size : {}, time spend {:.6f} f1:{:.6f},f1_sub:{:.6f}, f1_micro:{:.6f}, f1_macro:{:.6f}, pre:{:.4f}, pre_sub:{:.4f}, recall:{:.4f}, recall_sub:{:.4f}"
            .format(count, index, i,src, dest, pre_n_i, total_n_i, float(pre_n_i/total_n_i), len(sub_node_set), sub_graph_finding_time+model_exectue_time, 
            f1_score_1, f1_score_1_s, f1_score_2, f1_score_3,pre_score,pre_score_s,rec_score,rec_score_s))
            line = "{} {} {} {:.6f} {} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f}\n".format(int(src), int(
                dest), sub_node_set, sub_graph_finding_time+model_exectue_time, target_node_list, f1_score_1, f1_score_1_s, f1_score_2, f1_score_3,pre_score,pre_score_s,rec_score,rec_score_s)
            f.write(line)
