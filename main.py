from fastapi import FastAPI, HTTPException
from fastapi import Request
from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from pymongo.mongo_client import MongoClient
from pymongo.server_api import ServerApi
from datetime import date
from pyfcm import FCMNotification

uri = "mongodb+srv://root:021201@cluster0.ctdyt.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"
fcm = FCMNotification(service_account_file="service_account_file.json", project_id="pushchat-bfe3b")

# 连接到MongoDB
client = MongoClient(uri, server_api = ServerApi('1'))
db = client["chatapp"]
chatrooms_collection = db["chatrooms"]
messages_collection = db["messages"]
tokens_collection = db["tokens"]

app = FastAPI()

# 数据模型
class Message(BaseModel):
    chatroom_id: int
    user_id: int
    name: str
    message: str
    message_time: str


class Chatroom(BaseModel):
    id: int
    name: str


# 数据模型
class Token(BaseModel):
    user_id: int
    token: str


@app.post("/submit_push_token")
async def submit_push_token(data: Token):

    tokens_collection.insert_one({"user_id": data.user_id, "token": data.token})

    data = {"status": "OK"}
    return JSONResponse(content=jsonable_encoder(data))

@app.get("/")
async def root():
    return {"message": "Hello World"}  # the API returns a JSON response

@app.get("/demo/")
async def get_demo(a: int = 0, b: int = 0, status_code=200):
    sum = a+b
    data = {"sum": sum, "date": date.today()}
    return JSONResponse(content=jsonable_encoder(data))

@app.get("/get_chatrooms")
async def get_chatrooms():
    chatrooms = list(chatrooms_collection.find({}, {"_id": 0}))
    data =  {"data": chatrooms, "status": "OK"}
    return JSONResponse(content=jsonable_encoder(data))


@app.get("/get_messages")
async def get_messages(chatroom_id: int):
    messages = list(messages_collection.find({"chatroom_id": chatroom_id}, {"_id": 0}))
    data = {"data": {"messages": messages}, "status": "OK"}
    return JSONResponse(content=jsonable_encoder(data))


@app.post("/send_message/")
async def send_message(request: Request):
    global fcm_token
    item = await request.json()
    print(request, "\n", item)

    if "chatroom_id" not in item.keys() or item["chatroom_id"] not in [1,2, 3,"1", "2", "3"]:
        raise HTTPException(status_code=400, detail="No chatroom")

    if "name" not in item.keys() or len(item["name"]) > 20:
        raise HTTPException(status_code=400, detail="Name is exceeding 20 characters")

    if "message" not in item.keys() or len(item["message"]) > 200:
        raise HTTPException(status_code=400, detail="Message is exceeding 200 characters")


    messages_collection.insert_one({
        "chatroom_id": item["chatroom_id"],
        "user_id":item["user_id"],
        "name": item["name"],
        "message": item["message"],
        "message_time": item["message_time"]
    })
    if item["user_id"]==1:
        fcm_token = "d08vz9mNSDCXBtcdZ0JBb5:APA91bEQdtBpigtDvU3nNo9rGIQ7i-ZcF4xDtJKzyKB8FP7J8o6WBK4HUEpamOHtl0zXDOJuoioGRSkgB0p5ClveGsyZbDVAci4S0xicRWJPg6y8M4n3j0o"
    if item["user_id"]==0:
        fcm_token = "eYXCFD_OShC5jdV-itsTsT:APA91bHEwi1HYWwSBF686-kjsYiDkafYBjvRZR6EXuxOwp0lrODgidbfA9UKXfxyviJdIfIA6kJYl8zgL2eK0cUiAMOa_nKbD8im4p0QzOuPJA6zvZt8bXg"
    fcm.notify(fcm_token=fcm_token,  notification_title=f"Chatroom{item['chatroom_id']}", notification_body=item["message"])
    data = {"status": "OK"}
    return JSONResponse(content=jsonable_encoder(data))


