



# Python 数据模型设计指南

> 其他语言代码示例
* [iOS / OS X](relation_guide-ios.html)
* [Android](relation_guide-android.html)
* [JavaScript](relation_guide-js.html)
* Python

多年以来，关系型数据库已经成为了企业数据管理的基础，很多工程师对于关系模型和 6 个范式都比较了解，但是如今来构建和运行一个应用，随着数据来源的越发多样和用户量的不断增长，关系数据库的限制逐渐成为业务的瓶颈，因此越来越多的公司开始向其它 NoSQL 数据库进行迁移。

众所周知，LeanCloud 存储后台大量采用了 MongoDB 这种文档数据库来存储结构化数据，正因如此我们才能提供面向对象的、海量的、schema free 的存储能力。从传统的关系型数据库转换到 LeanCloud（or MongoDB）存储系统，最基础的改变就是数据建模，也就是「schema 设计」。

## Schema 设计
在探索 schema 设计之前，我们先统一一下关系型数据库、MongoDB 和 LeanCloud 上的对应术语，如下表所示：

RDBMS    | MongoDB    | LeanCloud         
-------- | ---------- | ----------------- 
Database | Database   | Application       
Table    | Collection | Class             
Row      | Document   | Object            
Index    | Index      | Index             
JOIN     | Embedded，Reference  | Embedded Object, Pointer, Relation

关系型数据库和文档型数据库的根本区别在于：

- RDBMS 优化了数据存储效率（它的前提是系统中存储是一个非常昂贵的组件）。
- 文档数据库优化了数据访问（它认为开发时间和发布速度现在是比存储更可宝贵的东西）

在 LeanCloud 上进行 Schema 设计需要数据架构师、开发人员和 DBA 在观念上做一些转变：之前是传统的关系型数据模型，所有数据都会被映射到二维的表结构——行和列；现在是丰富、动态的对象模型（也就是 MongoDB 的「文档模型」），包括内嵌子对象和数组。

### 从死板的「表」结构到灵活、动态的「文档」

> 后文中我们有时候采用 LeanCloud 的核心概念**Object（对象）**，有时候提到 MongoDB 中的名词**Document（文档）**，他们是等同的。

我们现在使用的大部分数据，都有比较复杂的结构，用 「JSON 对象」来建模是比「表」更高效的方式。通过内嵌子对象和数组，JSON 对象可以和应用层的数据结构完全对齐。这对开发者来说，会更容易将应用层的数据映射到数据库里的对象。相反，将应用层的数据映射到关系数据库的表，则会降低开发效率，而比较普遍的增加额外的对象关系映射（ORM）层的做法，也同时降低了 schema 扩展和查询优化的灵活性，引入了新的复杂度。

例如，在 RDBMS 中有父子关系的两张表，通常就会变成 LeanCloud 里面含有内嵌子对象的单文档结构。以下图的数据为例：

- PERSON

Pers_ID | Surname | First_Name | City 
------- | ------- | ---------- | ---- 
0       |   柳      | 红           |  London    
1       |   杨      | 真           | Beijing     
2       |   洛      | 托马斯         |  Zurich    

- CAR

Car_ID | Model | Year | Value | Pers_ID 
------ | ----- | ---- | ----- | ------- 
101    |  大众迈腾     | 2015     | 180000      | 0        
102    |  丰田汉兰达     | 2016     | 240000      |      0   
103    |  福特翼虎     | 2014     | 220000      |      1  
104    |  现代索纳塔     | 2013     | 150000      |      2   
  

RDBMS 中通过 Pers_ID 域来连接 PERSON 表和 CAR 表，以此支持应用中显示每辆车的拥有者信息。使用文档模型，通过内嵌子对象和数组可以将相关数据提前合并到一个单一的数据结构中，传统的跨表的行和列现在都被存储到了一个文档内，完全省略掉了 join 操作。

换成 LeanCloud 来对同样的数据建模，则允许我们创建这样的 schema：一个单一的 Person 对象，里面通过一个子对象数组来保存该用户所拥有的每一部 Car，例如：

```json
{
  first_name:"红",
  surname: "柳",
  city:"London",
  location:[45.123,47.232],
  cars:[
    {model:"大众迈腾",
     year: 2015,
     value:180000,...},
    {model:"丰田汉兰达",
     year: 2016,
     value:240000,...}
  ]
}
```

