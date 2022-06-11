import math
import matplotlib.pyplot as plt
import pandas as pd
import sys
from os import listdir
from os.path import isfile, join, abspath

"""
Checks the principle of conservation of energy for results

Expected arguments:
 argv(1)  -  name of dir with results
"""

RESULTS_DIR = "../results/"
CSV_DELIMITER = ';'
G = 6.67408e-11

def count_KE(row):
    # KE = m * (v_x^2 + v_y^2) / 2
    return row['mass'] * (row['v_x'] ** 2 + row['v_y'] ** 2) / 2


def count_PE(row1, rows):
    # PE = sum(PE for each pair of bodies)
    return rows.apply(
        lambda row2: _count_PE(row1, row2),
        axis=1
    ).sum() / 2


def _count_PE(row1, row2):
    # PE = -G * m * M / r
    r = count_r(row1['pos_x'], row1['pos_y'], row2['pos_x'], row2['pos_y'])
    return -G * row1['mass'] * row2['mass'] / r if r > 100 else 0


def count_r(x_1, y_1, x_2, y_2):
    # r = sqrt((x_1 - x_2)^2 + (y_1 - y_2)^2)
    return math.sqrt((x_1 - x_2) ** 2 + (y_1 - y_2) ** 2)


dir_path = join(RESULTS_DIR, sys.argv[1])
files = [abspath(join(dir_path, f)) for f in listdir(dir_path) if isfile(join(dir_path, f))]
df = pd.DataFrame(columns=['id', 'mass', 'pos_x', 'pos_y', 'v_x', 'v_y', 'timestamp'])

for file_name in files:
    data = pd.read_csv(file_name, sep=CSV_DELIMITER, header=0, names=['id', 'mass', 'pos_x', 'pos_y', 'v_x', 'v_y', 'timestamp'])
    df = pd.concat([df, data])

points_count = df['id'].nunique()
timestamp = df['timestamp'].min()
steps = int(df['timestamp'].max() / timestamp)

curr_timestamp = timestamp
energy_df = pd.DataFrame([], columns=["KE", "PE", "E"])
for i in range(steps):
    curr_rows = df.loc[df['timestamp'] == curr_timestamp]

    KE = curr_rows.apply(count_KE, axis=1).sum()
    PE = curr_rows.apply(lambda row: count_PE(row, curr_rows), axis=1).sum()
    E = KE + PE

    energy_df = pd.concat([energy_df, pd.DataFrame([{"KE": KE, "PE": PE, "E": E}])], ignore_index=True)
    curr_timestamp += timestamp
    print(f"[{i} / {steps}]   E: {E}    KE: {KE}    PE: {PE}")

energy_df["E"].to_csv(f'E_{sys.argv[1]}.csv', index=False)

fig, (ax1, ax2) = plt.subplots(2)
energy_df.plot(ax=ax1)
energy_df.plot(ax=ax2, y="E")
plt.show()
