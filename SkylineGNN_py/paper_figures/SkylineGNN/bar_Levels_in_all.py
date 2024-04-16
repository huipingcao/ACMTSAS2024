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

labels = ['BBS', 'Level4', 'Level5', 'Level6',
          'Level7', 'Level8', 'Level9', 'SP-GNN']

G1_v1_method_value = [1.180452941, 1.276258824, 1.292823529]

G2_v1_method_value = [1.0116, 0.961, 0.9696]
G2_v2_method_value = [0.9944, 0.9581, 0.9562]

G3_v1_method_value = [1.0114, 0.9707, 0.9737]
G3_v2_method_value = [1.0178, 0.9855, 0.9883]

G4_v1_method_value = [1.0247,0.9941, 0.9995]
G4_v2_method_value = [1.0134, 0.99, 0.995]

G5_v1_method_value = [1.0138, 0.9958, 0.9946]
G5_v2_method_value = [1.0162, 1.0023, 0.9971]

G6_v1_method_value = [1.0083, 0.9812, 0.9834]
G6_v2_method_value = [1.0111, 0.9909, 0.9932]

G7_v1_method_value = [1.0091, 1.0008, 1.0072]
G7_v2_method_value = [1.0192, 1.0138, 1.0185]

G8_v1_method_value = [0.8971, 0.9094, 0.9154]
G8_v2_method_value = [0.9171, 0.9423, 0.953]

'''
===============================================================
'''

x = np.arange(len(labels))  # the label locations
width = 0.15  # the width of the bars
print(x)

# draw groups
g1_x = [x[0]-width*2-0.2, x[0]-0.2, x[0]+2*width-0.2]
g2_x = [x[1]+i*width-4*width for i in range(3)]
g3_x = [x[2]+i*width-4*width for i in range(3)]
g4_x = [x[3]+i*width-4*width for i in range(3)]
g5_x = [x[4]+i*width-4*width for i in range(3)]
g6_x = [x[5]+i*width-4*width for i in range(3)]
g7_x = [x[6]+i*width-4*width for i in range(3)]
g8_x = [x[7]+i*width-4*width for i in range(3)]

x_label_position = [i-width for i in x]
x_label_position[0] = x[0]

g1_x_position = g1_x
g2_x_position = g2_x+g3_x+g4_x+g5_x+g6_x+g7_x+g8_x
g3_x_position = [i+3*width for i in g2_x_position]
v1_values = G2_v1_method_value+G3_v1_method_value+G4_v1_method_value + \
    G5_v1_method_value+G6_v1_method_value+G7_v1_method_value + \
    G8_v1_method_value
v2_values = G2_v2_method_value+G3_v2_method_value+G4_v2_method_value + \
    G5_v2_method_value+G6_v2_method_value+G7_v2_method_value + \
    G8_v2_method_value

print(g1_x_position)
print(g2_x_position)


rects_g1_v1 = ax.bar(g1_x_position, G1_v1_method_value, width*2,
                     label='BBS', align='center', alpha=.99, linewidth=1, edgecolor="black")
rects_g1_v2 = ax.bar(g2_x_position, v1_values, width,
                     label='TSP-GNN-GCN', align='edge', alpha=.99, linewidth=1, edgecolor="black", hatch="\\")
rects_g1_v3 = ax.bar(g3_x_position, v2_values, width, label='TSP-GNN-SAGE',
                     align='edge', alpha=.99, linewidth=1, edgecolor="black", hatch="/")


# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_xlabel('Trained on backbone graphs of different levels')
ax.set_ylabel('RAC')
# ax.set_title('Comparison on {}'.format(graph))
ax.set_xticks(x_label_position)
ax.set_xticklabels(labels)
ax.legend(fontsize="large", loc=0, handletextpad=0.1, labelspacing=.1)

fig.tight_layout()

plt.xlim(0-6*width, len(x)-3*width)
plt.ylim(0.85, 1.4)
plt.axhline(y=1, color='k', linestyle='dashed')
# plt.show()
plt.savefig("""SkylineGNN/C9_NY_30K_RAC_Levels_in_all.pdf""", bbox_inches='tight')