文档数据库里的一篇文档，就相当于 LeanCloud 平台里的一个对象。这个例子里的关系模型虽然只由两张表组成（现实中大部分应用可能需要几十、几百甚至上千张表），但是它并不影响我们思考数据的方式。

为了更好地展示关系模型和文档模型的区别，我们考虑下图所示的博客平台的例子。在这里，依赖 RDBMS 的应用需要 join 5 张不同的表来获得一篇博客的完整数据，而在 LeanCloud 中，所有的博客数据都包含在一个文档中，博客作者和评论者的用户信息则通过一个到 User 的引用（指针）进行关联。

![](images/RDBMSvsMongoDB.png)

#### 文档模型的其它优点

除了数据表现更加自然之外，文档模型还有性能和扩展性方面的优势：

- 通过单一调用即可获得完整的文档，避免了多表 join 的开销。LeanCloud 的 Object 物理上作为一个单一的块进行存储，只需要一次内存或者磁盘的读操作即可。RDBMS 与此相反，一个 join 操作需要从不同地方多次读取操作才可完成。
- 文档是自包含的，将数据库内容分布到多个节点（也叫 sharding）会更简单，同时也更容易通过普通硬件的水平扩展获得更高性能。DBA 们不再需要担心跨节点进行 join 操作可能带来的性能恶化问题。

### 定义文档 schema

应用的数据访问模式决定了 schema 设计，因此我们需要特别明确以下几点：

- 数据库读写操作的比例以及是否需要重点优化某一方的性能；
- 对数据库进行查询和更新的操作类型；
- 数据生命周期和文档的增长率；

以此来设计更合适的 schema 结构。

对于普通的「属性名－值」对来说，设计比较简单，和 RDBMS 中平坦的表结构差别不大。对于 1:1 或 1:many 的关系会很自然地考虑使用内嵌对象：

- 数据「所有」和「包含」的关系，都可以通过内嵌对象来进行建模。
- 同时，在架构上也可以把那些经常需要同时、原子改动的属性作为一个对象嵌入到一个单独的属性中。

例如，为了记录每个学生的家庭住址，我们可以把住址信息作为一个整体嵌入 Student 类里面。


```python
import leancloud

student_tom = leancloud.Object.extend("Student")()
student_tom.set('name', 'Tom')

addr = { "city": "北京", "address": "西城区西长安街 1 号", "":"100017" };
student_tom.set('address', addr)

# 保存在云端
student_tom.save()
```



但并不是所有的 1:1 关系都适合内嵌的方式，对下面的情况后文介绍的「引用」（等同于 MongoDB 的 `reference`）方式会更加合适：

- 一个对象被频繁地读取，但是内嵌的子对象却很少会被访问。
- 对象的一部分属性频繁地被更新，数据大小持续增长，但是剩下的一部分属性基本稳定不变。
- 对象大小超过了 LeanCloud 当前最大 16 MB 限制。

接下来我们重点讨论一下在 LeanCloud 上如何通过「引用」机制来实现复杂的关系模型。

## 复杂关系模型的设计

数据对象之间存在 3 种类型的关系。一对一关系将一个对象与另一个对象关联，一对多关系是一个对象关联多个对象，多对多关系则用来实现大量对象之间的复杂关系。我们支持 4 种方式来构建对象之间的关系（都是通过 MongoDB 的文档引用来实现的）：

1. Pointers（适合一对一、一对多关系）
2. Arrays（一对多、多对多）
3. AVRelation（多对多）
4. 关联表（多对多）

### 一对多关系

在创建一对多关系时，选择用 Pointers 还是 Arrays 来实现，需要考虑关系中包含的对象数量。如果关系「多」方包含的对象数量非常大（大于 100 左右），那么就必须使用 Pointers。反之，如果对象数量很小（低于 100 或更少），那么 Arrays 可能会更方便，特别是在获取父对象的同时得到所有相关的对象，即一对多关系中的「多」。

#### 使用 Pointers 实现一对多关系

##### Pointers 存储

中国的「省份」与「城市」具有典型的一对多的关系。深圳和广州（城市）都属于广东省（省份），而朝阳区和海淀区（行政区）只能属于北京市（直辖市）。广东省对应着多个一级行政城市，北京对应着多个行政区。下面我们使用 Pointers 来存储这种一对多的关系。

