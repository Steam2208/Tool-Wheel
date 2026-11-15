import math
import os
from PIL import Image, ImageDraw

OUTPUT_DIR = r"d:\PrismLauncher\instances\1.21.1(1)\minecraft\mods_source\satisfactory_wheel\icons"
os.makedirs(OUTPUT_DIR, exist_ok=True)

SCALE = 4
SIZE = 512 * SCALE
CENTER = SIZE // 2

BG_COLOR = (24, 27, 31, 255)         # Deep slate charcoal
ORANGE_ACCENT = (250, 149, 73, 255)   # Satisfactory FICSIT Orange
WHITE = (245, 247, 250, 255)
SLATE_LIGHT = (70, 78, 88, 255)
SLATE_DARK = (40, 45, 52, 255)

def draw_rounded_rect(draw, bbox, radius, fill, outline=None, width=0):
    draw.rounded_rectangle(bbox, radius=radius, fill=fill, outline=outline, width=width)

def draw_wrench(draw, cx, cy, length, head_radius, angle_deg, color):
    rad = math.radians(angle_deg)
    cos_a, sin_a = math.cos(rad), math.sin(rad)
    
    # Shaft
    shaft_w = head_radius * 0.45
    shaft_len = length
    p1 = (cx - shaft_len/2 * cos_a - shaft_w/2 * -sin_a, cy - shaft_len/2 * sin_a - shaft_w/2 * cos_a)
    p2 = (cx + shaft_len/2 * cos_a - shaft_w/2 * -sin_a, cy + shaft_len/2 * sin_a - shaft_w/2 * cos_a)
    p3 = (cx + shaft_len/2 * cos_a + shaft_w/2 * -sin_a, cy + shaft_len/2 * sin_a + shaft_w/2 * cos_a)
    p4 = (cx - shaft_len/2 * cos_a + shaft_w/2 * -sin_a, cy - shaft_len/2 * sin_a + shaft_w/2 * cos_a)
    draw.polygon([p1, p2, p3, p4], fill=color)

    # Heads on both ends
    for sign in [-1, 1]:
        hx = cx + sign * (shaft_len/2) * cos_a
        hy = cy + sign * (shaft_len/2) * sin_a
        draw.ellipse([hx - head_radius, hy - head_radius, hx + head_radius, hy + head_radius], fill=color)
        # Jaw cut
        jaw_r = head_radius * 0.55
        jaw_cut_angle = angle_deg + (0 if sign == 1 else 180)
        jrad = math.radians(jaw_cut_angle)
        j_cos, j_sin = math.cos(jrad), math.sin(jrad)
        jx = hx + (head_radius * 0.3) * j_cos
        jy = hy + (head_radius * 0.3) * j_sin
        # Cut opening with background color
        draw.polygon([
            (jx - jaw_r * -j_sin, jy - jaw_r * j_cos),
            (jx + jaw_r * -j_sin, jy + jaw_r * j_cos),
            (jx + jaw_r * 2 * j_cos + jaw_r * -j_sin, jy + jaw_r * 2 * j_sin + jaw_r * j_cos),
            (jx + jaw_r * 2 * j_cos - jaw_r * -j_sin, jy + jaw_r * 2 * j_sin - jaw_r * j_cos)
        ], fill=BG_COLOR)

# --- ICON 1: FICSIT Radial Dial ---
def make_icon_1():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    margin = 40 * SCALE
    draw_rounded_rect(draw, [margin, margin, SIZE - margin, SIZE - margin], radius=90 * SCALE, fill=BG_COLOR)

    # Radial segmented ring
    r_outer = 175 * SCALE
    r_inner = 135 * SCALE
    num_sectors = 8
    gap_deg = 8

    for i in range(num_sectors):
        start_deg = i * (360 / num_sectors) + gap_deg / 2 - 90
        end_deg = (i + 1) * (360 / num_sectors) - gap_deg / 2 - 90
        
        # Sector 0 and 1 are orange (active selection like in Satisfactory)
        color = ORANGE_ACCENT if i == 0 else SLATE_DARK
        border = WHITE if i == 0 else SLATE_LIGHT
        
        # Draw arc with thickness
        draw.arc([CENTER - r_outer, CENTER - r_outer, CENTER + r_outer, CENTER + r_outer],
                 start=start_deg, end=end_deg, fill=color, width=int(r_outer - r_inner))

    # Center circle
    r_center = 85 * SCALE
    draw.ellipse([CENTER - r_center, CENTER - r_center, CENTER + r_center, CENTER + r_center],
                 fill=SLATE_DARK, outline=ORANGE_ACCENT, width=6 * SCALE)
    
    # Center Wrench (Orange & White)
    draw_wrench(draw, CENTER, CENTER, length=90 * SCALE, head_radius=32 * SCALE, angle_deg=45, color=ORANGE_ACCENT)

    # Resize with high quality downsampling
    img_final = img.resize((512, 512), Image.Resampling.LANCZOS)
    img_final.save(os.path.join(OUTPUT_DIR, "icon_option_1_radial_dial.png"))

