import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import matplotlib.patches as mpatches

font = {'family': 'Arial', 'weight': 'normal', 'size': 30}
matplotlib.rc('font', **font)
axis_font = {'fontname': 'Arial', 'size': '35'}
axis_font_x = {'fontname': 'Arial', 'size': '18'}

graph = "C9_NY_5K"

fig, ax = plt.subplots()
fig.set_figheight(8)
fig.set_figwidth(20)
fig.tight_layout()

labels = ['Backbone', 'SAGE', 'GCN', 'Trans', 'SAGE-dis', 'GCN-dis', 'Trans-dis',
          'SP-BASE1-SAGE', 'SP-BASE1-GCN', 'SP-BASE2-SAGE', 'SP-BASE2-GCN', '128-SAGE', '512-SAGE', '128-GCN', '512-GCN']

# euclidean_distance 15k
# v1_method_value = [1.2391, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
# v2_method_value = [1.4117, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
# v3_method_value = [1.4303, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
data = {}

#C9_NY_5K
data["Backbone"] = [1.2391, 1.4117, 1.4303]
data["SAGE"] = [1.0018, 1.0001, 0.9989]
data["GCN"] = [0.9966, 0.9979, 0.9984]
data["Trans"] = [0.9959, 0.9992, 0.9979]
data["SAGE-dis"] = [0.9948, 0.9976, 0.9954]
data["GCN-dis"] = [1.1018, 1.0697, 1.0848]
data["Trans-dis"] = [0.9959, 0.9976, 0.9972]
data["SP-BASE1-SAGE"] = [0.8884, 0.8135, 0.8169]
data["SP-BASE1-GCN"] = [0.811, 0.7667, 0.7645]
data["SP-BASE2-SAGE"] = [0.9951, 0.9976, 0.998]
data["SP-BASE2-GCN"] = [0.9963, 0.9985, 0.9978]
data["128-GCN"] = [1.0078, 1.02, 1.0096]
data["512-GCN"] = [0.9995, 1.0005, 0.9984]
data["128-SAGE"] = [0.9946, 1.0027, 0.9963]
data["512-SAGE"] = [1.0108, 1.0055, 1.0077]

#C9_NY_15K
# data["Backbone"] = [1.084475, 1.14555, 1.14915]
# data["SAGE"] = [1.0252, 1.0105, 1.0154]
# data["GCN"] = [1.0386, 1.0195, 1.0229]
# data["Trans"] = [1.0481, 1.0346, 1.0366]
# data["SAGE-dis"] = [1.0284, 1.0086, 1.0127]
# data["GCN-dis"] = [0.9319, 0.9573, 0.9579]
# data["Trans-dis"] = [1.0497, 1.024, 1.0287]
# data["SP-BASE1-SAGE"] = [0.8256, 0.7584, 0.7608]
# data["SP-BASE1-GCN"] = [0.8496, 0.7699, 0.7712]
# data["SP-BASE2-SAGE"] = [1.0279, 1.0068, 1.0084]
# data["SP-BASE2-GCN"] = [1.0104, 0.9999, 1.0069]
# data["128-GCN"] = [1.1349, 1.0394, 1.0444]
# data["512-GCN"] = [1.0595, 1.0291, 1.0268]
# data["128-SAGE"] = [1.1266, 1.0527, 1.0582]
# data["512-SAGE"] = [1.0391, 1.0129, 1.0154]



x = np.arange(len(labels))  # the label locations
x = [i+i*0.25 for i in x]
print(x)
width = 0.3  # the width of the bars

for i, l in enumerate(labels):
    l_data = data[l]
    height = max(l_data)
    ax.bar(x[i] - width, l_data[0], width, align='center',
           hatch="\\", alpha=.99, color="tab:blue")
    ax.bar(x[i], l_data[1], width, align='center',
           hatch="/", alpha=.99, color="tab:green")
    ax.bar(x[i] + width, l_data[2], width,
           align='center', hatch="-", alpha=.99, color="tab:orange")
    ax.annotate('{}'.format(l), xy=(x[i], height),
                xytext=(0, 5), textcoords="offset points", ha='center', va='bottom', size=20, weight='bold', rotation=60)

leg_dim_1 = mpatches.Patch(
    facecolor='tab:blue', hatch="\\", label='RAC on dimmension 1', alpha=1)
leg_dim_2 = mpatches.Patch(
    facecolor='tab:green', hatch="/", label='RAC on dimmension 2', alpha=1)
leg_dim_3 = mpatches.Patch(
    facecolor='tab:orange', hatch="-", label='RAC on dimmension 3', alpha=1)

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_xlabel('Models')
ax.set_ylabel('RAC')
# ax.set_ylabel('Goodness (cosine sim.)')
ax.set_xticks(x)
ax.tick_params(labelbottom=False)
plt.axhline(y=1, color='k', linestyle='dashed')


# ax.get_xaxis().set_visible(False)
# ax.set_xticklabels(labels, **axis_font_x)
# plt.xticks(rotation=45)
plt.legend(handles=[leg_dim_1, leg_dim_2, leg_dim_3],
           fontsize="x-small", loc=0, handletextpad=0.1, labelspacing=.1)


fig.tight_layout()

plt.xlim(0 - 2 * width, len(x)+(len(x)-3)*0.25)
# plt.ylim(0.80, 0.98)
plt.ylim(0.65, 1.7)

# plt.show()
plt.savefig("""SkylineGNN/RAC_Comparison_C9_NY_5K.pdf""",bbox_inches='tight')
# plt.savefig("""SkylineGNN/RAC_Comparison_C9_NY_15K.pdf""")