<div class="callout callout-info">注：为了表述方便，后文中提及城市都泛指一级行政市以及直辖市行政区，而省份也包含了北京、上海等直辖市。</div>



```python
import leancloud

leancloud.init("{{appid}}", "{{appkey}}")

guangZhou  = leancloud.Object.extend('City')()
guangZhou.set('name', '广州')

guangDong = leancloud.Object.extend('Province')()
guangDong.set('name', '广东')

# 为广州设置 dependent 属性为广东
guangZhou.set('dependent', guangDong)

# 广东无需被单独保存，因为在保存广州的时候已经上传到服务端。
guangZhou.save()
```


注意：保存关联对象的同时，被关联的对象也会随之被保存到云端。

要关联一个已经存在于云端的对象，例如将「东莞市」添加至「广东省」，方法如下：



```python
import leancloud

# 用 create_without_data 关联一个已经存在的对象
Provice = leancloud.Object.extend('Province')
guangDong = Province.create_without_data('574416af79bc44005c61bfa3')

dongGuan = leancloud.Object.extend('City')()
dongGuan.set('name', '东莞')
# 为东莞设置 dependent 属性为广东
dongGuan.set('dependent', guangDong)

dongGuan.save()
```



执行上述代码后，在应用控制台可以看到 `dependent` 字段显示为 Pointer 数据类型，而它本质上存储的是一个指向 `City` 这张表的某个 AVObject 的指针。

##### Pointers 查询

假如已知一个城市，想知道它的上一级的省份：



```python
import leancloud

City = leancloud.Object.extend('City')
guangZhou = City.create_without_data('5744189fdf0eea0063ad948b')
guangZhou.fetch()
province_id = guangZhou.get('dependent').id  # 获取广东省的 objectId

province = leancloud.Object.extend('Province')()
province.id = province_id
province.fetch()  # 根据 objectId 获取 province
```


假如查询结果中包含了城市，并想通过一次查询同时把对应的省份也一并加载到本地：



```python
import leancloud

query = leancloud.Query("City")
query.equal_to('name', '广州')
query.include('dependent')  # 关键代码，找出对应城市的省份

for city in query.find():
    province = city.get('dependent')
    province_name = province.get('name')
    # 可以获取 province 的信息
```



假如已知一个省份，要找出它的所有下辖城市：



```python
import leancloud

Provice = leancloud.Object.extend('Province')
guangDong = Provice.create_without_data('574416af79bc44005c61bfa3')

query = leancloud.Query("City")
query.equal_to('dependent', guangDong)

for city in query.find():
    city_name = city.get('name')
    # 结果为广东省下辖的所有城市
```


大多数场景下，Pointers 是实现一对多关系的最好选择。

#### 使用 Arrays 实现一对多关系

##### Arrays 存储

当一对多关系中所包含的对象数量很少时，使用 Arrays 比较理想。Arrays 可以通过 `include` 简化查询。传递对应的 key 可以在获取「一」方对象数据的同时获取到所有「多」方对象的数据。但是如果关系中包含的对象数量巨大，查询将响应缓慢。

城市与省份对应关系也可以使用 Arrays 实现。我们重新建立对象，为 `Province` 表添加一列 `cityList` 来保存城市数组：



```python
import leancloud

guangDong = leancloud.Object.extend('Province')()
guangZhou = leancloud.Object.extend('City')()
guangZhou.set('name', '广州')
shenZhen  = leancloud.Object.extend('City')()
shenZhen.set('name', '深圳')

guangDong.set('cityList',[guangZhou, shenZhen])
# 只要保存 guangDong 即可，它关联的对象都会一并被保存在服务端
guangDong.save()
```



##### Arrays 查询

获取这些 `City` 对象：



```python
import leancloud

Provice = leancloud.Object.extend('Province')
guangDong = Provice.create_without_data('57442c56df0eea0063ae2c35')
guangDong.fetch()
city_list = guangDong.get('cityList')

for city in city_list:
    city.fetch()
    name = city.get('name')  # 下面可以打印出所有城市的 name
```



如果要在查询某一个省份的时候，顺便把所有下辖的城市也获取到本地，可以在构建查询的时候使用 `include` 操作，这样就可以通过一次查询同时获取 `cityList` 列中存放的 `City` 对象集合：



