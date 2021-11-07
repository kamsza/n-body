import sys
import math
import pandas as pd
import matplotlib.pyplot as plt

RESULTS_DIR = "../results/"
CSV_DELIMITER = ' '
G = 6.67408e-11

if len(sys.argv) < 2:
    print("Specify the file path as an argument to the program")

file = RESULTS_DIR + sys.argv[1]
df = pd.read_csv(file, sep=CSV_DELIMITER, header=0, names=['id', 'mass', 'pos_x', 'pos_y', 'v_x', 'v_y'])
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


energy_df = pd.DataFrame([], columns=["KE", "PE", "E"])
for n in range(0, len(df) // points_count):
    curr_rows = df.iloc[n * points_count: (n + 1) * points_count]
    KE = curr_rows.apply(count_KE, axis=1).sum()
    PE = curr_rows.apply(lambda row: count_PE(row, curr_rows), axis=1).sum()
    E = KE + PE
    energy_df = energy_df.append({"KE": KE, "PE": PE, "E": E}, ignore_index=True)

fig, (ax1, ax2) = plt.subplots(2)
energy_df.plot(ax=ax1)
energy_df.plot(ax=ax2, y="E")
plt.show()
