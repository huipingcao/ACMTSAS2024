import os
from os.path import isfile, join
import sys

def getMaxCheckPointName(model_foler, embed_dim, hidden_dim, batch_size, heads, enable_embed, graph_size, model_name, conn_loss_enable, logger):
    parameters = [embed_dim, hidden_dim, batch_size,
                  heads, 'Embed{}'.format(enable_embed), graph_size, model_name, 'ConLoss{}'.format(conn_loss_enable)]
    parameters = [str(s) for s in parameters]
    max_iter = 0
    file_name = ''
    print('===================readCheckPoints.py===================')
    print(f'parameters: {parameters}')
    logger.info(parameters)

    for file in os.listdir(model_foler):
        if isfile(join(model_foler, file)):
            if 0 not in [c in str(file) for c in parameters]:
                print(file)
                logger.info(file)
                no_iter = int(file.split('_')[3])
                if no_iter >= max_iter:
                    max_iter = no_iter
                    file_name = file
    return max_iter, file_name

# path='/home/gqxwolf/skylineGNN/data/c9_ny_5k/models'
# n=5000
# embed_dim = 64
# hidden_dim = 64
# batch_size = 64
# enable_embed = True
# model_name = 'Trasn'
# heads = 1

# max_iter, check_point_filename=getMaxCheckPointName(path, enable_embed, hidden_dim, batch_size, heads, enable_embed, n, model_name)
# print(max_iter)
# print(check_point_filename)
