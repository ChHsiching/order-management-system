#!/usr/bin/env python3
import jwt
import time

# JWT配置
SECRET = "qwqw"
ALGORITHM = "HS256"

# 测试token生成
def test_jwt_generation():
    payload = {
        "sub": "frontenduser",
        "iat": int(time.time()),
        "exp": int(time.time()) + 86400,
        "role": 0,
        "email": "frontend@example.com"
    }

    token = jwt.encode(payload, SECRET, algorithm=ALGORITHM)
    print(f"Generated Token: {token}")

    # 验证token
    try:
        decoded = jwt.decode(token, SECRET, algorithms=[ALGORITHM])
        print(f"Decoded Payload: {decoded}")
        print("JWT verification SUCCESSFUL")
        return True
    except Exception as e:
        print(f"JWT verification FAILED: {e}")
        return False

if __name__ == "__main__":
    test_jwt_generation()