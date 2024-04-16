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
from sklearn.metrics import f1_score,precision_score, recall_score, roc_auc_score
import warnings
warnings.simplefilter(action='ignore')

"""
Functionality: Test performance of a pre-trained GNN model
Input: 1) graphinfo contains node info and edge into 2) folder path contains bbs results_bbs
3) full path of one checkpoint file containing a pre-trains GNN model  
4) processed data folder contains graph info where nodes annotated with 0,1,2 (stored train and test data splits)
5) output folder for saving .mapping files
Output: a .mapping file with the prediction performance on the testing data
This file contains multiple rows, and each row is for one testing query 
For each row the format is src_node_id, des_node_id, {ids of nodes forming a subgraph defining the search space of this query},
and 10 metrics in the range of 0 to 1: f1 , f1-micro, f1-macro precision, recall, f1, and roc_auc, f1-micro, f1-macro precision, recall, roc_auc on dijkstra_path
"""


# import type of the models
# sys.path.append('/home/gqxwolf/mydata/GNN_Combinatroal/skylineGNN/Base001')
# sys.path.append('/home/gqxwolf/mydata/GNN_Combinatroal/skylineGNN/validation')
# sys.path.append('/home/gqxwolf/mydata/GNN_Combinatroal/skylineGNN/utilities')
sys.path.append('/home/hchen/PycharmProjects/ICDE23/GNNquery/SkylineGNN_py/validation')
sys.path.append('/home/hchen/PycharmProjects/ICDE23/GNNquery/SkylineGNN_py/utilities')


from drawRawGraphs import draw_graph_with_DataObj
from model import GraphEncoder
from readData import LoadData, LoadGraph
from MultiCostNetworks import MultiCostNetworks
from readCheckPoints import getMaxCheckPointName

debug = 0


# Paths stores the information of graphs, the paths, the process dataset files, and the models
# check_point_file = '/home/gqxwolf/mydata/projectData/skylineGNN/models/C9_NY_15K/GCN_models/1/128_128_32_1000_1_EmbedTrue_15000_train_checkpoint_GCN_ConLossFalse_2GNN3FF_10k_train_samples.pt'
# path = "/home/gqxwolf/mydata/projectData/skylineGNN/dataset/C9_NY_15K/"
# train_paths_folder = "/home/gqxwolf/mydata/projectData/skylineGNN/results_back/C9_NY_15K/results_bbs"
# data_folder = "/home/gqxwolf/mydata/projectData/skylineGNN/processed/C9_NY_15K/1"
# save_folder = "/home/gqxwolf/mydata/projectData/skylineGNN/Mapped/C9_NY_15K"

path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_NA/L_NA_NONE"
train_paths_folder = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/results_bbs/L_NA_NONE_10000"
data_folder = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/L_NA/L_NA_NONE"
save_folder = "/home/hchen/PycharmProjects/ICDE23/GNNquery/Data/mapped/L_NA_NONE"
checkpoint_folder = '/home/hchen/PycharmProjects/ICDE23/GNNquery/SkylineGNN_py/checkpoints/L_NA_NONE/'
check_point_file = checkpoint_folder + '128_128_16_500_1_EmbedTrue_175813_train_checkpoint_Transformer_ConLossTrue_2GNN3FF_train_samples.pt'

os.makedirs(save_folder, exist_ok=True)

print(path)
print(train_paths_folder)
print(check_point_file)
print(data_folder)

model_name = "Transformer"
n_nodes = 175813
embedding_dim = 128
hidden_dim = 128

# Parameters
node_dim = 2
edge_dim = 3
batch_size = 8
enable_embed = True
output_dim = 3
heads = 1
conn_loss_enable = True
enable_edge_attr = True

device = torch.device('cpu')
print(device)


# load dataloader
num_node, graph = LoadGraph(
    path, logger=None, normalization_node=True, normalization_edge=True)
graph_size = num_node
print("======================================================")
# train_dataset = MultiCostNetworks(graph, train_paths_folder=train_paths_folder, root=data_folder, split='train')
test_dataset = MultiCostNetworks(graph, train_paths_folder=train_paths_folder, root=data_folder, split='test')
# train_loader = DataLoader(train_dataset, drop_last=True, batch_size=batch_size)
test_loader = DataLoader(test_dataset, drop_last=True, batch_size=batch_size)