# --- ICON 2: Minimalist Hexagonal Industrial Dial ---
def make_icon_2():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    margin = 40 * SCALE
    draw_rounded_rect(draw, [margin, margin, SIZE - margin, SIZE - margin], radius=90 * SCALE, fill=(20, 23, 26, 255))

    # Outer thin circular track
    r_track = 180 * SCALE
    draw.ellipse([CENTER - r_track, CENTER - r_track, CENTER + r_track, CENTER + r_track],
                 outline=SLATE_DARK, width=8 * SCALE)

    # 4 Cardinal selector pods
    num_pods = 6
    pod_r = 180 * SCALE
    for i in range(num_pods):
        angle = math.radians(i * (360 / num_pods) - 90)
        px = CENTER + pod_r * math.cos(angle)
        py = CENTER + pod_r * math.sin(angle)
        rad = 22 * SCALE
        col = ORANGE_ACCENT if i in [0, 1] else SLATE_LIGHT
        draw.ellipse([px - rad, py - rad, px + rad, py + rad], fill=col)

    # Center Gear / Cog
    cog_r = 95 * SCALE
    draw.ellipse([CENTER - cog_r, CENTER - cog_r, CENTER + cog_r, CENTER + cog_r], fill=BG_COLOR, outline=ORANGE_ACCENT, width=10 * SCALE)
    
    # Clean angled tool inside
    draw_wrench(draw, CENTER, CENTER, length=100 * SCALE, head_radius=35 * SCALE, angle_deg=-45, color=WHITE)

    # Orange accent pip
    draw.ellipse([CENTER - 16*SCALE, CENTER - 16*SCALE, CENTER + 16*SCALE, CENTER + 16*SCALE], fill=ORANGE_ACCENT)

    img_final = img.resize((512, 512), Image.Resampling.LANCZOS)
    img_final.save(os.path.join(OUTPUT_DIR, "icon_option_2_industrial_hex.png"))

# --- ICON 3: Modern Flat Vector Selector ---
def make_icon_3():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    margin = 40 * SCALE
    draw_rounded_rect(draw, [margin, margin, SIZE - margin, SIZE - margin], radius=90 * SCALE, fill=(28, 32, 38, 255))

    # Bold 3/4 circular orange sweep
    r_arc = 160 * SCALE
    draw.arc([CENTER - r_arc, CENTER - r_arc, CENTER + r_arc, CENTER + r_arc],
             start=-120, end=150, fill=ORANGE_ACCENT, width=32 * SCALE)

    # Pointer triangle at active sector
    p_angle = math.radians(-120)
    tip_x = CENTER + (r_arc + 36 * SCALE) * math.cos(p_angle)
    tip_y = CENTER + (r_arc + 36 * SCALE) * math.sin(p_angle)
    draw.polygon([
        (tip_x, tip_y),
        (tip_x + 30*SCALE * math.cos(p_angle + 2.5), tip_y + 30*SCALE * math.sin(p_angle + 2.5)),
        (tip_x + 30*SCALE * math.cos(p_angle - 2.5), tip_y + 30*SCALE * math.sin(p_angle - 2.5))
    ], fill=WHITE)

    # Inner Dark Plate
    r_inner = 100 * SCALE
    draw.ellipse([CENTER - r_inner, CENTER - r_inner, CENTER + r_inner, CENTER + r_inner],
                 fill=(20, 22, 26, 255), outline=SLATE_LIGHT, width=6 * SCALE)

    # Modern cross-tool silhouette (Wrench + Screwdriver)
    draw_wrench(draw, CENTER, CENTER, length=95 * SCALE, head_radius=32 * SCALE, angle_deg=30, color=ORANGE_ACCENT)

    img_final = img.resize((512, 512), Image.Resampling.LANCZOS)
    img_final.save(os.path.join(OUTPUT_DIR, "icon_option_3_modern_flat.png"))

make_icon_1()
make_icon_2()
make_icon_3()
print("All 3 icons generated successfully!")

