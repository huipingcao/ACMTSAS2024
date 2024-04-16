import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import sys

font = {'family': 'Arial', 'weight': 'normal', 'size': 30}
matplotlib.rc('font', **font)
axis_font = {'fontname': 'Arial', 'size': '35'}

graph = "C9_BAY"

fig, ax = plt.subplots()
fig.set_figheight(8)
fig.set_figwidth(20)

labels = ['10K', '20K', '30K', '40K', '60K', '60K', '70K', '80K', '100K']
''' grouped values '''

G1_v1_method_value = [0.9381, 1.0339, 1.05]
G1_v2_method_value = [0.9537, 0.8783, 0.8815]
G1_v3_method_value = [0.9281, 0.8909, 0.898]

G2_v1_method_value = [1.0864, 1.2314, 1.1872]
G2_v2_method_value = [0.9864, 0.9692, 0.9741]
G2_v3_method_value = [1.0001, 0.9688, 0.9771]

G3_v1_method_value = [1.2204, 1.2872, 1.2885]
G3_v2_method_value = [1.0252, 0.9886, 0.9876]
G3_v3_method_value = [1.0478, 1.0141, 0.9853]

G4_v1_method_value = [1.4654, 1.689, 1.6602]
G4_v2_method_value = [0.9833, 0.954, 0.9663]
G4_v3_method_value = [0.935, 0.9341, 0.9316]

G5_v1_method_value = [1.5381, 1.4155, 1.3689]
G5_v2_method_value = [0.9111, 0.9329, 0.9375]
G5_v3_method_value = [1.0268, 0.945, 0.9451]

G6_v1_method_value = [2.3467, 2.3861, 2.3984]
G6_v2_method_value = [1.1507, 1.0287, 1.0089]
G6_v3_method_value = [1.1234, 1.0288, 1.0166]

G7_v1_method_value = [2.2149, 1.7758, 1.8063]
G7_v2_method_value = [1.1898, 1.0224, 1.0162]
G7_v3_method_value = [1.371, 1.0238, 0.9977]

G8_v1_method_value = [2.0781, 2.4816, 2.5031]
G8_v2_method_value = [1.0733, 1.0599, 1.0602]
G8_v3_method_value = [1.0648, 1.0227, 1.0193]

G9_v1_method_value = [1.4679, 1.7395, 1.7778]
G9_v2_method_value = [0.9844, 0.937, 0.9304]
G9_v3_method_value = [0.9962, 0.9628, 0.9764]


'''
===============================================================
'''

x = np.arange(len(labels))  # the label locations
width = 0.1  # the width of the bars
print(x)

# draw groups
g1_x = [x[0]+i*width-4.5*width for i in range(3)]
g2_x = [x[1]+i*width-4.5*width for i in range(3)]
g3_x = [x[2]+i*width-4.5*width for i in range(3)]
g4_x = [x[3]+i*width-4.5*width for i in range(3)]
g5_x = [x[4]+i*width-4.5*width for i in range(3)]
g6_x = [x[5]+i*width-4.5*width for i in range(3)]
g7_x = [x[6]+i*width-4.5*width for i in range(3)]
g8_x = [x[7]+i*width-4.5*width for i in range(3)]
g9_x = [x[8]+i*width-4.5*width for i in range(3)]


g1_x_position = g1_x+g2_x+g3_x+g4_x+g5_x+g6_x+g7_x+g8_x+g9_x
g2_x_position = [i+3*width for i in g1_x_position]
g3_x_position = [i+6*width for i in g1_x_position]
v1_values = G1_v1_method_value+G2_v1_method_value+G3_v1_method_value+G4_v1_method_value + \
    G5_v1_method_value+G6_v1_method_value+G7_v1_method_value + \
    G8_v1_method_value+G9_v1_method_value
v2_values = G1_v2_method_value+G2_v2_method_value+G3_v2_method_value+G4_v2_method_value + \
    G5_v2_method_value+G6_v2_method_value+G7_v2_method_value + \
    G8_v2_method_value+G9_v2_method_value
v3_values = G1_v3_method_value+G2_v3_method_value+G3_v3_method_value+G4_v3_method_value + \
    G5_v3_method_value+G6_v3_method_value+G7_v3_method_value + \
    G8_v3_method_value+G9_v3_method_value

print(g1_x_position)


rects_g1_v1 = ax.bar(g1_x_position, v1_values, width,
                     label='BBS', align='edge', alpha=.99, linewidth=1, edgecolor="black")
rects_g1_v2 = ax.bar(g2_x_position, v2_values, width,
                     label='TSP-GNN-GCN', align='edge', alpha=.99, linewidth=1, edgecolor="black", hatch="\\")
rects_g1_v3 = ax.bar(g3_x_position, v3_values, width, label='TSP-GNN-SAGE',
                     align='edge', alpha=.99, linewidth=1, edgecolor="black", hatch="/")


# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_xlabel('Size of subgraphs of C9_BAY')
ax.set_ylabel('RAC')
# ax.set_title('Comparison on {}'.format(graph))
ax.set_xticks(x)
ax.set_xticklabels(labels)
ax.legend(fontsize="large", loc=0, handletextpad=0.1, labelspacing=.1)

fig.tight_layout()

plt.xlim(0-6*width, len(x)-width)
plt.ylim(0.75, 2.8)
plt.axhline(y=1, color='k', linestyle='dashed')
# plt.show()
plt.savefig("""SkylineGNN/C9_BAY_30K_RAC.pdf""", bbox_inches='tight')
