# coding=utf-8

import os
import time
import datetime
from watchdog.observers import Observer
from watchdog.events import LoggingEventHandler
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from watchdog.events import FileSystemEventHandler

# 以下为启动Chrome，并打开调试端口、新建配置文件的命令行。按需修改和调用
# "C:\Program Files\Google\Chrome\Application\chrome.exe" --remote-debugging-port=9222 --user-data-dir="C:\Users\Public\ChromeData"

# 打印日志
def log(str=""):
  print("[%s] %s" % (datetime.datetime.now(), str))

class MyEventHandler(FileSystemEventHandler):
    def __init__(self):
        FileSystemEventHandler.__init__(self)
 
    def on_any_event(self, event):
        print("-----")
        print(datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f'))
 
    # 移动
    def on_moved(self, event):
        if event.is_directory:
            print("目录 moved:{src_path} -> {dest_path}".format(src_path=event.src_path, dest_path=event.dest_path))
        else:
            print("文件 moved:{src_path} -> {dest_path}".format(src_path=event.src_path, dest_path=event.dest_path))
 
    # 新建
    def on_created(self, event):
        if event.is_directory:
            print("目录 created:{file_path}".format(file_path=event.src_path))
        else:
            print("文件 created:{file_path}".format(file_path=event.src_path))
 
    # 删除
    def on_deleted(self, event):
        if event.is_directory:
            print("目录 deleted:{file_path}".format(file_path=event.src_path))
        else:
            print("文件 deleted:{file_path}".format(file_path=event.src_path))
    # 修改
    def on_modified(self,event):
        ThisID = "!ThisIsPythonSend!!ThisGSQ!!!"
        JavaSendId = "!ThisIsJavaSend!!ThisGSQ!!!"
        with open(path+os.sep+"data.txt",'r',encoding='utf-8') as f:
          content = f.read()
        msg = content
        msg = msg.replace(JavaSendId,"")
        if not ThisID in msg:
          if msg.find(chr(17)) > -1:
            chatgpt.open(refresh=True)
          elif msg.find(chr(18)) > -1:
            chatgpt.regenerate()
          else:
            chatgpt.send(str=msg)
          remsg = chatgpt.getLastReply()
          with open(path+os.sep+"data.txt", "w", encoding='utf-8') as f:
            f.write(remsg + ThisID)

class ChatGPT(object):
  # 初始化，连接开了本地端口调试的Chrome浏览器
  def init(self, port):
    options = Options()
    options.add_experimental_option("debuggerAddress", "127.0.0.1:%d" % port)
    log("尝试在端口 %d 上连接浏览器" % port)
    self.driver = webdriver.Chrome(options=options)
    self.vars = {}
    # 该对象在生命周期内已收回复数
    self.reply_cnt = 0
    # self.reply_cnt的备份。重新生成时会用到，便于回溯。
    self.reply_cnt_old = 0

  # 关闭
  def close(self):
    self.driver.quit()

  # 打开ChatGPT网页。
  # 参数：
  # 1.delay:等待网页加载的秒数
  # 2.refresh:设为True，则强制Chrome重新载入网页。但会频繁触发CloudFlare。
  #    设为False，则什么都不做。但需要事先将浏览器开好。我是将ChatGPT设成了首页
  def open(self, delay=3, refresh=False):
    self.reply_cnt = 0
    self.reply_cnt_old = 0
    log("打开ChatGPT网页中...")
    if refresh:
      self.driver.get("https://chat.openai.com")
    time.sleep(delay)
    log("完成")

  # 向ChatGPT发送文本。delay为每个步骤间延迟的秒数。
  def send(self, str="你好", delay=0.25):
    self.reply_cnt_old = self.reply_cnt
    # 点击文本框
    txtbox = self.driver.find_element(By.CSS_SELECTOR, ".m-0")
    txtbox.click()
    time.sleep(delay)
    # 输入文本，需处理换行
    log("发送内容:"+repr(str))
    txtlines = str.split('\n')
    for txt in txtlines:
      txtbox.send_keys(txt)
      time.sleep(delay)
      txtbox.send_keys(Keys.SHIFT, Keys.ENTER)
      time.sleep(delay)
    # 发送
    txtbox.send_keys(Keys.ENTER)
    time.sleep(delay)

  # 重新生成
  def regenerate(self):
    self.reply_cnt = self.reply_cnt_old
    self.driver.find_element(By.CSS_SELECTOR, ".btn").click()

  # 获取最近一条回复。
  # timeout为超时秒数
  def getLastReply(self, timeout = 90):
    log("等待回复中...")
    time_cnt = 0
    reply_str = ""
    # 判断ChatGPT是否正忙
    while self.driver.find_elements(By.CSS_SELECTOR, ".result-streaming") != []:
      if time_cnt >= timeout:
        reply_str += "【超过时间阈值%d秒" % timeout
        elemList = self.getReplyList()
        if len(elemList) <= self.reply_cnt:
          reply_str += "，未收到任何有效回复】"
          return reply_str, False
        else:
          reply_str += "，以下是收到的部分回复】\n"
          break

      time.sleep(1)
      time_cnt = time_cnt + 1

    elemList = self.getReplyList()
    if len(elemList) <= self.reply_cnt:
      return "【发生不可描述错误，未收到任何有效回复】"

    for i in range (self.reply_cnt, len(elemList)):
      reply_str += elemList[i].text
      reply_str += "\n"

    log(reply_str)
    self.reply_cnt = len(elemList)
    return reply_str

  # 获取ChatGPT回复列表
  def getReplyList(self):
    return self.driver.find_elements(By.CSS_SELECTOR, ".markdown > p")

if __name__=="__main__":
  chatgpt = ChatGPT()
  chatgpt.init(9222)
  chatgpt.open()
  isThisSend = False

  myEventHandler = MyEventHandler()
  path = os.path.dirname(os.path.abspath(__file__))
  # 内置的LoggingEventHandler
  event_handler = LoggingEventHandler()
  # 观察者
  observer = Observer()
  # recursive:True 递归的检测文件夹下所有文件变化。
  observer.schedule(myEventHandler, path, recursive=True)
  # 观察线程，非阻塞式的。
  observer.start()
  try:
      while True:
          time.sleep(250)
  except KeyboardInterrupt:
      observer.stop()
  observer.join()
##非原创//本人不会python,大部分都是抄的