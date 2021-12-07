import math, sys

# python circle-gen.py <points_count> <r> <mass >
# python circle-gen.py 10 1e12 1e25

def get_pos(i, r, degree):
    x = r * math.cos(i * degree)
    y = r * math.sin(i * degree)
    return x, y

def get_v_vec(x, y, v):
    sin_L = y / math.sqrt(x**2 + y**2)
    cos_L = x / math.sqrt(x**2 + y**2)
    v_x = -1 * v * sin_L
    v_y = v * cos_L
    return v_x, v_y


points_count = int(sys.argv[1])
system_mass = float(sys.argv[3])

degree = 2.0 * math.pi / points_count

G = 6.67408e-11
r = float(sys.argv[2])
body_mass = system_mass / points_count
body_velocity = math.sqrt(G * system_mass / r)

bodies = []
for i in range(0, points_count):
    x, y = get_pos(i, r, degree)
    v_x, v_y = get_v_vec(x, y, body_velocity)
    bodies.append([body_mass, x, y, v_x, v_y])



bodies_str = '\n'.join([' '.join(map(str, body)) for body in bodies])

with open("../results/circle.txt", "w") as output_file:
    output_file.write(bodies_str)