# %%
import torch
from torch.nn import CrossEntropyLoss, BCELoss, BCEWithLogitsLoss
from torch_geometric.data import DataLoader
import numpy as np
from model import GraphEncoder
from readData import LoadData, LoadGraph
import os
from MultiCostNetworks import MultiCostNetworks
import sys
import torch.nn.functional as F
from drawRawGraphs import draw_graph
import time
from torch_geometric.utils import subgraph, k_hop_subgraph
import math

path = "/home/gqxwolf/mydata/skylineGNN/data/c9_ny_5k/"
train_paths_folder = "/home/gqxwolf/mydata/skylineGNN/data/c9_ny_5k"
check_point_file = '/home/gqxwolf/mydata/skylineGNN/data/c9_ny_5k/models/256_256_64_400_1_EmbedTrue_5000_train_checkpoint_GCN_ConLossTrue_2GNN3FF_10k_train_samples.pt'

# %%
node_dim = 2
edge_dim = 3
embedding_dim = 256
hidden_dim = 256
batch_size = 64
enable_embed = True
output_dim = 3
model_name = "GCN"
heads = 1
graph_size = 5000
conn_loss_enable = True


graph = LoadGraph(path)
print(graph)

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
print(device)

test_dataset = MultiCostNetworks(
    graph, train_paths_folder=train_paths_folder, root=path, split='test')
test_loader = DataLoader(test_dataset, drop_last=True, batch_size=batch_size)
model = GraphEncoder(node_dim, edge_dim, embedding_dim, hidden_dim, output_num_class=output_dim, device=device, enable_embedding=enable_embed,
                     batch=batch_size, model_name=model_name, heads=heads).to(device)
optimizer = torch.optim.Adam(params=model.parameters(), lr=0.0001)

# %%
test_data = None

for data in test_loader:
    test_data = data
    break

print(test_data)

# %%
checkpoint = torch.load(check_point_file)
model.load_state_dict(checkpoint['model_state_dict'])
resume_epoch = checkpoint['epoch']
loss = checkpoint['loss']

# %%
@torch.no_grad()
def test(loader):
    model.eval()

    total_loss = 0
    total_pre_n = 0
    total_n = 0
    total_graphs = 0
    total_conn_lost = 0
    total_nll_lost = 0

    for data in loader:
        data = data.to(device)
        logits = model(data)

        loss = F.nll_loss(logits, data.y)
        total_nll_lost += loss.item() * data.num_graphs

        if conn_loss_enable:
            c_loss = (1/graph_size)*- \
                math.log(model.connectivity_loss(
                    logits, data))
            total_conn_lost += c_loss * data.num_graphs
            loss = loss+c_loss

        total_pre_n += (np.argmax(logits.cpu().detach().numpy(), axis=1) == 1).sum() + (
            np.argmax(logits.cpu().detach().numpy(), axis=1) == 2).sum()
        total_n += ((data.y.cpu().detach().numpy() == 1).sum())

        total_graphs += data.num_graphs

        total_loss += loss.item() * data.num_graphs

        # if n!=0:
        #     print(count, np.argmax(logits.cpu().numpy(),axis=1), n )
        #     sys.exit()

    return total_loss / len(test_dataset), total_nll_lost/len(test_dataset), total_conn_lost/len(test_dataset), total_pre_n / total_graphs, total_n / total_graphs, logits


start_time = time.time()
val_auc, test_nll_loss, test_conn_loss, test_p_n, test_n, logits = test(test_loader)
end_time = time.time()
print("Val:{:.4f}({:.5f}+{:.5f})[{:.5f} - {:.5f}], running time each epoch:{:.2f}".format(
            val_auc, test_nll_loss, test_conn_loss, test_p_n, test_n, (end_time - start_time)))
       
# %%

# %%
# i = 16
i = 35
n = graph_size
predictions = np.argmax(logits[i*n:(i+1)*n, :].cpu().detach().numpy(), axis=1)
data_y = data.y.cpu().detach().numpy()[i*n:(i+1)*n]
pre_n_i = (predictions == 1).sum() + (predictions == 2).sum()
total_n_i = (data_y == 1).sum()+(data_y == 2).sum()

print("{} / {} ===== overall:{} / {} in {} second".format(pre_n_i,
      total_n_i, test_p_n, test_n, end_time - start_time))


draw_graph(path, data_y, test_data.src_node_idx[i].item(), test_data.dest_node_idx[i].item())
draw_graph(path, predictions, test_data.src_node_idx[i].item(), test_data.dest_node_idx[i].item())

# %%
