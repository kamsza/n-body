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
df = pd.read_csv(file, sep=CSV_DELIMITER, header=0, names=['id', 'mass', 'pos_x', 'pos_y', 'v_x', 'v_y', 'timestep'])
points_count = df['id'].nunique()
shift = 2*points_count
timesteps = list(df['timestep'].iloc[::shift])

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
result_E = []
result_p = []
result_L = []
for n in range(0, len(df) // points_count):
    curr_rows = df.iloc[n * points_count: (n + 1) * points_count]
    KE = curr_rows.apply(count_KE, axis=1).sum()
    PE = curr_rows.apply(lambda row: count_PE(row, curr_rows), axis=1).sum()
    E = KE + PE
    p = curr_rows.apply(count_momentum, axis=1).sum(axis=0).sum()
    L = curr_rows.apply(count_angular_momentum, axis=1).sum()
    result_df = result_df.append({"KE": KE, "PE": PE, "E": E, "p": p, "L": L}, ignore_index=True)
    result_E.append(E)
    result_p.append(p)
    result_L.append(L)

E_pres = []
for E_start, E_end in zip(result_E[::2], result_E[1::2]):
    E_pres.append(E_end/E_start)

print(result_p)
p_pres = []
for p_start, p_end in zip(result_p[::2], result_p[1::2]):
    p_pres.append(p_end/p_start)

L_pres = []
for L_start, L_end in zip(result_L[::2], result_L[1::2]):
    L_pres.append(L_end/L_start)

for i in range(0, len(timesteps)):
    print("{0:10d}:  {1:.20f}   |   {2:.20f}   |   {3:.20f}".format(timesteps[i], E_pres[i], p_pres[i], L_pres[i]))


# y = [E for E in E_pres if E > 0.85]
# x = [str(timestep) for timestep in timesteps][0:len(y)]
# plt.bar(x, y)
# plt.ylim([0.85, 1.01])
# plt.show()
