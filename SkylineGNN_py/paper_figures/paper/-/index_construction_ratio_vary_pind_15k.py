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

# #ED similarity
# data_str = """
# 0 1.1377
# 10 1.1766
# 20 1.0698
# 30 1.1033
# 40 1.2563
# 50 1.2311"""

#Cosine Similarity 
data_str = """
0 0.891248618
10 0.861370371
20 0.877833197
30 0.878200894
40 0.875645993
50 0.874485825"""

trend = pd.read_csv(io.StringIO(data_str), delimiter=" ", header=None)
print(type(trend))
print(trend.iloc[:, 1].astype('float64'))

line1 = ax.plot(trend.iloc[:, 0], trend.iloc[:, 1], c='r', marker='o', ls="-", label='Cosine sim.', ms=18, lw=4)
ax.set_ylabel('Goodness (Cosine sim.)', **axis_font)
ax.set_xlabel('$p_{ind}$', **axis_font)

plt.xticks(trend.iloc[:, 0])

ax.legend(fontsize="x-large",loc=0)
ax.set_ylim(0.7, 1)


# plt.show()
plt.savefig('index_construction_varying_pind_15k_cosine.pdf', bbox_inches='tight')
