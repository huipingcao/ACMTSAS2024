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


class GraphEncoder(torch.nn.Module):
    def __init__(self, node_features, edge_features, embedding_dim, hidden_features, output_num_class, device, enable_embedding=True,
                 batch=32, dropout=0.5, model_name="GCN", heads=1):
        super(GraphEncoder, self).__init__()

        self.batchsize = batch
        self.device = device
        self.dropout_rate = dropout
        self.enable_embedding = enable_embedding
        self.edge_dim = edge_features
        self.node_dim = node_features
        self.model_name = model_name
        self.heads = heads
        print(heads, hidden_features)

        # print(self.batchsize)

        if (enable_embedding):
            self.embedding = nn.Linear(node_features, embedding_dim)
            print(embedding_dim)

        # TransformerConv Initialnization
        # self.GNN = customTransformerConv(hidden_features, hidden_features, edge_dim=edge_features)

        # SAGEConv
        # self.GNN1 = SAGEConv(node_features, hidden_features)

        #Graph convolutional network
        #self.GNN1 = GCNConv(embedding_dim, hidden_features)

        if enable_embedding:
          if heads != 1 and model_name=="Transformer":
            self.GNN1 = self.getModel(embedding_dim, hidden_features)
          else:
            self.GNN1 = self.getModel(embedding_dim, hidden_features)
        else:
          if heads != 1 and model_name=="Transformer":
            self.GNN1 = self.getModel(node_features, heads*hidden_features)
          else:
            self.GNN1 = self.getModel(node_features, hidden_features)

        if heads != 1 and model_name=="Transformer":
          self.GNN2 = self.getModel(heads*hidden_features, hidden_features)
        else:
          self.GNN2 = self.getModel(hidden_features, hidden_features)

        # self.GNN3 = SAGEConv(hidden_features * 3, hidden_features)

        # self.bathnorm1 = BatchNorm(hidden_features)
        # self.bathnorm2 = BatchNorm(hidden_features)
        self.bathnorm3 = BatchNorm(hidden_features)

        # self.pool1 = TopKPooling(hidden_features, ratio=0.8)


        #
        if heads != 1 and model_name=="Transformer":
          self.FFConn = torch.nn.Linear(heads * hidden_features * 3, hidden_features)
        else:
          self.FFConn = torch.nn.Linear(hidden_features * 3, hidden_features)

        self.FFConn1 = torch.nn.Linear(hidden_features, hidden_features)
        self.FFConn2 = torch.nn.Linear(hidden_features, output_num_class)

        # self.convFinal = GCNConv(hidden_features * 3, 2, improved=True)
        print("Finish model initialization !!!!")

    def forward(self, data):
        # batch = [N*B]
        # start_time = time.time()

        x, edge_index, edge_attr, batch = data.x.float(), data.edge_index, data.edge_attr, data.batch
        # print(x.shape, edge_index.shape, edge_attr.shape, batch.shape)
        # print(batch) #tensor([ 0,  0,  0,  ..., 63, 63, 63], device='cuda:0') when batch_size = 64
        # sys.exit(0)

        # add_self_loops(edge_index, num_nodes=x.size(0))

        n = int(x.shape[0] / self.batchsize)  # number of nodes
        # print(n)

        # print(x.shape)
        if self.enable_embedding:
            x = self.embedding(x)
            # print("Linear Embedding, {}".format(time.time()-start_time))
            # print(x.shape)

        x = self.GNN1(x, edge_index)
        # print(x.shape)
        # print("1st GNN, {}".format(time.time()-start_time))


        x = x.relu()
        x = F.dropout(x, p=self.dropout_rate, training=self.training)
        # print(x.shape)


        x = self.GNN2(x, edge_index)
        # print(x.shape)

        # x1 = self.GNN1(x, edge_index, edge_attr) # TransformGNN
        # x = x + x1
        # x = F.relu(x)
        # x = self.bathnorm1(x)
        # batch = None
        # x, edge_index, _, batch, _, _ = self.pool1(x, edge_index, None, batch)
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

        src_cat_vec, dest_cat_vec = self.getBactchListOfSrcDestNodes(x, data, n)
        x = torch.cat((x, src_cat_vec, dest_cat_vec), dim=1) #form the new feature inputs
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
        # print("FF1, {}".format(time.time()-start_time))
        # print(x.shape)

        x = self.FFConn1(x)
        x = F.dropout(x)
        x = F.relu(x)
        # print("FF2, {}".format(time.time()-start_time))
        # print(x.shape)

        x = self.FFConn2(x)
        # print(x.shape)
        # print("FF3, {}".format(time.time()-start_time))

        # sys.exit(0)

        return F.log_softmax(x, dim=1)


    def getBactchListOfSrcDestNodes(self, x, data, n):
        src_list = torch.tensor([data.src_node_idx[i].item() + i * n for i in range(len(data.src_node_idx))])
        dest_list = torch.tensor([data.dest_node_idx[i].item() + i * n for i in range(len(data.dest_node_idx))])
        src_cat_vec = x[src_list].repeat_interleave(n, dim=0)
        dest_cat_vec = x[dest_list].repeat_interleave(n, dim=0)
        return src_cat_vec, dest_cat_vec

    def getModel(self, input_dim, output_dim, **kwargs):
      if self.model_name=="GCN":
        return GCNConv(input_dim, output_dim)
      elif self.model_name == "SAGE":
        return SAGEConv(input_dim, output_dim)
      elif self.model_name == "Transformer":
        # edge_dim=kwargs.get('edge_dim',None)
        # heads=kwargs.get('heads',None)
        edge_dim=self.edge_dim
        heads = self.heads
        print("Transformer model, edge dim:{} with multi-heads attention {} (input {} --> output {})".format(edge_dim,heads, input_dim, output_dim*heads))
        return customTransformerConv(input_dim, output_dim, edge_dim=edge_dim, heads=heads)