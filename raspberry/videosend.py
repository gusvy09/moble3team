# videosend.py
# 서버로 실시간 이미지 캡처 전송 및 상시 녹화
# 감지 여부 받아서 사고 발생 시 녹화
import requests
import cv2
import threading
import boto3
import datetime
import os
import time

# 어플관련 모듈
import pymysql
import firebase_admin
from firebase_admin import credentials, messaging

server_ip = ""
local_ip = ""
port = ""

# AWS 계정 정보
aws_access_key_id = ""
aws_secret_access_key = ""
bucket_name = ""

# S3 클라이언트 생성
s3 = boto3.client(
    "s3",
    aws_access_key_id=aws_access_key_id,
    aws_secret_access_key=aws_secret_access_key,
)

# Firebase Admin SDK 초기화
cred = credentials.Certificate("secretkey.json")
firebase_admin.initialize_app(cred)

# 카메라 초기화
cap = cv2.VideoCapture(0)
cap.set(3, 640)  # 가로 해상도 설정
cap.set(4, 480)  # 세로 해상도 설정
width = int(cap.get(3))
height = int(cap.get(4))
fourcc = cv2.VideoWriter_fourcc(*"H264")  # 코덱 정보

# 녹화 시간 초기화
time_event = 0


# 화재감지시 어플알람
def send_push():
    # print("함수 접근")
    check = False
    # rds연결
    connect_db_userinfo = pymysql.connect(
        user="",
        password="",
        host="",
        db="",
        charset="",
    )
    cursor_userinfo = connect_db_userinfo.cursor()

    # DB에서 가져올 테이블과 열
    sql_user_info = "SELECT login, token FROM USER"

    # 결과값 불러옴
    cursor_userinfo.execute(sql_user_info)

    # 튜플 형식으로 리턴
    db_user_info = cursor_userinfo.fetchall()

    db_user_logins = [item[0] for item in db_user_info]  # logins
    db_user_tokens = [item[1] for item in db_user_info]  # tokens
    registration_token = ""
    for i in range(len(db_user_tokens)):
        if db_user_logins[i] == "on":
            now = datetime.datetime.now()
            filename = now.strftime("%Y-%m-%d %H:%M")
            registration_token = db_user_tokens[i]
            message = messaging.Message(
                notification=messaging.Notification(
                    title="화재 발생",
                    body=f"{filename}\n화재 발생 확인 바랍니다.",
                ),
                data={
                    "key1": "value1",
                    "key2": "value2",
                    # 원하는 데이터 키-값 쌍 추가
                },
                token=registration_token,
            )
            try:
                response = messaging.send(message)
                # Response is a message ID string.
                print("Successfully sent message:", response)
            except Exception as e:
                print(e)
                print("실패")


# 스트리밍 이미지 서버로 전송
def send_img():
    while True:
        ret, frame = cap.read()

        if not ret:
            continue

        # 이미지를 jpeg형식으로 인코딩(넘파이배열)
        _, img_encoded = cv2.imencode(".jpg", frame)
        # 넘파이 배열을 binary 데이터로 변환
        img_bytes = img_encoded.tobytes()

        url = f"http://{server_ip}:{port}/video_feed"

        # flask 서버로 이미지 전송
        requests.post(url, data=img_bytes, headers={"Content-Type": "image/jpeg"})


# 2시간 간격으로 상시 녹화
def record():
    while True:
        # 저장 경로, 파일 이름 지정
        path = ""
        now = datetime.datetime.now()
        filename = now.strftime("%Y-%m-%d %H:%M:%S")
        output_file = path + filename + ".mp4"
        out = cv2.VideoWriter(output_file, fourcc, 10.0, (width, height))
        time_event = 0

        while True:
            time_event = time_event + 1
            ret, frame = cap.read()
            if not ret:
                continue

            # 비디오 프레임이 출력되면 해당파일에 프레임을 저장
            out.write(frame)

            # 시간 설정
            if time_event > 300:
                # 녹화 종료: 비디오 관련 장치들을 닫아줌.
                out.release()
                cv2.destroyAllWindows()
                # s3에 업로드
                # s3.upload_file(output_file, bucket_name, "event/" +filename + '.mp4')
                # 로컬에 저장된 영상 삭제
                os.remove(output_file)
                break
            time.sleep(0.1)


# 화재 발생 시 녹화
def record_fire():
    # 저장 경로, 파일 이름 지정
    path = ""
    now = datetime.datetime.now()
    filename = now.strftime("%Y-%m-%d %H:%M:%S")
    output_file = path + filename + ".mp4"
    out = cv2.VideoWriter(output_file, fourcc, 10.0, (width, height))
    time_event = 0

    while True:
        time_event = time_event + 1
        ret, frame = cap.read()
        if not ret:
            continue

        # 비디오 프레임이 출력되면 해당파일에 프레임을 저장
        out.write(frame)

        # 정지 버튼 클릭 여부 확인
        response = requests.get(f"http://{server_ip}:8000/check")
        activate = response.text

        # 일정 시간이 경과했거나 정지 버튼이 클릭되면
        if time_event > 300 or activate == "off":
            # 녹화 종료: 비디오 관련 장치들을 다 닫아줌.
            out.release()
            cv2.destroyAllWindows()
            # s3.upload_file(output_file, bucket_name, "event/" +filename + '.mp4')
            os.remove(output_file)
            break
        time.sleep(0.1)


# 이미지 캡처 및 전송 스레드
capture_thread = threading.Thread(target=send_img)
capture_thread.daemon = True  # 메인 스레드가 종료되면 백그라운드 스레드도 함께 종료
capture_thread.start()

# 상시 녹화 스레드
record_thread = threading.Thread(target=record)
record_thread.daemon = True
record_thread.start()


# 메인 스레드에서 화재 감지 유무 확인
while True:
    response = requests.get(f"http://{server_ip}:8000/")
    detection = response.text
    print(detection)
    # 화재 감지 시 어플 알림, 사고 녹화 시작
    if detection == "True":
        send_push()  # 어플 알림 (push)
        record_fire()
    time.sleep(0.1)
    # 'q' 키를 누를 때까지 실행
    if cv2.waitKey(1) & 0xFF == ord("q"):
        break

# 비디와 관련 장치들을 다 닫아줌.
cv2.destroyAllWindows()