```python
import leancloud

query = leancloud.Query('Province')
query.equal_to('name', '广东')
# 这条语句是关键语句，它表示可以将关联的数据下载到本地，而不用fetch
query.include('cityList')

province = query.find()[0]
province.get('cityList')
for city in province.get('cityList'):
    # 这里不用再添加 city.fetch() 这条语句
    name = city.get('name')
```


我们同样也可以根据已知的城市来查询它所属的上级省份，例如找出南京所属的省份：



```python
import leancloud
# 这是 广州 的 objectId
City = leancloud.Object.extend('City')
guangZhou = City.create_without_data('57442c562e958a006bf2d468')

query = leancloud.Query('Province')
query.equal_to('cityList', guangZhou)

province = query.find()[0]
provice_name = province.get('name')  # 这里 province_name 会得到 ‘广东’
```


### 多对多关系

假设有选课应用，我们需要为 `Student ` 对象和 `Course ` 对象建模。一个学生可以选多门课程，一个课程也有多个学生，这是一个典型的多对多关系。我们必须使用 Arrays、Relation 或创建自己的关联表来实现这种关系。决策的关键在于**是否需要为这个关系附加一些属性**。

如果不需要附加属性，使用 Relation 或 Arrays 最为简单。通常情况下，使用 Arrays 可以使用更少的查询并获得更好的性能。如果多对多关系中任何一方对象数量可能达到或超过 100，使用 Relation 或关联表是更好的选择。

反之，若需要为关系附加一些属性，就创建一个独立的表（关联表）来存储两端的关系。记住，附加的属性是描述这个关系的，不是描述关系中的任何一方。所附加的属性可以是：

* 关系创建的时间
* 关系创建者
* 某人查看此关系的次数

#### 使用 Relation 实现多对多关系

##### Relation 的存储

一个学生可以学习「多」门课程，一门课程也可以拥有「多」个学生。我们可以使用 Relation 构建 `Student` 和 `Course` 之间的关系。

为一个学生选择多门课程：



```python
import leancloud

student_tom = leancloud.Object.extend("Student")()
student_tom.set('name', 'Tom')

course_linear_algebra = leancloud.Object.extend('Cource')()
course_linear_algebra.set('name', 'Linear Algebra')

course_object_oriented_programming = leancloud.Object.extend('Cource')()
course_object_oriented_programming.set('name', 'Object-Oriented Programming')

course_operating_system = leancloud.Object.extend('Cource')()
course_operating_system.set('name', 'Operating System')
# 批量存储所有课程
leancloud.Object.save_all(
    [course_linear_algebra, course_object_oriented_programming, course_operating_system])

relation = student_tom.relation('course_chosen')
relation.add(course_linear_algebra)
relation.add(course_object_oriented_programming)
relation.add(course_operating_system)

student_tom.save()
```


##### Relation 的查询

要获取某个课程的所有学生，使用如下查询即可：



要获取某个学生学习的所有的课程，你可以构造一个稍微不同的查询来获取这种反向关系的结果：



```python
import leancloud

Course = leancloud.Object.extend("Course")
course_calculus = Course.create_without_data('574470ab2e958a006b728025')
query = leancloud.Query('Student')
query.equal_to('course_chosen', course_calculus)
student_list = query.find()  # student_list 就是所有选择了微积分的学生

for student in student_list:
    student_name = student.get('name')
```


#### 使用关联表实现多对多关系

有时我们需要知道更多关系的附加信息，比如在一个学生选课系统中，我们要了解学生打算选修的这门课的课时有多长，或者学生选修是通过手机选修还是通过网站操作的，这个时候单独使用 `AVRelation` 就无法满足需求了，因为 `AVRelation` 不支持额外的自定义属性，此时我们可以使用传统的数据模型设计方法：关联表。

为此，我们创建一个独立的表 `StudentCourseMap` 来保存 `Student` 和 `Course` 的关系：

字段|类型|说明
---|---|---
`course`|Pointer|Course 指针实例
`student`|Pointer|Student 指针实例
`duration`|Array|所选课程的开始和结束时间点，如 `["2016-02-19","2016-04-21"]`。
`platform`|String|操作时使用的设备，如 `iOS`。

