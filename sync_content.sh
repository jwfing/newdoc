#! /bin/bash

echo "/*************************************************"
echo " *"
echo " * 注意：本工程下并没有最新的在线文档，我们需要同步文档目录的 html 文件，"
echo " * 请先到 docs 目录下调用 grunt server 命令本地渲染出所有在线文档。"
echo " * 确保索引内容一致的前提下，测试新的搜索算法。"
echo " *"
echo "*************************************************/"
if read -p "请指定 docs repository 的位置(例如 ../docs): " docroot
then
distpath=$docroot/dist
if [ -d $distpath ];then
	echo "拷贝 $distpath 目录下 html 文件到本地。。。"
	cp -rf $distpath/* ./src/main/webapp
	echo "移除图片。。。"
	rm -rf ./src/main/webapp/images
	rm -rf ./src/main/webapp/md
	echo "修改 index.html 文件，改变搜索响应路径。。。"
	sed -i ".bak" "s/search\.html/search/g" ./src/main/webapp/index.html
	rm ./src/main/webapp/index.html.bak
	echo "done! 现在你可以调用 ./localrun.sh 脚本来本地运行，也可以通过 lean deploy 进行发布了"
else
	echo "$docroot 下 dist 目录不存在，无法获取内容源"
fi
fi
