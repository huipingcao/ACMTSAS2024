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
# data_str = """100 1.203
# 200 1.113325
# 300 1.24485
# 400 1.186233333
# 500 1.3529
# 600 1.258
# 700 1.3217
# 800 1.3489
# 1000 1.31165"""

#Cosine Similarity
data_str = """100 0.8859
200 0.878275
300 0.88905
400 0.8708
500 0.86735
600 0.8832
700 0.86045
800 0.8712333333
1000 0.87315"""

trend = pd.read_csv(io.StringIO(data_str), delimiter=" ", header=None)
print(type(trend))
print(trend.iloc[:, 1].astype('float64'))

line1 = ax.plot(trend.iloc[:, 0], trend.iloc[:, 1], c='r', marker='o', ls="-", label='Cosine sim.', ms=18, lw=4)
ax.set_ylabel('Goodness (Cosine sim.)', **axis_font)
ax.set_xlabel('$m_{max}$', **axis_font)

ax.set_xticks(np.arange(100,1100,100))
ax.set_xticklabels(['100','','','','500','','','','','1000'])

ax.legend(fontsize="x-large",loc=0)
ax.set_ylim(0.7, 1)


# plt.show()
plt.savefig('index_construction_varying_m_15k_cosine.pdf', bbox_inches='tight')
