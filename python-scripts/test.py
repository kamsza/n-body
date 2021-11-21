import sys
import math
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from functools import reduce

RESULTS_DIR = "../results/"
CSV_DELIMITER = ' '
G = 6.67408e-11

if len(sys.argv) < 2:
    print("Specify the file path as an argument to the program")

file = RESULTS_DIR + sys.argv[1]
df = pd.read_csv(file, sep=CSV_DELIMITER, header=0, names=['id', 'mass', 'pos_x', 'pos_y', 'v_x', 'v_y', 'msg id'])
points_count = df['id'].nunique()


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
    return math.sqrt((x_1 - x_2)**2 + (y_1 - y_2)**2)


def count_momentum(row):
    # p = (m * v_x, m * v_y)
    return pd.Series([row['mass'] * row['v_x'], row['mass'] * row['v_y']])


def count_angular_momentum(row):
    # L = m * v * r
    return np.cross([row['pos_x'], row['pos_y']], [row['v_x'], row['v_y']])


result_df = pd.DataFrame([], columns=["KE", "PE", "E", "p"])
for n in range(0, len(df) // points_count):
    curr_rows = df.iloc[n * points_count: (n + 1) * points_count]
    KE = curr_rows.apply(count_KE, axis=1).sum()
    PE = curr_rows.apply(lambda row: count_PE(row, curr_rows), axis=1).sum()
    E = KE + PE
    p = curr_rows.apply(count_momentum, axis=1).sum(axis=0).sum()
    L = curr_rows.apply(count_angular_momentum, axis=1).sum()
    result_df = result_df.append({"KE": KE, "PE": PE, "E": E, "p": p, "L": L}, ignore_index=True)

fig, axs = plt.subplots(3, 2)
result_df.plot(ax=axs[0, 0], y="KE")
result_df.plot(ax=axs[0, 1], y="PE")
result_df.plot(ax=axs[1, 0], y="E")
result_df.plot(ax=axs[1, 1], y=["KE", "PE", "E"])
result_df.plot(ax=axs[2, 0], y="p")
result_df.plot(ax=axs[2, 1], y="L")
plt.show()