如此，实现选修功能的代码如下：



```python
import leancloud

student_tom = leancloud.Object.extend('Student')()
student_tom.set('name', 'Tom')

course_linear_algebra = leancloud.Object.extend('Course')()
course_linear_algebra.set('name', 'Linear Algebra')
# 选课表对象
student_course_map_tom = leancloud.Object.extend('Student_course_map')()

# 设置关联
student_course_map_tom.set('student', student_tom)
student_course_map_tom.set('course', course_linear_algebra)

# 设置学习周期
student_course_map_tom.set('duration', ["2016-02-19", "2016-04-12"])

# 获取操作平台
student_course_map_tom.set('platform', 'ios')

# 保存选课表对象
student_course_map_tom.save()
```


查询选修了某一课程的所有学生：



```python
import leancloud

Course = leancloud.Object.extend('Course')
course_calculus = Course.create_without_data('57448184c26a38006b8d4761')
query = leancloud.Query('Student_course_map')
query.equal_to('course', course_calculus)

# 查询所有选择了线性代数的学生
student_course_map_list = query.find()

# list 是所有 course 等于线性代数的选课对象,
# 然后遍历过程中可以访问每一个选课对象的 student,course,duration,platform 等属性
for student_course_map in student_course_map_list:
    student = student_course_map.get('student')
    course  = student_course_map.get('course')
    duration = student_course_map.get('duration')
    platform = student_course_map.get('platform')
```


同样我们也可以很简单地查询某一个学生选修的所有课程，只需将上述代码变换查询条件即可：



```python

Student = leancloud.Object.extend('Student')
student_tom = Student.create_without_data("562da3fc00b0bf37b117c250")
query.whereEqualTo("student", student_tom)
```


#### 使用 Arrays 实现多对多关系

使用 Arrays 实现多对多关系，跟实现一对多关系大致相同。关系中一方的所有对象拥有一个数组列来包含关系另一方的一些对象。

以选课系统为例，现在我们使用 Arrays 方式来实现学生选课的操作：



```python
import leancloud

student_tom = leancloud.Object.extend("Student")()
student_tom.set('name', 'Tom')

course_linear_algebra = leancloud.Object.extend('Cource')()
course_linear_algebra.set('name', 'Linear Algebra')

course_object_oriented_programming = leancloud.Object.extend('Cource')()
course_object_oriented_programming.set('name', 'Object-Oriented Programming')

course_operating_system = leancloud.Object.extend('Cource')()
course_operating_system.set('name', 'Operating System')

# 所选课程的数组
courses = [course_linear_algebra, course_object_oriented_programming, course_operating_system]

# 使用属性名字 coursesChosen 保存所选课程的数组
student_tom.set('course_chosen', courses)

# 保存在云端
student_tom.save()
```


当查询某一个学生选修的所有课程时，需要使用 `include` 操作来获取对应的数组值：



```python
import leancloud

query = leancloud.Query("Student")
query.equal_to('name', 'Tom')

# 以下这句是关键句，它将关联的对象下载到本地
query.include('course_chosen')
tom_list = query.find()

for tom in tom_list:
    course_list = tom.get('course_chosen')
    for course in course_list:
        course_name = course.get('name')
```


查找选修了某一个课程的所有学生：



```python
import leancloud

Course = leancloud.Object.extend('Course')
course_linear_algebra = Course.create_without_data('5744f76971cfe4006bb41fc2')
query = leancloud.Query("Student")
query.equal_to('course_chosen', course_linear_algebra)
student_list = query.find()  # student_list 即为所有选择了线性代数这门课的学生

for student in student_list:
    student_id = student.id   # 这里即可获得学生的 id 和 name
    student_name = student.get('name')
```


### 一对一关系

当你需要将一个对象拆分成两个对象时，一对一关系是一种重要的需求。这种需求应该很少见，但是在下面的实例中体现了这样的需求：

