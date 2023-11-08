import pymysql
from flask import Flask, render_template, request, jsonify
import requests
import botocore
import boto3
from dotenv import load_dotenv
import os
from urllib.parse import unquote
import smtplib
from email.mime.text import MIMEText
from email.header import Header

# Flask 웹
app = Flask(__name__)

load_dotenv("ex.env")

### 서버와 연결
AWS_REGION = os.getenv("AWS_REGION")
AWS_ACCESS_KEY_ID = os.getenv("AWS_ACCESS_KEY_ID")
AWS_SECRET_ACCESS_KEY = os.getenv("AWS_SECRET_ACCESS_KEY")
AWS_BUCKET_NAME = os.getenv("AWS_BUCKET_NAME")

# 이메일 gmail 계정 이메일 주소, 앱 비밀번호 불러오기
email_ID = os.getenv("email_ID")
email_PW = os.getenv("email_PW")

mode_url = os.getenv("mode_url")

mysql_user = os.getenv("user")
mysql_password = os.getenv("password")
mysql_host = os.getenv("host")
mysql_db = os.getenv("db")

session = boto3.Session(
    aws_access_key_id=AWS_ACCESS_KEY_ID,
    aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
    region_name=AWS_REGION,
)

s3 = session.client("s3")


# 이메일 전송 함수
def send_email(addemailaddress, randomnumber):
    smtp = smtplib.SMTP("smtp.gmail.com", 587)  # Gmail의 SMTP 서버에 연결
    smtp.ehlo()  # SMTP 서버 연결
    smtp.starttls()  # TLS 연결 시작 (암호화된 통신을 위해 사용)
    smtp.login(email_ID, email_PW)  # gmail계정에 로그인
    msg = MIMEText("인증번호는 " + randomnumber + "입니다.")  # 이메일 내용
    msg["Subject"] = "FireGuard 이메일 인증번호 입니다."  # 이메일 제목
    msg["From"] = Header("FireGuard", "utf-8")  # 보내는 사람 이름 설정
    smtp.sendmail(email_ID, addemailaddress, msg.as_string())
    smtp.quit()  # SMTP 서버 연결 종료


# DB 연결 함수
def connect_to_db():
    return pymysql.connect(
        user=mysql_user,
        password=mysql_password,
        host=mysql_host,
        db=mysql_db,
        charset="utf8",
    )


# 비밀번호 재설정 함수
def update_pw(request, connect_db):
    update_result = "success"
    pwupdate = request.form.get("pwupdate")
    pwupdate_id = request.form.get("infoid")
    pwupdate_email = request.form.get("infoemail")
    try:
        with connect_db.cursor() as cursor_update:
            sql_select = "SELECT user_id, user_mail FROM USER"
            cursor_update.execute(sql_select)
            db_info = cursor_update.fetchall()
            db_userid = [item[0] for item in db_info]
            db_useremail = [item[1] for item in db_info]
            if (pwupdate_id in db_userid) and (pwupdate_email in db_useremail):
                id_index = db_userid.index(pwupdate_id)
                email_index = db_useremail.index(pwupdate_email)
                if id_index == email_index:
                    sql_update = "UPDATE USER SET user_pw = %s WHERE user_id = %s AND user_mail = %s"
                    cursor_update.execute(
                        sql_update, (pwupdate, pwupdate_id, pwupdate_email)
                    )
                    connect_db.commit()
                else:
                    print("일치하지 않음")
                    update_result = "fail"
            else:
                update_result = "fail"
    except pymysql.Error as e:
        connect_db.rollback()
        print(f"비밀번호 업데이트 오류: {e}")
    finally:
        print("비밀번호 업데이트 완료")
    return update_result


