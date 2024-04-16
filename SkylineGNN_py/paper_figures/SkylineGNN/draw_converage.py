from utilities import readLossValues

import matplotlib.pyplot as plt
import matplotlib
font = {'family': 'Arial',
        'weight': 'normal',
        'size': 18}
matplotlib.rc('font', **font)
axis_font = {'fontname': 'Arial', 'size': '18'}


#####################################################################
# C9_NY_5K
# sage_log_file_path = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_NONE_5K/SAGE/256_256_32_1000_1_train_checkpoint_SAGE_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_27092021_144616.log"
# gcn_log_file_path = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_NONE_5K/GCN/256_256_32_1000_1_train_checkpoint_GCN_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_30092021_003122.log"
# transformer_log_file_path = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_NONE_5K/Transformer/128_128_32_1000_2_train_checkpoint_Transformer_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_30092021_003126.log"

#####################################################################
# C9_NY_5K
sage_log_file_path = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_5K/256_256_32_1000_1_train_checkpoint_SAGE_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_27092021_144616.log"
sage_log_file_path_baseline1 = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_5K/256_256_32_1000_1_train_checkpoint_SAGE_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_30092021_111651_base001.log"
sage_log_file_path_baseline2 = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_5K/256_256_32_1000_1_train_checkpoint_SAGE_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_02102021_124127_baseline002.log"
gcn_log_file_path = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_5K/256_256_32_1000_1_train_checkpoint_GCN_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_30092021_105427.log"
gcn_log_file_path_baseline1 = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_5K/256_256_32_1000_1_train_checkpoint_GCN_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_03102021_162303_base001.log"
gcn_log_file_path_baseline2 = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_5K/256_256_32_1000_1_train_checkpoint_GCN_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_02102021_125202_baseline002.log"

# C9_NY_15K
# sage_log_file_path = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_15K/256_256_32_1000_1_train_checkpoint_SAGE_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_04102021_001604.log"
# sage_log_file_path_baseline1 = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_15K/256_256_32_1000_1_train_checkpoint_SAGE_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_30092021_121938_base001.log"
# sage_log_file_path_baseline2 = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_15K/256_256_32_1000_1_train_checkpoint_SAGE_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_02102021_124927_baseline002.log"
# gcn_log_file_path = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_15K/256_256_32_1000_1_train_checkpoint_GCN_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_04102021_000403.log"
# gcn_log_file_path_baseline1 = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_15K/256_256_32_1000_1_train_checkpoint_GCN_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_03102021_162303_base001.log"
# gcn_log_file_path_baseline2 = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_15K/256_256_32_1000_1_train_checkpoint_GCN_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_02102021_125202_baseline002.log"


sage_df = readLossValues(sage_log_file_path.strip())
sage_baseline1_df = readLossValues(sage_log_file_path_baseline1.strip())
sage_baseline2_df = readLossValues(sage_log_file_path_baseline2.strip())

gcn_df = readLossValues(gcn_log_file_path.strip())
gcn_baseline1_df = readLossValues(gcn_log_file_path_baseline1.strip())
gcn_baseline2_df = readLossValues(gcn_log_file_path_baseline2.strip())


#train_loss, val_loss
type = "val_loss" 
plt.plot(sage_df['epoch_no'], sage_df[type],
         c='g', ls="solid", label='SP-GNN-SAGE', marker=".")
plt.plot(sage_baseline1_df['epoch_no'], sage_baseline1_df[type],
         c='b', ls="solid", label='SP-BASE1-SAGE', marker="v")
plt.plot(sage_baseline2_df['epoch_no'], sage_baseline2_df[type],
         c='r', ls="solid", label='SP-BASE2-SAGE', marker="s")

plt.plot(gcn_df['epoch_no'], gcn_df[type],
         c='k', ls="solid", label='SP-GNN-GCN', marker="+")
plt.plot(gcn_baseline1_df['epoch_no'], gcn_baseline1_df[type],
         c='c', ls="solid", label='SP-BASE1-GCN', marker="x")
plt.plot(gcn_baseline2_df['epoch_no'], gcn_baseline2_df[type],
         c='k', ls="solid", label='SP-BASE2-GCN', marker="D")

plt.ylabel('Test Loss value', **axis_font)
# plt.xlabel(r'$\tau$ (kilometers)', **axis_font)
plt.xlabel('epoch number', **axis_font)
# plt.xlabel('# of objects (in thousands)', **axis_font)
plt.legend(loc=0, framealpha=0.3, fontsize="xx-small")
# plt.ticklabel_format(style='sci', axis='y', scilimits=(0, 0))
plt.savefig('SkylineGNN/C9_NY_5K_{}_Converge.pdf'.format(type), bbox_inches='tight')
# plt.show()
