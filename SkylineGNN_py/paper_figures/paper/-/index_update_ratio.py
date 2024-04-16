import sys

import numpy
import matplotlib.pyplot as plt
import matplotlib
import os
import io
import pandas as pd

font = {'family': 'Arial', 'weight': 'normal', 'size': 30}
matplotlib.rc('font', **font)
axis_font = {'fontname': 'Arial', 'size': '35'}

fig, ax = plt.subplots()
fig.set_figheight(8)
fig.set_figwidth(10)

folder_name = os.path.dirname(__file__)

### construction time and # of nodes in real world
# data_str = """NY 94.99725 254346
# BAY 89.58865 321270
# COL 147.19445 435666
# FLA 379.3793 1070376"""

### construction time and ratio with construction time in real world
# data_str = """NY 94.99725 2.873752113
# BAY 89.58865 2.931462173
# COL 147.19445 3.343783931
# FLA 379.3793 3.139791405"""

### construction time and ratio with construction time in sub-graphs
data_str = """5K 11.32595 17.72948561
10K 22.7187 14.61865143
15K 16.38155 10.49090618
22K 22.0033 8.20281016"""

trend = pd.read_csv(io.StringIO(data_str), delimiter=" ", header=None)
print(type(trend))
print(trend.iloc[:, 1].astype('float64'))

line1 = ax.plot(trend.iloc[:, 0], trend.iloc[:, 1], c='r', marker='o', ls="-", label='construction time', ms=18, lw=4)
ax.set_ylabel('Updating time (Sec.)', **axis_font)
# ax.set_xlabel('# of nodes in sub-graphs of C9_NY', **axis_font)
# ax.ticklabel_format(style='sci', axis='y', scilimits=(0, 0))

ax2 = ax.twinx()
color = 'tab:blue'
# we already handled the x-label with ax1
ax2.set_ylabel('Upd. time/ Constr. Time (%)', color=color)
line2 = ax2.plot(trend.iloc[:, 0], trend.iloc[:, 2], c='g', marker='s', fillstyle='none', markeredgewidth="4", ls="--",
                 label='index size', ms=18, lw=4)
ax2.tick_params(axis='y', labelcolor=color)
# ax2.ticklabel_format(style='sci', axis='y', scilimits=(0, 0))

ax.legend(line1 + line2, ['Updating time', 'Upd. time/ Constr. Time'], loc=8, fontsize='x-small')

# plt.show()
plt.savefig('index_update_sub_graphs_ny.pdf', bbox_inches='tight')