@app.route("/", methods=["POST", "GET"])
def home():
    if request.method == "POST":
        # db 연결
        connect_db_userinfo = connect_to_db()
        input_id = request.form.get("input_id")
        input_pw = request.form.get("input_pw")
        try:
            with connect_db_userinfo.cursor() as cursor_userinfo:
                # DB에서 가져올 테이블과 열
                sql_user_info = "SELECT user_id, user_pw, user_name FROM USER"
                # 결과값 불러옴
                cursor_userinfo.execute(sql_user_info)
                # 튜플 형식으로 리턴
                db_user_info = cursor_userinfo.fetchall()
                # 결과값에서 정보 추출
                db_user_ids = [item[0] for item in db_user_info]
                db_user_pws = [item[1] for item in db_user_info]
                db_user_names = [item[2] for item in db_user_info]

                if input_id in db_user_ids:
                    db_id_index = db_user_ids.index(input_id)
                    if db_user_pws[db_id_index] == input_pw:
                        return jsonify(
                            login_result="duplicate",
                            output_username=db_user_names[db_id_index],
                        )
                    else:
                        print("비밀번호가 틀렸습니다.")
                        return jsonify(login_result="wrong_pw")
                else:
                    print("존재하지 않는 아이디입니다.")
                    return jsonify(login_result="no_user")
        except pymysql.Error as e:
            print(f"Database error: {e}")
        finally:
            connect_db_userinfo.close()
    return render_template("main.html")


@app.route("/login")
def login():
    name = request.args.get("name")
    return render_template("login.html", name=name)


@app.route("/live", methods=["GET"])  # 실시간
def live():
    name = request.args.get("name")
    try:
        response = requests.get(mode_url)
        mode_result = response.text
    except:
        print("서버가 닫힘")
        return render_template("live.html", name=name)

    return render_template("live.html", name=name, mode_result=mode_result)


@app.route("/record", methods=["POST", "GET"])  # 상시 기록
def record():
    folder_name = "live/"
    objects = s3.list_objects_v2(Bucket=AWS_BUCKET_NAME, Prefix=folder_name)
    s3_urls = []
    file_names = []
    for obj in objects.get("Contents", []):
        file_key = obj["Key"]
        s3_url = s3.generate_presigned_url(
            ClientMethod="get_object",
            Params={"Bucket": AWS_BUCKET_NAME, "Key": file_key},
            ExpiresIn=3600,
        )
        s3_urls.append(s3_url)
        decoded_s3_url = unquote(s3_url)
        url_split = decoded_s3_url.split("?")[0]
        file_type = url_split.split("/")[-1]
        file_name = file_type.split(".")[0]
        file_names.append(file_name)
    ## 영상 삭제
    if request.method == "POST":
        try:
            data = request.json
            selectedFileNames = data.get("selectedFileNames")
            for delete_file_name in selectedFileNames:
                s3_object_key = f"live/{delete_file_name}.mp4"  ##"live/파일명.mp4"
                print(s3_object_key)
                s3.delete_object(Bucket=AWS_BUCKET_NAME, Key=s3_object_key)
        except botocore.exceptions.ClientError as e:
            print(e)
        finally:
            print("삭제되었습니다.")
    name = request.args.get("name")
    return render_template(
        "record.html", video_url=s3_urls, file_name=file_names, name=name
    )


@app.route("/accident", methods=["POST", "GET"])  # 사고 기록
def accident():
    folder_name = "event/"
    objects = s3.list_objects_v2(Bucket=AWS_BUCKET_NAME, Prefix=folder_name)
    s3_urls = []
    file_names = []
    for obj in objects.get("Contents", []):
        file_key = obj["Key"]  ## event/파일명.mp4
        s3_url = s3.generate_presigned_url(
            ClientMethod="get_object",
            Params={"Bucket": AWS_BUCKET_NAME, "Key": file_key},
            ExpiresIn=3600,
        )
        s3_urls.append(s3_url)
        decoded_s3_url = unquote(s3_url)
        url_split = decoded_s3_url.split("?")[0]
        file_type = url_split.split("/")[-1]
        file_name = file_type.split(".")[0]
        file_names.append(file_name)
    ## 영상 삭제
    if request.method == "POST":
        try:
            data = request.json
            selectedFileNames = data.get("selectedFileNames")
            for delete_file_name in selectedFileNames:
                s3_object_key = f"event/{delete_file_name}.mp4"
                s3.delete_object(Bucket=AWS_BUCKET_NAME, Key=s3_object_key)
        except botocore.exceptions.ClientError as e:
            print(e)
        finally:
            print("삭제되었습니다.")

    name = request.args.get("name")
    return render_template(
        "accident.html", video_url=s3_urls, file_name=file_names, name=name
    )


