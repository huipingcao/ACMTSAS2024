import sys

import numpy as np
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

#ED similarity
# data_str = """0 1.1369
# 10 1.1251
# 20 1.1071
# 30 1.0446
# 40 1.0742
# 50 1.1212
# 80 1.112
# 100 1.1634
# 120 1.3099
# 150 1.291"""

#Cosine Similarity
data_str = """0 0.8877
10 0.8968
20 0.8756
30 0.8827
40 0.8834
50 0.8903
80 0.8719
100 0.8689
120 0.8724
150 0.8654"""

trend = pd.read_csv(io.StringIO(data_str), delimiter=" ", header=None)
print(type(trend))
print(trend.iloc[:, 1].astype('float64'))

line1 = ax.plot(trend.iloc[:, 0], trend.iloc[:, 1], c='r', marker='o', ls="-", label='Cosine sim.', ms=18, lw=4)
ax.set_ylabel('Goodness (Cosine sim.)', **axis_font)
ax.set_xlabel('$m_{min}$', **axis_font)

ax.set_xticks(np.arange(0,160,10))
ax.set_xticklabels(['0','','','','','50','','','','','100','','','','','150'])

ax.legend(fontsize="x-large",loc=0)
ax.set_ylim(0.7, 1)

# plt.show()
plt.savefig('index_construction_varying_msize_15k_cosine.pdf', bbox_inches='tight')
