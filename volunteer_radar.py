import matplotlib
matplotlib.use('Agg')   

import base64
import io

import matplotlib.pyplot as plt
import numpy as np
from matplotlib.patches import Circle, RegularPolygon
from matplotlib.path import Path
from matplotlib.projections import register_projection
from matplotlib.projections.polar import PolarAxes
from matplotlib.spines import Spine
from matplotlib.transforms import Affine2D

# ============================================================
# 中文字体 & 样式
# ============================================================
plt.rcParams['font.family'] = ['SimHei', 'Microsoft YaHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False      # 修复负号乱码

# ============================================================
# 五维画像定义
# ============================================================
CATEGORIES = ['累计时长(h)', '活动次数', '类型多样性', '平均评分', '准时率(%)']

# 各维度归一化参考范围
AXIS_VALUE_RANGES = {
    '累计时长(h)':  (0, 100),   # 超过 100h 按满分处理
    '活动次数':     (0, 20),    # 超过 20 场按满分处理
    '类型多样性':   (0, 10),    # 超过 10 种按满分处理
    '平均评分':     (0, 5),     # 满分 5 分
    '准时率(%)':   (0, 100),   # 0 ~ 100%
}

# 各维度真实值显示格式
VALUE_FORMATS = ['.1f', 'd', 'd', '.1f', '.1f']

# ============================================================
# 绘图参数
# ============================================================
FIG_SIZE            = (7, 6)
DPI                 = 150           # base64 / 保存分辨率
DISPLAY_DPI         = 100           # 本地 show() 分辨率
LINE_WIDTH          = 2.5
MARKER_SIZE         = 7
FILL_ALPHA          = 0.20
GRID_LEVELS         = [0.2, 0.4, 0.6, 0.8, 1.0]
GRID_COLOR          = '#AAAAAA'
GRID_LINEWIDTH      = 0.8
RADIAL_LINE_COLOR   = '#666666'
BACKGROUND_COLOR    = 'white'
VOLUNTEER_COLOR     = '#4E9AF1'     # 蓝色主调
CATEGORY_FONT_SIZE  = 11
VALUE_LABEL_FONT_SIZE = 9
VALUE_LABEL_OFFSET  = 0.07          # 数值标签偏移（归一化单位）

# ============================================================
# 本地调试用示例数据
# ============================================================
SAMPLE_PROFILE = {
    'real_name':        '张三',
    'total_hours':      45.5,   # h
    'activity_count':   8,      # 场
    'type_diversity':   5,      # 种
    'avg_score':        4.2,    # 分
    'punctuality_rate': 95.0,   # %
}


# ============================================================
# 雷达图极坐标投影（保留 UAV 模板核心实现，仅增加重复注册保护）
# ============================================================
_RADAR_REGISTERED = False   # 模块级标志，防止 Flask 多次请求重复注册

def _build_radar_factory(num_vars, frame='polygon'):
    """注册自定义极坐标投影并返回角度数组"""
    global _RADAR_REGISTERED

    theta = np.linspace(0, 2 * np.pi, num_vars, endpoint=False)

    class RadarAxes(PolarAxes):
        name = 'radar'
        RESOLUTION = 1

        def __init__(self, *args, **kwargs):
            super().__init__(*args, **kwargs)
            self.set_theta_zero_location('N')

        def fill(self, *args, closed=True, **kwargs):
            return super().fill(closed=closed, *args, **kwargs)

        def plot(self, *args, **kwargs):
            lines = super().plot(*args, **kwargs)
            for line in lines:
                self._close_line(line)
            return lines

        def _close_line(self, line):
            x, y = line.get_data()
            if x[0] != x[-1]:
                x = np.append(x, x[0])
                y = np.append(y, y[0])
                line.set_data(x, y)

        def set_varlabels(self, labels):
            self.set_thetagrids(np.degrees(theta), labels)

        def _gen_axes_patch(self):
            if frame == 'circle':
                return Circle((0.5, 0.5), 0.5)
            if frame == 'polygon':
                return RegularPolygon((0.5, 0.5), num_vars, radius=0.5, edgecolor='k')
            raise ValueError(f'Unknown frame: {frame}')

        def _gen_axes_spines(self):
            if frame == 'circle':
                return super()._gen_axes_spines()
            spine = Spine(axes=self, spine_type='circle',
                          path=Path.unit_regular_polygon(num_vars))
            spine.set_transform(Affine2D().scale(0.5).translate(0.5, 0.5) + self.transAxes)
            return {'polar': spine}

    if not _RADAR_REGISTERED:
        register_projection(RadarAxes)
        _RADAR_REGISTERED = True

    return theta


# ============================================================
# 核心工具函数
# ============================================================
def _profile_to_values(profile: dict) -> list:
    """将 profile 字典按 CATEGORIES 顺序提取为值列表"""
    return [
        float(profile.get('total_hours',       0)),
        float(profile.get('activity_count',    0)),
        float(profile.get('type_diversity',    0)),
        float(profile.get('avg_score',         0)),
        float(profile.get('punctuality_rate',  0)),
    ]


def _normalize(raw_values: list) -> list:
    """将原始值归一化到 [0, 1]"""
    result = []
    for idx, val in enumerate(raw_values):
        label = CATEGORIES[idx]
        lo, hi = AXIS_VALUE_RANGES[label]
        norm = (val - lo) / (hi - lo) if hi != lo else 0.0
        result.append(float(np.clip(norm, 0.0, 1.0)))
    return result


# ============================================================
# 主绘图函数（Flask 调用入口）
# ============================================================
def generate_radar_chart(profile: dict, to_base64: bool = True) -> str | None:
    """
    生成志愿者画像雷达图。

    Parameters
    ----------
    profile : dict
        包含以下键（均可缺省，缺省值为 0）：
          real_name, total_hours, activity_count,
          type_diversity, avg_score, punctuality_rate
    to_base64 : bool
        True  → 返回 PNG base64 字符串（Flask 线上模式）
        False → 弹出窗口预览并保存 PNG（本地调试模式）

    Returns
    -------
    str | None
        to_base64=True 时返回 base64 字符串；否则返回 None
    """
    raw_values  = _profile_to_values(profile)
    norm_values = _normalize(raw_values)
    name        = profile.get('real_name', '志愿者')

    num_dims = len(CATEGORIES)
    theta    = _build_radar_factory(num_dims, frame='polygon')
    angles   = np.concatenate((theta, [theta[0]]))

    closed_norm = norm_values + norm_values[:1]

    # ---------- 创建画布 ----------
    fig, ax = plt.subplots(
        figsize=FIG_SIZE,
        subplot_kw=dict(projection='radar'),
        dpi=DISPLAY_DPI if not to_base64 else DPI
    )
    fig.patch.set_facecolor(BACKGROUND_COLOR)
    ax.set_facecolor(BACKGROUND_COLOR)

    ax.set_title(f'{name}  志愿者画像', fontsize=13, fontweight='bold', pad=20)

    # ---------- 网格 ----------
    ax.grid(False)
    ax.set_ylim(0, 1.0)
    ax.spines['polar'].set_linestyle('-')
    ax.spines['polar'].set_linewidth(2.0)
    ax.spines['polar'].set_edgecolor('#333333')

    for level in GRID_LEVELS:
        ax.plot(
            angles, [level] * (num_dims + 1),
            color=GRID_COLOR, linewidth=GRID_LINEWIDTH,
            linestyle='-', alpha=0.8, zorder=1
        )

    for angle in theta:
        ax.plot(
            [angle, angle], [0, 1.0],
            color=RADIAL_LINE_COLOR, linewidth=1.0,
            linestyle='-', alpha=0.6, zorder=1
        )

    # ---------- 数据折线 + 填充 ----------
    ax.plot(
        angles, closed_norm,
        color=VOLUNTEER_COLOR, linewidth=LINE_WIDTH,
        marker='o', markersize=MARKER_SIZE, zorder=3
    )
    ax.fill(angles, closed_norm, color=VOLUNTEER_COLOR, alpha=FILL_ALPHA, zorder=2)

    # ---------- 真实值标注 ----------
    for i, (angle, norm_v, raw_v, fmt) in enumerate(
            zip(theta, norm_values, raw_values, VALUE_FORMATS)):
        label_r = min(norm_v + VALUE_LABEL_OFFSET, 1.08)
        display_v = int(raw_v) if fmt == 'd' else raw_v
        ax.text(
            angle, label_r,
            format(display_v, fmt),
            ha='center', va='center',
            fontsize=VALUE_LABEL_FONT_SIZE,
            color='#1A1A1A', fontweight='bold', zorder=4
        )

    # ---------- 维度标签 ----------
    ax.set_varlabels(CATEGORIES)
    ax.set_yticklabels([])
    for label in ax.get_xticklabels():
        label.set_fontsize(CATEGORY_FONT_SIZE)
        label.set_fontweight('bold')

    plt.tight_layout()

    # ---------- 输出 ----------
    if to_base64:
        buf = io.BytesIO()
        plt.savefig(buf, format='png', dpi=DPI,
                    bbox_inches='tight', facecolor=BACKGROUND_COLOR)
        plt.close(fig)
        buf.seek(0)
        return base64.b64encode(buf.read()).decode('utf-8')
    else:
        plt.savefig('volunteer_radar_preview.png', dpi=DPI,
                    bbox_inches='tight', facecolor=BACKGROUND_COLOR)
        plt.show()
        plt.close(fig)
        print('已保存预览图：volunteer_radar_preview.png')
        return None


# ============================================================
# 本地调试入口
# ============================================================
if __name__ == '__main__':
    # ── 弹窗预览 ──
    generate_radar_chart(SAMPLE_PROFILE, to_base64=False)

    # ── 测试 base64 输出 ──
    b64 = generate_radar_chart(SAMPLE_PROFILE, to_base64=True)
    print(f'base64 长度：{len(b64)} 字节（前 60 字符：{b64[:60]}…）')
    with open('volunteer_radar_b64_test.png', 'wb') as f:
        f.write(base64.b64decode(b64))
    print('base64 解码验证图已保存：volunteer_radar_b64_test.png')
