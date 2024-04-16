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

labels = ['ANTI', 'CORR', 'NONE']
''' grouped values '''

#BAY
G1_v1_method_value = [1.3058, 1.1508, 1.141]
G1_v2_method_value = [0.959, 0.9884, 0.9924]
G1_v3_method_value = [0.9525, 0.9992, 1.0044]

G2_v1_method_value = [1.1819, 1.1003, 1.1091]
G2_v2_method_value = [1.0378, 1.0019, 1.0006]
G2_v3_method_value = [1.0334, 0.9835, 0.9823]

G3_v1_method_value = [1.1851, 1.3354, 1.284]
G3_v2_method_value = [0.9816, 0.9676, 0.9707]
G3_v3_method_value = [0.9648, 0.9584, 0.9659]



#NY
# G1_v1_method_value = [1.4042, 1.3257, 1.3247]
# G1_v2_method_value = [1.0176, 0.9603, 0.9623]
# G1_v3_method_value = [1.0164, 0.962, 0.9625]

# G2_v1_method_value = [1.581, 1.5077, 1.5414]
# G2_v2_method_value = [1.0023, 0.9775, 0.968]
# G2_v3_method_value = [0.9758, 0.9356, 0.9271]

# G3_v1_method_value = [1.2187, 1.2527, 1.2622]
# G3_v2_method_value = [1.0026, 0.9882, 0.9804]
# G3_v3_method_value = [0.989, 0.9762, 0.9697]


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

g1_x_position = g1_x+g2_x+g3_x
g2_x_position = [i+3*width for i in g1_x_position]
g3_x_position = [i+6*width for i in g1_x_position]
v1_values = G1_v1_method_value+G2_v1_method_value+G3_v1_method_value
v2_values = G1_v2_method_value+G2_v2_method_value+G3_v2_method_value
v3_values = G1_v3_method_value+G2_v3_method_value+G3_v3_method_value

print(g1_x_position)


rects_g1_v1 = ax.bar(g1_x_position, v1_values, width,
                     label='BBS', align='edge', alpha=.99, linewidth=1, edgecolor="black")
rects_g1_v2 = ax.bar(g2_x_position, v2_values, width,
                     label='TSP-GNN-GCN', align='edge', alpha=.99, linewidth=1, edgecolor="black", hatch="\\")
rects_g1_v3 = ax.bar(g3_x_position, v3_values, width, label='TSP-GNN-SAGE',
                     align='edge', alpha=.99, linewidth=1, edgecolor="black", hatch="/")


# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_xlabel('Edge weights relationship')
ax.set_ylabel('RAC')
# ax.set_title('Comparison on {}'.format(graph))
ax.set_xticks(x)
ax.set_xticklabels(labels)
ax.legend(fontsize="large", loc=0, handletextpad=0.1, labelspacing=.1)

fig.tight_layout()

plt.xlim(0-5*width, len(x)-5*width)
plt.ylim(0.75, 1.5)
plt.axhline(y=1, color='k', linestyle='dashed')
# plt.show()
plt.savefig("""SkylineGNN/C9_BAY_REL_RAC.pdf""")
