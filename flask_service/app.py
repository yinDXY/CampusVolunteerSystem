"""
校园志愿服务管理系统 - Flask 雷达图微服务
端口：5001
唯一端点：POST /radar

调用方：Spring Boot ChartController（内部调用，前端不直接访问）

请求体（camelCase JSON，与 v_volunteer_profile 视图字段对应）：
{
    "userId":          10,
    "realName":        "王五",
    "totalHours":      32.5,
    "activityCount":   6,
    "typeDiversity":   4,
    "avgScore":        4.5,
    "punctualityRate": 100.0
}

成功响应：
{
    "code": 200,
    "imageBase64": "iVBORw0KGgoAAAANSUhEUgAA..."
}
"""

import os
import sys

# 将项目根目录加入 sys.path，使 volunteer_radar 可被直接导入
_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
if _ROOT not in sys.path:
    sys.path.insert(0, _ROOT)

# volunteer_radar 内部已调用 matplotlib.use('Agg')，必须在 Flask 启动前导入
from volunteer_radar import generate_radar_chart  # noqa: E402

from flask import Flask, jsonify, request

app = Flask(__name__)


@app.route('/radar', methods=['POST'])
def radar():
    """
    生成志愿者画像雷达图。

    接收 Spring Boot 传入的五维画像数据（camelCase），
    调用 volunteer_radar.generate_radar_chart 生成 PNG，
    以 base64 字符串返回。
    """
    body = request.get_json(silent=True)
    if not body:
        return jsonify({"code": 400, "msg": "请求体不能为空或格式非 JSON"}), 400

    # camelCase → snake_case，映射至 generate_radar_chart 期望的 profile 字段
    profile = {
        'real_name':        str(body.get('realName', '')),
        'total_hours':      float(body.get('totalHours',      0)),
        'activity_count':   float(body.get('activityCount',   0)),
        'type_diversity':   float(body.get('typeDiversity',   0)),
        'avg_score':        float(body.get('avgScore',        0)),
        'punctuality_rate': float(body.get('punctualityRate', 0)),
    }

    image_base64 = generate_radar_chart(profile, to_base64=True)
    return jsonify({
        "code":        200,
        "imageBase64": image_base64,
    })


@app.route('/health', methods=['GET'])
def health():
    """健康检查，Spring Boot 启动时可 ping 此接口确认 Flask 服务就绪。"""
    return jsonify({"code": 200, "msg": "ok"})


if __name__ == '__main__':
    # 生产部署建议改用 gunicorn/waitress 替代内置 server
    app.run(host='0.0.0.0', port=5001, debug=False)
