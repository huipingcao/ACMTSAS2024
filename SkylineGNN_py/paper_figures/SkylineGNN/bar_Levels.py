import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import matplotlib.patches as mpatches

font = {'family': 'Arial', 'weight': 'normal', 'size': 35}
matplotlib.rc('font', **font)
axis_font = {'fontname': 'Arial', 'size': '35'}
axis_font_x = {'fontname': 'Arial', 'size': '35'}

graph = "C9_NY_5K"

fig, ax = plt.subplots()
fig.set_figheight(8)
fig.set_figwidth(25)

labels = ['BBS', 'Level4', 'Level5', 'Level6', 'Level7', 'Level8', 'Level9', 'SP-GNN']

data = {}

#C9_NY_30K_GCN
data["BBS"] = [1.1805, 1.2762, 1.2928]
data["Level4"] = [1.0116, 0.961, 0.9696]
data["Level5"] = [1.0114, 0.9707, 0.9737]
data["Level6"] = [1.0247, 0.9941, 0.9995]
data["Level7"] = [1.0138, 0.9958, 0.9946]
data["Level8"] = [1.0083, 0.9812, 0.9834]
data["Level9"] = [1.0091, 1.0008, 1.0072]
data["SP-GNN"] = [0.8971, 0.9094, 0.9154]

#C9_NY_30K_SAGE
# data["BBS"] = [1.1805, 1.2762, 1.2928]
# data["Level4"] = [0.9944, 0.9581, 0.9562]
# data["Level5"] = [1.0178, 0.9855, 0.9883]
# data["Level6"] = [1.0134, 0.99, 0.995]
# data["Level7"] = [1.0162, 1.0023, 0.9971]
# data["Level8"] = [1.0111, 0.9909, 0.9932]
# data["Level9"] = [1.0192, 1.0138, 1.0185]
# data["SP-GNN"] = [0.9171, 0.9423, 0.953]



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
    # ax.annotate('{}'.format(l), xy=(x[i], height),
    #             xytext=(0, 5), textcoords="offset points", ha='center', va='bottom', size=20, weight='bold', rotation=60)

leg_dim_1 = mpatches.Patch(
    facecolor='tab:blue', hatch="\\", label='RAC on dimmension 1', alpha=1)
leg_dim_2 = mpatches.Patch(
    facecolor='tab:green', hatch="/", label='RAC on dimmension 2', alpha=1)
leg_dim_3 = mpatches.Patch(
    facecolor='tab:orange', hatch="-", label='RAC on dimmension 3', alpha=1)

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_xlabel('Trained on backbone graphs of different levels')
ax.set_ylabel('RAC')
# ax.set_ylabel('Goodness (cosine sim.)')
ax.set_xticks(x)
# ax.tick_params(labelbottom=False)
plt.axhline(y=1, color='k', linestyle='dashed')


# ax.get_xaxis().set_visible(False)
ax.set_xticklabels(labels, **axis_font_x)
# plt.xticks(rotation=45)
plt.legend(handles=[leg_dim_1, leg_dim_2, leg_dim_3], loc=0, handletextpad=0.1, labelspacing=.1)


fig.tight_layout()

plt.xlim(0 - 2 * width, len(x)+(len(x)-3)*0.25)
# plt.ylim(0.80, 0.98)
plt.ylim(0.75, 1.4)

# plt.show()
plt.savefig("""SkylineGNN/C9_NY_30K_GCN_Levels.pdf""")