* **限制部分用户数据的权限**<br/>
  在这个场景中，你可以将此对象拆分成两部分，一部分包含所有用户可见的数据，另一部分包含所有仅自己可见的数据（通过 [ACL 控制](data_security.html#Class_级别的_ACL) ）。同样你也可以实现一部分包含所有用户可修改的数据，另一部分包含所有仅自己可修改的数据。
* **避免大对象**<br/>
  原始对象大小超过了对象的 128 KB 的上限值，此时你可以创建另一个对象来存储额外的数据。当然通常的作法是更好地设计和优化数据模型来避免出现大对象，但如果确实无法避免，则可以考虑使用 AVFile 存储大数据。
* **更灵活的文件对象**<br/>
  AVFile 可以方便地存取文件，但对对象进行查询和修改等操作就不是很方便了。此时可以使用 AVObject 构造一个自己的文件对象并与 AVFile 建立一对一关联，将文件属性存于 AVObject 中，这样既可以方便查询修改文件属性，也可以方便存取文件。
  
### 关联数据的删除
当表中有一个 Pointer 或 Relation 指向的源数据被删除时，这个源数据对应的 Pointer 和 Relation **不会**被自动删除。所以建议用户在删除源数据时自行检查是否有 Pointer 或 Relation 指向这条数据，基于业务场景有必要做数据清理的话，可以调用对应的对象上的删除接口将 Pointer 或 Relation 关联的对象删除。

## 索引
在任何一个数据库系统中，索引都是优化性能的重要手段，同时它与 schema 设计也是密不可分的。LeanCloud 也支持索引，其索引与关系数据库中基本相同。在索引的选择上，应用查询操作的模式和频率起决定性作用，同时我们也要明白，索引不是没有代价的，在加速查询的同时，它也会降低写入速度、消耗更多存储（磁盘和内存）资源。是否建索引，如何建索引，建多少索引，我们需要综合权衡后来下决定。

### 索引类型
LeanCloud 的索引可以包含任意的属性（包括数组），下面是一些索引选项：
- **复合索引**——在多个属性域上构建一个单独的索引结构。例如，以一个存储客户数据的应用为例，我们可能需要根据姓、名和居住地来查询客户信息。通过在「姓」、「名」、「居住地」上建立复合索引，LeanCloud 可以快速给出满足这三个条件的结果，此外，这一复合索引也能加速任何前置属性的查询。例如根据「姓」或者根据「姓」＋「名」的查询，都会使用到这个复合索引。注意，如果单按照「名」来查询，则此复合索引不起作用。
- **唯一索引**——通过给索引加上唯一性约束，LeanCloud 就会拒绝含有相同索引值的对象插入和更新。所有的索引默认都不是唯一索引，如果把复合索引指定为唯一索引，那么应用层必须保证索引列的组合值是唯一的。
- **数组索引**——对数组属性也能创建索引。
- **地理空间索引**——MongoDB 提供了地理空间索引，来方便大家进行地理位置相关的查询。LeanCloud 会自动为 GeoPoint 类型的属性建立地理空间索引，但是要求一个 Object 内 GeoPoint 的属性不能超过一个。
- **稀疏索引**——这种索引只包含那些含有指定属性的文档，如果文档不含有目标属性，那么就不会进入索引。稀疏索引体积更小，查询性能更高。LeanCloud 默认都会创建稀疏索引。

LeanCloud 的索引可以在任何域上建立，包括内嵌对象和数组类型，这使它带来了比 RDBMS 更强大的功能。

### 通过索引优化性能
LeanCloud 后台会根据每天的访问日志，自动归纳、学习频繁使用的访问模式，并自动创建合适的索引。不过如果你对索引优化比较有经验，也可以在控制台为每一个 Class 手动创建索引。

## 持续优化 Schema
在 LeanCloud 的存储系统里，Class 可以在没有完整的结构定义（包含哪些属性，数据类型如何，等）时就提前创建好，一个 Class 下的对象（Object）也无需包含所有属性域，我们可以随时往对象中增减新的属性。

这种灵活、动态的 schema 机制，使 schema 的持续优化变得非常简单。相比之下，关系数据库的开发人员和 DBA 在开始一个新项目的时候，写下第一行代码之前，就需要制定好数据库 schema，这至少需要几天，有的需要数周甚至更长。而 LeanCloud 则允许开发者通过不断迭代和敏捷过程，持续优化 schema。开发者可以开始写代码并将他们创建的对象持久化存储起来，以后当需要增加新的功能，LeanCloud 可以继续存储新的对象而不需要对原来的 Class 做 ALTER TABLE 操作，这会给我们的开发带来很大的便利。