# initialize model
model = GraphEncoder(node_dim, edge_dim, embedding_dim, hidden_dim, output_num_class=output_dim, device=device, enable_embedding=enable_embed,
                     batch=batch_size, model_name=model_name, heads=heads).to(device)
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
    if debug:
        print(f'data: {data}')
    logits = model(data, n_nodes)

    loss = F.nll_loss(logits, data.y)
    if debug:
        # print(f'data.y: {torch.bincount(data.y)}')
        print(f'data.y: {data.y.unique(return_counts=True)}')
    total_nll_lost += loss.item() * data.num_graphs

    if conn_loss_enable:
        c_loss = (1/graph_size) * \
            (-math.log(model.connectivity_loss(logits, data)))
        total_conn_lost += c_loss * data.num_graphs
        loss = loss+c_loss
    if debug:
        print(f'total_pre_n_0: {(np.argmax(logits.cpu().detach().numpy(), axis=1) == 0).sum()}')
        print(f'total_pre_n_1: {(np.argmax(logits.cpu().detach().numpy(), axis=1) == 1).sum()}')
        print(f'total_pre_n_2: {(np.argmax(logits.cpu().detach().numpy(), axis=1) == 2).sum()}')
    total_pre_n += (np.argmax(logits.cpu().detach().numpy(), axis=1) == 1).sum() + (
        np.argmax(logits.cpu().detach().numpy(), axis=1) == 2).sum()
    total_n += ((data.y.cpu().detach().numpy() == 1).sum())

    total_graphs += data.num_graphs

    total_loss += loss.item() * data.num_graphs
    if debug:
        print(f'num_graphs:{data.num_graphs}, total_n: {total_n}, total_pre_n: {total_pre_n}')

    # if n!=0:
    #     print(count, np.argmax(logits.cpu().numpy(),axis=1), n )
    #     sys.exit()

    return total_loss / data.num_graphs, total_nll_lost/data.num_graphs, total_conn_lost/data.num_graphs, total_pre_n / total_graphs, total_n / total_graphs, logits


n = graph_size
if debug:
    print(graph)
n_graph = to_networkx(graph, to_undirected=True, remove_self_loops=False)


# ===========================================================================================================
# count = 0
# exit_loop = False
#
# for index, test_data in enumerate(test_loader):
#     if exit_loop:
#         break
#     start_time = time.time()
#     val_auc, test_nll_loss, test_conn_loss, test_p_n, test_n, logits = test(
#         test_data)
#     end_time = time.time()
#     print("{}, Val:{:.4f}({:.5f}+{:.5f})[{:.5f} - {:.5f}], running time each epoch:{:.2f}".format(
#         index, val_auc, test_nll_loss, test_conn_loss, test_p_n, test_n, (end_time - start_time)))
#     model_exectue_time = end_time - start_time
#
#     for i in range(batch_size):    # (huiying) decide query # in .mapping files
#         count=count+1
#         if count == 100:
#             exit_loop=True
#             break
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
#
#         m_data_y = np.zeros(n, dtype=bool)
#         m_data_y[np.nonzero(data_y)] = True
#         m_predict_y = np.zeros(n, dtype=bool)
#         m_predict_y[np.nonzero(predictions)] = True
#         m_sub_predict_y = np.zeros(n, dtype=bool)
#         m_sub_predict_y[list(sub_node_set)] = True
#
#         f1_score_1 = f1_score(m_data_y, m_predict_y)
#         f1_score_2 = f1_score(data_y, predictions, average='micro')
#         f1_score_3 = f1_score(data_y, predictions, average='macro')
#         # f1_score_4 = f1_score(data_y, predictions, average='none')
#         f1_score_1_s = f1_score(m_data_y, m_sub_predict_y)
#
#         pre_score = precision_score(m_data_y, m_predict_y)
#         pre_score_s = precision_score(m_data_y, m_sub_predict_y)
#
#         rec_score = recall_score(m_data_y, m_predict_y)
#         rec_score_s = recall_score(m_data_y, m_sub_predict_y)
#
#         print("{}  {}-{}: {}->{} {} / {} ===== {}, sub_graph size : {}, time spend {:.6f} f1:{:.6f},f1_sub:{:.6f}, f1_micro:{:.6f}, f1_macro:{:.6f}, pre:{:.4f}, pre_sub:{:.4f}, recall:{:.4f}, recall_sub:{:.4f}"
#         .format(count, index, i,src, dest, pre_n_i, total_n_i, float(pre_n_i/total_n_i), len(sub_node_set), sub_graph_finding_time+model_exectue_time,
#         f1_score_1, f1_score_1_s, f1_score_2, f1_score_3,pre_score,pre_score_s,rec_score,rec_score_s))
#
#         draw_graph_with_DataObj(graph, data_y, test_data.src_node_idx[i].item(), test_data.dest_node_idx[i].item())
#         draw_graph_with_DataObj(graph, predictions, test_data.src_node_idx[i].item(), test_data.dest_node_idx[i].item())
# ===========================================================================================================


