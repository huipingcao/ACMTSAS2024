import sys
import torch
from torch_geometric.data import InMemoryDataset, Data
from os import listdir
from os.path import isfile, join
import random
from tqdm import tqdm
import logging
import os
from readData import readLogs


reg_exp = "\\(\\d+\\)"

logger = logging.getLogger("skylineGNN") 

#find the 1-hop nodes of given node list, if the neigbors are not in the nodes list 
def getNeigbhorNodes(nodes, grahp, k=1):
    row, col = grahp.edge_index[0], grahp.edge_index[1]

    order_neighbors = set()

    for src in nodes:
        # print(src)
        mask = row == src  # neighbor nodes, where the lines are the row_id == src
        # mask_2 = torch.tensor([c_ not in nodes for c_ in col])  # not in the target nodes list
        # mask = torch.logical_and(mask_1, mask_2)

        # print(len(mask), torch.sum(mask == True), col[mask])
        # neigbor = grahp.edge_index[:, mask]
        for n in col[mask]:
            if n.item() not in nodes:
                order_neighbors.add(n.item())

    return list(order_neighbors)


class MultiCostNetworks(InMemoryDataset):
    def __init__(self, dataset, train_paths_folder, root, ratio=0.8, split="train", transform=None, pre_transform=None):
        self.ratio = ratio
        self.data = dataset
        self.train_paths_folder = train_paths_folder
        super(MultiCostNetworks, self).__init__(root, transform, pre_transform)
        index = ['train', 'test'].index(split)
        self.data, self.slices = torch.load(self.processed_paths[index])

    @property
    def raw_file_names(self):
        # train_paths_folder = "/content/drive/MyDrive/ColabNotebooks/GNN_Skyline/data/c9_ny_1k/results_bbs/training_GNN"
        onlyfiles = [join(self.train_paths_folder, f) for f in listdir(self.train_paths_folder) if
                     isfile(join(self.train_paths_folder, f))]
        return onlyfiles

    @property
    def processed_file_names(self):
        return ['train_data.pt', 'test_data.pt']

    def process(self):
        dataList = []
        random.seed(12345)
        torch.manual_seed(12345)
        print("Process", self.data)
        logger.info("Process %s", self.data)

        for index, f in tqdm(enumerate(self.raw_paths)):
            p = readLogs(f)
            # print(torch.zeros(self.data.num_nodes, 2).shape)
            # print(self.data.x.shape)

            # new data, added extra columns to store the flag of the src node and the dest node
            # new_x = torch.cat((self.data.x, torch.zeros(self.data.num_nodes, 1)), dim=1)
            # # features for the src and dest node
            # new_x[p.src][2] = 1
            # new_x[p.dest][2] = 1

            new_x = self.data.x

            y = torch.zeros(self.data.num_nodes, dtype=torch.long)

            # print(y)
            # print(p.nodes)
            y[p.nodes] = 1

            order_neighbors = getNeigbhorNodes(p.nodes, self.data)

            # print(p.nodes)
            # print(sec_order_neighbors)
            # sys.exit(0)
            # print(sec_order_neighbors)

            y[order_neighbors] = 2

            # print(len(sec_order_neighbors), len(p.nodess))
            new_data = Data(x=new_x, edge_index=self.data.edge_index, y=y, edge_attr=self.data.edge_attr)

            new_data.src_node_idx = p.src
            new_data.dest_node_idx = p.dest

            dataList.append(new_data)
            # if index==1000:
            #   break

            # sys.exit(0)

        random.shuffle(dataList)
        splittor = int(len(dataList) * self.ratio)

        print(len(dataList), '==', splittor, len(dataList[:splittor]), len(dataList[splittor:]))
        logger.info('%s %s %s %s %s', len(dataList), '==', splittor, len(dataList[:splittor]), len(dataList[splittor:]))
        train_list = dataList[:splittor]
        test_list = dataList[splittor:]

        torch.save(self.collate(train_list), self.processed_paths[0])
        torch.save(self.collate(test_list), self.processed_paths[1])

