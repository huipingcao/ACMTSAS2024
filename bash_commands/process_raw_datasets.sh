#!/bin/bash

python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/C9_NY_NONE/ --data_name NY --relation none --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/C9_NY_CORR/ --data_name NY --relation corr --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/C9_NY_ANTI/ --data_name NY --relation anti --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/C9_BAY_NONE/ --data_name BAY --relation none --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/C9_BAY_CORR/ --data_name BAY --relation corr --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/C9_BAY_ANTI/ --data_name BAY --relation anti --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/C9_CTR_NONE/ --data_name CTR --relation none --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/C9_CTR_CORR/ --data_name CTR --relation corr --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/C9_CTR_ANTI/ --data_name CTR --relation anti --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/L_CAL_NONE/ --data_name CAL --relation none --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/L_CAL_CORR/ --data_name CAL --relation corr --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/L_CAL_ANTI/ --data_name CAL --relation anti --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/C9_E_NONE/ --data_name E --relation none --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/C9_E_CORR/ --data_name E --relation corr --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/C9_E_ANTI/ --data_name E --relation anti --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/L_NA_NONE/ --data_name NA --relation none --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/L_NA_CORR/ --data_name NA --relation corr --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/L_NA_ANTI/ --data_name NA --relation anti --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/L_SF_NONE/ --data_name SF --relation none --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/L_SF_CORR/ --data_name SF --relation corr --num_dim 2
python ../SkylineGNN_py/DataProcess/dataprocess.py --graph_folder Data/L_SF_ANTI/ --data_name SF --relation anti --num_dim 2
