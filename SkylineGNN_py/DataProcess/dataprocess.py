import argparse
from os.path import expanduser
from pathlib import Path
import os
from NADataProcess.NADataProcess import na_data_process
from process_challenge9.C9DataProcess import c9_data_process

# home = expanduser("~")
home = expanduser("/Users/hyingchen/PycharmProjects/ICDE23/GNNquery/")


def dir_path(path):
    Path(path).mkdir(parents=True, exist_ok=True)
    if os.path.isdir(path):
        return path
    else:
        raise argparse.ArgumentTypeError(
            f"readable_dir:{path} is not a valid path")


parser = argparse.ArgumentParser(
    description="Data process, generating synthetic dimension of real-world datasets")

parser.add_argument('--graph_folder', default=home+"Data/datasets", type=dir_path,
                    help='the folder that contains the graph information, node and edge')
parser.add_argument('--data_name', default="BAY", type=str,
                    # help="LFF:['NA','SF',CAL], C9:['NY','BAY','FLA','E','CTR','COL','USA']")
                    help="LFF:['NA','SF',CAL], C9:['NY','BAY','E','CTR']")
parser.add_argument('--relation', default="anti", type=str,
                    help="The relationship between the distance of a edge with other synthetic dimensions, none, anti, corr")
parser.add_argument('--num_dim', default="2", type=int,
                    help="number of synthetic dimensions")

params = parser.parse_args()

for arg in vars(params):
    print(arg, getattr(params, arg))

data_name = params.data_name
basepath = params.graph_folder
r_type = params.relation
num_dim = params.num_dim

if data_name in ['NA', 'SF', 'CAL']:
    print(r_type)
    na_data_process(data_name, basepath, r_type, num_dim)
# elif data_name in ['NY','BAY','FLA','E','CTR','COL','USA']:
elif data_name in ['NY','BAY','E','CTR']:
    c9_data_process(data_name, basepath, r_type, num_dim)
