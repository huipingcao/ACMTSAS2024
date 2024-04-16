

edge_path = '/Data/datasets/L_CAL/CAL.cnode.txt'


with open(edge_path) as f:
    content = f.readlines()

print(f'Node #: {len(content)}.')
