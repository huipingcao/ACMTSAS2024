import argparse
import sys
from readData import LoadData, LoadGraph
from torch_geometric.data import DataLoader
from MultiCostNetworks import MultiCostNetworks
import os
import torch
from model import GraphEncoder
from tqdm import tqdm
from pathlib import Path
import time
import torch.nn.functional as F
from torch.nn import CrossEntropyLoss, BCELoss, BCEWithLogitsLoss
import numpy as np
import logging
from datetime import datetime
from readCheckPoints import getMaxCheckPointName


def dir_path(path):
    Path(path).mkdir(parents=True, exist_ok=True)
    if os.path.isdir(path):
        return path
    else:
        raise argparse.ArgumentTypeError(
            f"readable_dir:{path} is not a valid path")


parser = argparse.ArgumentParser(
    description="Pytorch implementation of Skyline GNN")

parser.add_argument('--graph_folder', default='/home/gqxwolf/mydata/skylineGNN/data/c9_ny_1k',
                    type=dir_path, help='the folder that contains the graph information, node and edge')
parser.add_argument('--paths_folder', default='/home/gqxwolf/mydata/skylineGNN/data/c9_ny_1k/results_bbs/training_GNN',
                    type=dir_path, help='the folder that stores the results_bbs of queries to build the train and test graph')
parser.add_argument('--checkpoints_folder', default='/home/gqxwolf/mydata/skylineGNN/data/c9_ny_1k/models',
                    type=dir_path, help='the folder that stores the checkpoints of the models')
parser.add_argument('--gpu', default=True,
                    action='store_false', help='Enable gpu')
parser.add_argument('--no_epoch', default=1000,
                    type=int, help='Number of epochs')
parser.add_argument('--node_dim', default=2, type=int,
                    help='node feature dimension')
parser.add_argument('--edge_dim', default=3, type=int,
                    help="edge feature dimension")
parser.add_argument('--embed_dim', default=64, type=int,
                    help="linear embedding dimension")
parser.add_argument('--hidden_dim', default=64,
                    type=int, help="hidden dimension")
parser.add_argument('--enable_embed', default=True, action='store_false',
                    help='Enable linear embedding of node features')
parser.add_argument('--output_dim', default=3,
                    type=int, help='output dimension')
parser.add_argument('--gnn_name', default="GCN",
                    type=str, help='the model of GNN')
parser.add_argument('--heads', default=1, type=int,
                    help="number of multi-header attention")
parser.add_argument('--batch_size', default=64, type=int, help='batch size')
parser.add_argument('--log_folder', default='/home/gqxwolf/skylineGNN/mydata/data/c9_ny_1k/logs',
                    type=dir_path, help="Location of log files")

params = parser.parse_args()
now = datetime.now()
dt_string = now.strftime('%d%m%Y_%H%M%S')

logging.basicConfig(filename='{}/{}_{}_{}_{}_{}_train_checkpoint_{}_2GNN3FF_10k_train_samples_{}.log'
                    .format(params.log_folder, params.embed_dim, params.hidden_dim, params.batch_size, params.no_epoch, params.heads, params.gnn_name, dt_string),
                    filemode='w', format='%(name)s - %(asctime)s : %(message)s', datefmt='%m/%d/%Y %I:%M:%S %p')
logger = logging.getLogger("skylineGNN")
logger.setLevel(logging.DEBUG)

for arg in vars(params):
    print(arg, getattr(params, arg))
    logger.info('%s, %s', arg, getattr(params, arg))
print("=======================================================")
logger.info("=======================================================")
print()
logger.info('\n')
# sys.exit()


path = params.graph_folder
num_node, graph = LoadGraph(path, logger)
print(params.gpu, torch.cuda.is_available())
logger.info('%s %s', params.gpu, torch.cuda.is_available())

if params.gpu and torch.cuda.is_available():
    if torch.cuda.is_available():
        USE_CUDA = True
        print('Using GPU, %i devices.' % torch.cuda.device_count())
        logger.info('Using GPU, %i devices.' % torch.cuda.device_count())
        device = torch.device('cuda')
    else:
        USE_CUDA = False
        device = torch.device('cpu')

else:
    USE_CUDA = False
    device = torch.device('cpu')

print("device: {} , number of nodes: {}" .format(device, num_node))
logger.info("device: %s , number of nodes: %s", device, num_node)


node_dim = params.node_dim
edge_dim = params.edge_dim
embedding_dim = params.embed_dim
hidden_dim = params.hidden_dim
batch_size = params.batch_size
enable_embed = params.enable_embed
output_dim = params.output_dim
model_name = params.gnn_name
heads = params.heads
no_epoch = params.no_epoch

# load the datasets
train_paths_folder = params.paths_folder
train_dataset = MultiCostNetworks(
    graph, train_paths_folder=train_paths_folder, root=path, split='train')
test_dataset = MultiCostNetworks(
    graph, train_paths_folder=train_paths_folder, root=path, split='test')
