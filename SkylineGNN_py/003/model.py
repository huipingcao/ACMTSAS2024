import sys

import torch
import torch.nn.functional as F
from torch_geometric.nn import GCNConv, TransformerConv, GINConv, ChebConv, BatchNorm, TopKPooling, SAGEConv
import torch.nn as nn
from torch_geometric.utils import add_self_loops
import numpy as np
from torch_geometric.nn import global_mean_pool as gap, global_max_pool as gmp

from customTransformerConv import customTransformerConv

import time
import logging

logger = logging.getLogger("skylineGNN")
EPS = 1e-15

debug = 0

class GraphEncoder(torch.nn.Module):
    def __init__(self, node_features, edge_features, embedding_dim, hidden_features, output_num_class, device, enable_embedding=True,
                 batch=32, dropout=0.5, model_name="GCN", heads=1):
        super(GraphEncoder, self).__init__()

        self.batchsize = batch
        self.device = device
        self.dropout_rate = dropout
        self.enable_embedding = enable_embedding
        self.model_name = model_name
        self.edge_dim = edge_features
        self.heads = heads

        # print(self.batchsize)

        if (enable_embedding):
            self.embedding = nn.Linear(node_features, embedding_dim)
            # print(embedding_dim)

        # TransformerConv Initialnization
        # self.GNN = customTransformerConv(hidden_features, hidden_features, edge_dim=edge_features)

        # SAGEConv
        # self.GNN1 = SAGEConv(node_features, hidden_features)

        # Graph convolutional network
        #self.GNN1 = GCNConv(embedding_dim, hidden_features)

        if enable_embedding:
            if self.model_name == "Transformer" and heads != 1:
                self.GNN1 = self.getModel(
                    embedding_dim, int(hidden_features/heads))
            else:
                self.GNN1 = self.getModel(embedding_dim, hidden_features)
        else:
            if self.model_name == "Transformer" and heads != 1:
                self.GNN1 = self.getModel(
                    node_features, int(hidden_features/heads))
            else:
                self.GNN1 = self.getModel(node_features, hidden_features)

        if self.model_name == "Transformer" and heads != 1:
            self.GNN2 = self.getModel(
                hidden_features, int(hidden_features/heads))
        else:
            self.GNN2 = self.getModel(hidden_features, hidden_features)

        # self.GNN3 = SAGEConv(hidden_features * 3, hidden_features)

        self.bathnorm1 = BatchNorm(hidden_features)
        self.bathnorm2 = BatchNorm(hidden_features)
        self.bathnorm3 = BatchNorm(hidden_features)

        # print('bathnorm1:', self.bathnorm1)

        # self.pool1 = TopKPooling(hidden_features, ratio=0.8)   # comment out pooling for index mismatch


        self.FFConn = torch.nn.Linear(hidden_features * 3, hidden_features)
        self.FFConn1 = torch.nn.Linear(hidden_features, hidden_features)
        self.FFConn2 = torch.nn.Linear(hidden_features, output_num_class)

        # self.convFinal = GCNConv(hidden_features * 3, 2, improved=True)
        print("Finish model initialization !!!!")

    def forward(self, data):
        # batch = [N*B]
        # print(data)
        start_time = time.time()

        x, edge_index, edge_attr, batch = data.x.float(), data.edge_index, data.edge_attr, data.batch

        if debug:
            print(f'x.shape {x.shape}, edge_index.shape {edge_index.shape}, edge_attr.shape {edge_attr.shape}')
            print('data.batch:', batch) #tensor([ 0,  0,  0,  ..., 63, 63, 63], device='cuda:0') when batch_size = 64
        # sys.exit(0)

        # add_self_loops(edge_index, num_nodes=x.size(0))

        n = int(x.shape[0] / self.batchsize)  # number of nodes
        self.graph_size = n
        # print(n)
        if debug:
            print(f'self.GNN1 {self.GNN1}, self.GNN2 {self.GNN2}')
            print('x.shape:', x.shape)

        if self.enable_embedding:
            x = self.embedding(x)
            # print("Linear Embedding, {}".format(time.time()-start_time))
            # print(x.shape)
        if debug:
            print(f'x.shape {x.shape}, edge_index.shape {edge_index.shape}')
        x = self.GNN1(x, edge_index)
        # print('x.shape - GNN1:', x.shape)
        # print("1st GNN, {}".format(time.time()-start_time))

        x = x.relu()
        x = F.dropout(x, p=self.dropout_rate, training=self.training)

        x = self.GNN2(x, edge_index)
        # print('x.shape - GNN2:', x.shape)

        # x1 = self.GNN1(x, edge_index, edge_attr) # TransformGNN
        # x = x + x1
        # x = F.relu(x)
        # x = self.bathnorm1(x)
        # batch = None

        # comment out pooling for index mismatch
        # x, edge_index, _, batch, _, _ = self.pool1(x, edge_index, None, batch)
        # print(f'pool1 -> x.shape {x.shape}, edge_index.shape {edge_index.shape}, batch {batch}')

        # x1 = torch.cat([gmp(x, batch), gap(x, batch)], dim=1)
        #
        # sys.exit(0)

        # x = F.dropout(x, p=0.1)

        # x2 = self.GNN2(x1, edge_index)
        # x = x1 + x2
        # x = F.relu(x)
        # x = self.bathnorm2(x)

        # x = F.dropout(x, p=0.1)

        # x = self.ChebConv(x, edge_index)

        # x = F.relu(x)
        # x = self.conv1(x, edge_index)
        # x = F.relu(x)
        # x = F.dropout(x, training=self.training)
        # x = self.conv2(x, edge_index)
        # x = F.relu(x)
        #

        # print(data.dest_node_idx)

        src_cat_vec, dest_cat_vec = self.getBactchListOfSrcDestNodes(
                                            x, data, n)
        # form the new feature inputs
        x = torch.cat((x, src_cat_vec, dest_cat_vec), dim=1)

        # print(x.shape)
        # sys.exit(0)

        # print("Concatenate, {}".format(time.time()-start_time))

        # print(x.shape)

        # x = self.TranConv3(x, edge_index, edge_attr)
        # x = self.bathnorm1(x)
        # x = F.relu(x)
        # x = x + F.dropout(x)

        x = self.FFConn(x)
        x = self.bathnorm3(x)
        # x = F.dropout(x)
        x = F.relu(x)
        if debug:
            print(f"FF -> x.shape {x.shape} time spent {time.time()-start_time}")

        x = self.FFConn1(x)
        x = F.dropout(x)
        x = F.relu(x)
        if debug:
            print(f"FF1 -> x.shape {x.shape} time spent {time.time()-start_time}")

        x = self.FFConn2(x)
        if debug:
            print(f"FF2 -> x.shape {x.shape} time spent {time.time() - start_time}")

        # sys.exit(0)

        return F.log_softmax(x, dim=1)

    def getBactchListOfSrcDestNodes(self, x, data, n):
        src_list = torch.tensor(
            [data.src_node_idx[i].item() + i * n for i in range(len(data.src_node_idx))])
        dest_list = torch.tensor(
            [data.dest_node_idx[i].item() + i * n for i in range(len(data.dest_node_idx))])
        # src_list = torch.tensor(
        #     [data.src_node_idx[i].item() + n for i in range(len(data.src_node_idx))])
        # dest_list = torch.tensor(
        #     [data.dest_node_idx[i].item() + n for i in range(len(data.dest_node_idx))])
        if debug:
            print('=========================================')
            print(f'data.src_list {src_list}')
            print(f'data.src_node_idx {data.src_node_idx}')
            print(f'x {x}')
            print('=========================================')
        src_cat_vec = x[src_list].repeat_interleave(n, dim=0)
        dest_cat_vec = x[dest_list].repeat_interleave(n, dim=0)
        if debug:
            print(f'src_list {src_list}')
            print(f'dest_list {dest_list}')
            print(f'x[src_list]: {x[src_list]}')
            print(f'x[dest_list]: {x[dest_list]}')
        return src_cat_vec, dest_cat_vec

    def getModel(self, input_dim, output_dim, **kwargs):
        if self.model_name == "GCN":
            return GCNConv(input_dim, output_dim)
        elif self.model_name == "SAGE":
            return SAGEConv(input_dim, output_dim)
        elif self.model_name == "Transformer":
            print("Transformer model, edge dim:{} with multi-heads attention {}".format(
                self.edge_dim, self.heads))
            return customTransformerConv(input_dim, output_dim, edge_dim=self.edge_dim, heads=self.heads)

    def connectivity_loss(self, logits, data):
        pre_nodes_masks = [True if x == 1 or x == 2 else False for x in np.argmax(
            logits.cpu().detach().numpy(), axis=1)]
        num_node_b = self.graph_size * self.batchsize
        n_idx = torch.arange(num_node_b)[pre_nodes_masks].to("cpu")
        row, col = data.edge_index.to('cpu')
        n_mask = torch.zeros(num_node_b, dtype=torch.bool)
        n_mask[n_idx] = 1
        mask = n_mask[row] & n_mask[col]
        edges_index = data.edge_index[:, mask]
        connected_node_idx = torch.unique(edges_index).size(0)
        # print(connected_node_idx, len(pre_nodes_masks), len(n_idx))
        connectivity_loss = connected_node_idx/(len(n_idx)+EPS)
        # print(connectivity_loss)
        # return {
        #     "loss": {
        #         "losses": connectivity_loss,
        #         "indices": None,
        #         "reduction_type": "already_reduced",
        #     }
        # }
        return connectivity_loss/data.num_graphs+EPS