save_mapping_file_path = "{}/{}_{}_{}_{}_Embed{}_{}_ConLoss{}.mapping".format(save_folder, embedding_dim, hidden_dim, batch_size, heads,
                                                                              enable_embed, model_name, conn_loss_enable)
if os.path.exists(save_mapping_file_path):
    os.remove(save_mapping_file_path)
    print("removed existing file:", save_mapping_file_path)
else:
    print(save_mapping_file_path, "The file does not exist")

print(save_mapping_file_path)
# sys.exit(0)

count = 0
exit_loop = False
with open(save_mapping_file_path, 'a+') as f:
    print(f'len test_loader: {len(test_loader)}')
    for index, test_data in enumerate(test_loader):
        # print(f'index: {index}')
        # print(f'exit_loop: {exit_loop}')

        if exit_loop:
            break
        start_time = time.time()
        val_auc, test_nll_loss, test_conn_loss, test_p_n, test_n, logits = test(test_data)
        end_time = time.time()
        # print(f'logits: {logits}')
        print("idex: {}, Val: {:.4f} (test_nll_loss+test_conn_loss): ({:.5f}+{:.5f}), \n"
              "[test_p_n/test_n]: [{:.5f}/{:.5f}], running time each epoch:{:.2f}".format(
            index, val_auc, test_nll_loss, test_conn_loss, test_p_n, test_n, (end_time - start_time)))
        model_exectue_time = end_time - start_time

        # sys.exit(0)

        for i in range(batch_size):    # (huiying) decide query # in .mapping files
            count=count+1
            if count == 100:
                exit_loop=True
                break
            start_time = time.time()
            predictions = np.argmax(
                logits[i*n:(i+1)*n, :].cpu().detach().numpy(), axis=1)
            # print(f'predictions: {predictions}')

            data_y = test_data.y.cpu().detach().numpy()[i*n:(i+1)*n]
            pre_n_i = (predictions == 1).sum() + (predictions == 2).sum()
            total_n_i = (data_y == 1).sum()+(data_y == 2).sum()
            src = test_data.src_node_idx[i].item()
            dest = test_data.dest_node_idx[i].item()
            # Find connected sub_grap
            target_node_list = np.where(predictions == 1)[0].tolist()
            # print(f'target_node_list: {target_node_list}')

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

            roc = roc_auc_score(m_data_y, m_predict_y)
            roc_s = roc_auc_score(m_data_y, m_sub_predict_y)


            print("{}  {}-{}: {}->{} {} / {} ===== {}, sub_graph size : {}, time spend {:.6f} f1:{:.6f},f1_sub:{:.6f}, f1_micro:{:.6f}, f1_macro:{:.6f}"
                  ", pre:{:.4f}, pre_sub:{:.4f}, recall:{:.4f}, recall_sub:{:.4f}, roc:{:.4f}, roc_s:{:.4f}"
            .format(count, index, i,src, dest, pre_n_i, total_n_i, float(pre_n_i/total_n_i), len(sub_node_set), sub_graph_finding_time+model_exectue_time,
            f1_score_1, f1_score_1_s, f1_score_2, f1_score_3, pre_score, pre_score_s, rec_score, rec_score_s, roc, roc_s))

            line = "{} {} {} {:.6f} {} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f} {:.6f}\n".format(int(src), int(
                dest), sub_node_set, sub_graph_finding_time+model_exectue_time, target_node_list, f1_score_1, f1_score_1_s, f1_score_2, f1_score_3,
                pre_score, pre_score_s, rec_score, rec_score_s, roc, roc_s)
            f.write(line)