train_loader = DataLoader(train_dataset, drop_last=True, batch_size=batch_size)
test_loader = DataLoader(test_dataset, drop_last=True, batch_size=batch_size)

# init model
model = GraphEncoder(node_dim, edge_dim, embedding_dim, hidden_dim, output_num_class=output_dim, device=device, enable_embedding=enable_embed,
                     batch=batch_size, model_name=model_name, heads=heads).to(device)
optimizer = torch.optim.Adam(params=model.parameters(), lr=0.0001)

# train function


def train():
    model.train()

    total_loss = 0
    total_pre_n = 0
    total_n = 0
    total_graphs = 0

    for index, data in enumerate(train_loader):
        start_time = time.time()
        data = data.to(device)
        # printm()
        # print(data, data.src_node_idx, data.dest_node_idx)
        logits = model(data)
        # printm()
        # print(logits)
        # print(data.y, (data.y.cpu().detach().numpy() == 1).sum())
        loss = F.nll_loss(logits, data.y)

        total_pre_n += (np.argmax(logits.cpu().detach().numpy(), axis=1) == 1).sum() + (
            np.argmax(logits.cpu().detach().numpy(), axis=1) == 2).sum()
        total_n += ((data.y.cpu().detach().numpy() == 1).sum())

        total_graphs += data.num_graphs
        total_loss += loss.item() * data.num_graphs

        optimizer.zero_grad()
        loss.backward()
        # torch.nn.utils.clip_grad_norm_(model.parameters(), 10)
        optimizer.step()
        # print(index, time.time()-start_time)

    return total_loss / len(train_dataset), total_pre_n / total_graphs, total_n / total_graphs

# test function


@torch.no_grad()
def test(loader):
    model.eval()

    total_loss = 0
    total_pre_n = 0
    total_n = 0
    total_graphs = 0

    for data in loader:
        data = data.to(device)
        logits = model(data)

        loss = F.nll_loss(logits, data.y)

        total_pre_n += (np.argmax(logits.cpu().detach().numpy(), axis=1) == 1).sum() + (
            np.argmax(logits.cpu().detach().numpy(), axis=1) == 2).sum()
        total_n += ((data.y.cpu().detach().numpy() == 1).sum())

        total_graphs += data.num_graphs

        total_loss += loss.item() * data.num_graphs

        # if n!=0:
        #     print(count, np.argmax(logits.cpu().numpy(),axis=1), n )
        #     sys.exit()

    return total_loss / len(loader), total_pre_n / total_graphs, total_n / total_graphs


# check if there exist check point file under given model folder parameter
max_iter, check_pt_filename = getMaxCheckPointName(params.checkpoints_folder, embedding_dim, hidden_dim, batch_size, heads, enable_embed, num_node, model_name, logger)
start_epoch = 0
print("The previous check point file exists, max iter:{}, check point file {}/{}".format(max_iter, params.checkpoints_folder,check_pt_filename))
if max_iter != 0 and check_pt_filename != '':
    logger.info("The previous check point file exists, max iter:{}, check point file {}/{}".format(max_iter, params.checkpoints_folder,check_pt_filename))
    start_epoch = max_iter+1
    checkpoint = torch.load("{}/{}".format(params.checkpoints_folder,check_pt_filename))
    model.load_state_dict(checkpoint['model_state_dict'])
    optimizer.load_state_dict(checkpoint['optimizer_state_dict'])
    resume_epoch = checkpoint['epoch']
    loss = checkpoint['loss']

# sys.exit(0)

# train process
best_val_auc = test_auc = 0
print('Epoch from {} =======> {}'.format(start_epoch, no_epoch+1))
logger.info('Epoch from {} =======> {}'.format(start_epoch, no_epoch+1))

for epoch in range(start_epoch, no_epoch+1):
    start_time = time.time()
    loss, pre_n, n = train()
    val_auc, test_p_n, test_n = test(test_loader)
    end_time = time.time()
    if epoch % 10 == 0:
        print("Epoch:{}, Loss:{:.4f}[{:.5f} - {:.5f}], Val:{:.4f}[{:.5f} - {:.5f}], running time each epoch:{:.2f}".format(
            epoch, loss, pre_n, n, val_auc, test_p_n, test_n, (end_time - start_time)))
        logger.info("Epoch:{}, Loss:{:.4f}[{:.5f} - {:.5f}], Val:{:.4f}[{:.5f} - {:.5f}], running time each epoch:{:.2f}".format(
            epoch, loss, pre_n, n, val_auc, test_p_n, test_n, (end_time - start_time)))
    if epoch % 100 == 0:
        check_pt_path = "{}/{}_{}_{}_{}_{}_{}_{}_train_checkpoint_{}_2GNN3FF_10k_train_samples.pt".\
            format(params.checkpoints_folder, embedding_dim, hidden_dim,
                   batch_size, epoch, heads, enable_embed, num_node, model_name)
        torch.save({'epoch': epoch, 'model_state_dict': model.state_dict(
        ), 'optimizer_state_dict': optimizer.state_dict(), 'loss': loss, }, check_pt_path)
