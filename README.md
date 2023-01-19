# BukkitPlugin-ChatGPT
MinecraftBukkit服务器使用ChatGPT对话
# 介绍
本插件是一个可以让bukkit服务器中玩家提问ChatGPT的插件，有着api模式和网页访问ChatGPT模式
接下来是两种模式的不同使用方法
# 各种模式
1. 使用API模式
2. ~~使用网页访问模式~~ 1.0.2版本之前还能用(暂时删除了)
## API模式
### 需求
openai的API KEY
### 使用方法
在插件中，config.yml配置内将APISet.APIKey设置成你的key。就行这样
```
APISet:  
  APIKey: 'YOUR_KEY'  
```
这样写就插件会自动启用为API模式
## 网页访问模式
### 需求
1. python的运行环境
2. Chrome游览器
### 使用方法
+ 运行插件启用网页访问模式
+ 安装python 并运行插件生成出的ChatGPT.py文件
+ 启用GoogleChrome游览器端口调试
#### 启用网页访问模式
和API启用方式反过来
```
APISet:  
  APIKey: ''  
```
这里的APIKey: ''什么也不要填，就算启用网页访问模式了
#### 安装python
这个还要教么？前往python官网
[**[Python官网]**](https://www.python.org/downloads/)
过去吧，不送~
安装好后运行安装一下依赖，在插件的配置目录下运行CMD命令
~~~ 
pip3 install -r requirements.txt
~~~
如果你是python2就用pip不要用pip3
安装好后用python运行目录下的ChatGPT.py文件，然后就不用管打开的python窗口了
#### 启用Chrome的端口调用
找到Chrome的目录位置记录下来
然后运行CMD命令
~~~
"文件目录.\chrome.exe" --remote-debugging-port=9222
~~~
这样就完成了
## 最后
弄好任何一种模式后，只需要玩家发送信息内带个@ChatGPT的关键字，就能进行对话了，这个关键字的位置可以再要发送信息的任何位置

# 其他
欢迎加群讨论: [**[QQ群]**](https://jq.qq.com/?_wv=1027&k=bT2Elg8l)
个人对python语言并不好，所有上方提到的所有ChatGPT.py的源码是复制的其他公开源码和从ChatGPT上问的文件监听功能搓在一起的，优化不好，不行就请大佬自己优化吧~
后台发送方法
~~~
/gpt chat <内容>
~~~
祝你玩得愉快
