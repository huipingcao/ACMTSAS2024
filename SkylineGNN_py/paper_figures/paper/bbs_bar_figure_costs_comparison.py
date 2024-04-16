import matplotlib
import matplotlib.pyplot as plt
import numpy as np


#### RAC


font = {'family': 'Arial', 'weight': 'normal', 'size': 30}
matplotlib.rc('font', **font)
axis_font = {'fontname': 'Arial', 'size': '35'}

graph = "C9_NY_15K"

fig, ax = plt.subplots()
fig.set_figheight(8)
fig.set_figwidth(10)

labels = ['200', '400', '600']
''' grouped values '''

# '''5K'''
G1_v1_method_value = [1.369127099, 1.489779884, 1.504607185]
# G1_v2_method_value = [1.547471426, 1.654349113, 1.665267749]
# G1_v3_method_value = [1.399152818, 1.523247313, 1.539989644]

# G2_v1_method_value = [1.096234355, 1.075795806, 1.07754004]
# G2_v2_method_value = [1.442752085, 1.461690470, 1.472726828]
# G2_v3_method_value = [1.712052473, 1.749465477, 1.745053295]

# G3_v1_method_value = [1.095212059, 1.121513002, 1.129150209]
# G3_v2_method_value = [1.450933058, 1.420362869, 1.460839563]
# G3_v3_method_value = [1.313705302, 1.331818766, 1.352263377]


# '''10K'''
# G1_v1_method_value = [1.422, 1.528, 1.508]
# G1_v2_method_value = [1.937, 2.269, 2.184]
# G1_v3_method_value = [1.432, 1.539, 1.518]
#
# G2_v1_method_value = [1.552, 1.697, 1.695]
# G2_v2_method_value = [2.06, 2.483, 2.428]
# G2_v3_method_value = [1.655, 1.841, 1.838]
#
# G3_v1_method_value = [1.196, 1.258, 1.267]
# G3_v2_method_value = [1.51, 1.76, 1.751]
# G3_v3_method_value = [1.676, 1.973, 1.992]


'''15K'''
# G1_v1_method_value = [1.297615033, 1.3725352, 1.376095828]
G1_v2_method_value = [1.641030721, 2.008800401, 1.961711491]
G1_v3_method_value = [1.308759644, 1.386256207, 1.388236891]

G2_v1_method_value = [1.028640778, 1.031790266, 1.031283365]
G2_v2_method_value = [1.835546711, 2.154834644, 2.127194143]
G2_v3_method_value = [1.900361636, 2.602837007, 2.52715196]

G3_v1_method_value = [1.67800807, 1.63773321, 1.64528561]
G3_v2_method_value = [1.319248603, 1.544925614, 1.538374289]
G3_v3_method_value = [1.863639317, 1.900054525, 1.893507383]


'''
===============================================================
'''

x = np.arange(len(labels))  # the label locations
width = 0.1  # the width of the bars

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

rects_g1_v1 = ax.bar(g1_x_position, v1_values, width,
                     label='none', align='edge', alpha=.99, linewidth=1, edgecolor="black")
rects_g1_v2 = ax.bar(g2_x_position, v2_values, width,
                     label='each', align='edge', alpha=.99, linewidth=1, edgecolor="black", hatch="\\")
rects_g1_v3 = ax.bar(g3_x_position, v3_values, width, label='normal',
                     align='edge', alpha=.99, linewidth=1, edgecolor="black", hatch="/")


# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_xlabel('cluster size')
ax.set_ylabel('Average cost ratio')
# ax.set_title('Comparison on {}'.format(graph))
ax.set_xticks(x)
ax.set_xticklabels(labels)
ax.legend(fontsize="large", loc=0, handletextpad=0.1, labelspacing=.1)

fig.tight_layout()

plt.xlim(0-6*width, len(x)+width)
plt.ylim(1)

# plt.show()
plt.savefig("""distance_{}_new.pdf""".format(graph))
