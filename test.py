import base64
import requests
from PIL import Image
import io
from flask import Flask, request, jsonify
from flask_cors import CORS  # 导入 CORS
import base64

app = Flask(__name__)
CORS(app)  # 允许所有跨域请求
API_KEY = "4ce0LZTouA65gNZOIgckOLRY"  # 替换为你的 API Key
SECRET_KEY = "5b7Tk0rrxeResVXdo5cziMf4I66bNBtr"  # 替换为你的 Secret Key
TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token"
RECOGNITION_URL = "https://aip.baidubce.com/rest/2.0/image-classify/v2/dish"


def get_access_token():
    try:
        response = requests.get(TOKEN_URL, params={
            "grant_type": "client_credentials",
            "client_id": API_KEY,
            "client_secret": SECRET_KEY
        })
        response_data = response.json()
        if 'access_token' not in response_data:
            raise RuntimeError("Failed to retrieve access token.")
        return response_data['access_token']
    except Exception as e:
        print(f"Error getting access token: {str(e)}")
        return None


def validate_image(image_path):
    try:
        with Image.open(image_path) as img:
            print(f"Image format: {img.format}")  # 打印图像格式

            if img.format not in ['JPEG', 'PNG', 'BMP']:  # 仅支持这些格式
                raise ValueError(f"Image format {img.format} is not supported.")

            img_bytes = io.BytesIO()
            img.save(img_bytes, format='JPEG')  # 保存为 JPEG
            img_bytes.seek(0)

            # 检查图像大小
            if img_bytes.tell() > 4 * 1024 * 1024:  # 超过 4 MB
                raise ValueError("Image size exceeds 4 MB")
            if img.size[0] < 15 or img.size[1] < 15:  # 最小边长要求
                raise ValueError("Image dimensions must be at least 15 pixels")
            if img.size[0] > 4096 or img.size[1] > 4096:  # 最大边长要求
                raise ValueError("Image dimensions exceed 4096 pixels")

        return img_bytes.getvalue()
    except Exception as e:
        print(f"Error validating image: {str(e)}")
        raise

@app.route('/api/recognize', methods=['POST'])
def recognize_food():
    access_token = get_access_token()
    if access_token is None:
        raise RuntimeError("Failed to retrieve access token")

    try:
        data=request.get_json()
        base64_image=data.get("image")
        # 将图像转换为 Base64
        # image_bytes = validate_image(image_path)  # 验证图像
        # base64_image = base64.b64encode(image_bytes).decode('utf-8')  # 编码为 Base64

        # 创建请求体
        data = {
            'image': base64_image  # 直接使用 Base64 数据
        }

        # 发送 POST 请求
        headers = {'Content-Type': 'application/x-www-form-urlencoded'}
        response = requests.post(f"{RECOGNITION_URL}?access_token={access_token}", data=data, headers=headers)

        # 打印状态码和响应内容
        print("Response Status Code:", response.status_code)
        print("Response Content:", response.text)

        if response.status_code != 200:
            raise RuntimeError(f"Request failed with status {response.status_code}: {response.text}")

        return response.json()
    except Exception as e:
        print(f"Error during recognition: {str(e)}")
        raise


if __name__ == "__main__":
    # image_path = "fimg.jpg"  # 替换为你的图像路径
    app.run(host='0.0.0.0', port=5000)  # 使 Flask 应用在5000端口可用