@app.route("/register", methods=["POST", "GET"])
def register():
    ## DB에서 정보 가져오기
    try:
        connect_db = connect_to_db()
        with connect_db.cursor() as cursor:
            # DB에서 가져올 테이블과 열
            sql_userid = "SELECT user_mail, user_id FROM USER"
            # 결과값 불러옴
            cursor.execute(sql_userid)
            # 튜플 형식으로 리턴
            db_emails_ids = cursor.fetchall()
            db_user_email = [item[0] for item in db_emails_ids]
            db_userid = [item[1] for item in db_emails_ids]
    except pymysql.Error as e:
        print(f"Database error: {e}")
    finally:
        connect_db.close()
    if request.method == "POST":
        # db연결
        connect_db = connect_to_db()
        newID = request.form.get("newID")
        adduserid = request.form.get("adduserid")
        adduserpw = request.form.get("adduserpw")
        addusername = request.form.get("addusername")
        addemailaddress = request.form.get("addemailaddress")
        randomnumber = request.form.get("randomnumber")

        ## 이메일로 인증 번호 보내기
        if randomnumber != None:
            send_email(addemailaddress, randomnumber)

        # 회원가입 정보가 들어오면 DB에 추가
        if adduserid != None:
            try:
                connect_db_input = connect_to_db()
                with connect_db_input.cursor() as cursor_in:
                    # 사용자 정보를 데이터베이스에 추가하는 SQL 쿼리
                    add_userinfo = "INSERT INTO USER(user_mail, user_name, user_id, user_pw) VALUES (%s, %s, %s, %s)"
                    cursor_in.execute(
                        add_userinfo,
                        (addemailaddress, addusername, adduserid, adduserpw),
                    )
                connect_db_input.commit()
            except pymysql.Error as e:
                print(f"Database error: {e}")
            finally:
                connect_db_input.close()
        # DB와 입력받은 ID가 동일한지 확인
        elif newID in db_userid:
            return jsonify(result="duplicate")
        elif newID == "":
            return jsonify(result="no_blank")
    return render_template("register.html", db_user_email=db_user_email)


@app.route("/find", methods=["POST", "GET"])
def find():
    if request.method == "POST":
        # db연결
        connect_db = connect_to_db()
        ran_number = request.form.get("ran_number")

        # 비밀번호 재설정
        # 이메일 인증
        if "in_email" in request.form:
            in_email = request.form.get("in_email")
            send_email(in_email, ran_number)  # 이메일에 인증번호 보내기
        # 비밀번호 재설정
        if "pwupdate" in request.form:
            update_result = update_pw(request, connect_db)
            return jsonify(update_result=update_result)

        # 아이디 찾기
        findid_name = request.form.get("findid_name")
        findid_email = request.form.get("findid_email")

        try:
            with connect_db.cursor() as cursor_info:
                # DB에서 가져올 테이블과 열
                sql_userinfo = "SELECT user_name, user_mail, user_id FROM USER"
                # 결과값 불러옴
                cursor_info.execute(sql_userinfo)
                # 튜플 형식으로 리턴
                db_user_info = cursor_info.fetchall()
                db_username = [item[0] for item in db_user_info]
                db_usernum = [item[1] for item in db_user_info]
                db_userid = [item[2] for item in db_user_info]
                if (findid_name in db_username) and (findid_email in db_usernum):
                    # 입력받은 이름과 이메일 주소의 인덱스
                    db_name_index = db_username.index(findid_name)
                    db_num_index = db_usernum.index(findid_email)
                    if db_name_index == db_num_index:
                        find_id = db_userid[db_name_index]
                        return jsonify(find=find_id)
                # DB와 입력받은 이름과 이메일 주소로 아이디 찾기
                else:
                    find_id = "존재하지 않음"
                    return jsonify(find=find_id)
        except pymysql.Error as e:
            print(f"Database error: {e}")
        finally:
            connect_db.close()
    return render_template("find.html")


if __name__ == "__main__":
    app.run(port=8080)
