# getvideo.py
# 라즈베리파이로부터 받은 이미지 스트리밍 및 반환
import os
from flask import Flask, Response, request, render_template, jsonify
import cv2
import numpy as np
import threading
import boto3


app = Flask(__name__)

frame_data = None
frame_lock = threading.Lock()

AWS_REGION = os.getenv("AWS_REGION")
AWS_ACCESS_KEY_ID = os.getenv("AWS_ACCESS_KEY_ID")
AWS_SECRET_ACCESS_KEY = os.getenv("AWS_SECRET_ACCESS_KEY")
AWS_BUCKET_NAME = os.getenv("AWS_BUCKET_NAME")

session = boto3.Session(
    aws_access_key_id=AWS_ACCESS_KEY_ID,
    aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
    region_name=AWS_REGION,
)
s3 = session.client("s3")


# 이미지 스트리밍
def video_frames():
    while True:
        if frame_data is not None:
            frame = cv2.imdecode(np.frombuffer(frame_data, np.uint8), cv2.IMREAD_COLOR)
            ret, buffer = cv2.imencode(".jpg", frame)
            cv2.imwrite("pic.jpg", frame)
            if ret:
                frame_bytes = buffer.tobytes()
                yield (
                    b"--frame\r\n"
                    b"Content-Type: image/jpeg\r\n\r\n" + frame_bytes + b"\r\n"
                )


# 이미지 받기
@app.route("/video_feed", methods=["POST"])
def video_feed():
    global frame_data
    with frame_lock:
        frame_data = request.data
    return ("", 204)


@app.route("/")
def index():
    return render_template("index.html")


# 이미지 스트리밍
@app.route("/video")
def video():
    return Response(
        video_frames(), mimetype="multipart/x-mixed-replace; boundary=frame"
    )


# S3버킷내의 live폴더 안에 들어있는 객체 리턴
@app.route("/get_data", methods=["GET"])
def get_data():
    folder_name = "live/"
    objects = s3.list_objects_v2(Bucket=AWS_BUCKET_NAME, Prefix=folder_name)
    s3_urls = []
    file_names = []
    for obj in objects.get("Contents", []):
        file_key = obj["Key"]
        print(file_key)  ## qqq/test1.mp4
        s3_url = s3.generate_presigned_url(
            ClientMethod="get_object",
            Params={"Bucket": AWS_BUCKET_NAME, "Key": file_key},
            ExpiresIn=3600,
        )
        s3_urls.append(s3_url)
    data = s3_urls
    return jsonify(data=data)


# S3버킷내의 event폴더 안에 들어있는 객체 리턴
@app.route("/get_data2", methods=["GET"])
def get_data2():
    folder_name = "event/"
    objects = s3.list_objects_v2(Bucket=AWS_BUCKET_NAME, Prefix=folder_name)
    s3_urls = []
    file_names = []
    for obj in objects.get("Contents", []):
        file_key = obj["Key"]
        print(file_key)  ## qqq/test1.mp4
        s3_url = s3.generate_presigned_url(
            ClientMethod="get_object",
            Params={"Bucket": AWS_BUCKET_NAME, "Key": file_key},
            ExpiresIn=3600,
        )
        s3_urls.append(s3_url)
    data = s3_urls
    return jsonify(data=data)


if __name__ == "__main__":
    t = threading.Thread(target=video_frames)
    t.daemon = True
    t.start()
    app.run(host="0.0.0.0", port=5000, threaded=True, debug=True)
